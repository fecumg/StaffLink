package fpt.edu.user_service.services;

import fpt.edu.user_service.dtos.requestDtos.RoleRequest;
import fpt.edu.user_service.dtos.responseDtos.RoleResponse;
import fpt.edu.user_service.pagination.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Service
public interface RoleService {
    RoleResponse save(RoleRequest roleRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException;
    RoleResponse update(int id, RoleRequest roleRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException;
    List<RoleResponse> getAll(Pagination pagination);
    RoleResponse get(int id);
    void delete(int id);
}
