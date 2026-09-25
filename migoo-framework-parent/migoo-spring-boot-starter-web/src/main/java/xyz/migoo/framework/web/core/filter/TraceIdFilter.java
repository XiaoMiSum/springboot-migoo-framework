package xyz.migoo.framework.web.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.migoo.framework.common.observability.TraceIdResolver;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 过滤器
 *
 * <p>traceId 的取值顺序（顺序无关的确定性行为）：</p>
 * <ol>
 *     <li><b>已存在</b>：tracing（Micrometer Tracing / OTel 桥）已写入 MDC {@code traceId} → 不覆盖，
 *         本过滤器只负责响应头回写；</li>
 *     <li><b>SPI 解析</b>：通过 {@link TraceIdResolver} 取当前 span 的 traceId（上游 {@code traceparent}
 *     优先于 {@code X-Trace-Id}）；</li>
 *     <li><b>上游头</b>：仅收到 {@code X-Trace-Id} 时沿用，维持旧契约；</li>
 *     <li><b>本地生成</b>：无 tracing 时自造 UUID（去横线），与引入可观测性组件前的行为一致。</li>
 * </ol>
 *
 * @author xiaomi
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * traceId 解析 SPI（可空：未引入可观测性组件时回退到上述 3/4 步）
     */
    private final TraceIdResolver traceIdResolver;

    /**
     * 是否回写 {@code X-Trace-Id} 响应头（migoo.observability.tracing.propagate-x-trace-id）
     */
    private final boolean propagateXTraceId;

    public TraceIdFilter() {
        this(null, true);
    }

    /**
     * @param traceIdResolver   traceId 解析 SPI，可为 null
     * @param propagateXTraceId 是否回写 {@code X-Trace-Id} 响应头
     */
    public TraceIdFilter(TraceIdResolver traceIdResolver, boolean propagateXTraceId) {
        this.traceIdResolver = traceIdResolver;
        this.propagateXTraceId = propagateXTraceId;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws IOException, ServletException {
        String existing = MDC.get(TRACE_ID);
        // tracing 已写入 → 不覆盖、不清理（归 tracing 的作用域管理）；本过滤器写入的才自行清理
        boolean owned = existing == null || existing.isBlank();
        String traceId = owned ? resolveTraceId(request) : existing;
        if (owned) {
            MDC.put(TRACE_ID, traceId);
        }
        if (propagateXTraceId) {
            response.setHeader(HEADER_TRACE_ID, traceId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (owned) {
                // 链路执行期间 tracing 可能写入了真实 traceId → 响应头补写最终生效值
                String current = MDC.get(TRACE_ID);
                if (propagateXTraceId && current != null && !current.equals(traceId)) {
                    response.setHeader(HEADER_TRACE_ID, current);
                }
                MDC.remove(TRACE_ID);
            }
        }
    }

    /**
     * 按「SPI → 上游头 → 本地生成」的顺序解析 traceId
     */
    private String resolveTraceId(HttpServletRequest request) {
        if (traceIdResolver != null) {
            String resolved = traceIdResolver.resolve();
            if (resolved != null && !resolved.isBlank()) {
                return resolved;
            }
        }
        String upstream = request.getHeader(HEADER_TRACE_ID);
        if (upstream != null && !upstream.isBlank()) {
            return upstream;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

}
