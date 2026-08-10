package xyz.migoo.framework.mq.core.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import xyz.migoo.framework.mq.core.message.AbstractMessage;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link IdempotentMessageInterceptor} 单元测试
 */
class IdempotentMessageInterceptorTest {

    private static final Duration EXPIRE_TIME = Duration.ofHours(24);

    /** 测试用消息：channel = 类名 "DemoMessage" */
    static class DemoMessage extends AbstractMessage {
    }

    private IdempotentMessageInterceptor newInterceptor(StringRedisTemplate template) {
        return new IdempotentMessageInterceptor(template, EXPIRE_TIME);
    }

    @Test
    void consumeMessageBeforeAllowsFirstConsumption() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        // Lua 脚本返回 "ok" = SETNX 成功（首次消费）
        when(template.execute(any(), anyList(), any(), any())).thenReturn("ok");
        IdempotentMessageInterceptor interceptor = newInterceptor(template);

        DemoMessage message = new DemoMessage();
        message.setMessageId("msg-1");

        assertThatCode(() -> interceptor.consumeMessageBefore(message)).doesNotThrowAnyException();

        // 幂等 key 规则：mq:idempotent:{channel}:{messageId}
        org.mockito.ArgumentCaptor<List<String>> keysCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(template).execute(any(), keysCaptor.capture(), any(), any());
        assertThat(keysCaptor.getValue()).containsExactly("mq:idempotent:DemoMessage:msg-1");
    }

    @Test
    void consumeMessageBeforeThrowsWhenAlreadyConsumed() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        // Lua 脚本返回已有状态值（重复消费）
        when(template.execute(any(), anyList(), any(), any())).thenReturn("consumed");
        IdempotentMessageInterceptor interceptor = newInterceptor(template);

        DemoMessage message = new DemoMessage();
        message.setMessageId("msg-1");

        Throwable thrown = catchThrowable(() -> interceptor.consumeMessageBefore(message));
        assertThat(thrown).isInstanceOf(IdempotentMessageInterceptor.MessageAlreadyConsumedException.class);
        IdempotentMessageInterceptor.MessageAlreadyConsumedException ex =
                (IdempotentMessageInterceptor.MessageAlreadyConsumedException) thrown;
        assertThat(ex.getMessageId()).isEqualTo("msg-1");
        assertThat(ex.getStatus()).isEqualTo("consumed");
    }

    @Test
    void consumeMessageBeforeSkipsWhenMessageIdNull() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        IdempotentMessageInterceptor interceptor = newInterceptor(template);

        DemoMessage message = new DemoMessage();
        message.setMessageId(null);

        assertThatCode(() -> interceptor.consumeMessageBefore(message)).doesNotThrowAnyException();
        // 缺少 messageId 时不做幂等检查，不访问 Redis
        verifyNoInteractions(template);
    }

    @Test
    void consumeMessageBeforeSkipsWhenMessageIdEmpty() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        IdempotentMessageInterceptor interceptor = newInterceptor(template);

        DemoMessage message = new DemoMessage();
        message.setMessageId("");

        assertThatCode(() -> interceptor.consumeMessageBefore(message)).doesNotThrowAnyException();
        verifyNoInteractions(template);
    }

    @Test
    void consumeMessageAfterMarksConsumedWithExpireTime() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(valueOps);
        IdempotentMessageInterceptor interceptor = newInterceptor(template);

        DemoMessage message = new DemoMessage();
        message.setMessageId("msg-1");

        interceptor.consumeMessageAfter(message);

        // 更新为 consumed 状态并刷新过期时间
        verify(valueOps).set("mq:idempotent:DemoMessage:msg-1", "consumed", EXPIRE_TIME);
    }

    @Test
    void consumeMessageAfterSkipsWhenMessageIdBlank() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        IdempotentMessageInterceptor interceptor = newInterceptor(template);

        DemoMessage message = new DemoMessage();
        message.setMessageId(null);
        interceptor.consumeMessageAfter(message);

        verifyNoInteractions(template);
    }

    @Test
    void consumeMessageErrorIgnoresAlreadyConsumedException() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        IdempotentMessageInterceptor interceptor = newInterceptor(template);

        DemoMessage message = new DemoMessage();
        message.setMessageId("msg-1");
        // 幂等异常不删除标记，允许幂等跳过
        interceptor.consumeMessageError(message,
                new IdempotentMessageInterceptor.MessageAlreadyConsumedException("msg-1", "consumed"));

        verifyNoInteractions(template);
    }

    @Test
    void consumeMessageErrorDeletesIdempotentKeyOnFailure() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        when(template.delete("mq:idempotent:DemoMessage:msg-1")).thenReturn(true);
        IdempotentMessageInterceptor interceptor = newInterceptor(template);

        DemoMessage message = new DemoMessage();
        message.setMessageId("msg-1");

        interceptor.consumeMessageError(message, new IllegalStateException("consume failed"));

        // 消费失败删除幂等标记，允许重试
        verify(template).delete("mq:idempotent:DemoMessage:msg-1");
    }

    @Test
    void consumeMessageErrorSkipsWhenMessageIdBlank() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        IdempotentMessageInterceptor interceptor = newInterceptor(template);

        DemoMessage message = new DemoMessage();
        message.setMessageId("");
        interceptor.consumeMessageError(message, new IllegalStateException("boom"));

        verifyNoInteractions(template);
    }

    @Test
    void alreadyConsumedExceptionDefaultsUnknownStatus() {
        IdempotentMessageInterceptor.MessageAlreadyConsumedException ex =
                new IdempotentMessageInterceptor.MessageAlreadyConsumedException("msg-1", null);
        assertThat(ex.getStatus()).isEqualTo("unknown");
        assertThat(ex.getMessageId()).isEqualTo("msg-1");
        assertThat(ex.getMessage()).contains("msg-1");
    }
}
