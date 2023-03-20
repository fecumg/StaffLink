package fpt.edu.user_service.services;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Service
public interface AuthService {
    String login(String username, String password);
    ExchangeUser loadUserByUsername(String username);
    List<String> getAllGuardedPaths();
}
