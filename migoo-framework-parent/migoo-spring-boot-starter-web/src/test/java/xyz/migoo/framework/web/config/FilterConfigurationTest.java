package xyz.migoo.framework.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import xyz.migoo.framework.common.enums.WebFilterOrderEnum;
import xyz.migoo.framework.common.observability.TraceIdResolver;
import xyz.migoo.framework.web.core.filter.CacheRequestBodyFilter;
import xyz.migoo.framework.web.core.filter.TraceIdFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FilterConfiguration} 单元测试：@Bean 方法可直接脱离容器调用。
 */
class FilterConfigurationTest {

    @Test
    @SuppressWarnings("unchecked")
    void traceIdFilterRegistersWithTraceOrder() {
        // 未引入可观测性组件时 ObjectProvider 取不到 TraceIdFilter，回退本地生成
        ObjectProvider<TraceIdResolver> resolvers = mock(ObjectProvider.class);
        FilterRegistrationBean<TraceIdFilter> bean = new FilterConfiguration().traceIdFilter(resolvers, true);
        assertThat(bean.getFilter()).isInstanceOf(TraceIdFilter.class);
        assertThat(bean.getOrder()).isEqualTo(WebFilterOrderEnum.TRACE_FILTER);
        verify(resolvers).getIfAvailable();
    }

    @Test
    void requestBodyCacheFilterRegistersWithCacheOrder() {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCacheBody().setMaxSize(4096);

        FilterRegistrationBean<CacheRequestBodyFilter> bean =
                new FilterConfiguration().requestBodyCacheFilter(properties);

        assertThat(bean.getFilter()).isInstanceOf(CacheRequestBodyFilter.class);
        assertThat(bean.getOrder()).isEqualTo(WebFilterOrderEnum.REQUEST_BODY_CACHE_FILTER);
    }

    @Test
    void cacheBodyMaxSizePropagatesToFilter() throws Exception {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCacheBody().setMaxSize(8);

        FilterRegistrationBean<CacheRequestBodyFilter> bean =
                new FilterConfiguration().requestBodyCacheFilter(properties);

        // 通过过滤行为间接验证 maxSize 被正确传递：Content-Length 超过 8 字节的请求体跳过缓存
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("application/json");
        when(request.getHeader("Content-Length")).thenReturn("16");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        bean.getFilter().doFilter(request, response, chain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain).doFilter(captor.capture(), any());
        assertThat(captor.getValue()).isSameAs(request);
    }
}
