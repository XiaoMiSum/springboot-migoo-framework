package xyz.migoo.framework.mq.core.stream;

import xyz.migoo.framework.common.util.TypeUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.mq.core.RedisMQTemplate;
import xyz.migoo.framework.mq.core.interceptor.IdempotentMessageInterceptor;
import xyz.migoo.framework.mq.core.interceptor.RedisMessageInterceptor;
import xyz.migoo.framework.mq.core.message.AbstractMessage;

import java.lang.reflect.Type;
import java.util.List;
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
     * Redis Channel
     */
    @Getter
    private final String streamKey;

    /**
     * Redis 消费者分组，默认使用 migoo.mq.group 名字
     */
    @Value("${migoo.mq.group:def_group}")
    @Getter
    private String group;

    /**
     * 最大重试次数
     */
    @Value("${migoo.mq.max-retry:3}")
    @Getter
    private int maxRetry = 3;

    /**
     * 是否启用死信队列
     */
    @Value("${migoo.mq.dead-letter-enabled:true}")
    @Getter
    private boolean deadLetterEnabled = true;

    /**
     * RedisMQTemplate
     */
    @Setter
    private RedisMQTemplate redisMQTemplate;
    /**
     *
     */
    @Setter
    private RedisTemplate<String, ?> redisTemplate;

    @SneakyThrows
    protected AbstractStreamMessageListener() {
        this.messageType = getMessageClass();
        this.streamKey = messageType.getConstructor().newInstance().getChannel();
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
            // 消费成功后删除消息（可选，保留一段时间用于追踪）
            redisTemplate.opsForStream().delete(message);
        } catch (IdempotentMessageInterceptor.MessageAlreadyConsumedException e) {
            // 消息已被消费，直接 ACK 并跳过
            redisTemplate.opsForStream().acknowledge(group, message);
            redisTemplate.opsForStream().delete(message);
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
        List<RedisMessageInterceptor> interceptors = redisMQTemplate.getInterceptors();
        // 正序
        interceptors.forEach(interceptor -> interceptor.consumeMessageBefore(message));
    }

    private void consumeMessageAfter(AbstractMessage message) {
        Objects.requireNonNull(redisMQTemplate, "RedisMQTemplate 未注入，请检查 MQAutoConfiguration 配置");
        List<RedisMessageInterceptor> interceptors = redisMQTemplate.getInterceptors();
        // 倒序
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).consumeMessageAfter(message);
        }
    }

    private void consumeMessageError(AbstractMessage message, Throwable throwable) {
        Objects.requireNonNull(redisMQTemplate, "RedisMQTemplate 未注入，请检查 MQAutoConfiguration 配置");
        List<RedisMessageInterceptor> interceptors = redisMQTemplate.getInterceptors();
        // 倒序
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).consumeMessageError(message, throwable);
        }
    }

}
