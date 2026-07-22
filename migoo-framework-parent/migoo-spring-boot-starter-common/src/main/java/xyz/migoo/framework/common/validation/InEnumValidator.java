package xyz.migoo.framework.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import xyz.migoo.framework.common.core.ArrayValuable;

import java.util.Collection;

public class InEnumValidator implements ConstraintValidator<InEnum, Object> {

    private Collection<?> values;

    @Override
    public void initialize(InEnum annotation) {
        Class<? extends ArrayValuable> enumClass = annotation.value();
        ArrayValuable[] constants = enumClass.getEnumConstants();
        if (constants.length == 0) {
            this.values = java.util.Set.of();
        } else {
            this.values = constants[0].toCollection();
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (values.contains(value)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                context.getDefaultConstraintMessageTemplate()
                        .replaceAll("\\{value}", values.toString())
        ).addConstraintViolation();
        return false;
    }

}

