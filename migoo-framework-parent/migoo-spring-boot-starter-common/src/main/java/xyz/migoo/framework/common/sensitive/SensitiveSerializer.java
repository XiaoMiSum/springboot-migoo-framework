package xyz.migoo.framework.common.sensitive;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * 敏感数据脱敏序列化器
 * <p>
 * 由 {@link Sensitive} 注解经 {@code @JsonSerialize(using = ...)} 元注解挂载，
 * 在上下文化阶段从字段注解读取脱敏策略（{@link Sensitive#type()}）。
 * 数值等非字符串字段也会以掩码后的<b>字符串</b>输出——脱敏值本身即字符串形态。
 *
 * @author xiaomi
 */
public class SensitiveSerializer extends ValueSerializer<Object> {

    /**
     * 脱敏策略（字段注解读取结果；为 null 时原样输出，见 {@link #createContextual}）
     */
    private final SensitiveType type;

    /**
     * Jackson 实例化入口（经 {@code @JsonSerialize} 引用时反射调用）
     */
    public SensitiveSerializer() {
        this(null);
    }

    private SensitiveSerializer(SensitiveType type) {
        this.type = type;
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext context, BeanProperty property) {
        if (property != null) {
            Sensitive sensitive = property.getAnnotation(Sensitive.class);
            if (sensitive != null) {
                return new SensitiveSerializer(sensitive.type());
            }
        }
        return this;
    }

    @Override
    public void serialize(Object value, JsonGenerator generator, SerializationContext context)
            throws JacksonException {
        if (value == null) {
            generator.writeNull();
            return;
        }
        String text = String.valueOf(value);
        generator.writeString(type == null ? text : SensitiveDataUtil.mask(text, type));
    }
}
