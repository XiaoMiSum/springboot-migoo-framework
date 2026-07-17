package xyz.migoo.framework.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher;
import xyz.migoo.framework.security.core.filter.JwtAuthenticationFilter;

/**
 * Spring Security 过滤链配置
 * <p>
 * 定义 SecurityFilterChain，包括认证管理器、请求授权、异常处理、登出等。
 * 与 {@link MiGooSecurityAutoConfiguration} 分离，避免 AuthenticationManager 初始化报错。
 *
 * @author xiaomi
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class MiGooWebSecurityFilterChainConfiguration {

    @Autowired
    private SecurityProperties properties;

    /**
     * AuthUserDetailsFetcher 可选注入
     * <p>
     * JWT 模式下存在，OAuth2 模式下不存在
     */
    @Autowired(required = false)
    private AuthUserDetailsFetcher<? extends AuthUserDetails<?, ?>> userDetailsFetcher;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired(required = false)
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 创建 AuthenticationManager Bean
     * <p>
     * 仅在 JWT 模式下创建，OAuth2 模式由 Spring Security 内部管理。
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    @ConditionalOnProperty(name = "migoo.security.mode", havingValue = "jwt", matchIfMissing = true)
    public AuthenticationManager authenticationManagerBean(ObjectPostProcessor<Object> objectPostProcessor) throws Exception {
        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
        builder.userDetailsService(userDetailsFetcher).passwordEncoder(passwordEncoder);
        return builder.build();
    }

    /**
     * SecurityFilterChain 核心配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // CSRF 禁用，因为不使用 Session
                .csrf(AbstractHttpConfigurer::disable)
                // 基于 token 机制，所以不需要 Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 禁用 headers（无需缓存控制等）
                .headers(AbstractHttpConfigurer::disable)
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

}
