package xyz.migoo.framework.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import xyz.migoo.framework.common.core.ArrayValuable;

import java.lang.annotation.*;

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
        validatedBy = InEnumValidator.class
)
public @interface InEnum {

    /**
     * @return 实现 ArrayValuable 接口的枚举类
     */
    Class<? extends ArrayValuable> value();

    String message() default "{validation.values.range.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
