package fpt.edu.fileservice.controllers.exceptionHandler;

import fpt.edu.fileservice.responses.ErrorApiResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.NoSuchFileException;

/**
 * @author Truong Duc Duong
 */

@ControllerAdvice
@Log4j2
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity<ErrorApiResponse> handleUnauthorizedException(NoSuchFileException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ErrorApiResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApiResponse> handleUnauthorizedException(Exception e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(new ErrorApiResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
