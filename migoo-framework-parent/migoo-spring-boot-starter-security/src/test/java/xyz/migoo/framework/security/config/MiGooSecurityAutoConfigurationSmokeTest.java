package xyz.migoo.framework.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher;
import xyz.migoo.framework.security.core.authentication.UserDetailsBridge;
import xyz.migoo.framework.security.utils.PasswordUtils;
import xyz.migoo.framework.web.config.MiGooWebAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * {@link MiGooSecurityAutoConfiguration} + {@link MiGooWebSecurityFilterChainConfiguration} 冒烟测试
 *
 * <p>验证「默认 JWT 模式可启动 / 密钥非法 fail-fast / 用户编码器可覆盖」。
 * Web 自动配置一并加载，补齐 I18N、异常处理、状态存储等安全组件的依赖。</p>
 */
class MiGooSecurityAutoConfigurationSmokeTest {

    private static final String STRONG_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MiGooWebAutoConfiguration.class,
                    MiGooSecurityAutoConfiguration.class,
                    MiGooWebSecurityFilterChainConfiguration.class))
            // Spring Security 基础设施（HttpSecurity / ObjectPostProcessor / AuthenticationConfiguration）
            .withUserConfiguration(SecurityInfrastructure.class)
            // 应用必须提供的两个扩展点，冒烟测试以 mock 顶替
            .withBean(UserDetailsBridge.class, () -> mock(UserDetailsBridge.class))
            .withBean(AuthUserDetailsFetcher.class, () -> mock(AuthUserDetailsFetcher.class))
            .withPropertyValues("migoo.security.jwt.secret-key=" + STRONG_SECRET);

    /**
     * 承载 {@code @EnableWebSecurity}，引入 HttpSecurity、ObjectPostProcessor 等基础设施 Bean
     */
    @EnableWebSecurity
    static class SecurityInfrastructure {
    }

    // ==================== 默认可启动 ====================

    @Test
    void defaultJwtModeStartsUp() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            // 过滤链 + 认证/加密/登出组件全部就位
            assertThat(context).hasSingleBean(SecurityFilterChain.class);
            assertThat(context).hasSingleBean(AuthenticationManager.class);
            assertThat(context).hasSingleBean(PasswordEncoder.class);
            assertThat(context).hasSingleBean(PasswordUtils.class);
            assertThat(context).hasSingleBean(LogoutSuccessHandler.class);
        });
    }

    // ==================== 非法配置 fail-fast ====================

    @Test
    void missingJwtSecretFailsStartup() {
        runner.withPropertyValues("migoo.security.jwt.secret-key=")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("secret-key");
                });
    }

    @Test
    void weakJwtSecretFailsStartup() {
        runner.withPropertyValues("migoo.security.jwt.secret-key=too-weak")
                .run(context -> {
                    assertThat(context).hasFailed();
                    // 熵校验：HS256 密钥不足 32 字节直接拒绝启动
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("32");
                });
    }

    // ==================== 扩展点可覆盖 ====================

    @Test
    void userPasswordEncoderOverridesDefault() {
        PasswordEncoder custom = new BCryptPasswordEncoder(8);
        runner.withBean(PasswordEncoder.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    // 应用自定义编码器生效，框架 BCrypt 默认值退让；PasswordUtils 复用应用编码器
                    assertThat(context.getBeansOfType(PasswordEncoder.class)).hasSize(1);
                    assertThat(context.getBean(PasswordEncoder.class)).isSameAs(custom);
                    assertThat(context).hasSingleBean(PasswordUtils.class);
                });
    }
}
