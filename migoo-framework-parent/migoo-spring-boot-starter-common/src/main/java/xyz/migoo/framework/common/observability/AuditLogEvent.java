package xyz.migoo.framework.common.observability;

/**
 * 安全审计事件（可观测性信号 + 应用落库契约）
 *
 * <p>发出点：security 组件 {@code AuditLogAspect} 拦截 {@code @AuditLog} 方法——
 * 成功与失败各发一次；同一事件也会以 {@code migoo.audit} 为 logger 名输出 JSON 行
 * （独立审计日志），应用需要落库时自行订阅本事件持久化。</p>
 *
 * @param operator    操作人（登录用户 {@code id(username)}，未登录 {@code anonymous}）
 * @param clientIp    客户端 IP（取不到时 {@code unknown}）
 * @param path        入口路由模板（如 {@code /user/{id}}，低基数，可作 tag）
 * @param action      审计动作（注解 action 或 {@code 类名#方法名}，低基数，可作 tag）
 * @param success     操作是否成功
 * @param errorMessage 失败原因（成功时 {@code null}）
 * @param params      入参 JSON（已脱敏：{@code @Sensitive} 掩码 + 敏感关键词字段 {@code ******}；
 *                    {@code recordParams=false} 或无可摘要参数时 {@code null}）
 * @author xiaomi
 */
public record AuditLogEvent(String operator, String clientIp, String path, String action,
                            boolean success, String errorMessage, String params) {
}
