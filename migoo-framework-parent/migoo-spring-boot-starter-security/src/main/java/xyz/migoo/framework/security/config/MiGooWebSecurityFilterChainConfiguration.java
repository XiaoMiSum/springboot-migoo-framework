package xyz.migoo.framework.security.config;

import jakarta.servlet.DispatcherType;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import xyz.migoo.framework.security.core.filter.JwtAuthenticationFilter;

/**
 * Spring Security 过滤链配置
 * <p>
 * 定义 SecurityFilterChain，包括请求授权、异常处理、登出等。
 * AuthenticationManager 已移至 {@link MiGooSecurityAutoConfiguration}，避免循环依赖。
 * <p>
 * 使用 @AutoConfiguration 确保在 Spring Boot 默认 SecurityFilterChain 之前注册，
 * 使其 @ConditionalOnMissingBean 生效并跳过默认配置。
 *
 * @author xiaomi
 */
@AutoConfiguration(before = ServletWebSecurityAutoConfiguration.class)
@EnableMethodSecurity(securedEnabled = true)
public class MiGooWebSecurityFilterChainConfiguration {

    /**
     * SecurityFilterChain 核心配置
     * <p>
     * 所有依赖通过方法参数注入，避免字段注入引起的循环依赖。
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   SecurityProperties properties,
                                                   AuthenticationEntryPoint authenticationEntryPoint,
                                                   AccessDeniedHandler accessDeniedHandler,
                                                   LogoutSuccessHandler logoutSuccessHandler,
                                                   @Nullable JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        httpSecurity
                // CSRF 禁用，因为不使用 Session
                .csrf(AbstractHttpConfigurer::disable)
                // 基于 token 机制，所以不需要 Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 安全响应头：按 migoo.security.headers.* 开关装配（默认恢复安全头集）
                .headers(headers -> applyHeaders(headers, properties.getHeaders()))
                // 异常处理
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // 登出
                .logout(logout -> logout
                        .logoutUrl(properties.getLogoutUrl())
                        .logoutSuccessHandler(logoutSuccessHandler))
                // 请求授权
                .authorizeHttpRequests(requests -> requests
                        // ASYNC/ERROR dispatch 放行：SseEmitter 等异步场景的二次分发无 SecurityContext，需跳过授权
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        .requestMatchers(properties.getPermitAllUrls().toArray(new String[0])).permitAll()
                        .anyRequest().authenticated());

        // 模式分支
        if (properties.getMode() == SecurityProperties.SecurityMode.OAUTH2) {
            // OAuth2 Resource Server: 使用 Spring 原生 JWT 解析
            httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(Customizer.withDefaults())
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler));
        } else {
            // JWT 模式: 使用自定义 JwtAuthenticationFilter
            if (jwtAuthenticationFilter != null) {
                httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            }
        }

        return httpSecurity.build();
    }

    /**
     * 按配置装配安全响应头
     * <p>
     * 装配策略：<b>要关的才碰</b>——仅对显式关闭的头调用 {@code disable}，
     * 其余保持 Spring Security 默认头集（nosniff / X-Frame-Options: DENY /
     * HSTS（仅 HTTPS 请求携带）/ Cache-Control / X-XSS-Protection）；
     * {@code Referrer-Policy} 与 {@code Content-Security-Policy} 非 Spring 默认头，
     * 分别由 {@code referrer-policy}（默认开）与 {@code content-security-policy}（默认空=不下发）按需启用。
     *
     * @param headers    响应头配置器
     * @param properties 响应头属性（{@code migoo.security.headers.*}）
     */
    static void applyHeaders(HeadersConfigurer<HttpSecurity> headers, SecurityProperties.Headers properties) {
        // 总开关关闭：完全禁用响应头（等同旧版行为）
        if (!properties.isEnabled()) {
            headers.disable();
            return;
        }
        if (!properties.isContentTypeOptions()) {
            headers.contentTypeOptions(config -> config.disable());
        }
        if (!properties.isFrameOptions()) {
            headers.frameOptions(config -> config.disable());
        }
        if (!properties.isHsts()) {
            headers.httpStrictTransportSecurity(config -> config.disable());
        }
        if (!properties.isCacheControl()) {
            headers.cacheControl(config -> config.disable());
        }
        if (!properties.isXssProtection()) {
            headers.xssProtection(config -> config.disable());
        }
        if (properties.isReferrerPolicy()) {
            headers.referrerPolicy(config ->
                    config.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
        }
        String contentSecurityPolicy = properties.getContentSecurityPolicy();
        if (contentSecurityPolicy != null && !contentSecurityPolicy.isBlank()) {
            headers.contentSecurityPolicy(config -> config.policyDirectives(contentSecurityPolicy));
        }
    }

}
