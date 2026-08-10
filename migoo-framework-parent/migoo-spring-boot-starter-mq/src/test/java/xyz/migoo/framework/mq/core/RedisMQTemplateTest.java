package xyz.migoo.framework.mq.core;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.mq.core.interceptor.RedisMessageInterceptor;
import xyz.migoo.framework.mq.core.message.AbstractMessage;
import xyz.migoo.framework.mq.core.pubsub.AbstractChannelMessage;
import xyz.migoo.framework.mq.core.stream.AbstractStreamMessage;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RedisMQTemplate} 单元测试
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class RedisMQTemplateTest {

    /** 测试用 Channel 消息 */
    static class DemoChannelMessage extends AbstractChannelMessage {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /** 测试用 Stream 消息 */
    static class DemoStreamMessage extends AbstractStreamMessage {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /** 记录调用事件的拦截器 */
    static class RecordingInterceptor implements RedisMessageInterceptor {
        private final String name;
        private final List<String> events;

        RecordingInterceptor(String name, List<String> events) {
            this.name = name;
            this.events = events;
        }

        @Override
        public void sendMessageBefore(AbstractMessage message) {
            events.add(name + ":before");
        }

        @Override
        public void sendMessageAfter(AbstractMessage message) {
            events.add(name + ":after");
        }

        @Override
        public void sendMessageError(AbstractMessage message, Throwable throwable) {
            events.add(name + ":error");
        }
    }

    @Test
    void constructorInitializesEmptyInterceptors() {
        RedisMQTemplate template = new RedisMQTemplate(mock(RedisTemplate.class));
        assertThat(template.getInterceptors()).isEmpty();
        assertThat(template.getRedisTemplate()).isNotNull();
    }

    @Test
    void addInterceptorRegistersInterceptor() {
        RedisMQTemplate template = new RedisMQTemplate(mock(RedisTemplate.class));
        RedisMessageInterceptor interceptor = mock(RedisMessageInterceptor.class);
        template.addInterceptor(interceptor);
        assertThat(template.getInterceptors()).containsExactly(interceptor);
    }

    @Test
    void sendChannelMessageConvertsAndSendsJson() {
        RedisTemplate<String, ?> redisTemplate = mock(RedisTemplate.class);
        RedisMQTemplate template = new RedisMQTemplate(redisTemplate);

        DemoChannelMessage message = new DemoChannelMessage();
        message.setContent("hello");
        template.send(message);

        // convertAndSend(channel=类名, body=序列化 JSON)
        verify(redisTemplate).convertAndSend("DemoChannelMessage", JsonUtils.toJsonString(message));
    }

    @Test
    void sendChannelMessageInvokesInterceptorsBeforeThenReverseAfter() {
        List<String> events = new ArrayList<>();
        RedisMQTemplate template = new RedisMQTemplate(mock(RedisTemplate.class));
        template.addInterceptor(new RecordingInterceptor("A", events));
        template.addInterceptor(new RecordingInterceptor("B", events));
        template.addInterceptor(new RecordingInterceptor("C", events));

        template.send(new DemoChannelMessage());

        // before 正序 A->B->C，after 倒序 C->B->A
        assertThat(events).containsExactly("A:before", "B:before", "C:before", "C:after", "B:after", "A:after");
    }

    @Test
    void sendChannelMessageRethrowsAndNotifiesErrorInterceptor() {
        List<String> events = new ArrayList<>();
        RedisTemplate<String, ?> redisTemplate = mock(RedisTemplate.class);
        when(redisTemplate.convertAndSend(anyString(), any())).thenThrow(new RuntimeException("send failed"));
        RedisMQTemplate template = new RedisMQTemplate(redisTemplate);
        template.addInterceptor(new RecordingInterceptor("A", events));
        template.addInterceptor(new RecordingInterceptor("B", events));

        assertThatThrownBy(() -> template.send(new DemoChannelMessage()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("send failed");

        // error 倒序 B->A，after 在 finally 中仍按倒序执行
        assertThat(events).containsExactly("A:before", "B:before", "B:error", "A:error", "B:after", "A:after");
    }

    @Test
    void sendStreamMessageReturnsRecordId() {
        RedisTemplate<String, ?> redisTemplate = mock(RedisTemplate.class);
        StreamOperations streamOps = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);
        RecordId recordId = RecordId.of("1-0");
        when(streamOps.add(any(ObjectRecord.class))).thenReturn(recordId);
        RedisMQTemplate template = new RedisMQTemplate(redisTemplate);

        DemoStreamMessage message = new DemoStreamMessage();
        message.setContent("stream-data");
        RecordId result = template.send(message);

        assertThat(result).isEqualTo(recordId);
        // Stream Key = 类名，value = 序列化 JSON
        org.mockito.ArgumentCaptor<ObjectRecord<String, String>> captor =
                org.mockito.ArgumentCaptor.forClass(ObjectRecord.class);
        verify(streamOps).add(captor.capture());
        assertThat(captor.getValue().getStream()).isEqualTo("DemoStreamMessage");
        assertThat(captor.getValue().getValue()).isEqualTo(JsonUtils.toJsonString(message));
    }

    @Test
    void sendStreamMessageRethrowsWhenAddFails() {
        List<String> events = new ArrayList<>();
        RedisTemplate<String, ?> redisTemplate = mock(RedisTemplate.class);
        StreamOperations streamOps = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);
        when(streamOps.add(any(ObjectRecord.class))).thenThrow(new RuntimeException("stream add failed"));
        RedisMQTemplate template = new RedisMQTemplate(redisTemplate);
        template.addInterceptor(new RecordingInterceptor("A", events));

        assertThatThrownBy(() -> template.send(new DemoStreamMessage()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("stream add failed");

        // 异常被重新抛出，错误拦截器被通知
        assertThat(events).containsExactly("A:before", "A:error", "A:after");
    }

    @Test
    void sendChannelMessageRethrowsWhenInterceptorBeforeThrows() {
        RedisTemplate<String, ?> redisTemplate = mock(RedisTemplate.class);
        RedisMQTemplate template = new RedisMQTemplate(redisTemplate);
        RedisMessageInterceptor interceptor = mock(RedisMessageInterceptor.class);
        template.addInterceptor(interceptor);
        org.mockito.Mockito.doThrow(new IllegalStateException("interceptor failed"))
                .when(interceptor).sendMessageBefore(any(AbstractMessage.class));

        assertThatThrownBy(() -> template.send(new DemoChannelMessage()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("interceptor failed");

        // 发送被中断，不会调用 convertAndSend
        verify(redisTemplate, never()).convertAndSend(anyString(), any());
        // 错误与 after 拦截器仍被调用
        verify(interceptor).sendMessageError(any(AbstractMessage.class), any(Throwable.class));
        verify(interceptor).sendMessageAfter(any(AbstractMessage.class));
    }
}
