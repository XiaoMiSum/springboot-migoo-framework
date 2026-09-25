package xyz.migoo.framework.observability.metrics;

import org.springframework.context.event.EventListener;
import xyz.migoo.framework.common.observability.*;

/**
 * 信号事件订阅器
 *
 * <p>按事件类型逐个订阅 common 中的信号 record，转发给 {@link SignalMetrics} 计数。
 * 事件是同步分发的，因此本类（及其委托的 {@link SignalMetrics}）保证不抛异常、不做 IO，
 * 以免观测影响业务链路。</p>
 *
 * @author xiaomi
 */
public class SignalEventListener {

    private final SignalMetrics metrics;

    public SignalEventListener(SignalMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * 限流拒绝
     */
    @EventListener
    public void onRateLimitExceeded(RateLimitExceededEvent event) {
        metrics.rateLimitExceeded(event.path());
    }

    /**
     * 登录失败
     */
    @EventListener
    public void onAuthenticationFailed(AuthenticationFailedEvent event) {
        metrics.loginFailed(event.reason());
    }

    /**
     * 账号锁定
     */
    @EventListener
    public void onAccountLocked(AccountLockedEvent event) {
        metrics.accountLocked(event.reason());
    }

    /**
     * 令牌撤销
     */
    @EventListener
    public void onTokenRevoked(TokenRevokedEvent event) {
        metrics.tokenRevoked(event.scope());
    }

    /**
     * 服务端错误（500）
     */
    @EventListener
    public void onServerError(ServerErrorEvent event) {
        metrics.serverError(event.path(), event.exceptionType());
    }

    /**
     * MQ 消息发送
     */
    @EventListener
    public void onMqMessageSent(MqMessageSentEvent event) {
        metrics.mqMessageSent(event.stream());
    }

    /**
     * MQ 消费失败
     */
    @EventListener
    public void onMqConsumeFailed(MqMessageConsumeFailedEvent event) {
        metrics.mqConsumeFailed(event.stream(), event.willRetry());
    }

    /**
     * MQ 死信
     */
    @EventListener
    public void onMqDeadLettered(MqMessageDeadLetteredEvent event) {
        metrics.mqDeadLettered(event.stream(), event.reason());
    }

}
