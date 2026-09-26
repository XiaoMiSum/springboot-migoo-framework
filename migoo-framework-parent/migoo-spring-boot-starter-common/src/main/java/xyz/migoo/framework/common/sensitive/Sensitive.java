package xyz.migoo.framework.common.sensitive;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感数据脱敏注解（Jackson 序列化方向自动生效）
 * <p>
 * 标注在字段或 getter 上，序列化（响应/输出）时按 {@link SensitiveType} 掩码：
 * {@code JsonUtils} 与 Spring MVC 响应序列化均走 Jackson 注解内省，无需注册额外模块。
 * <p>
 * 示例:
 * <pre>
 * public class UserVO {
 *     {@code @Sensitive(type = SensitiveType.MOBILE)}
 *     private String mobile;        // -&gt; "138****5678"
 *
 *     {@code @Sensitive(type = SensitiveType.PASSWORD)}
 *     private String password;      // -&gt; "******"
 *
 *     private String nickname;      // 未标注，原样输出
 * }
 * </pre>
 * 约定：
 * <ul>
 *     <li>只作用于<b>序列化</b>（输出方向），反序列化不还原——入参无需脱敏；</li>
 *     <li>数值字段（如 Long 卡号）同样适用，脱敏后以字符串形态输出；</li>
 *     <li>null 原样输出 JSON null；长度不足以保留两侧的整体掩码；</li>
 *     <li>独立调用（非 JSON 场景）用 {@link SensitiveDataUtil#mask}。</li>
 * </ul>
 *
 * @author xiaomi
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerializer.class)
public @interface Sensitive {

    /**
     * 脱敏策略
     */
    SensitiveType type();
}
