package xyz.migoo.framework.common.validation;

import cn.hutool.core.util.StrUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {


    private static final Pattern password = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[_@#$!]).{6,32}$");


    public void initialize(Password annotation) {
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !StrUtil.isEmpty(value) && value.length() >= 8 && password.matcher(value).matches();
    }
}