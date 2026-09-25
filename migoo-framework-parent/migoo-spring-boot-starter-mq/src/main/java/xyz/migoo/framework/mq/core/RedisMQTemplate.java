package xyz.migoo.framework.mq.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.common.observability.MqMessageSentEvent;
import xyz.migoo.framework.mq.core.interceptor.RedisMessageInterceptor;
import xyz.migoo.framework.mq.core.message.AbstractMessage;
import xyz.migoo.framework.mq.core.pubsub.AbstractChannelMessage;
import xyz.migoo.framework.mq.core.stream.AbstractStreamMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Redis MQ 操作模板类
 *
 */
@Slf4j
public class RedisMQTemplate {

    @Getter
    private final RedisTemplate<String, ?> redisTemplate;
    /**
     * 拦截器数组
     */
    @Getter
    private final List<RedisMessageInterceptor> interceptors = new CopyOnWriteArrayList<>();

    /**
     * 事件发布器（可观测性信号，可空：未注入时不发布）
     */
    private ApplicationEventPublisher eventPublisher;

    public RedisMQTemplate(RedisTemplate<String, ?> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 注入事件发布器（可观测性信号）
     *
     * @param eventPublisher Spring 事件发布器
     */
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 发送 Redis 消息，基于 Redis pub/sub 实现
     *
     * @param message 消息
     */
    public <T extends AbstractChannelMessage> void send(T message) {
        try {
            sendMessageBefore(message);
            // 发送消息
            redisTemplate.convertAndSend(message.getChannel(), JsonUtils.toJsonString(message));
            publishEvent(new MqMessageSentEvent(message.getChannel()));
        } catch (Exception e) {
            log.error("[send][发送Channel消息失败] channel={}, messageId={}", message.getChannel(), message.getMessageId(), e);
            sendMessageError(message, e);
            throw e;
        } finally {
            sendMessageAfter(message);
        }
    }

    /**
     * 发送 Redis 消息，基于 Redis Stream 实现
     *
     * @param message 消息
     * @return 消息记录的编号对象
     */
    public <T extends AbstractStreamMessage> RecordId send(T message) {
        try {
            sendMessageBefore(message);
            // 发送消息
            RecordId recordId = redisTemplate.opsForStream().add(StreamRecords.newRecord()
                    .ofObject(JsonUtils.toJsonString(message)) // 设置内容
                    .withStreamKey(message.getChannel())); // 设置 stream key
            publishEvent(new MqMessageSentEvent(message.getChannel()));
            return recordId;
        } catch (Exception e) {
            log.error("[send][发送Stream消息失败] stream={}, messageId={}", message.getChannel(), message.getMessageId(), e);
            sendMessageError(message, e);
            throw e;
        } finally {
            sendMessageAfter(message);
        }
    }

    /**
     * 发布可观测性信号事件（观测不得影响业务：发布异常只记日志）
     *
     * @param event common 中的信号事件 record
     */
    private void publishEvent(Object event) {
        if (eventPublisher == null) {
            return;
        }
        try {
            eventPublisher.publishEvent(event);
        } catch (Exception ex) {
            log.warn("[publishEvent][发布可观测性信号事件失败] event({})", event, ex);
        }
    }

    /**
     * 添加拦截器
     *
     * @param interceptor 拦截器
     */
    public void addInterceptor(RedisMessageInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    private void sendMessageBefore(AbstractMessage message) {
        // 正序
        interceptors.forEach(interceptor -> interceptor.sendMessageBefore(message));
    }

    private void sendMessageAfter(AbstractMessage message) {
        // 倒序
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).sendMessageAfter(message);
        }
    }

    private void sendMessageError(AbstractMessage message, Throwable throwable) {
        // 倒序
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).sendMessageError(message, throwable);
        }
    }

}