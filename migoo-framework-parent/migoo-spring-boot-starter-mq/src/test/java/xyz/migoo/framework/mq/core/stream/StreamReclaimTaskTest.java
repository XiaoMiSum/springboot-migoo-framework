package xyz.migoo.framework.mq.core.stream;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * {@link StreamReclaimTask} 单元测试
 * <p>
 * 覆盖：按间隔轮询全部监听器、单监听器异常不影响其余、destroy 幂等。
 * 测试用极短轮询间隔 + 闩锁等待，避免时序脆弱断言。
 */
class StreamReclaimTaskTest {

    private static final Duration BACKOFF = Duration.ofSeconds(5);

    @Test
    void taskInvokesReclaimOnAllListeners() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AbstractStreamMessageListener<?> first = mock(AbstractStreamMessageListener.class);
        AbstractStreamMessageListener<?> second = mock(AbstractStreamMessageListener.class);
        stubCountdown(first, latch);
        stubCountdown(second, latch);

        StreamReclaimTask task = new StreamReclaimTask(List.of(first, second), BACKOFF, Duration.ofMillis(10));
        try {
            // 两个监听器都被轮询到
            assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        } finally {
            task.destroy();
        }
        verify(first).reclaimPending(BACKOFF);
        verify(second).reclaimPending(BACKOFF);
    }

    @Test
    void listenerExceptionDoesNotBreakOtherListeners() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AbstractStreamMessageListener<?> failing = mock(AbstractStreamMessageListener.class);
        doThrow(new RuntimeException("boom")).when(failing).reclaimPending(any(Duration.class));
        AbstractStreamMessageListener<?> healthy = mock(AbstractStreamMessageListener.class);
        stubCountdown(healthy, latch);

        StreamReclaimTask task = new StreamReclaimTask(List.of(failing, healthy), BACKOFF, Duration.ofMillis(10));
        try {
            // 单个监听器抛异常不影响后续监听器执行
            assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        } finally {
            task.destroy();
        }
        verify(failing).reclaimPending(BACKOFF);
        verify(healthy).reclaimPending(BACKOFF);
    }

    @Test
    void destroyIsIdempotent() {
        StreamReclaimTask task = new StreamReclaimTask(List.of(), BACKOFF, Duration.ofHours(1));
        assertThatCode(() -> {
            task.destroy();
            task.destroy();
        }).doesNotThrowAnyException();
    }

    // ==================== 夹具 ====================

    @SuppressWarnings("unchecked")
    private void stubCountdown(AbstractStreamMessageListener<?> listener, CountDownLatch latch) {
        doAnswer(invocation -> {
            latch.countDown();
            return 0;
        }).when(listener).reclaimPending(any(Duration.class));
    }
}
