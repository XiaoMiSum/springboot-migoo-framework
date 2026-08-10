package xyz.migoo.framework.mq.core.pubsub;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.mq.core.RedisMQTemplate;
import xyz.migoo.framework.mq.core.interceptor.RedisMessageInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AbstractChannelMessageListener} 单元测试
 */
@SuppressWarnings("rawtypes")
class AbstractChannelMessageListenerTest {

    /** 测试用 Pub/Sub 消息 */
    static class DemoChannelMessage extends AbstractChannelMessage {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /** 记录收到的消息，用于断言抽象 onMessage 被调用 */
    static class RecordingChannelListener extends AbstractChannelMessageListener<DemoChannelMessage> {
        final List<DemoChannelMessage> received = new ArrayList<>();

        @Override
        public void onMessage(DemoChannelMessage message) {
            received.add(message);
        }
    }

    /** 抽象 onMessage 委托给可 mock 的 Consumer，支持 Mockito verify */
    static class SpyChannelListener extends AbstractChannelMessageListener<DemoChannelMessage> {
        private final Consumer<DemoChannelMessage> consumer;

        SpyChannelListener(Consumer<DemoChannelMessage> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onMessage(DemoChannelMessage message) {
            consumer.accept(message);
        }
    }

    /** 抽象 onMessage 抛出异常的监听器 */
    static class FailingChannelListener extends AbstractChannelMessageListener<DemoChannelMessage> {
        final List<DemoChannelMessage> received = new ArrayList<>();

        @Override
        public void onMessage(DemoChannelMessage message) {
            received.add(message);
            throw new IllegalStateException("boom");
        }
    }

    /** 未声明泛型参数的监听器 */
    static class PlainChannelListener extends AbstractChannelMessageListener {
        @Override
        public void onMessage(AbstractChannelMessage message) {
        }
    }

    /** 构造一个真实 RedisMQTemplate（拦截器为空） */
    private RedisMQTemplate newRedisMQTemplate() {
        return new RedisMQTemplate(mock(RedisTemplate.class));
    }

    @Test
    void getChannelResolvesFromGenericMessageType() {
        // channel 通过解析类上的泛型得到，直接使用消息类名
        RecordingChannelListener listener = new RecordingChannelListener();
        assertThat(listener.getChannel()).isEqualTo("DemoChannelMessage");
    }

    @Test
    void constructorThrowsWhenGenericTypeMissing() {
        assertThatThrownBy(PlainChannelListener::new)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("需要设置消息类型");
    }

    @Test
    void onMessageParsesBodyAndInvokesAbstractOnMessage() {
        @SuppressWarnings("unchecked")
        Consumer<DemoChannelMessage> consumer = mock(Consumer.class);
        AbstractChannelMessageListener<DemoChannelMessage> listener = new SpyChannelListener(consumer);
        listener.setRedisMQTemplate(newRedisMQTemplate());

        DemoChannelMessage message = new DemoChannelMessage();
        message.setContent("hello");
        message.addHeader("trace-id", "t-1");
        Message redisMessage = mock(Message.class);
        when(redisMessage.getBody()).thenReturn(JsonUtils.toJsonString(message).getBytes(StandardCharsets.UTF_8));

        listener.onMessage(redisMessage, new byte[0]);

        // 抽象 onMessage 被调用，且收到的是解析后的消息对象
        org.mockito.ArgumentCaptor<DemoChannelMessage> captor =
                org.mockito.ArgumentCaptor.forClass(DemoChannelMessage.class);
        verify(consumer).accept(captor.capture());
        DemoChannelMessage parsed = captor.getValue();
        assertThat(parsed.getContent()).isEqualTo("hello");
        assertThat(parsed.getHeader("trace-id")).isEqualTo("t-1");
        assertThat(parsed.getMessageId()).isEqualTo(message.getMessageId());
    }

    @Test
    void onMessageInvokesInterceptorsAroundConsumption() {
        RecordingChannelListener listener = new RecordingChannelListener();
        RedisMQTemplate template = newRedisMQTemplate();
        RedisMessageInterceptor first = mock(RedisMessageInterceptor.class);
        RedisMessageInterceptor second = mock(RedisMessageInterceptor.class);
        template.addInterceptor(first);
        template.addInterceptor(second);
        listener.setRedisMQTemplate(template);

        DemoChannelMessage message = new DemoChannelMessage();
        message.setContent("hello");
        Message redisMessage = mock(Message.class);
        when(redisMessage.getBody()).thenReturn(JsonUtils.toJsonString(message).getBytes(StandardCharsets.UTF_8));

        listener.onMessage(redisMessage, new byte[0]);

        // before 正序、after 倒序，所有拦截器都会被调用
        verify(first).consumeMessageBefore(any(DemoChannelMessage.class));
        verify(second).consumeMessageBefore(any(DemoChannelMessage.class));
        verify(second).consumeMessageAfter(any(DemoChannelMessage.class));
        verify(first).consumeMessageAfter(any(DemoChannelMessage.class));
        verify(first, never()).consumeMessageError(any(), any());
        verify(second, never()).consumeMessageError(any(), any());
        // 消费成功，消息被正常处理
        assertThat(listener.received).hasSize(1);
    }

    @Test
    void onMessageNotifiesErrorInterceptorWhenConsumptionFails() {
        FailingChannelListener listener = new FailingChannelListener();
        RedisMQTemplate template = newRedisMQTemplate();
        RedisMessageInterceptor interceptor = mock(RedisMessageInterceptor.class);
        template.addInterceptor(interceptor);
        listener.setRedisMQTemplate(template);

        DemoChannelMessage message = new DemoChannelMessage();
        message.setContent("x");
        Message redisMessage = mock(Message.class);
        when(redisMessage.getBody()).thenReturn(JsonUtils.toJsonString(message).getBytes(StandardCharsets.UTF_8));

        listener.onMessage(redisMessage, new byte[0]);

        // 消费失败：错误拦截器被调用，消费后拦截器在 finally 中仍被执行
        verify(interceptor).consumeMessageError(any(DemoChannelMessage.class), any(Throwable.class));
        verify(interceptor).consumeMessageAfter(any(DemoChannelMessage.class));
        assertThat(listener.received).hasSize(1);
    }

    @Test
    void onMessageThrowsWhenBodyCannotBeParsed() {
        RecordingChannelListener listener = new RecordingChannelListener();
        listener.setRedisMQTemplate(newRedisMQTemplate());
        Message redisMessage = mock(Message.class);
        when(redisMessage.getBody()).thenReturn("not-json".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> listener.onMessage(redisMessage, new byte[0]))
                .isInstanceOf(RuntimeException.class);
        assertThat(listener.received).isEmpty();
    }

    @Test
    void onMessageThrowsWhenBodyIsEmpty() {
        RecordingChannelListener listener = new RecordingChannelListener();
        listener.setRedisMQTemplate(newRedisMQTemplate());
        Message redisMessage = mock(Message.class);
        when(redisMessage.getBody()).thenReturn(new byte[0]);

        assertThatThrownBy(() -> listener.onMessage(redisMessage, new byte[0]))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("解析消息失败");
        assertThat(listener.received).isEmpty();
    }

    @Test
    void onMessageThrowsWhenRedisMQTemplateNotSet() {
        RecordingChannelListener listener = new RecordingChannelListener();
        Message redisMessage = mock(Message.class);
        when(redisMessage.getBody()).thenReturn("{}".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> listener.onMessage(redisMessage, new byte[0]))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("RedisMQTemplate");
    }
}
