package xyz.migoo.framework.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import xyz.migoo.framework.common.util.validation.ValidationUtils;

public class MobileValidator implements ConstraintValidator<Mobile, String> {

    private String region;

    @Override
    public void initialize(Mobile annotation) {
        this.region = annotation.region();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return ValidationUtils.isMobile(value, region);
    }

}
