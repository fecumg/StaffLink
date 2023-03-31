package fpt.edu.user_service.controllers;

import fpt.edu.user_service.dtos.requestDtos.FunctionRequest;
import fpt.edu.user_service.dtos.responseDtos.FunctionResponse;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.pagination.Pagination;
import fpt.edu.user_service.services.FunctionService;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Truong Duc Duong
 */

@CrossOrigin
@RestController
@RequestMapping("/functions")
public class FunctionController extends BaseController {

    @Autowired
    private FunctionService functionService;

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> newFunction(@ModelAttribute @Valid FunctionRequest functionRequest, HttpServletRequest request, BindingResult bindingResult)
            throws UniqueKeyViolationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException
    {
        if (bindingResult.hasErrors()) {
            return createBindingErrorResponse(bindingResult);
        } else {
            FunctionResponse functionResponse = functionService.save(functionRequest, request);
            return createSuccessResponse(functionResponse);
        }
    }

    @PutMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editFunction(@PathVariable("id") int id, @Valid @ModelAttribute FunctionRequest functionRequest, HttpServletRequest request, BindingResult bindingResult)
            throws UniqueKeyViolationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException
    {
        if (bindingResult.hasErrors()) {
            return createBindingErrorResponse(bindingResult);
        } else {
            FunctionResponse functionResponse = functionService.update(id, functionRequest, request);
            return createSuccessResponse(functionResponse);
        }
    }

    @GetMapping(value = "")
    public ResponseEntity<Object> getFunctions(@Nullable Pagination pagination) {
        List<FunctionResponse> functionResponses = functionService.getAll(pagination);
        return createSuccessResponse(functionResponses);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getFunction(@PathVariable("id") int id) {
        FunctionResponse functionResponse = functionService.get(id);
        return createSuccessResponse(functionResponse);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteFunction(@PathVariable("id") int id) {
        functionService.delete(id);
        return createSuccessResponse();
    }

    @GetMapping(value = "/authorized")
    public ResponseEntity<Object> getAuthorizedFunctions(HttpServletRequest request) {
        List<FunctionResponse> authorizedFunctions = functionService.getAuthorizedFunctions(request);
        return createSuccessResponse(authorizedFunctions);
    }

    @GetMapping(value = "/potentialParents/{id}")
    public ResponseEntity<Object> getPotentialParents(@PathVariable("id") int id) {
        List<FunctionResponse> potentialParents = functionService.getPotentialParentFunctions(id);
        return createSuccessResponse(potentialParents);
    }
}
