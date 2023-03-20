package fpt.edu.user_service.services;

import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.EditUserRequest;
import fpt.edu.user_service.dtos.requestDtos.userRequestDtos.NewUserRequest;
import fpt.edu.user_service.dtos.responseDtos.UserResponse;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.pagination.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Service
public interface UserService {
    UserResponse save(NewUserRequest newUserRequest, HttpServletRequest request) throws UniqueKeyViolationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException;
    UserResponse update(int id, EditUserRequest editUserRequest, HttpServletRequest request) throws UniqueKeyViolationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException;
    List<UserResponse> getAll(Pagination pagination);
    UserResponse get(int id);
    void delete(int id);

    List<String> getAllAvatarNames();
}
