package xyz.migoo.framework.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.CorsFilter;
import xyz.migoo.framework.common.enums.WebFilterOrderEnum;
import xyz.migoo.framework.web.core.cors.CorsMode;
import xyz.migoo.framework.web.core.cors.CorsOriginPredicate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @SuppressWarnings("unchecked")
    private static ObjectProvider<CorsOriginPredicate> noPredicate() {
        return mock(ObjectProvider.class);
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<CorsOriginPredicate> withPredicate(CorsOriginPredicate predicate) {
        ObjectProvider<CorsOriginPredicate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(predicate);
        return provider;
    }

    private static FilterRegistrationBean<CorsFilter> build(MigooWebProperties properties) {
        return new FrameworkCorsConfiguration().corsFilterBean(properties, noPredicate());
    }

    @Test
    void defaultStrictWithEmptyOriginsDisablesFilter() {
        FilterRegistrationBean<CorsFilter> bean = build(new MigooWebProperties());

        // 安全默认：STRICT + 空来源 = 不放行任何跨域，过滤器不注册
        assertThat(bean.isEnabled()).isFalse();
        assertThat(bean.getOrder()).isEqualTo(WebFilterOrderEnum.CORS_FILTER);
    }

    @Test
    void strictModeRegistersFilterWithConfiguredOrigin() throws Exception {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setAllowedOrigins(List.of("https://example.com"));

        FilterRegistrationBean<CorsFilter> bean = build(properties);

        assertThat(bean.isEnabled()).isTrue();
        assertThat(bean.getFilter()).isInstanceOf(CorsFilter.class);
        assertThat(bean.getOrder()).isEqualTo(WebFilterOrderEnum.CORS_FILTER);
    }

    @Test
    void corsFilterAllowsConfiguredOriginInPreflight() throws Exception {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setAllowedOrigins(List.of("https://example.com"));

        CorsFilter filter = build(properties).getFilter();

        HttpServletRequest request = mockPreflightRequest("https://example.com");
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
        properties.getCors().setAllowedOrigins(List.of("https://allowed.com"));

        CorsFilter filter = build(properties).getFilter();

        HttpServletRequest request = mockPreflightRequest("https://evil.com");
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(mock(jakarta.servlet.ServletOutputStream.class));
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        // 不被允许的来源直接拒绝：不进入 Filter 链
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void openModeAllowsAnyOriginWithoutCredentials() throws Exception {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setMode(CorsMode.OPEN);

        CorsFilter filter = build(properties).getFilter();

        HttpServletRequest request = mockPreflightRequest("https://any-partner.example.com");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        verify(response).setHeader(eq("Access-Control-Allow-Origin"), eq("https://any-partner.example.com"));
    }

    @Test
    void openModeRejectsCredentials() {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setMode(CorsMode.OPEN);
        properties.getCors().setAllowCredentials(true);

        assertThatThrownBy(() -> build(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("open");
    }

    @Test
    void strictModeRejectsWildcardWithCredentials() {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setAllowedOrigins(List.of("*"));
        properties.getCors().setAllowCredentials(true);

        assertThatThrownBy(() -> build(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("credentials");
    }

    @Test
    void patternModeRequiresPatterns() {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setMode(CorsMode.PATTERN);

        assertThatThrownBy(() -> build(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("allowed-origin-patterns");
    }

    @Test
    void patternModeMatchesConfiguredPattern() throws Exception {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setMode(CorsMode.PATTERN);
        properties.getCors().setAllowedOriginPatterns(List.of("https://*.example.com"));

        CorsFilter filter = build(properties).getFilter();

        HttpServletRequest request = mockPreflightRequest("https://partner1.example.com");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).setHeader(eq("Access-Control-Allow-Origin"), eq("https://partner1.example.com"));
    }

    @Test
    void dynamicModeRequiresPredicateBean() {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setMode(CorsMode.DYNAMIC);

        assertThatThrownBy(() -> build(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CorsOriginPredicate");
    }

    @Test
    void dynamicModeDelegatesToPredicate() throws Exception {
        MigooWebProperties properties = new MigooWebProperties();
        properties.getCors().setMode(CorsMode.DYNAMIC);
        CorsOriginPredicate predicate = origin -> origin.endsWith(".partner.com");
        CorsFilter filter = new FrameworkCorsConfiguration()
                .corsFilterBean(properties, withPredicate(predicate)).getFilter();

        // 注册域名准入 → 放行
        HttpServletResponse allowedResponse = mock(HttpServletResponse.class);
        FilterChain allowedChain = mock(FilterChain.class);
        filter.doFilter(mockPreflightRequest("https://app.partner.com"), allowedResponse, allowedChain);
        verify(allowedResponse).setHeader(eq("Access-Control-Allow-Origin"), eq("https://app.partner.com"));

        // 未注册域名 → 拒绝
        HttpServletResponse deniedResponse = mock(HttpServletResponse.class);
        when(deniedResponse.getOutputStream()).thenReturn(mock(jakarta.servlet.ServletOutputStream.class));
        FilterChain deniedChain = mock(FilterChain.class);
        filter.doFilter(mockPreflightRequest("https://evil.example.org"), deniedResponse, deniedChain);
        verify(deniedChain, never()).doFilter(any(), any());
    }

    private static HttpServletRequest mockPreflightRequest(String origin) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("OPTIONS");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("/api/test");
        when(request.getHttpServletMapping()).thenReturn(mock(jakarta.servlet.http.HttpServletMapping.class));
        when(request.getHeader("Origin")).thenReturn(origin);
        when(request.getHeader("Access-Control-Request-Method")).thenReturn("GET");
        when(request.getHeaders("Access-Control-Request-Headers"))
                .thenReturn(java.util.Collections.emptyEnumeration());
        return request;
    }
}
