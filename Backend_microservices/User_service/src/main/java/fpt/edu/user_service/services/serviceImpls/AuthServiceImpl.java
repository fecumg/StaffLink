package fpt.edu.user_service.services.serviceImpls;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.entities.Function;
import fpt.edu.user_service.entities.User;
import fpt.edu.user_service.jwt.JwtUtilities;
import fpt.edu.user_service.repositories.FunctionRepository;
import fpt.edu.user_service.repositories.UserRepository;
import fpt.edu.user_service.services.AuthService;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public List<String> getAllGuardedPaths() {
        return functionRepository.findAll().stream()
                .map(Function::getUri)
                .collect(Collectors.toList());
    }
}
