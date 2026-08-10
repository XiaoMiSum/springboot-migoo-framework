package xyz.migoo.framework.mq.core.interceptor;

import org.junit.jupiter.api.Test;
import xyz.migoo.framework.mq.core.message.AbstractMessage;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link RedisMessageInterceptorUtils} 单元测试
 */
class RedisMessageInterceptorUtilsTest {

    /** 记录调用事件（共享列表，用于验证顺序）的拦截器 */
    static class RecordingInterceptor implements RedisMessageInterceptor {
        private final String name;
        private final List<String> events;

        RecordingInterceptor(String name, List<String> events) {
            this.name = name;
            this.events = events;
        }

        @Override
        public void consumeMessageBefore(AbstractMessage message) {
            events.add(name + ":before");
        }

        @Override
        public void consumeMessageAfter(AbstractMessage message) {
            events.add(name + ":after");
        }

        @Override
        public void consumeMessageError(AbstractMessage message, Throwable throwable) {
            events.add(name + ":error");
        }
    }

    /** 在 consumeMessageBefore 中抛异常的拦截器 */
    static class ThrowingInterceptor implements RedisMessageInterceptor {
        private final List<String> events;

        ThrowingInterceptor(List<String> events) {
            this.events = events;
        }

        @Override
        public void consumeMessageBefore(AbstractMessage message) {
            events.add("throw:before");
            throw new IllegalStateException("stop-chain");
        }

        @Override
        public void consumeMessageAfter(AbstractMessage message) {
            events.add("throw:after");
        }
    }

    static class DemoMessage extends AbstractMessage {
    }

    @Test
    void emptyInterceptorsAreNoOp() {
        DemoMessage message = new DemoMessage();
        assertThatCode(() -> RedisMessageInterceptorUtils.consumeMessageBefore(List.of(), message)).doesNotThrowAnyException();
        assertThatCode(() -> RedisMessageInterceptorUtils.consumeMessageAfter(List.of(), message)).doesNotThrowAnyException();
        assertThatCode(() -> RedisMessageInterceptorUtils.consumeMessageError(List.of(), message, new RuntimeException())).doesNotThrowAnyException();
    }

    @Test
    void nullInterceptorsThrowNpe() {
        // 当前实现不做 null 防御（拦截器列表来自 RedisMQTemplate，非 null），记录现有行为
        DemoMessage message = new DemoMessage();
        assertThatThrownBy(() -> RedisMessageInterceptorUtils.consumeMessageBefore(null, message))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void consumeMessageBeforeCallsInterceptorsInOrder() {
        List<String> events = new ArrayList<>();
        List<RedisMessageInterceptor> interceptors = List.of(
                new RecordingInterceptor("A", events),
                new RecordingInterceptor("B", events),
                new RecordingInterceptor("C", events));

        RedisMessageInterceptorUtils.consumeMessageBefore(interceptors, new DemoMessage());

        // 正序：A -> B -> C
        assertThat(events).containsExactly("A:before", "B:before", "C:before");
    }

    @Test
    void consumeMessageAfterCallsInterceptorsInReverseOrder() {
        List<String> events = new ArrayList<>();
        List<RedisMessageInterceptor> interceptors = List.of(
                new RecordingInterceptor("A", events),
                new RecordingInterceptor("B", events),
                new RecordingInterceptor("C", events));

        RedisMessageInterceptorUtils.consumeMessageAfter(interceptors, new DemoMessage());

        // 倒序：C -> B -> A
        assertThat(events).containsExactly("C:after", "B:after", "A:after");
    }

    @Test
    void consumeMessageErrorCallsInterceptorsInReverseOrderWithThrowable() {
        List<String> events = new ArrayList<>();
        List<RedisMessageInterceptor> interceptors = List.of(
                new RecordingInterceptor("A", events),
                new RecordingInterceptor("B", events),
                new RecordingInterceptor("C", events));
        RuntimeException error = new RuntimeException("consume failed");

        RedisMessageInterceptorUtils.consumeMessageError(interceptors, new DemoMessage(), error);

        // 倒序：C -> B -> A
        assertThat(events).containsExactly("C:error", "B:error", "A:error");
    }

    @Test
    void exceptionFromInterceptorPropagatesAndStopsChain() {
        List<String> events = new ArrayList<>();
        List<RedisMessageInterceptor> interceptors = List.of(
                new ThrowingInterceptor(events),
                new RecordingInterceptor("B", events));

        assertThatThrownBy(() -> RedisMessageInterceptorUtils.consumeMessageBefore(interceptors, new DemoMessage()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("stop-chain");

        // 异常中断链：后续拦截器未被调用
        assertThat(events).containsExactly("throw:before");
    }
}
