package xyz.migoo.framework.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.common.observability.*;
import xyz.migoo.framework.observability.config.MigooObservabilityProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

/**
 * {@link SignalEventListener} + {@link SignalMetrics} 单元测试
 *
 * <p>覆盖：8 个事件逐一计数与 tag 断言、单信号开关、空值兜底、注册表故障不上抛。</p>
 */
class SignalEventListenerTest {

    private SimpleMeterRegistry registry;
    private MigooObservabilityProperties properties;
    private SignalEventListener listener;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        properties = new MigooObservabilityProperties();
        listener = new SignalEventListener(new SignalMetrics(registry, properties));
    }

    @Test
    void allEightEventsAreCountedWithTags() {
        listener.onRateLimitExceeded(new RateLimitExceededEvent("/user/{id}", "ip", 10));
        listener.onAuthenticationFailed(new AuthenticationFailedEvent("BadCredentialsException"));
        listener.onAccountLocked(new AccountLockedEvent("failure_threshold"));
        listener.onTokenRevoked(new TokenRevokedEvent("user"));
        listener.onServerError(new ServerErrorEvent("/order", "POST", "IllegalStateException", "boom"));
        listener.onMqMessageSent(new MqMessageSentEvent("Order"));
        listener.onMqConsumeFailed(new MqMessageConsumeFailedEvent("Order", true));
        listener.onMqDeadLettered(new MqMessageDeadLetteredEvent("Order", "IllegalStateException"));

        assertThat(count(SignalMetrics.RATE_LIMIT_REJECTED, "path", "/user/{id}")).isEqualTo(1.0);
        assertThat(count(SignalMetrics.LOGIN_FAILED, "reason", "BadCredentialsException")).isEqualTo(1.0);
        assertThat(count(SignalMetrics.ACCOUNT_LOCKED, "reason", "failure_threshold")).isEqualTo(1.0);
        assertThat(count(SignalMetrics.TOKEN_REVOKED, "scope", "user")).isEqualTo(1.0);
        assertThat(count(SignalMetrics.SERVER_ERROR, "path", "/order", "exception", "IllegalStateException"))
                .isEqualTo(1.0);
        assertThat(count(SignalMetrics.MQ_MESSAGE_SENT, "stream", "Order")).isEqualTo(1.0);
        assertThat(count(SignalMetrics.MQ_CONSUME_FAILED, "stream", "Order", "will_retry", "true")).isEqualTo(1.0);
        assertThat(count(SignalMetrics.MQ_DEAD_LETTERED, "stream", "Order", "reason", "IllegalStateException"))
                .isEqualTo(1.0);
    }

    @Test
    void sameEventAccumulatesCount() {
        listener.onServerError(new ServerErrorEvent("/order", "POST", "IllegalStateException", "boom"));
        listener.onServerError(new ServerErrorEvent("/order", "POST", "IllegalStateException", "boom again"));
        listener.onServerError(new ServerErrorEvent("/order", "POST", "NullPointerException", "npe"));

        assertThat(count(SignalMetrics.SERVER_ERROR, "path", "/order",
                "exception", "IllegalStateException")).isEqualTo(2.0);
        assertThat(count(SignalMetrics.SERVER_ERROR, "path", "/order",
                "exception", "NullPointerException")).isEqualTo(1.0);
    }

    @Test
    void signalSwitchDisablesSingleCounter() {
        properties.getMetrics().getSignals().put(SignalMetrics.RATE_LIMIT_REJECTED, false);

        listener.onRateLimitExceeded(new RateLimitExceededEvent("/x", "ip", 1));
        // 其余信号不受影响
        listener.onServerError(new ServerErrorEvent("/x", "GET", "RuntimeException", "m"));

        assertThat(registry.find(SignalMetrics.RATE_LIMIT_REJECTED).counters()).isEmpty();
        assertThat(registry.find(SignalMetrics.SERVER_ERROR).counters()).hasSize(1);
    }

    @Test
    void nullFieldsFallBackToUnknownTag() {
        listener.onServerError(new ServerErrorEvent(null, null, null, null));
        listener.onRateLimitExceeded(new RateLimitExceededEvent("  ", null, 1));

        assertThat(count(SignalMetrics.SERVER_ERROR, "path", "unknown", "exception", "unknown")).isEqualTo(1.0);
        assertThat(count(SignalMetrics.RATE_LIMIT_REJECTED, "path", "unknown")).isEqualTo(1.0);
    }

    @Test
    void registryFailureDoesNotPropagateToPublisher() {
        // 注册表故障（返回 null → NPE）→ 监听器按契约吞掉，不影响事件发布方
        MeterRegistry failing = mock(MeterRegistry.class);
        SignalEventListener listener = new SignalEventListener(new SignalMetrics(failing, properties));

        assertThatCode(() -> {
            listener.onServerError(new ServerErrorEvent("/x", "GET", "RuntimeException", "m"));
            listener.onMqConsumeFailed(new MqMessageConsumeFailedEvent("Order", false));
        }).doesNotThrowAnyException();
    }

    private double count(String metric, String... tags) {
        return registry.get(metric).tags(tags).counter().count();
    }

}
