package xyz.migoo.framework.jackson.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.module.SimpleModule;
import xyz.migoo.framework.jackson.databind.BigDecimalSerializer;
import xyz.migoo.framework.jackson.databind.LocalDateTimeDeserializer;
import xyz.migoo.framework.jackson.databind.LocalDateTimeSerializer;

import java.time.LocalDateTime;

/**
 * Jackson 序列化定制自动配置
 *
 * <p>注册框架内置的 {@link BigDecimalSerializer}（BigDecimal → 保留 2 位小数字符串）、
 * {@link LocalDateTimeSerializer}/{@link LocalDateTimeDeserializer}（{@code yyyy-MM-dd HH:mm:ss}）。
 * 仅作用于 Spring MVC 响应序列化使用的 {@code JsonMapper}；
 * {@code JsonUtils} 等工具类持有独立 mapper，不受影响。</p>
 *
 * <p><strong>默认关闭</strong>：开启会改变响应中 BigDecimal/LocalDateTime 的输出格式
 * （如 BigDecimal 由数字变字符串），存量应用需评估前端兼容性后再打开
 * {@code migoo.web.jackson.enabled=true}。</p>
 *
 * @author xiaomi
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "migoo.web.jackson", name = "enabled", havingValue = "true")
public class JacksonAutoConfiguration {

    /**
     * 框架内置序列化器注册器
     * <p>
     * 应用自定义同名 Bean 时框架默认值不生效
     */
    @Bean
    @ConditionalOnMissingBean(name = "migooJacksonCustomizer")
    public JsonMapperBuilderCustomizer migooJacksonCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("migoo-framework");
            module.addSerializer(new BigDecimalSerializer());
            module.addSerializer(new LocalDateTimeSerializer());
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
            builder.addModule(module);
        };
    }
}
