package fpt.edu.user_service.services.serviceImpls;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.entities.Function;
import fpt.edu.user_service.entities.User;
import fpt.edu.user_service.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Truong Duc Duong
 */

@Log4j2
public class BaseService {

    @Value("${http.request.auth.id}")
    protected String AUTH_ID;

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;
    @Value("${rabbitmq.routing-key.auth-cache-update}")
    private String AUTH_UPDATE_ROUTING_KEY;
    @Value("${rabbitmq.routing-key.avatar-processing}")
    private String AVATAR_PROCESSING_ROUTING_KEY;
    @Value("${rabbitmq.routing-key.auth-cache-delete}")
    private String AUTH_DELETE_ROUTING_KEY;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    public RabbitTemplate rabbitTemplate;
    @Autowired
    private CacheManager cacheManager;

    protected void setCreatedBy(Object object, HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String authUserIdString = request.getHeader(AUTH_ID);
        if (StringUtils.hasText(authUserIdString)) {
            int authUserId = Integer.parseInt(authUserIdString);
            Optional<User> optionalAuthUser = userRepository.findById(authUserId);
            if (optionalAuthUser.isPresent()) {
                Method method = object.getClass().getMethod("setCreatedBy", int.class);
                method.invoke(object, authUserId);
            }
        }
    }

    protected void setUpdatedBy(Object object, HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String authUserIdString = request.getHeader(AUTH_ID);
        if (StringUtils.hasText(authUserIdString)) {
            int authUserId = Integer.parseInt(authUserIdString);
            Optional<User> optionalAuthUser = userRepository.findById(authUserId);
            if (optionalAuthUser.isPresent()) {
                Method method = object.getClass().getMethod("setUpdatedBy", int.class);
                method.invoke(object, authUserId);
            }
        }
    }

    protected void sendMessageToUpdateAuthCache(List<ExchangeUser> exchangeUserList) {
        rabbitTemplate.convertAndSend(EXCHANGE, AUTH_UPDATE_ROUTING_KEY, exchangeUserList);
        log.info("Message has been published via routing key '{}'", AUTH_UPDATE_ROUTING_KEY);
    }

    protected void clearCache(String getAllMethodCacheName, String getMethodCacheName) {
        Cache getAllMethodCache = cacheManager.getCache(getAllMethodCacheName);
        Cache getMethodCache = cacheManager.getCache(getMethodCacheName);
        if (getAllMethodCache != null) {
            getAllMethodCache.clear();
            log.info("Cache '{}' cleared", getAllMethodCache);
        }
        if (getMethodCache != null) {
            getMethodCache.clear();
            log.info("Cache '{}' cleared", getMethodCache);
        }
    }

    protected String sendImageToFileService(MultipartFile uploadedFile) throws IOException {
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

    protected void sendMessageToDeleteAuthCache(String username) {
        rabbitTemplate.convertAndSend(EXCHANGE, AUTH_DELETE_ROUTING_KEY, username);
        log.info("Message has been published via routing key '{}'", AUTH_DELETE_ROUTING_KEY);
    }

    protected String formatUri(String uri) {
        uri = formatUriPrefix(uri);
        uri = formatUriSuffix(uri);
        return uri;
    }

    private String formatUriPrefix(String uri) {
        if (!StringUtils.hasText(uri)) {
            return uri;
        }
        if (!uri.startsWith("/")) {
            return  "/" + uri;
        } else {
            return formatUriPrefix(uri.substring(1));
        }
    }

    private String formatUriSuffix(String uri) {
        if (!StringUtils.hasText(uri)) {
            return uri;
        }
        if (!uri.endsWith("/")) {
            return uri;
        } else {
            return formatUriSuffix(uri.substring(0, uri.length() - 1));
        }
    }

    protected List<Function> rearrange(List<Function> functions, Function initialParent) {
        List<Function> rearrangedList = new ArrayList<>();
        functions.forEach(function -> {
            if ((initialParent == null && function.getParent() == null) ||
                    (initialParent != null && function.getParent() != null && initialParent.getId() == function.getParent().getId())) {
                rearrangedList.add(function);
                rearrangedList.addAll(rearrange(functions, function));
            }
        });
        return rearrangedList;
    }
}
