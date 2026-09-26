package xyz.migoo.framework.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import xyz.migoo.framework.observability.config.MigooObservabilityProperties;

import java.util.Map;

/**
 * 框架信号指标（{@code migoo.*}）注册中心
 *
 * <p>按事件懒创建 Counter（MeterRegistry 对同名同 tag 幂等复用），并承担两道护栏：</p>
 * <ul>
 *     <li><b>单信号开关</b>：{@code migoo.observability.metrics.signals.<指标名>=false} 即停用该指标，
 *         未列出的信号跟随 {@code metrics.enabled}；</li>
 *     <li><b>观测不得影响业务</b>：注册/计数异常只记日志、不上抛。</li>
 * </ul>
 *
 * <p><b>tag 基数护栏</b>：仅允许低基数字段（path / reason / stream / scope / exception / will_retry），
 * 事件中的 message、limit 等高基数字段只随事件流转、不进 tag。</p>
 *
 * @author xiaomi
 * @see SignalEventListener 事件 → 指标的订阅入口
 */
@Slf4j
public class SignalMetrics {

    /**
     * 限流拒绝（tags: path）
     */
    public static final String RATE_LIMIT_REJECTED = "migoo.ratelimit.rejected";

    /**
     * 登录失败（tags: reason）
     */
    public static final String LOGIN_FAILED = "migoo.security.login.failed";

    /**
     * 账号锁定（tags: reason）
     */
    public static final String ACCOUNT_LOCKED = "migoo.security.account.locked";

    /**
     * 令牌撤销（tags: scope）
     */
    public static final String TOKEN_REVOKED = "migoo.security.token.revoked";

    /**
     * 服务端错误 500（tags: path、exception）
     */
    public static final String SERVER_ERROR = "migoo.server.error";

    /**
     * MQ 消息发送（tags: stream）
     */
    public static final String MQ_MESSAGE_SENT = "migoo.mq.message.sent";

    /**
     * MQ 消费失败（tags: stream、will_retry）
     */
    public static final String MQ_CONSUME_FAILED = "migoo.mq.message.consume.failed";

    /**
     * MQ 死信（tags: stream、reason）
     */
    public static final String MQ_DEAD_LETTERED = "migoo.mq.message.dead.lettered";

    /**
     * 安全审计操作（tags: action、success）
     */
    public static final String AUDIT_OPERATION = "migoo.security.audit.operation";

    private final MeterRegistry registry;

    /**
     * 单信号开关（key 为指标名，未列出跟随上级）
     */
    private final Map<String, Boolean> switches;

    public SignalMetrics(MeterRegistry registry, MigooObservabilityProperties properties) {
        this.registry = registry;
        this.switches = properties.getMetrics().getSignals();
    }

    /**
     * 记录限流拒绝
     *
     * @param path 入口路由模板
     */
    public void rateLimitExceeded(String path) {
        increment(RATE_LIMIT_REJECTED, "path", nullToUnknown(path));
    }

    /**
     * 记录登录失败
     *
     * @param reason 失败原因
     */
    public void loginFailed(String reason) {
        increment(LOGIN_FAILED, "reason", nullToUnknown(reason));
    }

    /**
     * 记录账号锁定
     *
     * @param reason 锁定原因
     */
    public void accountLocked(String reason) {
        increment(ACCOUNT_LOCKED, "reason", nullToUnknown(reason));
    }

    /**
     * 记录令牌撤销
     *
     * @param scope 撤销范围
     */
    public void tokenRevoked(String scope) {
        increment(TOKEN_REVOKED, "scope", nullToUnknown(scope));
    }

    /**
     * 记录服务端错误
     *
     * @param path          入口路由模板
     * @param exceptionType 异常类型简单名
     */
    public void serverError(String path, String exceptionType) {
        increment(SERVER_ERROR, "path", nullToUnknown(path), "exception", nullToUnknown(exceptionType));
    }

    /**
     * 记录 MQ 消息发送
     *
     * @param stream Stream Key / Channel
     */
    public void mqMessageSent(String stream) {
        increment(MQ_MESSAGE_SENT, "stream", nullToUnknown(stream));
    }

    /**
     * 记录 MQ 消费失败
     *
     * @param stream    Stream Key
     * @param willRetry 是否还会重试
     */
    public void mqConsumeFailed(String stream, boolean willRetry) {
        increment(MQ_CONSUME_FAILED, "stream", nullToUnknown(stream), "will_retry", String.valueOf(willRetry));
    }

    /**
     * 记录 MQ 死信
     *
     * @param stream Stream Key
     * @param reason 触发原因
     */
    public void mqDeadLettered(String stream, String reason) {
        increment(MQ_DEAD_LETTERED, "stream", nullToUnknown(stream), "reason", nullToUnknown(reason));
    }

    /**
     * 记录安全审计操作
     *
     * @param action  审计动作（低基数：注解 action 或 类名#方法名）
     * @param success 操作是否成功
     */
    public void auditOperation(String action, boolean success) {
        increment(AUDIT_OPERATION, "action", nullToUnknown(action),
                "success", String.valueOf(success));
    }

    /**
     * 按开关计数（观测不得影响业务：异常只记日志）
     *
     * @param metric 指标名（同时是单信号开关的 key）
     * @param tags   key/value 形式的 tag，只允许低基数字段
     */
    private void increment(String metric, String... tags) {
        try {
            if (Boolean.FALSE.equals(switches.get(metric))) {
                return;
            }
            registry.counter(metric, tags).increment();
        } catch (Exception ex) {
            log.warn("[increment][信号指标计数失败] metric({})", metric, ex);
        }
    }

    private static String nullToUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

}
