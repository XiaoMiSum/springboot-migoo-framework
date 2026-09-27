package xyz.migoo.framework.web.config;

import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import xyz.migoo.framework.common.enums.WebFilterOrderEnum;
import xyz.migoo.framework.web.core.cors.CorsMode;
import xyz.migoo.framework.web.core.cors.CorsOriginPredicate;

import java.util.List;

/**
 * CORS 过滤器装配，按 {@link CorsMode} 四档模式构建来源限制策略
 *
 * <p>安全原则：默认不放行任何跨域来源；开放能力必须由使用方按场景显式选择模式。</p>
 *
 * @author xiaomi
 */
@Configuration
@ConditionalOnProperty(prefix = "migoo.web.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FrameworkCorsConfiguration {

    private static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterBean(MigooWebProperties properties,
            ObjectProvider<CorsOriginPredicate> originPredicates) {
        MigooWebProperties.Cors corsConfig = properties.getCors();
        CorsConfiguration config = buildCorsConfiguration(corsConfig, originPredicates);
        if (config == null) {
            // STRICT 模式且未配置任何来源 = 不放行跨域，无需注册 CORS 过滤器
            FilterRegistrationBean<CorsFilter> disabled = new FilterRegistrationBean<>(new CorsFilter(
                    new UrlBasedCorsConfigurationSource()));
            disabled.setEnabled(false);
            disabled.setOrder(WebFilterOrderEnum.CORS_FILTER);
            return disabled;
        }
        config.setAllowedHeaders(corsConfig.getAllowedHeaders());
        config.setAllowedMethods(corsConfig.getAllowedMethods());
        config.setMaxAge(corsConfig.getMaxAge());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return createFilterBean(new CorsFilter(source), WebFilterOrderEnum.CORS_FILTER);
    }

    /**
     * 按模式构建来源判定配置；返回 {@code null} 表示不需要注册 CORS 过滤器（STRICT 空来源）
     */
    private CorsConfiguration buildCorsConfiguration(MigooWebProperties.Cors corsConfig,
            ObjectProvider<CorsOriginPredicate> originPredicates) {
        return switch (corsConfig.getMode()) {
            case STRICT -> buildStrict(corsConfig);
            case OPEN -> buildOpen(corsConfig);
            case PATTERN -> buildPattern(corsConfig);
            case DYNAMIC -> buildDynamic(corsConfig, originPredicates);
        };
    }

    private CorsConfiguration buildStrict(MigooWebProperties.Cors corsConfig) {
        List<String> origins = corsConfig.getAllowedOrigins();
        List<String> patterns = corsConfig.getAllowedOriginPatterns();
        if (origins.isEmpty() && patterns.isEmpty()) {
            return null;
        }
        rejectWildcardWithCredentials(corsConfig, origins, patterns, "STRICT");
        CorsConfiguration config = new CorsConfiguration();
        if (!origins.isEmpty()) {
            config.setAllowedOrigins(origins);
        }
        if (!patterns.isEmpty()) {
            config.setAllowedOriginPatterns(patterns);
        }
        config.setAllowCredentials(corsConfig.isAllowCredentials());
        return config;
    }

    private CorsConfiguration buildOpen(MigooWebProperties.Cors corsConfig) {
        if (corsConfig.isAllowCredentials()) {
            throw new IllegalStateException("[buildOpen][migoo.web.cors.mode=open 不允许开启凭证] "
                    + "OPEN 模式为 * 全开放，规范禁止 * + credentials 组合；"
                    + "Cookie 场景请改用 mode=dynamic + CorsOriginPredicate Bean");
        }
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowCredentials(false);
        return config;
    }

    private CorsConfiguration buildPattern(MigooWebProperties.Cors corsConfig) {
        List<String> patterns = corsConfig.getAllowedOriginPatterns();
        if (patterns.isEmpty()) {
            throw new IllegalStateException("[buildPattern][PATTERN 模式必须配置 allowed-origin-patterns] "
                    + "请设置 migoo.web.cors.allowed-origin-patterns，例如 https://*.example.com");
        }
        rejectWildcardWithCredentials(corsConfig, List.of(), patterns, "PATTERN");
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(patterns);
        config.setAllowCredentials(corsConfig.isAllowCredentials());
        return config;
    }

    private CorsConfiguration buildDynamic(MigooWebProperties.Cors corsConfig,
            ObjectProvider<CorsOriginPredicate> originPredicates) {
        CorsOriginPredicate predicate = originPredicates.getIfAvailable();
        if (predicate == null) {
            throw new IllegalStateException("[buildDynamic][DYNAMIC 模式缺少来源谓词] "
                    + "migoo.web.cors.mode=dynamic 时必须注册一个 CorsOriginPredicate Bean（如查询应用注册表判定域名准入）");
        }
        // 逐请求委托谓词判定，命中则反射该具体 Origin（而非 *），凭证能力由配置决定
        CorsConfiguration config = new CorsConfiguration() {
            @Override
            public String checkOrigin(String requestOrigin) {
                if (!StringUtils.hasText(requestOrigin) || !predicate.isAllowed(requestOrigin)) {
                    return null;
                }
                return requestOrigin;
            }
        };
        config.setAllowCredentials(corsConfig.isAllowCredentials());
        return config;
    }

    private void rejectWildcardWithCredentials(MigooWebProperties.Cors corsConfig,
            List<String> origins, List<String> patterns, String mode) {
        if (!corsConfig.isAllowCredentials()) {
            return;
        }
        boolean wildcard = origins.contains("*") || patterns.contains("*");
        if (wildcard) {
            throw new IllegalStateException(String.format(
                    "[%s 模式禁止 * 来源 + credentials 组合] 规范不允许，且等价于向任意站点开放携带凭证的跨域；"
                            + "请列出精确来源（Cookie 场景），或使用 mode=open（Token 场景，无凭证）/ mode=dynamic（动态域名）", mode));
        }
    }
}
