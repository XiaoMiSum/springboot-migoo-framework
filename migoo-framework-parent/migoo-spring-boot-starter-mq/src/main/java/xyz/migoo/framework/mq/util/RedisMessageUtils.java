package xyz.migoo.framework.mq.util;

import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.mq.core.pubsub.AbstractChannelMessage;
import xyz.migoo.framework.mq.core.stream.AbstractStreamMessage;

/**
 * Redis 消息工具类
 *
 * @deprecated 此工具类绕过了 {@link xyz.migoo.framework.mq.core.RedisMQTemplate} 的拦截器链，
 *             包括幂等性检查等机制会被静默跳过。请使用 {@code RedisMQTemplate.send()} 代替。
 */
@Deprecated(forRemoval = true)
public class RedisMessageUtils {

    /**
     * 发送 Redis 消息，基于 Redis pub/sub 实现
     *
     * @param redisTemplate Redis 操作模板
     * @param message       消息
     * @deprecated 请使用 {@code RedisMQTemplate.send()} 代替，此方法绕过拦截器链
     */
    @Deprecated(forRemoval = true)
    public static <T extends AbstractChannelMessage> void sendChannelMessage(RedisTemplate<?, ?> redisTemplate, T message) {
        redisTemplate.convertAndSend(message.getChannel(), JsonUtils.toJsonString(message));
    }

    /**
     * 发送 Redis 消息，基于 Redis Stream 实现
     *
     * @param redisTemplate Redis 操作模板
     * @param message       消息
     * @return 消息记录的编号对象
     * @deprecated 请使用 {@code RedisMQTemplate.send()} 代替，此方法绕过拦截器链
     */
    @Deprecated(forRemoval = true)
    public static <T extends AbstractStreamMessage> RecordId sendStreamMessage(RedisTemplate<String, ?> redisTemplate, T message) {
        return redisTemplate.opsForStream().add(StreamRecords.newRecord()
                .ofObject(JsonUtils.toJsonString(message))
                .withStreamKey(message.getChannel()));
    }
}
