package xyz.migoo.framework.web.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.CorsFilter;
import xyz.migoo.framework.web.core.cors.CorsOriginPredicate;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;
import xyz.migoo.framework.web.core.store.StateStore;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MiGooWebAutoConfiguration} 冒烟测试：真实刷新 Spring 上下文，验证
 * 「默认可启动 / 非法配置 fail-fast / 用户 Bean 可覆盖」三条契约。
 *
 * <p>与逐 @Bean 方法的单元测试互补：这里覆盖条件注解、属性绑定与装配顺序的组合行为。</p>
 */
class MiGooWebAutoConfigurationSmokeTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MiGooWebAutoConfiguration.class));

    // ==================== 默认可启动 ====================

    @Test
    void defaultStrictModeStartsAndDisablesCorsFilter() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            // 默认 STRICT + 空来源 = 不放行任何跨域，CORS 过滤器注册但禁用
            FilterRegistrationBean<CorsFilter> cors = corsFilterBean(context);
            assertThat(cors.isEnabled()).isFalse();
            // 限流/幂等的默认内存存储已就位
            assertThat(context).hasSingleBean(StateStore.class);
        });
    }

    @Test
    void strictModeOnlyAllowsConfiguredOrigins() {
        runner.withPropertyValues("migoo.web.cors.allowed-origins=https://admin.example.com")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(corsFilterBean(context).isEnabled()).isTrue();
                    CorsFilter filter = corsFilterBean(context).getFilter();
                    // 命中白名单来源：回显具体 Origin
                    assertThat(allowOrigin(filter, "https://admin.example.com"))
                            .isEqualTo("https://admin.example.com");
                    // 未登记来源：不返回任何 Allow-Origin
                    assertThat(allowOrigin(filter, "https://evil.example.org")).isNull();
                });
    }

    @Test
    void openModeAllowsAnyOriginWithoutCredentials() {
        runner.withPropertyValues("migoo.web.cors.mode=open")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    CorsFilter filter = corsFilterBean(context).getFilter();
                    assertThat(allowOrigin(filter, "https://anywhere.io")).isNotNull();
                    // OPEN 模式禁止凭证
                    assertThat(allowCredentials(filter, "https://anywhere.io")).isNull();
                });
    }

    // ==================== 非法配置 fail-fast ====================

    @Test
    void patternModeRejectsWildcardWithCredentials() {
        runner.withPropertyValues(
                        "migoo.web.cors.mode=pattern",
                        "migoo.web.cors.allowed-origin-patterns=*",
                        "migoo.web.cors.allow-credentials=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    Throwable root = rootCause(context.getStartupFailure());
                    assertThat(root).isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("credentials");
                });
    }

    @Test
    void patternModeWithoutPatternsFailsStartup() {
        runner.withPropertyValues("migoo.web.cors.mode=pattern")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(rootCause(context.getStartupFailure()))
                            .isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("allowed-origin-patterns");
                });
    }

    @Test
    void dynamicModeWithoutPredicateBeanFailsStartup() {
        runner.withPropertyValues("migoo.web.cors.mode=dynamic")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(rootCause(context.getStartupFailure()))
                            .isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("CorsOriginPredicate");
                });
    }

    // ==================== 扩展点接入 ====================

    @Test
    void dynamicModeDelegatesToPredicateBean() {
        CorsOriginPredicate registry = origin -> origin.endsWith(".example.com");
        runner.withPropertyValues("migoo.web.cors.mode=dynamic", "migoo.web.cors.allow-credentials=true")
                .withBean(CorsOriginPredicate.class, () -> registry)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    CorsFilter filter = corsFilterBean(context).getFilter();
                    // 谓词放行的动态域名
                    assertThat(allowOrigin(filter, "https://open-tenant.example.com"))
                            .isEqualTo("https://open-tenant.example.com");
                    // 谓词拒绝的外部域名
                    assertThat(allowOrigin(filter, "https://attacker.io")).isNull();
                });
    }

    @Test
    void userStateStoreOverridesInMemoryDefault() {
        StateStore custom = new InMemoryStateStore();
        runner.withBean(StateStore.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    // 应用自定义 StateStore 覆盖默认内存实现（限流/幂等复用同一存储）
                    assertThat(context).hasSingleBean(StateStore.class);
                    assertThat(context.getBean(StateStore.class)).isSameAs(custom);
                });
    }

    @Test
    void corsFilterCanBeTurnedOffByProperty() {
        runner.withPropertyValues("migoo.web.cors.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean("corsFilterBean");
                });
    }

    // ==================== 工具方法 ====================

    @SuppressWarnings("unchecked")
    private static FilterRegistrationBean<CorsFilter> corsFilterBean(AssertableWebApplicationContext context) {
        return context.getBean("corsFilterBean", FilterRegistrationBean.class);
    }

    /**
     * 触发一次跨域请求，返回响应中的 Access-Control-Allow-Origin 头（null = 来源被拒绝）
     */
    private static String allowOrigin(CorsFilter filter, String origin) {
        return exchange(filter, origin).getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    /**
     * 触发一次跨域请求，返回响应中的 Access-Control-Allow-Credentials 头
     */
    private static String allowCredentials(CorsFilter filter, String origin) {
        return exchange(filter, origin).getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
    }

    private static MockHttpServletResponse exchange(CorsFilter filter, String origin) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/smoke");
        request.addHeader(HttpHeaders.ORIGIN, origin);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
        };
        try {
            filter.doFilter(request, response, chain);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return response;
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
