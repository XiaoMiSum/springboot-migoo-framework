package xyz.migoo.framework.observability.trace;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link MicrometerTraceIdResolver} 单元测试
 *
 * <p>验证有 span 返回 traceId、无 span 返回 null、Tracer 异常不外抛（SPI 契约）。</p>
 */
class MicrometerTraceIdResolverTest {

    @Test
    void resolvesTraceIdFromCurrentSpan() {
        Tracer tracer = mock(Tracer.class);
        Span span = mock(Span.class);
        TraceContext traceContext = mock(TraceContext.class);
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("abc123trace");

        assertThat(new MicrometerTraceIdResolver(tracer).resolve()).isEqualTo("abc123trace");
    }

    @Test
    void returnsNullWhenNoCurrentSpan() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(null);

        assertThat(new MicrometerTraceIdResolver(tracer).resolve()).isNull();
    }

    @Test
    void neverPropagatesTracerFailure() {
        // Tracer 抛异常（如上下文已关闭）→ 按 SPI 契约返回 null，不上抛
        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenThrow(new IllegalStateException("context closed"));

        assertThat(new MicrometerTraceIdResolver(tracer).resolve()).isNull();
    }

}
