package xyz.migoo.framework.common.observability;

/**
 * TraceId 解析 SPI
 *
 * <p>用于在不引入 micrometer-tracing 依赖的前提下，让业务组件（如 web 的 {@code TraceIdFilter}）
 * 取到当前请求的真实 traceId。由可观测性组件（migoo-spring-boot-starter-observability）提供
 * 基于 Micrometer Tracing 的实现，未引入该组件时业务侧回退到自身行为（自造 traceId）。</p>
 *
 * <p>实现约定：解析不到（无 tracing / 无当前 span）时返回 {@code null} 或空串，调用方自行回退，
 * 实现不得抛异常。</p>
 *
 * @author xiaomi
 */
public interface TraceIdResolver {

    /**
     * 解析当前上下文的 traceId
     *
     * @return 当前 traceId；不可用时返回 {@code null} 或空串
     */
    String resolve();

}
