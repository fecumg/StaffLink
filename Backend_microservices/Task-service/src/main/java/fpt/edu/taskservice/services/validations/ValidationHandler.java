package fpt.edu.taskservice.services.validations;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Truong Duc Duong
 */

@Component
@RequiredArgsConstructor
public class ValidationHandler {

    @Autowired
    private Validator validator;

    public <T> void validate(T t) {
        Set<ConstraintViolation<T>> validate = validator.validate(t);
        if(! validate.isEmpty()) {
            ConstraintViolation<T> violation = validate.stream()
                    .iterator()
                    .next();
            throw new BadRequestException(violation.getMessage());
        }
    }
}
