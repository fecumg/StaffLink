package fpt.edu.user_service.customAnnotations;

import fpt.edu.user_service.customAnnotations.customValidator.PhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * @author Truong Duc Duong
 */

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Phone {
    String message() default "Invalid phone format";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}