package xyz.migoo.framework.common.observability;

/**
 * 令牌撤销事件（可观测性信号）
 *
 * <p>发出点：security 组件 {@code DefaultJwtAuthenticator} 的 {@code clean}（登出撤销当前令牌）
 * 与 {@code revokeUserTokens}（踢出用户、撤销其全部令牌）。</p>
 *
 * @param scope 撤销范围（{@code token} 单个令牌 / {@code user} 用户全部令牌，低基数，可作 tag）
 * @author xiaomi
 */
public record TokenRevokedEvent(String scope) {
}
