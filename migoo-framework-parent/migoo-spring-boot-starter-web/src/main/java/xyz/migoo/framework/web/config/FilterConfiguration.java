package xyz.migoo.framework.web.config;

import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.common.enums.WebFilterOrderEnum;
import xyz.migoo.framework.common.observability.TraceIdResolver;
import xyz.migoo.framework.web.core.filter.CacheRequestBodyFilter;
import xyz.migoo.framework.web.core.filter.TraceIdFilter;

@Configuration
public class FilterConfiguration {

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter(
            // 可选注入：引入可观测性组件后由其提供基于 Micrometer Tracing 的实现，未引入则回退本地生成
            ObjectProvider<TraceIdResolver> traceIdResolver,
            @Value("${migoo.observability.tracing.propagate-x-trace-id:true}") boolean propagateXTraceId) {
        return createFilterBean(new TraceIdFilter(traceIdResolver.getIfAvailable(), propagateXTraceId),
                WebFilterOrderEnum.TRACE_FILTER);
    }

    @Bean
    @ConditionalOnProperty(prefix = "migoo.web.cache-body", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<CacheRequestBodyFilter> requestBodyCacheFilter(MigooWebProperties properties) {
        return createFilterBean(new CacheRequestBodyFilter(properties.getCacheBody().getMaxSize()),
                WebFilterOrderEnum.REQUEST_BODY_CACHE_FILTER);
    }

    private static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }
}
