package fpt.edu.user_service.customAnnotations.customValidator;

import fpt.edu.user_service.customAnnotations.FieldsMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanWrapperImpl;

/**
 * @author Truong Duc Duong
 */
@Log4j2
public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {
    private String field;
    private String matchedField;
    @Override
    public void initialize(FieldsMatch constraintAnnotation) {
        field = constraintAnnotation.field();
        matchedField = constraintAnnotation.matchedField();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        Object fieldValue = new BeanWrapperImpl(object).getPropertyValue(field);
        Object matchedFieldValue = new BeanWrapperImpl(object).getPropertyValue(matchedField);
        if (fieldValue != null) {
            return fieldValue.equals(matchedFieldValue);
        } else {
            return matchedFieldValue == null;
        }
    }
}
