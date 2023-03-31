package fpt.edu.user_service.services.serviceImpls;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.EditUserRequest;
import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.NewUserRequest;
import fpt.edu.user_service.dtos.responseDtos.UserResponse;
import fpt.edu.user_service.entities.Role;
import fpt.edu.user_service.entities.User;
import fpt.edu.user_service.entities.UserRoleMapping;
import fpt.edu.user_service.exceptions.UnauthorizedException;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.pagination.Pagination;
import fpt.edu.user_service.repositories.RoleRepository;
import fpt.edu.user_service.repositories.UserRepository;
import fpt.edu.user_service.repositories.UserRoleMappingRepository;
import fpt.edu.user_service.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Truong Duc Duong
 */

@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class UserServiceImpl extends BaseService implements UserService {
    private static final String getAllMethodCache = "allUserResponses";
    private static final String getMethodCache = "userResponses";

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;
    @Value("${rabbitmq.routing-key.auth-cache-delete}")
    private String AUTH_DELETE_ROUTING_KEY;
    @Value("${rabbitmq.routing-key.avatar-processing}")
    private String AVATAR_PROCESSING_ROUTING_KEY;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @Override
    @CacheEvict(value = getAllMethodCache, allEntries = true)
    @CachePut(value = getMethodCache, key = "#result.getId()")
    public UserResponse save(NewUserRequest newUserRequest, HttpServletRequest request) throws UniqueKeyViolationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
//        Validate unique properties
        if (userRepository.existsByUsername(newUserRequest.getUsername())) {
            throw new UniqueKeyViolationException("Username already exists");
        }
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new UniqueKeyViolationException("Email has been used");
        }

        User user = modelMapper.map(newUserRequest, User.class);

//        encrypt password
        user.setPassword(new BCryptPasswordEncoder().encode(newUserRequest.getPassword()));

//        set authenticated user to createdBy
        setCreatedBy(user, request);

//        process uploaded avatar image
        MultipartFile avatar = newUserRequest.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            String nameWithoutExtension = this.sendImageToFileService(avatar);
            user.setAvatar(nameWithoutExtension);
        }

//         set roles
        user.setRoles(this.getAssignedRoles(newUserRequest));

        User newUser = userRepository.save(user);

        return this.get(newUser.getId());
    }

    @Override
    @CacheEvict(value = getAllMethodCache, allEntries = true)
    @CachePut(value = getMethodCache, key = "#id")
    public UserResponse update(int id, EditUserRequest editUserRequest, HttpServletRequest request) throws UniqueKeyViolationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        Optional<User> optionalCurrentUser = userRepository.findById(id);
        if (optionalCurrentUser.isPresent()) {
//                Validate unique properties
            if (userRepository.existsByEditedUsername(id, editUserRequest.getUsername())) {
                throw new UniqueKeyViolationException("Username already exists");
            }
            if (userRepository.existsByEditedEmail(id, editUserRequest.getEmail())) {
                throw new UniqueKeyViolationException("Email has been used");
            }

            User currentUser = optionalCurrentUser.get();

            User user = modelMapper.map(editUserRequest, User.class);

//            set current password
            user.setPassword(currentUser.getPassword());

//            set authenticated user to createdBy
            setUpdatedBy(user, request);

//            set LoginFailureLogs
            user.setLoginFailureLogs(currentUser.getLoginFailureLogs());

//            set id
            user.setId(id);

//            process uploaded avatar image if exists
            MultipartFile avatar = editUserRequest.getAvatar();
            if (avatar != null && !avatar.isEmpty()) {
                String filename = this.sendImageToFileService(avatar);
                user.setAvatar(filename);
            } else {
                user.setAvatar(currentUser.getAvatar());
            }

//            set roles
            user.setRoles(this.getAssignedRoles(editUserRequest));

            User editedUser = userRepository.save(user);

//            send message to demand auth-gateway to modify authenticatedUser redis cache
            List<ExchangeUser> exchangeUsers = new ArrayList<>();
            ExchangeUser exchangeUser = ExchangeUser.build(editedUser);
            exchangeUsers.add(exchangeUser);
            this.sendMessageToUpdateAuthCache(exchangeUsers);
//            if username changes, delete cache with previous username
            if (!editUserRequest.getUsername().equals(currentUser.getUsername())) {
                this.sendMessageToDeleteAuthCache(currentUser.getUsername());
            }

            return this.get(editedUser.getId());
        } else {
            throw new NotFoundException("User not found");
        }
    }

    private List<Role> getAssignedRoles(Object requestObject) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method =  requestObject.getClass().getMethod("getRoleIds");

        List<Role> roles = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Integer> roleIds = (List<Integer>) method.invoke(requestObject);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (int roleId: roleIds
            ) {
                Optional<Role> optionalRole = roleRepository.findById(roleId);
                optionalRole.ifPresent(roles::add);
            }
        }
        return roles;
    }

    private void sendMessageToDeleteAuthCache(String username) {
        rabbitTemplate.convertAndSend(EXCHANGE, AUTH_DELETE_ROUTING_KEY, username);
        log.info("Message has been published via routing key '{}'", AUTH_DELETE_ROUTING_KEY);
    }

    private String sendImageToFileService(MultipartFile uploadedFile) throws IOException {
        byte[] bytes = uploadedFile.getBytes();
        String extension = FilenameUtils.getExtension(uploadedFile.getOriginalFilename());

        if (extension == null ||
                (!extension.equalsIgnoreCase("jpg") &&
                !extension.equalsIgnoreCase("jpeg") &&
                !extension.equalsIgnoreCase("png"))) {
            throw new BadRequestException("Uploaded images must be in '.jpg', '.jpeg' or '.png' format");
        }

        String newName = UUID.randomUUID().toString();
        String filename = newName + "." + extension;
        Message message = MessageBuilder
                .withBody(bytes)
                .setHeader("Filename", filename)
                .build();

        rabbitTemplate.send(EXCHANGE, AVATAR_PROCESSING_ROUTING_KEY, message);
        log.info("Message has been published via routing key '{}'", AVATAR_PROCESSING_ROUTING_KEY);

        return newName + ".jpg";
    }

    @Override
    @Cacheable(value = getAllMethodCache, key = "#pagination.getPageNumber()")
    public List<UserResponse> getAll(Pagination pagination) {
        List<User> users;
        if (pagination == null) {
            users =  userRepository.findAll();
        } else {
            PageRequest pageRequest = Pagination.getPageRequest(pagination);
            users =  userRepository.findAll(pageRequest).getContent();
        }
        return modelMapper.map(users, new TypeToken<List<UserResponse>>() {}.getType());
    }

    @Override
    @Cacheable(value = getMethodCache, key = "#id")
    public UserResponse get(int id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            return modelMapper.map(optionalUser.get(), UserResponse.class);
        } else {
            throw new NotFoundException("User not found");
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = getAllMethodCache),
            @CacheEvict(value = getMethodCache, key = "#id")
    })
    public void delete(int id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            userRepository.delete(user);

//            delete all existing all role assignments (needs experiments)
            List<UserRoleMapping> userRoleMappings = user.getUserRoleMappings();
            if (!userRoleMappings.isEmpty()) {
                userRoleMappingRepository.deleteAll(userRoleMappings);
            }

//            send message to demand auth-gateway to update authenticatedUser redis cache
            this.sendMessageToDeleteAuthCache(user.getUsername());
        } else {
            throw new NotFoundException("User not found");
        }
    }

    @Override
    public List<String> getAllAvatarNames() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(User::getAvatar)
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
            String filename = this.sendImageToFileService(avatar);
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

        return this.get(editedUser.getId());
    }

    @Scheduled(fixedDelay = 60000)
    public void cacheEvict() {
        super.clearCache(getAllMethodCache, getMethodCache);
    }
}
