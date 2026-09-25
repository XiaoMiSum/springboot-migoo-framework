package xyz.migoo.framework.observability.trace;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import xyz.migoo.framework.common.observability.TraceIdResolver;

/**
 * 基于 Micrometer Tracing 的 {@link TraceIdResolver} 实现
 *
 * <p>取当前 span（通常由 OTel 桥在进入请求作用域时建立）的 traceId；
 * 无 tracing 或无当前 span 时返回 {@code null}，由调用方（web 的 TraceIdFilter）回退到
 * 上游 {@code X-Trace-Id} 或本地生成。</p>
 *
 * @author xiaomi
 */
@Slf4j
public class MicrometerTraceIdResolver implements TraceIdResolver {

    private final Tracer tracer;

    public MicrometerTraceIdResolver(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public String resolve() {
        try {
            Span span = tracer.currentSpan();
            return span != null ? span.context().traceId() : null;
        } catch (Exception ex) {
            // SPI 契约: 不得抛异常（观测不得影响业务）
            log.debug("[resolve][解析当前 traceId 失败]", ex);
            return null;
        }
    }

}
