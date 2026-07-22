package xyz.migoo.framework.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private Pattern pattern;

    @Override
    public void initialize(Password annotation) {
        String special = Pattern.quote(annotation.specialChars());
        this.pattern = Pattern.compile(
                "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[" + special + "]).{" + annotation.min() + "," + annotation.max() + "}$");
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return (value != null && !value.isEmpty()) && pattern.matcher(value).matches();
    }
}