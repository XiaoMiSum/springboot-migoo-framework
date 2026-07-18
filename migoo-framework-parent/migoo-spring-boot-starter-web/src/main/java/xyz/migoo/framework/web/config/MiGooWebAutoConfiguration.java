package xyz.migoo.framework.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * MiGoo Web 模块自动配置入口
 *
 * <p>通过 {@link Import} 显式导入各子配置类，不再使用 {@code @ComponentScan}。</p>
 */
@Configuration
@EnableConfigurationProperties(MigooWebProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({
        FrameworkCorsConfiguration.class,
        FilterConfiguration.class,
        ExceptionHandlingConfiguration.class,
        ResponseBodyConfiguration.class,
        I18nConfiguration.class,
        VirtualThreadConfiguration.class
})
public class MiGooWebAutoConfiguration {
}
