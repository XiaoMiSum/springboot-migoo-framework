package xyz.migoo.framework.springdoc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MiGooSpringdocAutoConfiguration} 单元测试
 *
 * <p>覆盖元信息标题三级回退（显式配置 / spring.application.name / 默认值）、
 * Bearer JWT 安全方案与全局安全要求、关闭安全方案、复用既有 Components。</p>
 */
class MiGooSpringdocAutoConfigurationTest {

    private final MiGooSpringdocAutoConfiguration configuration = new MiGooSpringdocAutoConfiguration();

    @Test
    void appliesTitleFromSpringApplicationNameAndRegistersBearerScheme() {
        StandardEnvironment environment = environmentWithAppName("demo-app");

        OpenAPI openApi = customise(environment, new MigooSpringdocProperties());

        // 元信息：标题回退 spring.application.name，描述/版本取默认
        assertThat(openApi.getInfo().getTitle()).isEqualTo("demo-app");
        assertThat(openApi.getInfo().getDescription()).isEqualTo("MiGoo Framework API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("1.0.0");

        // Bearer JWT 安全方案
        SecurityScheme scheme = openApi.getComponents().getSecuritySchemes().get("bearer-jwt");
        assertThat(scheme).isNotNull();
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");

        // 全局安全要求
        assertThat(openApi.getSecurity()).hasSize(1);
        assertThat(openApi.getSecurity().get(0)).containsKey("bearer-jwt");
    }

    @Test
    void explicitTitleOverridesSpringApplicationName() {
        StandardEnvironment environment = environmentWithAppName("demo-app");
        MigooSpringdocProperties properties = new MigooSpringdocProperties();
        properties.setTitle("订单中心 API");
        properties.setVersion("2.0.0");

        OpenAPI openApi = customise(environment, properties);

        assertThat(openApi.getInfo().getTitle()).isEqualTo("订单中心 API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("2.0.0");
    }

    @Test
    void fallsBackToDefaultTitleWhenNoApplicationName() {
        OpenAPI openApi = customise(new StandardEnvironment(), new MigooSpringdocProperties());

        assertThat(openApi.getInfo().getTitle()).isEqualTo("MiGoo API");
    }

    @Test
    void securitySchemeDisabledKeepsInfoOnly() {
        MigooSpringdocProperties properties = new MigooSpringdocProperties();
        properties.setSecurityScheme(false);

        OpenAPI openApi = customise(new StandardEnvironment(), properties);

        assertThat(openApi.getInfo()).isNotNull();
        // 未触碰安全方案：Components 与全局安全要求均不产生
        assertThat(openApi.getComponents()).isNull();
        assertThat(openApi.getSecurity()).isNull();
    }

    @Test
    void reusesExistingComponents() {
        OpenAPI openApi = new OpenAPI();
        Components existing = new Components();
        existing.addSchemas("Order", new Schema<>());
        openApi.components(existing);

        customise(new StandardEnvironment(), new MigooSpringdocProperties(), openApi);

        // 既有 Components 保留，安全方案叠加进去
        assertThat(openApi.getComponents().getSchemas()).containsKey("Order");
        assertThat(openApi.getComponents().getSecuritySchemes()).containsKey("bearer-jwt");
    }

    /**
     * 构造带 spring.application.name 的环境（StandardEnvironment 无 setProperty，经属性源注入）
     */
    private StandardEnvironment environmentWithAppName(String appName) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(
                new MapPropertySource("test", Map.of("spring.application.name", appName)));
        return environment;
    }

    private OpenAPI customise(Environment environment, MigooSpringdocProperties properties) {
        return customise(environment, properties, new OpenAPI());
    }

    private OpenAPI customise(Environment environment, MigooSpringdocProperties properties, OpenAPI openApi) {
        OpenApiCustomizer customizer = configuration.migooOpenApiCustomizer(environment, properties);
        customizer.customise(openApi);
        return openApi;
    }
}
