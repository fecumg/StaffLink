package fpt.edu.user_service.customAnnotations;

import fpt.edu.user_service.customAnnotations.customValidator.FieldsMatchValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * @author Truong Duc Duong
 */

@Documented
@Constraint(validatedBy = FieldsMatchValidator.class)
@Target( ElementType.TYPE )
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsMatch {
    String message() default "{field} and {matchedField} mismatch";

    String field();

    String matchedField();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
