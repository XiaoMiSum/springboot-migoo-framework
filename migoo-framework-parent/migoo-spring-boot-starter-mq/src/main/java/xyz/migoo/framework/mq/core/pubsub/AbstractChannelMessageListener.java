package xyz.migoo.framework.mq.core.pubsub;

import xyz.migoo.framework.common.util.TypeUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.mq.core.RedisMQTemplate;
import xyz.migoo.framework.mq.core.interceptor.RedisMessageInterceptor;
import xyz.migoo.framework.mq.core.interceptor.RedisMessageInterceptorUtils;
import xyz.migoo.framework.mq.core.message.AbstractMessage;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Redis Pub/Sub 消息监听器抽象基类
 *
 * @param <T> 消息类型
 */
@Slf4j
public abstract class AbstractChannelMessageListener<T extends AbstractChannelMessage> implements MessageListener {

    /**
     * 消息类型
     */
    private final Class<T> messageType;
    /**
     * Redis Channel
     */
    private final String channel;

    /**
     * RedisMQTemplate
     */
    @Setter
    private RedisMQTemplate redisMQTemplate;

    protected AbstractChannelMessageListener() {
        this.messageType = getMessageClass();
        // 直接使用类名作为 Channel，无需反射创建临时对象
        this.channel = messageType.getSimpleName();
    }

    /**
     * 获得 Sub 订阅的 Redis Channel 通道
     *
     * @return channel
     */
    public final String getChannel() {
        return channel;
    }

    @Override
    public final void onMessage(Message message, byte[] bytes) {
        T messageObj = JsonUtils.parseObject(message.getBody(), messageType);
        Objects.requireNonNull(messageObj, "解析消息失败，消息内容为空");
        try {
            consumeMessageBefore(messageObj);
            // 消费消息
            this.onMessage(messageObj);
            log.debug("[onMessage][消费Channel消息成功] channel={}, messageId={}", channel, messageObj.getMessageId());
        } catch (Exception e) {
            log.error("[onMessage][消费Channel消息失败] channel={}, messageId={}", channel, messageObj.getMessageId(), e);
            consumeMessageError(messageObj, e);
            // Pub/Sub 模式不支持重试，仅记录日志
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
