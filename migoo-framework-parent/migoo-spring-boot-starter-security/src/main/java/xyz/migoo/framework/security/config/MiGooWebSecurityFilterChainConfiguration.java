package xyz.migoo.framework.security.config;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import xyz.migoo.framework.security.core.filter.JwtAuthenticationFilter;

/**
 * Spring Security 过滤链配置
 * <p>
 * 定义 SecurityFilterChain，包括请求授权、异常处理、登出等。
 * AuthenticationManager 已移至 {@link MiGooSecurityAutoConfiguration}，避免循环依赖。
 *
 * @author xiaomi
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class MiGooWebSecurityFilterChainConfiguration {

    /**
     * SecurityFilterChain 核心配置
     * <p>
     * 所有依赖通过方法参数注入，避免字段注入引起的循环依赖。
     */
    @Bean
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
