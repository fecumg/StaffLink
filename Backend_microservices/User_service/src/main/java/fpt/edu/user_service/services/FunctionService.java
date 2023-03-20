package fpt.edu.user_service.services;

import fpt.edu.user_service.dtos.requestDtos.FunctionRequest;
import fpt.edu.user_service.dtos.responseDtos.FunctionResponse;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.pagination.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Service
public interface FunctionService {
    FunctionResponse save(FunctionRequest functionRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, UniqueKeyViolationException;
    FunctionResponse update(int id, FunctionRequest functionRequest, HttpServletRequest request) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, UniqueKeyViolationException;
    List<FunctionResponse> getAll(Pagination pagination);
    FunctionResponse get(int id);
    void delete(int id);
}
