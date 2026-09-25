package xyz.migoo.framework.common.observability;

/**
 * 认证失败事件（可观测性信号）
 *
 * <p>发出点：security 组件 {@code DefaultJwtAuthenticator#authenticate} 认证失败分支。</p>
 *
 * @param reason 失败原因（Spring 认证异常简单名，如 {@code BadCredentialsException} /
 *               {@code DisabledException}，低基数，可作 tag）
 * @author xiaomi
 */
public record AuthenticationFailedEvent(String reason) {
}
