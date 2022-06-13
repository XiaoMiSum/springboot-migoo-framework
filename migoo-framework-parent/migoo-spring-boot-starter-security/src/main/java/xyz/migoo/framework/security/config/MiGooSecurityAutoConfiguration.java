package xyz.migoo.framework.security.config;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.migoo.framework.security.core.filter.JWTAuthenticationTokenFilter;
import xyz.migoo.framework.security.core.handler.AccessDeniedHandlerImpl;
import xyz.migoo.framework.security.core.handler.AuthenticationEntryPointImpl;
import xyz.migoo.framework.security.core.handler.LogoutSuccessHandlerImpl;
import xyz.migoo.framework.security.core.interceptor.AuthenticatorInterceptor;
import xyz.migoo.framework.security.core.resolver.CurrentUserMethodArgumentResolver;
import xyz.migoo.framework.security.core.resolver.TokenMethodArgumentResolver;
import xyz.migoo.framework.security.core.service.SecurityAuthFrameworkService;
import xyz.migoo.framework.security.core.service.SecurityAuthenticatorService;
import xyz.migoo.framework.web.config.WebProperties;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;

import java.util.List;

/**
 * Spring Security 自动配置类，主要用于相关组件的配置
 * <p>
 * 注意，不能和 {@link MiGooWebSecurityConfigurerAdapter} 用一个，原因是会导致初始化报错。
 * 参见 https://stackoverflow.com/questions/53847050/spring-boot-delegatebuilder-cannot-be-null-on-autowiring-authenticationmanager 文档。
 *
 * @author 芋道源码
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@AutoConfigureAfter(SecurityAuthenticatorService.class)
public class MiGooSecurityAutoConfiguration implements WebMvcConfigurer {

    /**
     * 身份验证器
     */
    @Bean
    public SecurityAuthenticatorService securityAuthenticatorService() {
        return new SecurityAuthenticatorService();
    }

    /**
     * 添加自定义拦截器
     *
     * @param registry 拦截器注册
     */
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticatorInterceptor(SpringUtil.getBean(SecurityAuthenticatorService.class)))
                .addPathPatterns(api("/**"))
                .excludePathPatterns("/**/images/**", api("/**/images/**"));
    }

    private String api(String api) {
        return SpringUtil.getBean(WebProperties.class).getApiPrefix() + api;
    }

    /**
     * 认证失败处理类 Bean
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPointImpl();
    }

    /**
     * 权限不够处理器 Bean
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandlerImpl();
    }

    /**
     * 退出处理类 Bean
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(SecurityAuthFrameworkService securityFrameworkService) {
        return new LogoutSuccessHandlerImpl(SpringUtil.getBean(SecurityProperties.class), securityFrameworkService);
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
     * Token 认证过滤器 Bean
     */
    @Bean
    public JWTAuthenticationTokenFilter authenticationTokenFilter(SecurityAuthFrameworkService securityFrameworkService,
                                                                  GlobalExceptionHandler globalExceptionHandler) {
        return new JWTAuthenticationTokenFilter(SpringUtil.getBean(SecurityProperties.class), securityFrameworkService, globalExceptionHandler);
    }

    /**
     * 方法参数转换处理器
     */
    @Bean
    public CurrentUserMethodArgumentResolver currentUserMethodArgumentResolver() {
        return new CurrentUserMethodArgumentResolver();
    }

    /**
     * 方法参数转换处理器
     */
    @Bean
    public TokenMethodArgumentResolver tokenMethodArgumentResolver() {
        return new TokenMethodArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(currentUserMethodArgumentResolver());
        argumentResolvers.add(tokenMethodArgumentResolver());
    }

}
