package xyz.migoo.framework.web.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link TraceIdFilter} 单元测试
 *
 * <p>验证 TraceId 的三种来源（上游透传 / 本地生成）、响应头回写以及请求结束后的 MDC 清理。</p>
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

    private static AtomicReference<String> captureMdc(FilterChain filterChain) throws Exception {
        AtomicReference<String> ref = new AtomicReference<>();
        doAnswer(invocation -> {
            ref.set(MDC.get("traceId"));
            return null;
        }).when(filterChain).doFilter(any(), any());
        return ref;
    }
}
