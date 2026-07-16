package xyz.migoo.framework.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import xyz.migoo.framework.common.util.validation.ValidationUtils;

/**
 * @author xiaomi
 * Created on 2021/12/4 12:28
 */
public class EmailValidator implements ConstraintValidator<Email, String> {

    @Override
    public void initialize(Email annotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return ValidationUtils.isEmail(value);
    }
}
