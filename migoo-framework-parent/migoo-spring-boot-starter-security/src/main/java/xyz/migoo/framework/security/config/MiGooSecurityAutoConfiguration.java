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
import xyz.migoo.framework.security.core.lockout.*;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;
import xyz.migoo.framework.web.core.store.StateStore;
import xyz.migoo.framework.web.i18n.I18NMessage;
import org.springframework.beans.factory.ObjectProvider;

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
                                                           SecurityProperties properties,
                                                           LoginLockManager lockManager) {
        return new DefaultJwtAuthenticator(tokenProvider, userBridge, authenticationManager, properties, lockManager);
    }

    // ==================== 登录失败锁定 ====================

    /**
     * 固定时长锁定策略 Bean（migoo.security.login-lock.fixed.enabled 控制，默认启用）
     */
    @Bean
    @ConditionalOnMissingBean(FixedDurationLockStrategy.class)
    @ConditionalOnProperty(name = "migoo.security.login-lock.fixed.enabled", havingValue = "true", matchIfMissing = true)
    public FixedDurationLockStrategy fixedDurationLockStrategy(StateStore stateStore,
                                                               SecurityProperties properties) {
        var fixed = properties.getLoginLock().getFixed();
        return new FixedDurationLockStrategy(stateStore, fixed.getThreshold(),
                properties.getLoginLock().getFailureWindow(), fixed.getDuration());
    }

    /**
     * 递增时长锁定策略 Bean（migoo.security.login-lock.incremental.enabled 控制，默认启用）
     */
    @Bean
    @ConditionalOnMissingBean(IncrementalDurationLockStrategy.class)
    @ConditionalOnProperty(name = "migoo.security.login-lock.incremental.enabled", havingValue = "true", matchIfMissing = true)
    public IncrementalDurationLockStrategy incrementalDurationLockStrategy(StateStore stateStore,
                                                                          SecurityProperties properties) {
        var incremental = properties.getLoginLock().getIncremental();
        return new IncrementalDurationLockStrategy(stateStore, incremental.getThreshold(),
                properties.getLoginLock().getFailureWindow(), incremental.getInitialDuration(),
                incremental.getMultiplier(), incremental.getMaxDuration());
    }

    /**
     * 滑动窗口累计锁定策略 Bean（migoo.security.login-lock.sliding-window.enabled 控制，默认启用）
     */
    @Bean
    @ConditionalOnMissingBean(SlidingWindowLockStrategy.class)
    @ConditionalOnProperty(name = "migoo.security.login-lock.sliding-window.enabled", havingValue = "true", matchIfMissing = true)
    public SlidingWindowLockStrategy slidingWindowLockStrategy(StateStore stateStore,
                                                               SecurityProperties properties) {
        var slidingWindow = properties.getLoginLock().getSlidingWindow();
        return new SlidingWindowLockStrategy(stateStore, slidingWindow.getThreshold(),
                slidingWindow.getWindow(), slidingWindow.getDuration());
    }

    /**
     * 登录失败锁定管理器 Bean
     * <p>
     * 组合容器中全部 {@link LoginLockStrategy} Bean（命中取最严），
     * 应用注册自定义策略 Bean 即可参与组合。
     */
    @Bean
    @ConditionalOnMissingBean(LoginLockManager.class)
    public LoginLockManager loginLockManager(StateStore stateStore,
                                             ObjectProvider<LoginLockStrategy> strategyProvider) {
        return new DefaultLoginLockManager(stateStore, strategyProvider.orderedStream().toList());
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

}
