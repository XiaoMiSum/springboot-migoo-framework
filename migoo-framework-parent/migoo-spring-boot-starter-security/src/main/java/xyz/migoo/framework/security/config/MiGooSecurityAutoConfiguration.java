package xyz.migoo.framework.security.config;

import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.filter.JwtAuthenticationFilter;
import xyz.migoo.framework.security.core.handler.AccessDeniedHandlerImpl;
import xyz.migoo.framework.security.core.handler.AuthenticationEntryPointImpl;
import xyz.migoo.framework.security.core.handler.LogoutSuccessHandlerImpl;
import xyz.migoo.framework.security.core.interceptor.TotpInterceptor;
import xyz.migoo.framework.security.core.resolver.AuthUserMethodArgumentResolver;
import xyz.migoo.framework.security.core.service.AuthUserDetailsService;
import xyz.migoo.framework.security.core.service.JwtTokenProvider;
import xyz.migoo.framework.security.core.service.TotpService;
import xyz.migoo.framework.security.core.service.UserDetailsBridge;
import xyz.migoo.framework.security.core.service.impl.DefaultJwtAuthService;
import xyz.migoo.framework.security.core.service.impl.JJwtTokenProvider;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;
import xyz.migoo.framework.web.i18n.I18NMessage;

/**
 * Spring Security 自动配置类，主要用于相关组件的配置
 * <p>
 * 注意，不能和 {@link MiGooWebSecurityConfigurerAdapter} 用一个，原因是会导致初始化报错。
 * 参见 <a href="https://stackoverflow.com/questions/53847050/spring-boot-delegatebuilder-cannot-be-null-on-autowiring-authenticationmanager">...</a> 文档。
 *
 * @author xiaomi
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@AutoConfigureAfter(TotpService.class)
public class MiGooSecurityAutoConfiguration implements WebMvcConfigurer {

    @Resource
    private TotpService totpService;

    /**
     * TOTP 二次验证服务
     */
    @Bean
    public TotpService totpService() {
        return new TotpService();
    }

    /**
     * 添加自定义拦截器
     *
     * @param registry 拦截器注册
     */
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TotpInterceptor(totpService))
                .addPathPatterns("/**");
    }

    /**
     * 认证失败处理类 Bean
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(I18NMessage i18n) {
        return new AuthenticationEntryPointImpl(i18n);
    }

    /**
     * 权限不够处理器 Bean
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler(I18NMessage i18n) {
        return new AccessDeniedHandlerImpl(i18n);
    }

    /**
     * 退出处理类 Bean
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(SecurityProperties securityProperties,
                                                     AuthUserDetailsService<? extends AuthUserDetails<?, ?>> securityFrameworkService) {
        return new LogoutSuccessHandlerImpl(securityProperties, securityFrameworkService);
    }

    /**
     * Spring Security 加密器
     * 考虑到安全性，这里采用 BCryptPasswordEncoder 加密器
     *
     * @see <a href="http://stackabuse.com/password-encoding-with-spring-security/">Password Encoding with Spring Security</a>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT Token Provider Bean
     * <p>
     * 仅在 JWT 模式下注册，提供 token 创建/解析/验证能力
     */
    @Bean
    @ConditionalOnMissingBean(JwtTokenProvider.class)
    @ConditionalOnProperty(name = "migoo.security.mode", havingValue = "jwt", matchIfMissing = true)
    public JwtTokenProvider jwtTokenProvider(SecurityProperties properties) {
        return new JJwtTokenProvider(properties);
    }

    /**
     * 默认 JWT 认证服务 Bean
     * <p>
     * 仅在 JWT 模式下注册，且当应用未自行实现 AuthUserDetailsService 时生效。
     * 组合 JwtTokenProvider + UserDetailsBridge，实现完整的认证/验证/刷新流程。
     */
    @Bean
    @ConditionalOnMissingBean(AuthUserDetailsService.class)
    @ConditionalOnProperty(name = "migoo.security.mode", havingValue = "jwt", matchIfMissing = true)
    public DefaultJwtAuthService defaultJwtAuthService(JwtTokenProvider tokenProvider,
                                                       UserDetailsBridge userBridge,
                                                       PasswordEncoder passwordEncoder,
                                                       SecurityProperties properties) {
        return new DefaultJwtAuthService(tokenProvider, userBridge, passwordEncoder, properties);
    }

    /**
     * Authorization 认证过滤器 Bean
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(SecurityProperties securityProperties,
                                                           AuthUserDetailsService<? extends AuthUserDetails<?, ?>> securityFrameworkService,
                                                           GlobalExceptionHandler globalExceptionHandler,
                                                           I18NMessage i18nMessage) {
        return new JwtAuthenticationFilter(securityProperties, securityFrameworkService,
                globalExceptionHandler, i18nMessage);
    }

    /**
     * 方法参数转换处理器
     */
    @Bean
    public AuthUserMethodArgumentResolver currentUserMethodArgumentResolver() {
        return new AuthUserMethodArgumentResolver();
    }


}
