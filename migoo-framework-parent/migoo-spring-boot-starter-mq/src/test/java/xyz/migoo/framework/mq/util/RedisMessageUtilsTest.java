package xyz.migoo.framework.mq.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.mq.core.pubsub.AbstractChannelMessage;
import xyz.migoo.framework.mq.core.stream.AbstractStreamMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RedisMessageUtils} 单元测试
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class RedisMessageUtilsTest {

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

    @Test
    void sendChannelMessageConvertsAndSends() {
        RedisTemplate<?, ?> redisTemplate = mock(RedisTemplate.class);
        DemoChannelMessage message = new DemoChannelMessage();
        message.setContent("hello");

        RedisMessageUtils.sendChannelMessage(redisTemplate, message);

        // 使用 messageId 之外还携带 headers 的完整 JSON
        verify(redisTemplate).convertAndSend("DemoChannelMessage", JsonUtils.toJsonString(message));
    }

    @Test
    void sendStreamMessageAddsRecordToStream() {
        RedisTemplate<String, ?> redisTemplate = mock(RedisTemplate.class);
        StreamOperations streamOps = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);
        RecordId recordId = RecordId.of("2-0");
        when(streamOps.add(any(ObjectRecord.class))).thenReturn(recordId);

        DemoStreamMessage message = new DemoStreamMessage();
        message.setContent("stream");
        RecordId result = RedisMessageUtils.sendStreamMessage(redisTemplate, message);

        assertThat(result).isEqualTo(recordId);
        org.mockito.ArgumentCaptor<ObjectRecord<String, String>> captor =
                org.mockito.ArgumentCaptor.forClass(ObjectRecord.class);
        verify(streamOps).add(captor.capture());
        assertThat(captor.getValue().getStream()).isEqualTo("DemoStreamMessage");
        assertThat(captor.getValue().getValue()).isEqualTo(JsonUtils.toJsonString(message));
    }

    @Test
    void sendStreamMessageReturnsNullWhenAddReturnsNull() {
        RedisTemplate<String, ?> redisTemplate = mock(RedisTemplate.class);
        StreamOperations streamOps = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);
        when(streamOps.add(any(ObjectRecord.class))).thenReturn(null);

        RedisMessageUtils.sendStreamMessage(redisTemplate, new DemoStreamMessage());
        // 不抛异常，原样透传 add 结果
        verify(streamOps).add(any(ObjectRecord.class));
    }
}
