package fpt.edu.user_service.controllers.exceptionHandler;

import fpt.edu.user_service.exceptions.UnauthorizedException;
import fpt.edu.user_service.exceptions.UniqueKeyViolationException;
import fpt.edu.user_service.responses.ErrorApiResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Truong Duc Duong
 */

@ControllerAdvice
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

    @ExceptionHandler(UniqueKeyViolationException.class)
    public ResponseEntity<ErrorApiResponse> handleUniqueKeyViolationException(UniqueKeyViolationException e) {
        e.printStackTrace();
        return new ResponseEntity<>(new ErrorApiResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApiResponse> handleException(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(new ErrorApiResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @NotNull HttpHeaders headers, @NotNull HttpStatusCode status, @NotNull WebRequest request) {
        List<String> errorDetails = ex.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorApiResponse errorApiResponse = new ErrorApiResponse("Submitted information invalid", errorDetails);
        return new ResponseEntity<>(errorApiResponse, HttpStatus.BAD_REQUEST);
    }
}
