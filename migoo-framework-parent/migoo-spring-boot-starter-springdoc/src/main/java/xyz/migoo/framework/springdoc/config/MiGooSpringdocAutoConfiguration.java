package xyz.migoo.framework.springdoc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * springdoc 文档组件自动配置
 *
 * <p>在 springdoc-openapi 默认扫描能力之上，叠加框架级默认定制：</p>
 * <ol>
 *     <li>OpenAPI 元信息（title/description/version）：标题优先取 {@code migoo.springdoc.title}，
 *     留空回退 {@code spring.application.name}；</li>
 *     <li>Bearer JWT 安全方案（可选，默认开启）：注册 {@value #SECURITY_SCHEME_NAME} 方案并加全局
 *     安全要求，Swagger UI 顶部出现 Authorize 按钮，联调时一次填入 token 全接口可用；
 *     免认证端点可在控制器方法上用 {@code @SecurityRequirements} 清空覆盖。</li>
 * </ol>
 *
 * <p>总开关 {@code migoo.springdoc.enabled}（默认开启，关闭后仅保留 springdoc 原生行为）；
 * 应用自定义 {@link OpenApiCustomizer} Bean 可整体覆盖本定制。</p>
 *
 * @author xiaomi
 */
@AutoConfiguration
@EnableConfigurationProperties(MigooSpringdocProperties.class)
@ConditionalOnProperty(prefix = "migoo.springdoc", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class MiGooSpringdocAutoConfiguration {

    /**
     * Bearer JWT 安全方案标识（Components 与全局 SecurityRequirement 共用）
     */
    static final String SECURITY_SCHEME_NAME = "bearer-jwt";

    /**
     * OpenAPI 定制 Bean：元信息 + Bearer JWT 安全方案
     * <p>
     * 存在自定义 {@link OpenApiCustomizer} 时不再注册（整体覆盖默认定制）
     */
    @Bean
    @ConditionalOnMissingBean(OpenApiCustomizer.class)
    public OpenApiCustomizer migooOpenApiCustomizer(Environment environment,
                                                     MigooSpringdocProperties properties) {
        return openApi -> {
            // 1) 文档元信息
            openApi.info(new Info()
                    .title(resolveTitle(environment, properties))
                    .description(properties.getDescription())
                    .version(properties.getVersion()));

            // 2) Bearer JWT 安全方案（可选）
            if (properties.isSecurityScheme()) {
                Components components = openApi.getComponents() != null
                        ? openApi.getComponents() : new Components();
                components.addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("输入 access_token，Swagger UI 自动补全 Bearer 前缀"));
                openApi.components(components);
                openApi.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
            }
        };
    }

    /**
     * 解析文档标题：显式配置 &gt; spring.application.name &gt; 默认值
     */
    private String resolveTitle(Environment environment, MigooSpringdocProperties properties) {
        if (!properties.getTitle().isBlank()) {
            return properties.getTitle();
        }
        String applicationName = environment.getProperty("spring.application.name");
        return applicationName == null || applicationName.isBlank() ? "MiGoo API" : applicationName;
    }
}
