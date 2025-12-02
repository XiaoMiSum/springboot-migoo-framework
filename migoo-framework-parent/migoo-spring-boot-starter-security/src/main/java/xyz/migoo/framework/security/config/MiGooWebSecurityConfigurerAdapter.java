package xyz.migoo.framework.security.config;

import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.filter.JWTAuthenticationTokenFilter;
import xyz.migoo.framework.security.core.service.SecurityAuthFrameworkService;

/**
 * 自定义的 Spring Security 配置适配器实现
 *
 * @author xiaomi
 */
@Configuration
@AutoConfigureOrder(SecurityProperties.DEFAULT_FILTER_ORDER)
@EnableMethodSecurity(securedEnabled = true)
public class MiGooWebSecurityConfigurerAdapter {

    @Resource
    private xyz.migoo.framework.security.config.SecurityProperties properties;

    /**
     * 自定义用户【认证】逻辑
     */
    @Resource
    private SecurityAuthFrameworkService<? extends AuthUserDetails<?>> userDetailsService;
    /**
     * Spring Security 加密器
     */
    @Resource
    private PasswordEncoder passwordEncoder;
    /**
     * 认证失败处理类 Bean
     */
    @Resource
    private AuthenticationEntryPoint authenticationEntryPoint;
    /**
     * 权限不够处理器 Bean
     */
    @Resource
    private AccessDeniedHandler accessDeniedHandler;
    /**
     * 退出处理类 Bean
     */
    @Resource
    private LogoutSuccessHandler logoutSuccessHandler;
    /**
     * Token 认证过滤器 Bean
     */
    @Resource
    private JWTAuthenticationTokenFilter authenticationTokenFilter;

    /**
     * 由于 Spring Security 创建 AuthenticationManager 对象时，没声明 @Bean 注解，导致无法被注入
     * 通过覆写父类的该方法，添加 @Bean 注解，解决该问题
     */
    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManagerBean(ObjectPostProcessor<Object> objectPostProcessor) throws Exception {
        AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        return builder.build();
    }

    /**
     * 配置 URL 的安全配置
     * <p>
     * anyRequest          |   匹配所有请求路径
     * access              |   SpringEl表达式结果为true时可以访问
     * anonymous           |   匿名可以访问
     * denyAll             |   用户不能访问
     * fullyAuthenticated  |   用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority     |   如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole          |   如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority        |   如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress        |   如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole             |   如果有参数，参数表示角色，则其角色可以访问
     * permitAll           |   用户可以任意访问
     * rememberMe          |   允许通过remember-me登录的用户访问
     * authenticated       |   用户登录后可访问
     */
    @Bean
    public SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // CSRF 禁用，因为不使用 Session
                .csrf(AbstractHttpConfigurer::disable)
                // 基于 token 机制，所以不需要 Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(AbstractHttpConfigurer::disable)
                // 一堆自定义的 Spring Security 处理器
                .exceptionHandling(handling -> handling.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler))
                // 登出
                .logout(logout -> logout.logoutUrl(properties.getLogoutUrl()).logoutSuccessHandler(logoutSuccessHandler))

                // 设置每个请求的权限 ①：配置的可以任意访问的url
                .authorizeHttpRequests(requests -> requests.requestMatchers(properties.getPermitAllUrls().toArray(new String[0])).permitAll()
                )
                // 设置每个请求的权限 ②：兜底规则，必须认证
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                // 添加 JWT Filter
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/public/**", "/static/**", "/resources/**");
    }
}
