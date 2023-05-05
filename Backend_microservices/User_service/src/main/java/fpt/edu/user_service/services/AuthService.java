package fpt.edu.user_service.services;

import fpt.edu.user_service.dtos.ExchangeGuardedPath;
import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.EditUserRequest;
import fpt.edu.user_service.dtos.responseDtos.FunctionResponse;
import fpt.edu.user_service.dtos.responseDtos.UserResponse;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Service
public interface AuthService {
    String login(String username, String password);
    ExchangeUser loadUserByUsername(String username);
    List<ExchangeGuardedPath> getAllGuardedPaths();


    UserResponse getAuthenticatedUser(HttpServletRequest request);
    UserResponse editPersonalInfo(EditUserRequest editUserRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException, UniqueKeyViolationException;

    List<FunctionResponse> getAuthorizedFunctions(HttpServletRequest request);
}
