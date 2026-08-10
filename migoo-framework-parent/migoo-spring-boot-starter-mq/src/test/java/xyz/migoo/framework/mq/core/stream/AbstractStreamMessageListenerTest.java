package xyz.migoo.framework.mq.core.stream;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.mq.config.MQProperties;
import xyz.migoo.framework.mq.core.RedisMQTemplate;
import xyz.migoo.framework.mq.core.interceptor.IdempotentMessageInterceptor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AbstractStreamMessageListener} 单元测试
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class AbstractStreamMessageListenerTest {

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

    /** 记录收到的消息 */
    static class RecordingStreamListener extends AbstractStreamMessageListener<DemoStreamMessage> {
        final List<DemoStreamMessage> received = new ArrayList<>();

        RecordingStreamListener(MQProperties properties) {
            super(properties);
        }

        @Override
        public void onMessage(DemoStreamMessage message) {
            received.add(message);
        }
    }

    /** 抽象 onMessage 委托给可 mock 的 Consumer */
    static class SpyStreamListener extends AbstractStreamMessageListener<DemoStreamMessage> {
        private final Consumer<DemoStreamMessage> consumer;

        SpyStreamListener(MQProperties properties, Consumer<DemoStreamMessage> consumer) {
            super(properties);
            this.consumer = consumer;
        }

        @Override
        public void onMessage(DemoStreamMessage message) {
            consumer.accept(message);
        }
    }

    /** 抽象 onMessage 抛出异常的监听器 */
    static class FailingStreamListener extends AbstractStreamMessageListener<DemoStreamMessage> {
        final List<DemoStreamMessage> received = new ArrayList<>();

        FailingStreamListener(MQProperties properties) {
            super(properties);
        }

        @Override
        public void onMessage(DemoStreamMessage message) {
            received.add(message);
            throw new IllegalStateException("consume failed");
        }
    }

    /** 未声明泛型参数的监听器 */
    static class PlainStreamListener extends AbstractStreamMessageListener {
        PlainStreamListener(MQProperties properties) {
            super(properties);
        }

        @Override
        public void onMessage(AbstractStreamMessage message) {
        }
    }

    private MQProperties defaultProperties() {
        MQProperties properties = new MQProperties();
        properties.setGroup("test-group");
        return properties;
    }

    /** 将消息序列化后构造成 ObjectRecord */
    private ObjectRecord<String, String> toRecord(DemoStreamMessage message) {
        return StreamRecords.newRecord()
                .ofObject(JsonUtils.toJsonString(message))
                .withStreamKey(message.getChannel())
                .withId(RecordId.of("1-0"));
    }

    /** 组装 mock redisTemplate + streamOps 的监听器 */
    private RedisTemplate<String, ?> mockRedisTemplate(StreamOperations streamOps) {
        RedisTemplate<String, ?> redisTemplate = mock(RedisTemplate.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);
        return redisTemplate;
    }

    @Test
    void constructorResolvesStreamKeyFromMessageType() {
        // Stream Key 规则：直接使用消息类名
        RecordingStreamListener listener = new RecordingStreamListener(defaultProperties());
        assertThat(listener.getStreamKey()).isEqualTo("DemoStreamMessage");
    }

    @Test
    void constructorAppliesProperties() {
        MQProperties properties = new MQProperties();
        properties.setGroup("g1");
        properties.setMaxRetry(5);
        properties.setDeadLetterEnabled(false);
        properties.setDeleteAfterAck(true);
        RecordingStreamListener listener = new RecordingStreamListener(properties);
        assertThat(listener.getGroup()).isEqualTo("g1");
        assertThat(listener.getMaxRetry()).isEqualTo(5);
        assertThat(listener.isDeadLetterEnabled()).isFalse();
        assertThat(listener.isDeleteAfterAck()).isTrue();
    }

    @Test
    void constructorThrowsWhenGenericTypeMissing() {
        assertThatThrownBy(() -> new PlainStreamListener(defaultProperties()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("需要设置消息类型");
    }

    @Test
    void onMessageParsesValueAndInvokesAbstractOnMessage() {
        Consumer<DemoStreamMessage> consumer = mock(Consumer.class);
        SpyStreamListener listener = new SpyStreamListener(defaultProperties(), consumer);
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        listener.setRedisMQTemplate(new RedisMQTemplate(mock(RedisTemplate.class)));

        DemoStreamMessage message = new DemoStreamMessage();
        message.setContent("stream-data");
        message.addHeader("k", "v");
        ObjectRecord<String, String> record = toRecord(message);

        listener.onMessage(record);

        org.mockito.ArgumentCaptor<DemoStreamMessage> captor =
                org.mockito.ArgumentCaptor.forClass(DemoStreamMessage.class);
        verify(consumer).accept(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("stream-data");
        assertThat(captor.getValue().getHeader("k")).isEqualTo("v");
        assertThat(captor.getValue().getMessageId()).isEqualTo(message.getMessageId());
        // 消费成功即 ACK
        verify(streamOps).acknowledge("test-group", record);
        // 默认 deleteAfterAck=false，不删除消息
        verify(streamOps, never()).delete(record);
    }

    @Test
    void onMessageDeletesRecordWhenDeleteAfterAckEnabled() {
        MQProperties properties = defaultProperties();
        properties.setDeleteAfterAck(true);
        RecordingStreamListener listener = new RecordingStreamListener(properties);
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        listener.setRedisMQTemplate(new RedisMQTemplate(mock(RedisTemplate.class)));

        ObjectRecord<String, String> record = toRecord(new DemoStreamMessage());
        listener.onMessage(record);

        verify(streamOps).acknowledge("test-group", record);
        verify(streamOps).delete(record);
        assertThat(listener.received).hasSize(1);
    }

    @Test
    void onMessageSkipsAlreadyConsumedMessage() {
        RecordingStreamListener listener = new RecordingStreamListener(defaultProperties());
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));

        // 幂等拦截器：Redis 中已存在消费标记 -> 抛 MessageAlreadyConsumedException
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        when(stringRedisTemplate.execute(any(), anyList(), any(), any())).thenReturn("consumed");
        // 跳过消费后 finally 中 consumeMessageAfter 会更新状态，需 stub opsForValue()
        when(stringRedisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        RedisMQTemplate template = new RedisMQTemplate(mock(RedisTemplate.class));
        template.addInterceptor(new IdempotentMessageInterceptor(stringRedisTemplate, Duration.ofHours(24)));
        listener.setRedisMQTemplate(template);

        ObjectRecord<String, String> record = toRecord(new DemoStreamMessage());
        listener.onMessage(record);

        // 消息被跳过：抽象 onMessage 未被调用，但已 ACK
        assertThat(listener.received).isEmpty();
        verify(streamOps).acknowledge("test-group", record);
        verify(streamOps, never()).delete(record);
    }

    @Test
    void onMessageRetriesWhenConsumptionFails() {
        FailingStreamListener listener = new FailingStreamListener(defaultProperties());
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        listener.setRedisMQTemplate(new RedisMQTemplate(mock(RedisTemplate.class)));

        ObjectRecord<String, String> record = toRecord(new DemoStreamMessage());
        listener.onMessage(record);

        assertThat(listener.received).hasSize(1);
        // ACK 原消息
        verify(streamOps).acknowledge("test-group", record);
        // 重试：重新写入 Stream，携带递增后的 retry-count
        org.mockito.ArgumentCaptor<ObjectRecord<String, String>> addCaptor =
                org.mockito.ArgumentCaptor.forClass(ObjectRecord.class);
        verify(streamOps).add(addCaptor.capture());
        ObjectRecord<String, String> added = addCaptor.getValue();
        assertThat(added.getStream()).isEqualTo("DemoStreamMessage");
        assertThat(added.getValue()).contains("\"retry-count\":\"1\"");
        // 删除原消息
        verify(streamOps).delete(record);
    }

    @Test
    void onMessageSendsToDeadLetterQueueWhenRetriesExhausted() {
        FailingStreamListener listener = new FailingStreamListener(defaultProperties());
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        listener.setRedisMQTemplate(new RedisMQTemplate(mock(RedisTemplate.class)));

        DemoStreamMessage message = new DemoStreamMessage();
        // retry-count=3 已达到 maxRetry=3
        message.addHeader("retry-count", "3");
        ObjectRecord<String, String> record = toRecord(message);
        listener.onMessage(record);

        // 不再重试，直接进入死信队列
        org.mockito.ArgumentCaptor<ObjectRecord<String, String>> addCaptor =
                org.mockito.ArgumentCaptor.forClass(ObjectRecord.class);
        verify(streamOps).add(addCaptor.capture());
        ObjectRecord<String, String> added = addCaptor.getValue();
        assertThat(added.getStream()).isEqualTo("DemoStreamMessage:dead_letter");
        assertThat(added.getValue()).contains("\"error-message\"").contains("\"error-time\"");
        verify(streamOps).delete(record);
    }

    @Test
    void onMessageDoesNotResendWhenRetriesExhaustedAndDeadLetterDisabled() {
        MQProperties properties = defaultProperties();
        properties.setDeadLetterEnabled(false);
        FailingStreamListener listener = new FailingStreamListener(properties);
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        listener.setRedisMQTemplate(new RedisMQTemplate(mock(RedisTemplate.class)));

        DemoStreamMessage message = new DemoStreamMessage();
        message.addHeader("retry-count", "3");
        ObjectRecord<String, String> record = toRecord(message);
        listener.onMessage(record);

        // 只 ACK + 删除，不再重新投递
        verify(streamOps, never()).add(any(ObjectRecord.class));
        verify(streamOps).acknowledge("test-group", record);
        verify(streamOps).delete(record);
    }

    @Test
    void onMessageThrowsWhenRedisTemplateNotSet() {
        RecordingStreamListener listener = new RecordingStreamListener(defaultProperties());
        listener.setRedisMQTemplate(new RedisMQTemplate(mock(RedisTemplate.class)));
        ObjectRecord<String, String> record = toRecord(new DemoStreamMessage());

        assertThatThrownBy(() -> listener.onMessage(record))
                .isInstanceOf(NullPointerException.class);
    }
}
