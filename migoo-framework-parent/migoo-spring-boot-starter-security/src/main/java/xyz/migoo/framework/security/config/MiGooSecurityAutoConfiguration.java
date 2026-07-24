package xyz.migoo.framework.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.web.core.util.ServletUtils;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.authentication.*;
import xyz.migoo.framework.security.core.filter.JwtAuthenticationFilter;
import xyz.migoo.framework.security.core.handler.AccessDeniedHandlerImpl;
import xyz.migoo.framework.security.core.handler.AuthenticationEntryPointImpl;
import xyz.migoo.framework.security.core.handler.LogoutSuccessHandlerImpl;
import xyz.migoo.framework.security.core.interceptor.TotpInterceptor;
import xyz.migoo.framework.security.core.resolver.AuthUserMethodArgumentResolver;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;
import xyz.migoo.framework.web.i18n.I18NMessage;

/**
 * Spring Security 自动配置类
 * <p>
 * 注册安全组件所需的 Bean，与 {@link MiGooWebSecurityFilterChainConfiguration} 分离，
 * 避免 AuthenticationManager 初始化报错。
 *
 * @author xiaomi
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class MiGooSecurityAutoConfiguration implements WebMvcConfigurer {

    /**
     * TOTP 二次验证器
     */
    @Bean
    public TotpAuthenticator totpAuthenticator() {
        return new TotpAuthenticator();
    }

    /**
     * 添加自定义拦截器
     *
     * @param registry 拦截器注册
     */
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TotpInterceptor(totpAuthenticator()))
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
     * 退出处理类 Bean（JWT 模式下注册）
     */
    @Bean
    @ConditionalOnProperty(name = "migoo.security.mode", havingValue = "jwt", matchIfMissing = true)
    public LogoutSuccessHandler logoutSuccessHandler(SecurityProperties securityProperties,
                                                     AuthUserDetailsFetcher<? extends AuthUserDetails<?, ?>> userDetailsFetcher) {
        return new LogoutSuccessHandlerImpl(securityProperties, userDetailsFetcher);
    }

    /**
     * 退出处理类 Bean（OAuth2 模式下注册）
     * <p>
     * OAuth2 模式下无需自定义清理逻辑，直接返回成功
     */
    @Bean
    @ConditionalOnProperty(name = "migoo.security.mode", havingValue = "oauth2")
    public LogoutSuccessHandler oauth2LogoutSuccessHandler() {
        return (request, response, authentication) -> ServletUtils.writeJSON(response, Result.ok());
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
     * 创建 AuthenticationManager Bean
     * <p>
     * 仅在 JWT 模式下创建，OAuth2 模式由 Spring Security 内部管理。
     * <p>
     * 与 {@link MiGooWebSecurityFilterChainConfiguration} 分离，避免循环依赖:
     * FilterChain 创建 AuthenticationManager → DefaultJwtAuthenticator 依赖 AuthenticationManager
     * → LogoutSuccessHandler 依赖 DefaultJwtAuthenticator → FilterChain 消费 LogoutSuccessHandler
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    @ConditionalOnProperty(name = "migoo.security.mode", havingValue = "jwt", matchIfMissing = true)
    public AuthenticationManager authenticationManagerBean(ObjectPostProcessor<Object> objectPostProcessor,
                                                            UserDetailsBridge userDetailsBridge,
                                                            PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
        builder.userDetailsService(userDetailsBridge).passwordEncoder(passwordEncoder);
        return builder.build();
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
     * 默认 JWT 认证器 Bean
     * <p>
     * 仅在 JWT 模式下注册，且当应用未自行实现 AuthUserDetailsFetcher 时生效。
     * 组合 JwtTokenProvider + UserDetailsBridge，实现完整的认证/验证/刷新流程。
     */
    @Bean
    @ConditionalOnMissingBean(AuthUserDetailsFetcher.class)
    @ConditionalOnProperty(name = "migoo.security.mode", havingValue = "jwt", matchIfMissing = true)
    public DefaultJwtAuthenticator defaultJwtAuthenticator(JwtTokenProvider tokenProvider,
                                                           UserDetailsBridge userBridge,
                                                           AuthenticationManager authenticationManager,
                                                           SecurityProperties properties) {
        return new DefaultJwtAuthenticator(tokenProvider, userBridge, authenticationManager, properties);
    }

    /**
     * JWT 认证过滤器 Bean
     * <p>
     * 仅在 JWT 模式下注册
     */
    @Bean
    @ConditionalOnProperty(name = "migoo.security.mode", havingValue = "jwt", matchIfMissing = true)
    public JwtAuthenticationFilter jwtAuthenticationFilter(SecurityProperties securityProperties,
                                                           AuthUserDetailsFetcher<? extends AuthUserDetails<?, ?>> userDetailsFetcher,
                                                           GlobalExceptionHandler globalExceptionHandler,
                                                           I18NMessage i18nMessage) {
        return new JwtAuthenticationFilter(securityProperties, userDetailsFetcher,
                globalExceptionHandler, i18nMessage);
    }

    /**
     * 方法参数转换处理器（JWT 模式下注册）
     */
    @Bean
    @ConditionalOnProperty(name = "migoo.security.mode", havingValue = "jwt", matchIfMissing = true)
    public AuthUserMethodArgumentResolver currentUserMethodArgumentResolver(SecurityProperties securityProperties,
                                                                            AuthUserDetailsFetcher<? extends AuthUserDetails<?, ?>> userDetailsFetcher) {
        return new AuthUserMethodArgumentResolver(securityProperties, userDetailsFetcher);
    }

}
