package fpt.edu.user_service.controllers;

import fpt.edu.user_service.dtos.requestDtos.RoleRequest;
import fpt.edu.user_service.dtos.responseDtos.RoleResponse;
import fpt.edu.user_service.pagination.Pagination;
import fpt.edu.user_service.services.RoleService;
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
@RequestMapping("/roles")
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> newRole(@ModelAttribute @Valid RoleRequest roleRequest, HttpServletRequest request, BindingResult bindingResult)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException
    {
        if (bindingResult.hasErrors()) {
            return createBindingErrorResponse(bindingResult);
        } else {
            RoleResponse roleResponse = roleService.save(roleRequest, request);
            return createSuccessResponse(roleResponse);
        }
    }

    @PutMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editRole(@PathVariable("id") int id, @Valid @ModelAttribute RoleRequest roleRequest, HttpServletRequest request, BindingResult bindingResult)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException
    {
        if (bindingResult.hasErrors()) {
            return createBindingErrorResponse(bindingResult);
        } else {
            RoleResponse roleResponse = roleService.update(id, roleRequest, request);
            return createSuccessResponse(roleResponse);
        }
    }

    @GetMapping(value = "")
    public ResponseEntity<Object> getRoles(@Nullable Pagination pagination) {
        List<RoleResponse> roleResponses = roleService.getAll(pagination);
        return createSuccessResponse(roleResponses);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getRole(@PathVariable("id") int id) {
        RoleResponse roleResponse = roleService.get(id);
        return createSuccessResponse(roleResponse);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable("id") int id) {
        roleService.delete(id);
        return createSuccessResponse();
    }
}
