package xyz.migoo.framework.common.util.validation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.*;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.regex.Pattern;

import static xyz.migoo.framework.common.enums.NumberConstants.N_11;

/**
 * 校验工具类
 *
 * @author xiaomi
 */
public class ValidationUtils {

    private final static Pattern PATTERN_URL = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private final static Pattern PATTERN_MOBILE = Pattern.compile("^(1)\\d{10}$");

    private final static Pattern PATTERN_EMAIL = Pattern.compile("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$");

    public static boolean isMobile(String mobile) {
        return StrUtil.length(mobile) == N_11 && PATTERN_MOBILE.matcher(mobile).matches();
    }

    public static boolean isEmail(String email) {
        return PATTERN_EMAIL.matcher(email).matches();
    }

    public static boolean isUrl(String url) {
        return StringUtils.hasText(url) && PATTERN_URL.matcher(url).matches();
    }

    public static void validate(Object object, Class<?>... groups) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Assert.notNull(validator);
            validate(validator, object, groups);
        }
    }

    public static void validate(Validator validator, Object object, Class<?>... groups) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (CollUtil.isNotEmpty(constraintViolations)) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }
}
