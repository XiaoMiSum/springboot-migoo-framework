package xyz.migoo.framework.web.config;

import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import xyz.migoo.framework.common.enums.WebFilterOrderEnum;

@Configuration
@ConditionalOnProperty(prefix = "migoo.web.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FrameworkCorsConfiguration {

    private static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterBean(MigooWebProperties properties) {
        MigooWebProperties.Cors corsConfig = properties.getCors();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(corsConfig.isAllowCredentials());
        config.setAllowedOriginPatterns(corsConfig.getAllowedOrigins());
        config.setAllowedHeaders(corsConfig.getAllowedHeaders());
        config.setAllowedMethods(corsConfig.getAllowedMethods());
        config.setMaxAge(corsConfig.getMaxAge());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return createFilterBean(new CorsFilter(source), WebFilterOrderEnum.CORS_FILTER);
    }
}
