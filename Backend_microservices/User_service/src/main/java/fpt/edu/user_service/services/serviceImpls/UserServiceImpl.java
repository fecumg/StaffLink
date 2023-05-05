package fpt.edu.user_service.services.serviceImpls;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.EditUserRequest;
import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.NewUserRequest;
import fpt.edu.user_service.dtos.responseDtos.UserResponse;
import fpt.edu.user_service.entities.Role;
import fpt.edu.user_service.entities.User;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.pagination.Pagination;
import fpt.edu.user_service.repositories.RoleRepository;
import fpt.edu.user_service.repositories.UserRepository;
import fpt.edu.user_service.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
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

    @Override
    @Cacheable(value = getAllMethodCache, key = "(#search == null ? '' : #search).concat('-').concat(#pagination.getPageNumber())")
    public List<UserResponse> getAll(String search, Pagination pagination) {
        List<User> users;
        if (StringUtils.isEmpty(search)) {
            users = Pagination.retrieve(
                    pagination,
                    () -> userRepository.findAll(),
                    pageRequest -> userRepository.findAll(pageRequest).getContent(),
                    sort -> userRepository.findAll(sort),
                    User.class
            );
        } else {
            users = Pagination.retrieve(
                    pagination,
                    () -> userRepository.search(search),
                    pageRequest -> userRepository.search(search, pageRequest).getContent(),
                    sort -> userRepository.search(search, sort),
                    User.class
            );
        }
        return modelMapper.map(users, new TypeToken<List<UserResponse>>() {}.getType());
    }

    @Override
    public List<UserResponse> search(String search, Pagination pagination) {
        List<User> users;
        if (StringUtils.isEmpty(search)) {
            users = new ArrayList<>();
        } else {
            users = Pagination.retrieve(
                    pagination,
                    () -> userRepository.search(search),
                    pageRequest -> userRepository.search(search, pageRequest).getContent(),
                    sort -> userRepository.search(search, sort),
                    User.class
            );
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
            String username = user.getUsername();

            userRepository.delete(user);

//            send message to demand auth-gateway to update authenticatedUser redis cache
            this.sendMessageToDeleteAuthCache(username);
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

    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Ho_Chi_Minh")
//    @Scheduled(fixedDelay = 60000)
    public void cacheEvict() {
        super.clearCache(getAllMethodCache, getMethodCache);
    }
}
