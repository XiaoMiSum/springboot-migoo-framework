package xyz.migoo.framework.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {EmailValidator.class}
)
public @interface Password {

    String message() default "不能使用弱密码";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
