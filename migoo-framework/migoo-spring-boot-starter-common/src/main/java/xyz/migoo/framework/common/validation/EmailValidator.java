package xyz.migoo.framework.common.validation;

import cn.hutool.core.util.StrUtil;
import xyz.migoo.framework.common.util.validation.ValidationUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
            // 如果手机号为空，默认不校验，即校验通过
            if (StrUtil.isEmpty(value)) {
                return true;
            }
            // 校验手机
            return ValidationUtils.isEmail(value);
        }
}
