package xyz.migoo.framework.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.CorsFilter;
import xyz.migoo.framework.common.enums.WebFilterOrderEnum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FrameworkCorsConfiguration} 单元测试：@Bean 方法可直接脱离容器调用。
 */
class FrameworkCorsConfigurationTest {

    @Test
    void corsFilterRegistersWithCorsOrder() {
        FilterRegistrationBean<CorsFilter> bean =
                new FrameworkCorsConfiguration().corsFilterBean(new MigooWebProperties());

        assertThat(bean.getFilter()).isInstanceOf(CorsFilter.class);
        assertThat(bean.getOrder()).isEqualTo(WebFilterOrderEnum.CORS_FILTER);
    }

    @Test
    void corsFilterAllowsConfiguredOriginInPreflight() throws Exception {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setAllowedOrigins(java.util.List.of("https://example.com"));

        FilterRegistrationBean<CorsFilter> bean = new FrameworkCorsConfiguration().corsFilterBean(properties);
        CorsFilter filter = bean.getFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("OPTIONS");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("/api/test");
        when(request.getHttpServletMapping()).thenReturn(mock(jakarta.servlet.http.HttpServletMapping.class));
        when(request.getHeader("Origin")).thenReturn("https://example.com");
        when(request.getHeader("Access-Control-Request-Method")).thenReturn("GET");
        when(request.getHeaders("Access-Control-Request-Headers"))
                .thenReturn(java.util.Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        // 预检请求被 CorsFilter 短路：不进入后续 Filter 链，但回写 CORS 响应头
        verify(chain, never()).doFilter(any(), any());
        verify(response).setHeader(eq("Access-Control-Allow-Origin"), anyString());
    }

    @Test
    void corsFilterRejectsDisallowedOrigin() throws Exception {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setAllowedOrigins(java.util.List.of("https://allowed.com"));

        FilterRegistrationBean<CorsFilter> bean = new FrameworkCorsConfiguration().corsFilterBean(properties);
        CorsFilter filter = bean.getFilter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("OPTIONS");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("/api/test");
        when(request.getHttpServletMapping()).thenReturn(mock(jakarta.servlet.http.HttpServletMapping.class));
        when(request.getHeader("Origin")).thenReturn("https://evil.com");
        when(request.getHeader("Access-Control-Request-Method")).thenReturn("GET");
        when(request.getHeaders("Access-Control-Request-Headers"))
                .thenReturn(java.util.Collections.emptyEnumeration());
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(mock(jakarta.servlet.ServletOutputStream.class));
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        // 不被允许的来源直接拒绝：不进入 Filter 链
        verify(chain, never()).doFilter(any(), any());
    }
}
