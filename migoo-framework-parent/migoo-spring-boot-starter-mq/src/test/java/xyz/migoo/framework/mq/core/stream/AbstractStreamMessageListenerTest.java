package xyz.migoo.framework.mq.core.stream;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.common.observability.MqMessageConsumeFailedEvent;
import xyz.migoo.framework.common.observability.MqMessageDeadLetteredEvent;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    void onMessageLeavesMessageInPelWhenRetriesRemain() {
        FailingStreamListener listener = new FailingStreamListener(defaultProperties());
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        listener.setRedisMQTemplate(new RedisMQTemplate(mock(RedisTemplate.class)));
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        listener.setEventPublisher(publisher);

        ObjectRecord<String, String> record = toRecord(new DemoStreamMessage());
        listener.onMessage(record);

        assertThat(listener.received).hasSize(1);
        // 退避重试：不 ACK、不删除、不立即重投 —— 消息留在 PEL 等待 StreamReclaimTask 退避认领
        verify(streamOps, never()).acknowledge("test-group", record);
        verify(streamOps, never()).add(any(ObjectRecord.class));
        verify(streamOps, never()).delete(record);
        // 首次失败仍可重试 → willRetry=true，未进死信
        verify(publisher).publishEvent(new MqMessageConsumeFailedEvent("DemoStreamMessage", true));
        verify(publisher, never()).publishEvent(any(MqMessageDeadLetteredEvent.class));
    }

    @Test
    void onMessageSendsToDeadLetterQueueWhenRetriesExhausted() {
        FailingStreamListener listener = new FailingStreamListener(defaultProperties());
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        listener.setRedisMQTemplate(new RedisMQTemplate(mock(RedisTemplate.class)));
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        listener.setEventPublisher(publisher);

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
        // 事件顺序: 消费失败(willRetry=false) → 死信(原因=异常类型)
        org.mockito.ArgumentCaptor<Object> eventCaptor = org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(publisher, times(2)).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).containsExactly(
                new MqMessageConsumeFailedEvent("DemoStreamMessage", false),
                new MqMessageDeadLetteredEvent("DemoStreamMessage", "IllegalStateException"));
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

    // ==================== PEL 认领与退避重投 ====================

    @Test
    void reclaimRequeuesPelMessageWithIncrementedRetryCount() {
        RecordingStreamListener listener = new RecordingStreamListener(defaultProperties());
        listener.setConsumerName("consumer-1");
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        listener.setEventPublisher(publisher);

        // PEL 中消息 retry-count=0，闲置 30s（超过退避 5s）
        DemoStreamMessage message = new DemoStreamMessage();
        message.setContent("retry-me");
        MapRecord<String, Object, Object> claimed = claimedRecord(JsonUtils.toJsonString(message));
        stubPending(streamOps, pendingOf("10-0"));
        when(streamOps.claim(eq("DemoStreamMessage"), eq("test-group"), eq("consumer-1"),
                eq(Duration.ofSeconds(5)), eq(RecordId.of("10-0"))))
                .thenReturn(List.of(claimed));

        int handled = listener.reclaimPending(Duration.ofSeconds(5));

        // 重投：retry-count 递增为 1，消息回到 Stream
        assertThat(handled).isEqualTo(1);
        org.mockito.ArgumentCaptor<ObjectRecord<String, String>> addCaptor =
                org.mockito.ArgumentCaptor.forClass(ObjectRecord.class);
        verify(streamOps).add(addCaptor.capture());
        ObjectRecord<String, String> added = addCaptor.getValue();
        assertThat(added.getStream()).isEqualTo("DemoStreamMessage");
        assertThat(added.getValue()).contains("\"retry-count\":\"1\"");
        // 先重投后 ACK；delete-after-ack 默认 false → 原消息保留
        verify(streamOps).acknowledge("test-group", claimed);
        verify(streamOps, never()).delete(claimed);
        // 认领重投不产生新的失败/死信事件
        verify(publisher, never()).publishEvent(any(MqMessageDeadLetteredEvent.class));
        verify(publisher, never()).publishEvent(any(MqMessageConsumeFailedEvent.class));
    }

    @Test
    void reclaimSendsExhaustedMessageToDeadLetter() {
        RecordingStreamListener listener = new RecordingStreamListener(defaultProperties());
        listener.setConsumerName("consumer-1");
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        listener.setEventPublisher(publisher);

        // retry-count=3 已达 maxRetry=3：孤儿消息不再重投，兜底转死信
        DemoStreamMessage message = new DemoStreamMessage();
        message.addHeader("retry-count", "3");
        MapRecord<String, Object, Object> claimed = claimedRecord(JsonUtils.toJsonString(message));
        stubPending(streamOps, pendingOf("10-0"));
        when(streamOps.claim(eq("DemoStreamMessage"), eq("test-group"), eq("consumer-1"),
                eq(Duration.ofSeconds(5)), eq(RecordId.of("10-0"))))
                .thenReturn(List.of(claimed));

        int handled = listener.reclaimPending(Duration.ofSeconds(5));

        assertThat(handled).isEqualTo(1);
        org.mockito.ArgumentCaptor<ObjectRecord<String, String>> addCaptor =
                org.mockito.ArgumentCaptor.forClass(ObjectRecord.class);
        verify(streamOps).add(addCaptor.capture());
        ObjectRecord<String, String> added = addCaptor.getValue();
        assertThat(added.getStream()).isEqualTo("DemoStreamMessage:dead_letter");
        assertThat(added.getValue()).contains("\"error-message\"").contains("\"error-time\"");
        verify(streamOps).acknowledge("test-group", claimed);
        // 死信事件：原因=合成异常类型
        verify(publisher).publishEvent(new MqMessageDeadLetteredEvent("DemoStreamMessage", "IllegalStateException"));
    }

    @Test
    void reclaimAcksUnparseableMessageWithoutRequeue() {
        RecordingStreamListener listener = new RecordingStreamListener(defaultProperties());
        listener.setConsumerName("consumer-1");
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));

        // 毒消息：无法解析 → ACK 丢弃，不重投、不进死信循环
        MapRecord<String, Object, Object> claimed = claimedRecord("{{invalid-json");
        stubPending(streamOps, pendingOf("10-0"));
        when(streamOps.claim(eq("DemoStreamMessage"), eq("test-group"), eq("consumer-1"),
                eq(Duration.ofSeconds(5)), eq(RecordId.of("10-0"))))
                .thenReturn(List.of(claimed));

        int handled = listener.reclaimPending(Duration.ofSeconds(5));

        assertThat(handled).isEqualTo(1);
        verify(streamOps, never()).add(any(ObjectRecord.class));
        verify(streamOps).acknowledge("test-group", claimed);
    }

    @Test
    void reclaimNoopWhenNoPendingMessages() {
        RecordingStreamListener listener = new RecordingStreamListener(defaultProperties());
        listener.setConsumerName("consumer-1");
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        stubPending(streamOps, new PendingMessages("test-group", List.of()));

        int handled = listener.reclaimPending(Duration.ofSeconds(5));

        // 无闲置消息：直接返回，不产生任何写操作
        assertThat(handled).isEqualTo(0);
        verify(streamOps, never()).add(any(ObjectRecord.class));
        verify(streamOps, never()).acknowledge(eq("test-group"), any(Record.class));
    }

    @Test
    void reclaimReturnsZeroWhenRedisFails() {
        RecordingStreamListener listener = new RecordingStreamListener(defaultProperties());
        listener.setConsumerName("consumer-1");
        StreamOperations streamOps = mock(StreamOperations.class);
        listener.setRedisTemplate(mockRedisTemplate(streamOps));
        when(streamOps.pending(eq("DemoStreamMessage"), eq("test-group"), any(Range.class),
                eq(100L), eq(Duration.ofSeconds(5))))
                .thenThrow(new RuntimeException("connection refused"));

        // 自愈任务不外抛异常，降级返回 0
        assertThat(listener.reclaimPending(Duration.ofSeconds(5))).isEqualTo(0);
    }

    @Test
    void reclaimReturnsZeroWhenRedisTemplateNotSet() {
        RecordingStreamListener listener = new RecordingStreamListener(defaultProperties());
        assertThat(listener.reclaimPending(Duration.ofSeconds(5))).isEqualTo(0);
    }

    // ==================== 认领测试夹具 ====================

    /** 构造含单条 PEL 记录的 XPENDING 结果 */
    private PendingMessages pendingOf(String recordId) {
        return new PendingMessages("test-group", List.of(
                new PendingMessage(RecordId.of(recordId),
                        org.springframework.data.redis.connection.stream.Consumer.from("test-group", "old-consumer"),
                        Duration.ofSeconds(30), 1L)));
    }

    /** 构造 XCLAIM 认领到的单字段记录（值为消息 JSON） */
    private MapRecord<String, Object, Object> claimedRecord(String json) {
        return MapRecord.<String, Object, Object>create("DemoStreamMessage", java.util.Map.of("message", json))
                .withId(RecordId.of("10-0"));
    }

    /** stub XPENDING：闲置过滤 5s、单轮上限 100 条 */
    private void stubPending(StreamOperations streamOps, PendingMessages pending) {
        when(streamOps.pending(eq("DemoStreamMessage"), eq("test-group"), any(Range.class),
                eq(100L), eq(Duration.ofSeconds(5))))
                .thenReturn(pending);
    }
}
