package fpt.edu.user_service.controllers;

import fpt.edu.user_service.dtos.ExchangeGuardedPath;
import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.dtos.authenticationDtos.LoginRequest;
import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.EditUserRequest;
import fpt.edu.user_service.dtos.responseDtos.FunctionResponse;
import fpt.edu.user_service.dtos.responseDtos.UserResponse;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@CrossOrigin
@RestController
public class AuthenticationController extends BaseController {

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> login(@ModelAttribute LoginRequest loginRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return createBindingErrorResponse(bindingResult);
        }
        String jwt = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return createSuccessResponse(jwt);
    }

    @GetMapping(value = "/userByUsername")
    public ExchangeUser getUserByUsername(String username) {
        return authService.loadUserByUsername(username);
    }

    @GetMapping(value = "/allGuardedPaths")
    public List<ExchangeGuardedPath> getAllGuardedPaths() {
        return authService.getAllGuardedPaths();
    }

    @GetMapping(value = "/auth")
    public ResponseEntity<Object> getAuthUser(HttpServletRequest request) {
        UserResponse userResponse = authService.getAuthenticatedUser(request);
        return createSuccessResponse(userResponse);
    }

    @PutMapping(value = "/auth/editPersonalInfo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editPersonalInfo(@Valid @ModelAttribute EditUserRequest editUserRequest, HttpServletRequest request, BindingResult bindingResult)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException, UniqueKeyViolationException {
        if (bindingResult.hasErrors()) {
            return createBindingErrorResponse(bindingResult);
        } else {
            UserResponse userResponse = authService.editPersonalInfo(editUserRequest, request);
            return createSuccessResponse(userResponse);
        }
    }

    @GetMapping(value = "/auth/authorizedFunctions")
    public ResponseEntity<Object> getAuthorizedFunctions(HttpServletRequest request) {
        List<FunctionResponse> authorizedFunctions = authService.getAuthorizedFunctions(request);
        return createSuccessResponse(authorizedFunctions);
    }
}
