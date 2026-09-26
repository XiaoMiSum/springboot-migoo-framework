package xyz.migoo.framework.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.common.observability.*;
import xyz.migoo.framework.observability.config.MigooObservabilityProperties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * {@link SignalEventListener} + {@link SignalMetrics} 单元测试
 *
 * <p>覆盖：8 个事件逐一计数与 tag 断言、单信号开关、空值兜底、注册表故障不上抛，
 * 以及异步模式（关闭时排空队列、命名守护线程、重复关闭幂等）。</p>
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

        assertAllEightCounted();
    }

    private void assertAllEightCounted() {
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

    // ==================== 异步计数（metrics.async = true） ====================

    @Test
    void asyncModeDrainsQueueOnDestroy() {
        SignalEventListener async = new SignalEventListener(new SignalMetrics(registry, properties), true, 64);
        try {
            async.onRateLimitExceeded(new RateLimitExceededEvent("/user/{id}", "ip", 10));
            async.onAuthenticationFailed(new AuthenticationFailedEvent("BadCredentialsException"));
            async.onAccountLocked(new AccountLockedEvent("failure_threshold"));
            async.onTokenRevoked(new TokenRevokedEvent("user"));
            async.onServerError(new ServerErrorEvent("/order", "POST", "IllegalStateException", "boom"));
            async.onMqMessageSent(new MqMessageSentEvent("Order"));
            async.onMqConsumeFailed(new MqMessageConsumeFailedEvent("Order", true));
            async.onMqDeadLettered(new MqMessageDeadLetteredEvent("Order", "IllegalStateException"));
        } finally {
            // destroy = shutdown + awaitTermination：排空队列后再返回，故可确定性断言
            async.destroy();
        }

        assertAllEightCounted();
    }

    @Test
    void asyncModeRunsOnNamedDaemonThread() throws Exception {
        SignalMetrics metrics = mock(SignalMetrics.class);
        AtomicReference<String> threadName = new AtomicReference<>();
        AtomicBoolean daemon = new AtomicBoolean();
        doAnswer(invocation -> {
            threadName.set(Thread.currentThread().getName());
            daemon.set(Thread.currentThread().isDaemon());
            return null;
        }).when(metrics).serverError(anyString(), anyString());

        SignalEventListener async = new SignalEventListener(metrics, true, 16);
        try {
            async.onServerError(new ServerErrorEvent("/x", "GET", "RuntimeException", "m"));
        } finally {
            async.destroy();
        }

        // 单线程命名守护线程：不阻止 JVM 退出，也便于日志排查
        assertThat(threadName.get()).startsWith("migoo-observability-signal-");
        assertThat(daemon.get()).isTrue();
    }

    @Test
    void asyncFailuresAndDoubleDestroyDoNotThrow() {
        SignalEventListener async =
                new SignalEventListener(new SignalMetrics(mock(MeterRegistry.class), properties), true, 8);

        assertThatCode(() -> {
            // 注册表故障在异步线程内被吞掉，不上抛
            async.onServerError(new ServerErrorEvent("/x", "GET", "RuntimeException", "m"));
            async.destroy();
            // 重复关闭幂等（容器停机竞态）
            async.destroy();
        }).doesNotThrowAnyException();
    }

    private double count(String metric, String... tags) {
        return registry.get(metric).tags(tags).counter().count();
    }

}
