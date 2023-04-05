package fpt.edu.taskservice.controllers;

import fpt.edu.taskservice.responses.ErrorApiResponse;
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
