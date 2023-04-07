package fpt.edu.taskservice.controllers.exceptionHandler;

import com.fasterxml.jackson.core.JsonParseException;
import fpt.edu.taskservice.exceptions.UnauthorizedException;
import fpt.edu.taskservice.responses.ErrorApiResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Truong Duc Duong
 */


@RestControllerAdvice
@Log4j2
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorApiResponse> handleUnauthorizedException(UnauthorizedException e) {
        e.printStackTrace();
        return new ResponseEntity<>(new ErrorApiResponse(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorApiResponse> handleNotFoundException(NotFoundException e) {
        e.printStackTrace();
        return new ResponseEntity<>(new ErrorApiResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorApiResponse> handleBadRequestException(BadRequestException e) {
        e.printStackTrace();
        return new ResponseEntity<>(new ErrorApiResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<ErrorApiResponse> handleException(JsonParseException e) {
        e.printStackTrace();
        return new ResponseEntity<>(new ErrorApiResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApiResponse> handleException(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(new ErrorApiResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
