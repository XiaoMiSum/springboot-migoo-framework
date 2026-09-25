package xyz.migoo.framework.observability.config;

import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.migoo.framework.common.observability.TraceIdResolver;
import xyz.migoo.framework.observability.trace.MicrometerTraceIdResolver;

/**
 * 链路追踪桥接配置
 *
 * <p>向 common 的 SPI {@link TraceIdResolver} 提供基于 Micrometer Tracing 的实现，
 * 使 web 组件的 {@code TraceIdFilter} 无需依赖 micrometer 即可取到真实 traceId
 * （traceparent 优先于 X-Trace-Id、MDC 不覆盖的确定性行为见该过滤器 javadoc）。</p>
 *
 * @author xiaomi
 */
@Configuration(proxyBeanMethods = false)
public class ObservabilityTraceConfiguration {

    /**
     * TraceIdResolver 实现 Bean
     * <p>
     * 仅在 tracing 已激活（存在 {@link Tracer} Bean，含 Noop Tracer）且应用未自行提供
     * 实现时注册；{@code migoo.observability.tracing.enabled=false} 可整体关闭。
     *
     * @param tracer Micrometer Tracing 的 Tracer
     * @return TraceIdResolver 实现
     */
    @Bean
    @ConditionalOnBean(Tracer.class)
    @ConditionalOnMissingBean(TraceIdResolver.class)
    @ConditionalOnProperty(prefix = "migoo.observability.tracing", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    public TraceIdResolver micrometerTraceIdResolver(Tracer tracer) {
        return new MicrometerTraceIdResolver(tracer);
    }

}
