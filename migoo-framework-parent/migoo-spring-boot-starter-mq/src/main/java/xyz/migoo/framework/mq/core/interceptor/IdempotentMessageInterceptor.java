package xyz.migoo.framework.mq.core.interceptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import xyz.migoo.framework.mq.core.message.AbstractMessage;

import java.time.Duration;
import java.util.Objects;

/**
 * 消息幂等性拦截器
 * <p>
 * 基于 Redis Lua 脚本实现分布式环境下的消息去重，确保同一消息只被处理一次
 * <p>
 * 工作原理：
 * 1. 消费前：通过 Lua 脚本原子执行 SETNX + GET，成功则继续消费，失败则跳过
 * 2. 消费后：设置过期时间，防止 Redis 内存无限增长
 */
@Slf4j
@RequiredArgsConstructor
public class IdempotentMessageInterceptor implements RedisMessageInterceptor {

    /**
     * 幂等 Key 前缀
     */
    private static final String IDEMPOTENT_KEY_PREFIX = "mq:idempotent:";

    /**
     * 消费中状态标记
     */
    private static final String STATUS_PROCESSING = "processing";

    /**
     * 消费完成状态标记
     */
    private static final String STATUS_CONSUMED = "consumed";

    /**
     * Lua 脚本：原子 SETNX + GET
     * <p>
     * KEYS[1] = 幂等键
     * ARGV[1] = 状态值
     * ARGV[2] = 过期时间（秒）
     * <p>
     * 返回值：
     * - "ok" = SETNX 成功（首次消费）
     * - "skip" = 键已存在（重复消费），返回当前状态
     */
    private static final String IDEMPOTENT_LUA_SCRIPT = """
            local key = KEYS[1]
            local value = ARGV[1]
            local ttl = tonumber(ARGV[2])
            local current = redis.call('GET', key)
            if current then
                return current
            end
            redis.call('SETEX', key, ttl, value)
            return 'ok'
            """;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 幂等键过期时间（默认24小时）
     */
    private final Duration expireTime;

    /**
     * 消息消费前拦截
     * <p>
     * 使用 Lua 脚本原子操作检查消息是否已被处理，消除 TOCTOU 竞态
     *
     * @param message 消息
     * @throws MessageAlreadyConsumedException 如果消息已被消费
     */
    @Override
    public void consumeMessageBefore(AbstractMessage message) {
        String messageId = message.getMessageId();
        if (messageId == null || messageId.isEmpty()) {
            log.warn("[consumeMessageBefore][消息缺少 messageId，跳过幂等检查] message={}", message);
            return;
        }

        String idempotentKey = buildIdempotentKey(message);
        long ttlSeconds = expireTime.getSeconds();

        // Lua 脚本原子操作：SETNX + GET，消除 TOCTOU 竞态
        String result = stringRedisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(IDEMPOTENT_LUA_SCRIPT, String.class),
                java.util.List.of(idempotentKey),
                STATUS_PROCESSING,
                String.valueOf(ttlSeconds));

        if (!"ok".equals(result)) {
            log.info("[consumeMessageBefore][消息已被处理，跳过消费] messageId={}, status={}, channel={}",
                    messageId, result, message.getChannel());
            throw new MessageAlreadyConsumedException(messageId, result);
        }

        log.debug("[consumeMessageBefore][消息幂等检查通过] messageId={}, channel={}",
                messageId, message.getChannel());
    }

    /**
     * 消息消费后拦截
     * <p>
     * 更新状态为已消费，并刷新过期时间
     *
     * @param message 消息
     */
    @Override
    public void consumeMessageAfter(AbstractMessage message) {
        String messageId = message.getMessageId();
        if (messageId == null || messageId.isEmpty()) {
            return;
        }

        String idempotentKey = buildIdempotentKey(message);

        // 更新为已消费状态，并刷新过期时间
        stringRedisTemplate.opsForValue().set(idempotentKey, STATUS_CONSUMED, expireTime);
        log.debug("[consumeMessageAfter][消息消费完成，标记为已消费] messageId={}, channel={}",
                messageId, message.getChannel());
    }

    /**
     * 消息消费异常拦截
     * <p>
     * 消费失败时删除幂等标记，允许重试
     *
     * @param message   消息
     * @param throwable 异常
     */
    @Override
    public void consumeMessageError(AbstractMessage message, Throwable throwable) {
        // 如果是幂等异常，不需要删除标记
        if (throwable instanceof MessageAlreadyConsumedException) {
            return;
        }

        String messageId = message.getMessageId();
        if (messageId == null || messageId.isEmpty()) {
            return;
        }

        String idempotentKey = buildIdempotentKey(message);

        // 消费失败，删除幂等标记，允许重试
        Boolean deleted = stringRedisTemplate.delete(idempotentKey);
        log.debug("[consumeMessageError][消息消费失败，删除幂等标记以允许重试] messageId={}, deleted={}, channel={}",
                messageId, deleted, message.getChannel());
    }

    /**
     * 构建幂等 Key
     *
     * @param message 消息
     * @return 幂等 Key
     */
    private String buildIdempotentKey(AbstractMessage message) {
        return IDEMPOTENT_KEY_PREFIX + message.getChannel() + ":" + message.getMessageId();
    }

    /**
     * 消息已被消费异常
     * <p>
     * 用于在拦截器中标识消息已被处理，监听器需要捕获此异常并跳过消费
     */
    @Getter
    public static class MessageAlreadyConsumedException extends RuntimeException {

        private final String messageId;
        private final String status;

        public MessageAlreadyConsumedException(String messageId, String status) {
            super(String.format("消息已被处理: messageId=%s, status=%s", messageId, status));
            this.messageId = messageId;
            this.status = Objects.requireNonNullElse(status, "unknown");
        }

    }
}
