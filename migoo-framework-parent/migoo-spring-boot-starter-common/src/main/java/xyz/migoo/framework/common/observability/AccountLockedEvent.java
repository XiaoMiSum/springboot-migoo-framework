package xyz.migoo.framework.common.observability;

/**
 * 账号锁定事件（可观测性信号）
 *
 * <p>发出点：security 组件 {@code DefaultJwtAuthenticator#authenticate} 锁定分支。</p>
 *
 * @param reason 锁定原因（{@code already_locked} 已被锁定拦截 / {@code failure_threshold}
 *               连续失败达阈值，低基数，可作 tag）
 * @author xiaomi
 */
public record AccountLockedEvent(String reason) {
}
