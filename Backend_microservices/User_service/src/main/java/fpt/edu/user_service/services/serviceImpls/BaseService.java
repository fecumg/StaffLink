package fpt.edu.user_service.services.serviceImpls;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.entities.User;
import fpt.edu.user_service.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * @author Truong Duc Duong
 */

@Log4j2
public class BaseService {

    @Value("${http.request.auth.id}")
    private String AUTH_ID;

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;
    @Value("${rabbitmq.routing-key.auth-cache-update}")
    private String AUTH_UPDATE_ROUTING_KEY;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    public RabbitTemplate rabbitTemplate;

    protected void setCreatedBy(Object object, HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String authUserIdString = request.getHeader(AUTH_ID);
        if (StringUtils.hasText(authUserIdString)) {
            int authUserId = Integer.parseInt(authUserIdString);
            Optional<User> optionalAuthUser = userRepository.findById(authUserId);
            if (optionalAuthUser.isPresent()) {
                Method method = object.getClass().getMethod("setCreatedBy", User.class);
                method.invoke(object, optionalAuthUser.get());
            }
        }
    }

    protected void setUpdatedBy(Object object, HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String authUserIdString = request.getHeader(AUTH_ID);
        if (StringUtils.hasText(authUserIdString)) {
            int authUserId = Integer.parseInt(authUserIdString);
            Optional<User> optionalAuthUser = userRepository.findById(authUserId);
            if (optionalAuthUser.isPresent()) {
                Method method = object.getClass().getMethod("setUpdatedBy", User.class);
                method.invoke(object, optionalAuthUser.get());
            }
        }
    }

    protected void sendMessageToUpdateAuthCache(List<ExchangeUser> exchangeUserList) {
        rabbitTemplate.convertAndSend(EXCHANGE, AUTH_UPDATE_ROUTING_KEY, exchangeUserList);
        log.info("Message has been published via routing key '{}'", AUTH_UPDATE_ROUTING_KEY);
    }
}
