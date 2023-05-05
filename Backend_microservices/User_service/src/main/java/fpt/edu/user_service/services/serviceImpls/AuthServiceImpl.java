package fpt.edu.user_service.services.serviceImpls;

import fpt.edu.user_service.dtos.ExchangeGuardedPath;
import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.EditUserRequest;
import fpt.edu.user_service.dtos.responseDtos.FunctionResponse;
import fpt.edu.user_service.dtos.responseDtos.UserResponse;
import fpt.edu.user_service.entities.Function;
import fpt.edu.user_service.entities.User;
import fpt.edu.user_service.exceptions.UnauthorizedException;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.jwt.JwtUtilities;
import fpt.edu.user_service.repositories.FunctionRepository;
import fpt.edu.user_service.repositories.UserRepository;
import fpt.edu.user_service.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Truong Duc Duong
 */

@Service
@Log4j2
public class AuthServiceImpl extends BaseService implements AuthService {

    @Value("${rabbitmq.queue.new-auth-cache}")
    private String NEW_AUTH_CACHE_QUEUE_NAME;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private FunctionRepository functionRepository;
    @Autowired
    private JwtUtilities jwtUtilities;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User Not Found with username: " + username));
        if (passwordEncoder.matches(password, user.getPassword())) {
            ExchangeUser exchangeUser = ExchangeUser.build(user);

//            send message to Auth-gateway to inform about currently authenticated user
            rabbitTemplate.convertAndSend(NEW_AUTH_CACHE_QUEUE_NAME, exchangeUser);
            log.info("Message has been published via routing key '{}'", NEW_AUTH_CACHE_QUEUE_NAME);

            return jwtUtilities.generateToken(username);
        }
        throw new BadRequestException("Bad credentials");
    }

    @Override
    public ExchangeUser loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User Not Found with username: " + username));
        return ExchangeUser.build(user);
    }

    @Override
    public List<ExchangeGuardedPath> getAllGuardedPaths() {
        return functionRepository.findAll().stream()
                .map(function -> new ExchangeGuardedPath(function.getId(), function.getUri()))
                .filter(exchangeGuardedPath -> StringUtils.isNotEmpty(exchangeGuardedPath.getUri()))
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getAuthenticatedUser(HttpServletRequest request) {
        String authUserIdString = request.getHeader(super.AUTH_ID);
        if (StringUtils.isEmpty(authUserIdString)) {
            throw new UnauthorizedException("Unauthorized");
        }
        int authUserId = Integer.parseInt(authUserIdString);
        User authUser = userRepository.findById(authUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return modelMapper.map(authUser, UserResponse.class);
    }

    @Override
    public UserResponse editPersonalInfo(EditUserRequest editUserRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException, UniqueKeyViolationException {
        String authUserIdString = request.getHeader(super.AUTH_ID);
        if (StringUtils.isEmpty(authUserIdString)) {
            throw new UnauthorizedException("Unauthorized");
        }
        int authUserId = Integer.parseInt(authUserIdString);

        if (userRepository.existsByEditedUsername(authUserId, editUserRequest.getUsername())) {
            throw new UniqueKeyViolationException("Username already exists");
        }
        if (userRepository.existsByEditedEmail(authUserId, editUserRequest.getEmail())) {
            throw new UniqueKeyViolationException("Email has been used");
        }

        User authUser = userRepository.findById(authUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        User user = modelMapper.map(editUserRequest, User.class);

//        set current password
        user.setPassword(authUser.getPassword());

//        set authenticated user to createdBy
        setUpdatedBy(user, request);

//        set LoginFailureLogs
        user.setLoginFailureLogs(authUser.getLoginFailureLogs());

//        set id
        user.setId(authUserId);

//        process uploaded avatar image if exists
        MultipartFile avatar = editUserRequest.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            String filename = sendImageToFileService(avatar);
            user.setAvatar(filename);
        } else {
            user.setAvatar(authUser.getAvatar());
        }

//        set roles
        user.setRoles(authUser.getRoles());

        User editedUser = userRepository.save(user);

//        send message to demand auth-gateway to modify authenticatedUser redis cache
        List<ExchangeUser> exchangeUsers = new ArrayList<>();
        ExchangeUser exchangeUser = ExchangeUser.build(editedUser);
        exchangeUsers.add(exchangeUser);
        this.sendMessageToUpdateAuthCache(exchangeUsers);
//        if username changes, delete cache with previous username
        if (!editUserRequest.getUsername().equals(authUser.getUsername())) {
            this.sendMessageToDeleteAuthCache(authUser.getUsername());
        }

        return modelMapper.map(editedUser, UserResponse.class);
    }

    @Override
    public List<FunctionResponse> getAuthorizedFunctions(HttpServletRequest request) {
        String authUserIdString = request.getHeader(super.AUTH_ID);
        if (StringUtils.isEmpty(authUserIdString)) {
            throw new UnauthorizedException("Unauthorized");
        }
        int authUserId = Integer.parseInt(authUserIdString);
        User authUser = userRepository.findById(authUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Function> authorizedFunctions = authUser.getRoles().stream()
                .flatMap(role -> role.getFunctions().stream())
                .distinct()
                .sorted(Comparator.comparingInt(Function::getId))
                .toList();

        List<Function> rearrangedAuthorizedFunctions = rearrange(authorizedFunctions, null);

        return modelMapper.map(rearrangedAuthorizedFunctions, new TypeToken<List<FunctionResponse>>() {}.getType());
    }
}
