package xyz.migoo.framework.common.observability;

/**
 * 限流拒绝事件（可观测性信号）
 *
 * <p>发出点：web 组件 {@code RateLimitAspect} 命中限流、抛出 429 处。</p>
 *
 * @param path    入口路由模板（如 {@code /user/{id}}），取不到时为 {@code unknown}（低基数，可作 tag）
 * @param keyType 限流维度（{@code ip} / {@code user} / {@code key}）
 * @param limit   窗口内允许的最大请求数
 * @author xiaomi
 */
public record RateLimitExceededEvent(String path, String keyType, int limit) {
}
