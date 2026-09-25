package xyz.migoo.framework.web.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import xyz.migoo.framework.common.observability.TraceIdResolver;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link TraceIdFilter} 单元测试
 *
 * <p>验证 TraceId 的取值顺序（MDC 已有 → SPI resolver → 上游 X-Trace-Id → 本地生成）、
 * 响应头回写（含链路中 tracing 补写后的一致性）、propagation 开关以及请求结束后的 MDC 清理。</p>
 */
class TraceIdFilterTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void usesUpstreamTraceIdWhenPresent() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("upstream-trace-id");
        AtomicReference<String> mdcDuringChain = captureMdc(filterChain);

        new TraceIdFilter().doFilter(request, response, filterChain);

        assertThat(mdcDuringChain.get()).isEqualTo("upstream-trace-id");
        verify(response).setHeader("X-Trace-Id", "upstream-trace-id");
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void generatesTraceIdWhenHeaderMissing() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn(null);
        AtomicReference<String> mdcDuringChain = captureMdc(filterChain);

        new TraceIdFilter().doFilter(request, response, filterChain);

        // 生成的 TraceId 为 32 位十六进制（UUID 去横线）
        assertThat(mdcDuringChain.get()).matches("[0-9a-f]{32}");
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(org.mockito.ArgumentMatchers.eq("X-Trace-Id"), headerCaptor.capture());
        assertThat(headerCaptor.getValue()).isEqualTo(mdcDuringChain.get());
    }

    @Test
    void generatesTraceIdWhenHeaderBlank() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("   ");
        AtomicReference<String> mdcDuringChain = captureMdc(filterChain);

        new TraceIdFilter().doFilter(request, response, filterChain);

        assertThat(mdcDuringChain.get()).matches("[0-9a-f]{32}");
    }

    @Test
    void clearsMdcAfterFilterCompletes() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-123");

        new TraceIdFilter().doFilter(request, response, filterChain);

        // 请求结束后 MDC 中不再有 traceId
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void clearsMdcEvenWhenChainThrows() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-123");
        doAnswer(invocation -> {
            throw new IllegalStateException("chain failure");
        }).when(filterChain).doFilter(any(), any());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> new TraceIdFilter().doFilter(request, response, filterChain))
                .isInstanceOf(IllegalStateException.class);
        // finally 分支清理 MDC
        assertThat(MDC.get("traceId")).isNull();
    }

    // ==================== SPI 桥接（阶段 ③） ====================

    @Test
    void resolverTraceIdWinsOverUpstreamHeader() throws Exception {
        // 上游同时给 traceparent（由 resolver 解析出）与 X-Trace-Id → 真实 trace 优先
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("upstream-trace-id");
        TraceIdResolver resolver = mock(TraceIdResolver.class);
        when(resolver.resolve()).thenReturn("real-trace-id");
        AtomicReference<String> mdcDuringChain = captureMdc(filterChain);

        new TraceIdFilter(resolver, true).doFilter(request, response, filterChain);

        assertThat(mdcDuringChain.get()).isEqualTo("real-trace-id");
        verify(response).setHeader("X-Trace-Id", "real-trace-id");
    }

    @Test
    void fallsBackToUpstreamHeaderWhenResolverUnavailable() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("upstream-trace-id");
        TraceIdResolver resolver = mock(TraceIdResolver.class);
        when(resolver.resolve()).thenReturn(null);
        AtomicReference<String> mdcDuringChain = captureMdc(filterChain);

        new TraceIdFilter(resolver, true).doFilter(request, response, filterChain);

        assertThat(mdcDuringChain.get()).isEqualTo("upstream-trace-id");
    }

    @Test
    void doesNotOverrideMdcWrittenByTracing() throws Exception {
        // tracing 已写入 MDC → 不覆盖、不清理（归 tracing 作用域管理），只回写响应头
        MDC.put("traceId", "traced-by-micrometer");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("upstream-trace-id");
        TraceIdResolver resolver = mock(TraceIdResolver.class);
        AtomicReference<String> mdcDuringChain = captureMdc(filterChain);

        new TraceIdFilter(resolver, true).doFilter(request, response, filterChain);

        assertThat(mdcDuringChain.get()).isEqualTo("traced-by-micrometer");
        verify(response).setHeader("X-Trace-Id", "traced-by-micrometer");
        // 过滤器不清理自己没写入的 traceId
        assertThat(MDC.get("traceId")).isEqualTo("traced-by-micrometer");
        verify(resolver, never()).resolve();
        MDC.remove("traceId");
    }

    @Test
    void responseHeaderReflectsMdcWrittenDuringChain() throws Exception {
        // 链路执行期间 tracing 才写入真实 traceId → 响应头补写最终生效值
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn(null);
        doAnswer(invocation -> {
            MDC.put("traceId", "real-trace-later");
            return null;
        }).when(filterChain).doFilter(any(), any());

        new TraceIdFilter().doFilter(request, response, filterChain);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response, times(2)).setHeader(org.mockito.ArgumentMatchers.eq("X-Trace-Id"), captor.capture());
        assertThat(captor.getAllValues().get(0)).matches("[0-9a-f]{32}");
        assertThat(captor.getAllValues().get(1)).isEqualTo("real-trace-later");
        // 最终 MDC 由过滤器清理（tracing 未接管）
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void skipsResponseHeaderWhenPropagationDisabled() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(request.getHeader("X-Trace-Id")).thenReturn("trace-123");
        AtomicReference<String> mdcDuringChain = captureMdc(filterChain);

        new TraceIdFilter(null, false).doFilter(request, response, filterChain);

        // MDC 关联不受影响，只是不回写响应头
        assertThat(mdcDuringChain.get()).isEqualTo("trace-123");
        verify(response, never()).setHeader(any(String.class), any(String.class));
    }

    private static AtomicReference<String> captureMdc(FilterChain filterChain) throws Exception {
        AtomicReference<String> ref = new AtomicReference<>();
        doAnswer(invocation -> {
            ref.set(MDC.get("traceId"));
            return null;
        }).when(filterChain).doFilter(any(), any());
        return ref;
    }
}
