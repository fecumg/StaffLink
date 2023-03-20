package fpt.edu.user_service.controllers;

import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.EditUserRequest;
import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.NewUserRequest;
import fpt.edu.user_service.dtos.responseDtos.UserResponse;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.pagination.Pagination;
import fpt.edu.user_service.services.UserService;
import jakarta.annotation.Nullable;
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
@RequestMapping("/users")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> newUser(@ModelAttribute @Valid NewUserRequest newUserRequest, HttpServletRequest request, BindingResult bindingResult)
            throws UniqueKeyViolationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        if (bindingResult.hasErrors()) {
            return createBindingErrorResponse(bindingResult);
        } else {
            UserResponse userResponse = userService.save(newUserRequest, request);
            return createSuccessResponse(userResponse);
        }
    }

    @PutMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editUser(@PathVariable("id") int id, @Valid @ModelAttribute EditUserRequest editUserRequest, HttpServletRequest request, BindingResult bindingResult)
            throws UniqueKeyViolationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        if (bindingResult.hasErrors()) {
            return createBindingErrorResponse(bindingResult);
        } else {
            UserResponse userResponse = userService.update(id, editUserRequest, request);
            return createSuccessResponse(userResponse);
        }
    }

    @GetMapping(value = "")
    public ResponseEntity<Object> getUsers(@Nullable Pagination pagination) {
        List<UserResponse> userResponses = userService.getAll(pagination);
        return createSuccessResponse(userResponses);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getUser(@PathVariable("id") int id) {
        UserResponse userResponse = userService.get(id);
        return createSuccessResponse(userResponse);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") int id) {
        userService.delete(id);
        return createSuccessResponse();
    }

    @GetMapping(value = "/avatarNames")
    public List<String> getAvatarNames() {
        return userService.getAllAvatarNames();
    }
}
