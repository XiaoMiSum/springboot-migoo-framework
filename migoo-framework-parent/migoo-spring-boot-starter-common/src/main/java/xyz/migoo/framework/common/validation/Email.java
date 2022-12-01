package xyz.migoo.framework.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * @author xiaomi
 * Created on 2021/12/4 12:25
 */
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.PARAMETER,
        ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = EmailValidator.class
)
public @interface Email {

    String message() default "邮箱地址格式不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
