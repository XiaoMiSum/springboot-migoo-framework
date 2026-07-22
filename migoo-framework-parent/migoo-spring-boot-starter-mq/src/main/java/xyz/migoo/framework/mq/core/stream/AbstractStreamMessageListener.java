package xyz.migoo.framework.mq.core.stream;

import xyz.migoo.framework.common.util.type.TypeUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.mq.config.MQProperties;
import xyz.migoo.framework.mq.core.RedisMQTemplate;
import xyz.migoo.framework.mq.core.interceptor.IdempotentMessageInterceptor;
import xyz.migoo.framework.mq.core.interceptor.RedisMessageInterceptorUtils;
import xyz.migoo.framework.mq.core.message.AbstractMessage;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Redis Stream 消息监听器抽象基类
 * <p>
 * 支持消费者组、ACK确认、异常处理和死信队列
 *
 * @param <T> 消息类型
 */
@Slf4j
public abstract class AbstractStreamMessageListener<T extends AbstractStreamMessage> implements StreamListener<String, ObjectRecord<String, String>> {

    /**
     * 死信队列 Key 后缀
     */
    private static final String DEAD_LETTER_SUFFIX = ":dead_letter";

    /**
     * 消息类型
     */
    private final Class<T> messageType;

    /**
     * Redis Stream Key
     */
    @Getter
    private final String streamKey;

    /**
     * 消费者分组名称
     */
    @Getter
    private final String group;

    /**
     * 最大重试次数
     */
    @Getter
    private final int maxRetry;

    /**
     * 是否启用死信队列
     */
    @Getter
    private final boolean deadLetterEnabled;

    /**
     * 消费成功后是否删除消息
     */
    @Getter
    private final boolean deleteAfterAck;

    /**
     * RedisMQTemplate
     */
    @Setter
    private RedisMQTemplate redisMQTemplate;

    /**
     * RedisTemplate
     */
    @Setter
    private RedisTemplate<String, ?> redisTemplate;

    protected AbstractStreamMessageListener(MQProperties properties) {
        this.messageType = getMessageClass();
        // 直接使用类名作为 Stream Key，无需反射创建临时对象
        this.streamKey = messageType.getSimpleName();
        this.group = properties.getGroup();
        this.maxRetry = properties.getMaxRetry();
        this.deadLetterEnabled = properties.getDeadLetterEnabled();
        this.deleteAfterAck = properties.getDeleteAfterAck();
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        // 消费消息
        T messageObj = JsonUtils.parseObject(message.getValue(), messageType);
        Objects.requireNonNull(messageObj, "解析消息失败，消息内容为空");

        // 获取重试次数
        int retryCount = getRetryCount(messageObj);

        try {
            consumeMessageBefore(messageObj);
            this.onMessage(messageObj);

            // ack 消息消费完成
            redisTemplate.opsForStream().acknowledge(group, message);
            log.debug("[onMessage][消费Stream消息成功] stream={}, messageId={}, recordId={}",
                    streamKey, messageObj.getMessageId(), message.getId());
            // 根据配置决定是否删除消息
            if (deleteAfterAck) {
                redisTemplate.opsForStream().delete(message);
            }
        } catch (IdempotentMessageInterceptor.MessageAlreadyConsumedException e) {
            // 消息已被消费，直接 ACK 并跳过
            redisTemplate.opsForStream().acknowledge(group, message);
            if (deleteAfterAck) {
                redisTemplate.opsForStream().delete(message);
            }
            log.info("[onMessage][消息已被其他消费者处理，跳过] stream={}, messageId={}, status={}",
                    streamKey, messageObj.getMessageId(), e.getStatus());
        } catch (Exception e) {
            log.error("[onMessage][消费Stream消息失败] stream={}, messageId={}, recordId={}, retryCount={}/{}",
                    streamKey, messageObj.getMessageId(), message.getId(), retryCount, maxRetry, e);
            consumeMessageError(messageObj, e);

            // 处理消费失败
            handleConsumeError(message, messageObj, e, retryCount);
        } finally {
            consumeMessageAfter(messageObj);
        }
    }

    /**
     * 处理消息
     *
     * @param message 消息
     */
    public abstract void onMessage(T message);

    /**
     * 获取消息的重试次数
     *
     * @param message 消息
     * @return 重试次数
     */
    private int getRetryCount(T message) {
        String retryHeader = message.getHeader("retry-count");
        if (retryHeader == null) {
            return 0;
        }
        try {
            return Integer.parseInt(retryHeader);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 处理消费错误
     *
     * @param message    Redis 消息记录
     * @param messageObj 消息对象
     * @param e          异常
     * @param retryCount 当前重试次数
     */
    private void handleConsumeError(ObjectRecord<String, String> message, T messageObj, Exception e, int retryCount) {
        // ACK 消息，避免重复消费
        redisTemplate.opsForStream().acknowledge(group, message);

        if (retryCount < maxRetry) {
            // 重试：增加重试次数并重新发送到 Stream
            messageObj.addHeader("retry-count", String.valueOf(retryCount + 1));
            try {
                redisTemplate.opsForStream().add(StreamRecords.newRecord()
                        .ofObject(JsonUtils.toJsonString(messageObj))
                        .withStreamKey(streamKey));
                log.info("[handleConsumeError][消息重试] stream={}, messageId={}, retryCount={}",
                        streamKey, messageObj.getMessageId(), retryCount + 1);
            } catch (Exception ex) {
                log.error("[handleConsumeError][消息重试失败] stream={}, messageId={}",
                        streamKey, messageObj.getMessageId(), ex);
            }
        } else if (deadLetterEnabled) {
            // 超过最大重试次数，发送到死信队列
            sendToDeadLetterQueue(messageObj, e);
        }

        // 删除原消息
        redisTemplate.opsForStream().delete(message);
    }

    /**
     * 发送消息到死信队列
     *
     * @param messageObj 消息对象
     * @param e          异常
     */
    private void sendToDeadLetterQueue(T messageObj, Exception e) {
        String deadLetterKey = streamKey + DEAD_LETTER_SUFFIX;
        try {
            // 添加错误信息到 headers
            messageObj.addHeader("error-message", e.getMessage());
            messageObj.addHeader("error-time", String.valueOf(System.currentTimeMillis()));

            redisTemplate.opsForStream().add(StreamRecords.newRecord().ofObject(JsonUtils.toJsonString(messageObj)).withStreamKey(deadLetterKey));
            log.warn("[sendToDeadLetterQueue][消息已发送到死信队列] stream={}, messageId={}, deadLetterKey={}",
                    streamKey, messageObj.getMessageId(), deadLetterKey);
        } catch (Exception ex) {
            log.error("[sendToDeadLetterQueue][发送到死信队列失败] stream={}, messageId={}, deadLetterKey={}",
                    streamKey, messageObj.getMessageId(), deadLetterKey, ex);
        }
    }

    /**
     * 通过解析类上的泛型，获得消息类型
     *
     * @return 消息类型
     */
    @SuppressWarnings("unchecked")
    private Class<T> getMessageClass() {
        Type type = TypeUtils.getTypeArgument(getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
        }
        return (Class<T>) type;
    }

    private void consumeMessageBefore(AbstractMessage message) {
        Objects.requireNonNull(redisMQTemplate, "RedisMQTemplate 未注入，请检查 MQAutoConfiguration 配置");
        RedisMessageInterceptorUtils.consumeMessageBefore(redisMQTemplate.getInterceptors(), message);
    }

    private void consumeMessageAfter(AbstractMessage message) {
        Objects.requireNonNull(redisMQTemplate, "RedisMQTemplate 未注入，请检查 MQAutoConfiguration 配置");
        RedisMessageInterceptorUtils.consumeMessageAfter(redisMQTemplate.getInterceptors(), message);
    }

    private void consumeMessageError(AbstractMessage message, Throwable throwable) {
        Objects.requireNonNull(redisMQTemplate, "RedisMQTemplate 未注入，请检查 MQAutoConfiguration 配置");
        RedisMessageInterceptorUtils.consumeMessageError(redisMQTemplate.getInterceptors(), message, throwable);
    }
}
