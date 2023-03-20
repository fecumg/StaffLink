package fpt.edu.user_service.controllers;

import fpt.edu.user_service.dtos.authenticationDtos.ExchangeUser;
import fpt.edu.user_service.dtos.authenticationDtos.LoginRequest;
import fpt.edu.user_service.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
    public List<String> getAllGuardedPaths() {
        return authService.getAllGuardedPaths();
    }
}
