package fpt.edu.user_service.controllers;

import fpt.edu.user_service.responses.ErrorApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Truong Duc Duong
 */
public class BaseController {
    public ResponseEntity<Object> createSuccessResponse(Object object, HttpStatus httpStatus) {
        return new ResponseEntity<>(object, httpStatus);
    }

    public ResponseEntity<Object> createSuccessResponse(Object object) {
        return new ResponseEntity<>(object, HttpStatus.OK);
    }

    public ResponseEntity<?> createSuccessResponse() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<ErrorApiResponse> createErrorResponse(String message, Object object, HttpStatus httpStatus) {
        return new ResponseEntity<>(new ErrorApiResponse(message, object), httpStatus);
    }

    public ResponseEntity<ErrorApiResponse> createBindingErrorResponse(BindingResult bindingResult) {
        List<String> details = new ArrayList<>();
        for (ObjectError error: bindingResult.getAllErrors()
        ) {
            details.add(error.getDefaultMessage());
        }
        ErrorApiResponse errorApiResponse = new ErrorApiResponse("Submitted information invalid", details);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(errorApiResponse, httpStatus);
    }
}
