package xyz.migoo.framework.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import xyz.migoo.framework.apilog.core.ApiErrorLogFrameworkService;
import xyz.migoo.framework.common.enums.WebFilterOrderEnum;
import xyz.migoo.framework.web.core.filter.CacheRequestBodyFilter;
import xyz.migoo.framework.web.core.filter.XssFilter;
import xyz.migoo.framework.web.core.handler.GlobalExceptionHandler;
import xyz.migoo.framework.web.core.handler.GlobalResponseBodyHandler;

import javax.annotation.Resource;
import javax.servlet.Filter;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

@Configuration
@EnableConfigurationProperties({WebProperties.class, XssProperties.class})
public class MiGooWebAutoConfiguration implements WebMvcConfigurer {

    @Resource
    private WebProperties webProperties;
    /**
     * 应用名
     */
    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 设置 API 前缀，仅仅匹配 controller 包下的
        configurer.addPathPrefix(webProperties.getApiPrefix(), clazz -> Objects.equals(webProperties.getScanAll(), TRUE) ?
                clazz.getPackage().getName().startsWith(webProperties.getControllerPackage()) :
                clazz.isAnnotationPresent(RestController.class)
                        && clazz.getPackage().getName().startsWith(webProperties.getControllerPackage())); // 仅仅匹配 controller 包
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler(ApiErrorLogFrameworkService apiErrorLogFrameworkService) {
        return new GlobalExceptionHandler(applicationName, apiErrorLogFrameworkService);
    }

    @Bean
    public GlobalResponseBodyHandler globalResponseBodyHandler() {
        return new GlobalResponseBodyHandler();
    }

    // ========== Filter 相关 ==========

    /**
     * 创建 CorsFilter Bean，解决跨域问题
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterBean() {
        // 创建 CorsConfiguration 对象
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // 设置访问源地址
        config.addAllowedHeader("*"); // 设置访问源请求头
        config.addAllowedMethod("*"); // 设置访问源请求方法
        // 创建 UrlBasedCorsConfigurationSource 对象
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对接口配置跨域设置
        return createFilterBean(new CorsFilter(source), WebFilterOrderEnum.CORS_FILTER);
    }

    /**
     * 创建 RequestBodyCacheFilter Bean，可重复读取请求内容
     */
    @Bean
    public FilterRegistrationBean<CacheRequestBodyFilter> requestBodyCacheFilter() {
        return createFilterBean(new CacheRequestBodyFilter(), WebFilterOrderEnum.REQUEST_BODY_CACHE_FILTER);
    }

    /**
     * 创建 XssFilter Bean，解决 Xss 安全问题
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilter(XssProperties properties, PathMatcher pathMatcher) {
        return createFilterBean(new XssFilter(properties, pathMatcher), WebFilterOrderEnum.XSS_FILTER);
    }


    private static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }

}
