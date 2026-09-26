package xyz.migoo.framework.web.core.store;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.Collections;

/**
 * Redis 计数/状态存储
 * <p>
 * 多实例部署时共享限流计数、登录失败计数与锁定状态。
 * 由 {@code MiGooWebRedisStateStoreAutoConfiguration} 在检测到 Redis 时自动装配，
 * 替代默认的 {@link InMemoryStateStore}。
 *
 * @author xiaomi
 */
public class RedisStateStore implements StateStore {

    /**
     * 递增 + 每次刷新过期时间 Lua 脚本
     * <p>
     * KEYS[1]: key；ARGV[1]: 过期秒数；返回: 递增后的值
     */
    private static final String INCR_REFRESH_TTL_SCRIPT = """
            local v = redis.call('INCR', KEYS[1])
            redis.call('EXPIRE', KEYS[1], ARGV[1])
            return v
            """;

    private static final DefaultRedisScript<Long> INCR_SCRIPT =
            new DefaultRedisScript<>(INCR_REFRESH_TTL_SCRIPT, Long.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisStateStore(RedisConnectionFactory connectionFactory) {
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.string());
        template.setStringSerializer(RedisSerializer.string());
        template.afterPropertiesSet();
        this.redisTemplate = template;
    }

    @Override
    public long increment(String key, Duration ttl) {
        return redisTemplate.execute(INCR_SCRIPT, Collections.singletonList(key),
                String.valueOf(Math.max(ttl.toMillis() / 1000, 1)));
    }

    @Override
    public long get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return 0;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void put(String key, long value, Duration ttl) {
        redisTemplate.opsForValue().set(key, String.valueOf(value), ttl);
    }

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        // SET key 1 NX EX ttl：原子占位（多实例共享防重窗口）
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", ttl));
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
