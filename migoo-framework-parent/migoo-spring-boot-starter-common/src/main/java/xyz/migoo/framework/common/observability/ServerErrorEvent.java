package xyz.migoo.framework.common.observability;

/**
 * 服务端错误事件（可观测性信号，HTTP 500）
 *
 * <p>发出点：web 组件 {@code GlobalExceptionHandler} 兜底异常处理分支。</p>
 *
 * @param path         入口路由模板（如 {@code /user/{id}}），取不到时为 {@code unknown}（低基数，可作 tag）
 * @param method       HTTP 方法（GET / POST ...）
 * @param exceptionType 异常类型简单名（如 {@code IllegalStateException}，低基数，可作 tag）
 * @param message      异常摘要（已截断，仅供日志/排查，禁止作 tag）
 * @author xiaomi
 */
public record ServerErrorEvent(String path, String method, String exceptionType, String message) {
}
