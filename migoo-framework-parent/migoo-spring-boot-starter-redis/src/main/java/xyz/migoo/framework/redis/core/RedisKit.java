package xyz.migoo.framework.redis.core;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import xyz.migoo.framework.common.util.json.JsonUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisKit {

    /**
     * 释放锁的 Lua 脚本
     * KEYS[1]: 锁的 key
     * ARGV[1]: 锁的值（标识）
     * 返回: 1-释放成功, 0-释放失败
     */
    private static final String UNLOCK_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;

    // ==================== Lua 脚本 ====================
    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== String 操作 ====================

    /**
     * 获取缓存值
     * <p>
     * 支持泛型类型，包括 String、Integer、Boolean 等简单类型，以及 List&lt;T&gt;、Map&lt;K, V&gt; 等复杂类型
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @param <V>  值类型
     * @return 缓存值，如果不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <V> V get(RedisKeyDefine<V> key, Object... args) {
        Object value = redisTemplate.opsForValue().get(key.formatKey(args));
        if (value == null) {
            return null;
        }

        // 获取目标类型
        Class<?> targetClass = key.getValueType().getType().getClass();

        // 如果值已经是目标类型，直接返回
        if (targetClass.isInstance(value)) {
            return (V) value;
        }

        // 如果是 String 类型，尝试使用 TypeReference 反序列化
        if (value instanceof String strValue) {
            // 简单类型直接转换（避免 JSON 解析开销）
            if (targetClass == String.class) {
                return (V) strValue;
            }
            // 其他类型通过 JSON 反序列化
            return key.parse(strValue);
        }

        // 其他类型通过 Jackson 转换
        return JsonUtils.convert(value, key.getValueType());
    }

    /**
     * 设置缓存值
     * <p>
     * 根据 RedisKeyDefine 中的过期类型处理：
     * <ul>
     *   <li>PERMANENT：不设置过期时间</li>
     *   <li>FIXED：仅当 key 不存在时设置过期时间</li>
     *   <li>DYNAMIC：每次设置都重新计算过期时间</li>
     * </ul>
     *
     * @param key   RedisKeyDefine 定义
     * @param value 缓存值
     * @param args  模板参数
     * @param <V>   值类型
     */
    public <V> void set(RedisKeyDefine<V> key, V value, Object... args) {
        var formattedKey = key.formatKey(args);
        if (!key.hasTimeout()) {
            // 永久有效，不设置过期时间
            redisTemplate.opsForValue().set(formattedKey, value);
            return;
        }

        if (key.isFixedTimeout()) {
            // 固定过期时间：仅当 key 不存在时才设置过期时间
            var exists = redisTemplate.hasKey(formattedKey);
            redisTemplate.opsForValue().set(formattedKey, value);
            if (!Boolean.TRUE.equals(exists)) {
                redisTemplate.expire(formattedKey, key.getTimeout());
            }
            return;
        }
        // 动态过期时间：每次设置都重新计算过期时间
        redisTemplate.opsForValue().set(formattedKey, value, key.getTimeout());
    }

    /**
     * 仅在 key 不存在时设置值
     *
     * @param key   RedisKeyDefine 定义
     * @param value 缓存值
     * @param args  模板参数
     * @param <V>   值类型
     * @return true-设置成功, false-key已存在
     */
    public <V> boolean setIfAbsent(RedisKeyDefine<V> key, V value, Object... args) {
        String formattedKey = key.formatKey(args);
        Boolean success;
        if (key.hasTimeout()) {
            success = redisTemplate.opsForValue().setIfAbsent(formattedKey, value, key.getTimeout());
        } else {
            success = redisTemplate.opsForValue().setIfAbsent(formattedKey, value);
        }
        return Boolean.TRUE.equals(success);
    }

    /**
     * 仅在 key 不存在时设置值（带自定义过期时间）
     *
     * @param key     RedisKeyDefine 定义
     * @param value   缓存值
     * @param timeout 过期时间
     * @param args    模板参数
     * @param <V>     值类型
     * @return true-设置成功, false-key已存在
     */
    public <V> boolean setIfAbsent(RedisKeyDefine<V> key, V value, Duration timeout, Object... args) {
        String formattedKey = key.formatKey(args);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(formattedKey, value, timeout);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 删除缓存
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     */
    public void delete(RedisKeyDefine<?> key, Object... args) {
        redisTemplate.delete(key.formatKey(args));
    }

    /**
     * 检查缓存是否存在
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @return 是否存在
     */
    public boolean hasKey(RedisKeyDefine<?> key, Object... args) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key.formatKey(args)));
    }

    /**
     * 设置过期时间
     *
     * @param key     RedisKeyDefine 定义
     * @param timeout 超时时间（毫秒）
     * @param args    模板参数
     * @return 是否设置成功
     */
    public boolean expire(RedisKeyDefine<?> key, long timeout, Object... args) {
        return Boolean.TRUE.equals(redisTemplate.expire(key.formatKey(args), timeout, TimeUnit.MILLISECONDS));
    }

    // ==================== 原子计数操作 ====================

    /**
     * 原子递增（+1）
     * <p>
     * 如果 key 不存在，初始化为 0 后再递增
     * 如果 RedisKeyDefine 设置了过期时间，会根据过期类型自动处理：
     * <ul>
     *   <li>FIXED：仅在 key 不存在或已过期时设置过期时间，后续操作不修改</li>
     *   <li>DYNAMIC：每次都重新设置过期时间</li>
     * </ul>
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @return 递增后的值
     */
    public long increment(RedisKeyDefine<?> key, Object... args) {
        String formattedKey = key.formatKey(args);
            
        if (!key.hasTimeout()) {
            Long value = redisTemplate.opsForValue().increment(formattedKey);
            return value != null ? value : 0;
        }
            
        // 有过期时间，先执行原子递增
        Long value = redisTemplate.opsForValue().increment(formattedKey);
        long result = value != null ? value : 0;
            
        // 根据过期类型设置过期时间
        if (key.isFixedTimeout()) {
            // FIXED 模式：仅在 key 刚创建时设置过期时间
            Long ttl = redisTemplate.getExpire(formattedKey);
            if (ttl != null && ttl == -2) {
                redisTemplate.expire(formattedKey, key.getTimeout());
            }
        } else {
            // DYNAMIC 模式：每次都重新设置过期时间
            redisTemplate.expire(formattedKey, key.getTimeout());
        }
            
        return result;
    }

    /**
     * 原子递增（指定增量）
     * <p>
     * 如果 key 不存在，初始化为 0 后再递增
     * 如果 RedisKeyDefine 设置了过期时间，会根据过期类型自动处理：
     * <ul>
     *   <li>FIXED：仅在 key 不存在或已过期时设置过期时间，后续操作不修改</li>
     *   <li>DYNAMIC：每次都重新设置过期时间</li>
     * </ul>
     *
     * @param key   RedisKeyDefine 定义
     * @param delta 递增值
     * @param args  模板参数
     * @return 递增后的值
     */
    public Double increment(RedisKeyDefine<?> key, Double delta, Object... args) {
        String formattedKey = key.formatKey(args);
            
        if (!key.hasTimeout()) {
            Double value = redisTemplate.opsForValue().increment(formattedKey, delta);
            return value != null ? value : 0;
        }
            
        // 有过期时间，先执行原子递增
        Double value = redisTemplate.opsForValue().increment(formattedKey, delta);
        double result = value != null ? value : 0;
            
        // 根据过期类型设置过期时间
        if (key.isFixedTimeout()) {
            // FIXED 模式：仅在 key 刚创建时设置过期时间
            Long ttl = redisTemplate.getExpire(formattedKey);
            if (ttl != null && ttl == -2) {
                redisTemplate.expire(formattedKey, key.getTimeout());
            }
        } else {
            // DYNAMIC 模式：每次都重新设置过期时间
            redisTemplate.expire(formattedKey, key.getTimeout());
        }
            
        return result;
    }

    /**
     * 原子递增（指定增量）
     * <p>
     * 如果 key 不存在，初始化为 0 后再递增
     * 如果 RedisKeyDefine 设置了过期时间，会根据过期类型自动处理：
     * <ul>
     *   <li>FIXED：仅在 key 不存在或已过期时设置过期时间，后续操作不修改</li>
     *   <li>DYNAMIC：每次都重新设置过期时间</li>
     * </ul>
     *
     * @param key   RedisKeyDefine 定义
     * @param delta 递增值
     * @param args  模板参数
     * @return 递增后的值
     */
    public Long increment(RedisKeyDefine<?> key, Long delta, Object... args) {
        String formattedKey = key.formatKey(args);
            
        if (!key.hasTimeout()) {
            Long value = redisTemplate.opsForValue().increment(formattedKey, delta);
            return value != null ? value : 0;
        }
            
        // 有过期时间，先执行原子递增
        Long value = redisTemplate.opsForValue().increment(formattedKey, delta);
        long result = value != null ? value : 0;
            
        // 根据过期类型设置过期时间
        if (key.isFixedTimeout()) {
            // FIXED 模式：仅在 key 刚创建时设置过期时间
            Long ttl = redisTemplate.getExpire(formattedKey);
            if (ttl != null && ttl == -2) {
                redisTemplate.expire(formattedKey, key.getTimeout());
            }
        } else {
            // DYNAMIC 模式：每次都重新设置过期时间
            redisTemplate.expire(formattedKey, key.getTimeout());
        }
            
        return result;
    }

    /**
     * 原子递减（指定减量）
     * <p>
     * 如果 key 不存在，初始化为 0 后再递减
     * 如果 RedisKeyDefine 设置了过期时间，会根据过期类型自动处理：
     * <ul>
     *   <li>FIXED：仅在 key 不存在或已过期时设置过期时间，后续操作不修改</li>
     *   <li>DYNAMIC：每次都重新设置过期时间</li>
     * </ul>
     *
     * @param key   RedisKeyDefine 定义
     * @param delta 减少值
     * @param args  模板参数
     * @return 递减后的值
     */
    public long decrement(RedisKeyDefine<?> key, Long delta, Object... args) {
        String formattedKey = key.formatKey(args);
            
        if (!key.hasTimeout()) {
            Long value = redisTemplate.opsForValue().decrement(formattedKey, delta);
            return value != null ? value : 0;
        }
            
        // 有过期时间，先执行原子递减
        Long value = redisTemplate.opsForValue().decrement(formattedKey, delta);
        long result = value != null ? value : 0;
            
        // 根据过期类型设置过期时间
        if (key.isFixedTimeout()) {
            // FIXED 模式：仅在 key 刚创建时设置过期时间
            Long ttl = redisTemplate.getExpire(formattedKey);
            if (ttl != null && ttl == -2) {
                redisTemplate.expire(formattedKey, key.getTimeout());
            }
        } else {
            // DYNAMIC 模式：每次都重新设置过期时间
            redisTemplate.expire(formattedKey, key.getTimeout());
        }
            
        return result;
    }

    // ==================== 分布式锁操作 ====================

    /**
     * 尝试获取分布式锁
     * <p>
     * 使用 Redis SET NX EX 命令实现
     *
     * @param lockKey   锁的 key
     * @param lockValue 锁的值（建议使用 UUID）
     * @param timeout   锁的过期时间
     * @return true-获取成功, false-获取失败
     */
    public boolean tryLock(String lockKey, String lockValue, Duration timeout) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 尝试获取分布式锁（使用 RedisKeyDefine）
     *
     * @param lockKey   锁的 key 定义
     * @param lockValue 锁的值（建议使用 UUID）
     * @param args      模板参数
     * @return true-获取成功, false-获取失败
     */
    public boolean tryLock(RedisKeyDefine<?> lockKey, String lockValue, Object... args) {
        String formattedKey = lockKey.formatKey(args);
        Duration timeout = lockKey.hasTimeout() ? lockKey.getTimeout() : Duration.ofSeconds(30);
        return tryLock(formattedKey, lockValue, timeout);
    }

    /**
     * 释放分布式锁
     * <p>
     * 使用 Lua 脚本确保原子性：只有锁的持有者才能释放锁
     *
     * @param lockKey   锁的 key
     * @param lockValue 锁的值（必须与获取时一致）
     * @return true-释放成功, false-释放失败（锁不存在或不是当前线程持有）
     */
    public boolean unlock(String lockKey, String lockValue) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(UNLOCK_SCRIPT);
        script.setResultType(Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);
        return Long.valueOf(1L).equals(result);
    }

    /**
     * 释放分布式锁（使用 RedisKeyDefine）
     *
     * @param lockKey   锁的 key 定义
     * @param lockValue 锁的值（必须与获取时一致）
     * @param args      模板参数
     * @return true-释放成功, false-释放失败
     */
    public boolean unlock(RedisKeyDefine<?> lockKey, String lockValue, Object... args) {
        return unlock(lockKey.formatKey(args), lockValue);
    }


    // ==================== Sorted Set 操作 ====================

    /**
     * 向有序集合添加成员
     *
     * @param key    RedisKeyDefine 定义
     * @param member 成员
     * @param score  分数
     * @param args   模板参数
     * @return true-添加成功, false-成员已存在且分数未改变
     */
    public boolean zadd(RedisKeyDefine<?> key, Object member, double score, Object... args) {
        String formattedKey = key.formatKey(args);
        Boolean added = redisTemplate.opsForZSet().add(formattedKey, member, score);
        return Boolean.TRUE.equals(added);
    }

    /**
     * 从有序集合移除成员
     *
     * @param key    RedisKeyDefine 定义
     * @param member 成员
     * @param args   模板参数
     * @return 移除的成员数量
     */
    public long zrem(RedisKeyDefine<?> key, Object member, Object... args) {
        String formattedKey = key.formatKey(args);
        Long removed = redisTemplate.opsForZSet().remove(formattedKey, member);
        return removed != null ? removed : 0;
    }

    /**
     * 从有序集合移除多个成员
     *
     * @param key     RedisKeyDefine 定义
     * @param members 成员数组
     * @param args    模板参数
     * @return 移除的成员数量
     */
    public long zrem(RedisKeyDefine<?> key, Object[] members, Object... args) {
        String formattedKey = key.formatKey(args);
        Long removed = redisTemplate.opsForZSet().remove(formattedKey, members);
        return removed != null ? removed : 0;
    }

    /**
     * 获取有序集合的成员排名（从低到高，0开始）
     *
     * @param key    RedisKeyDefine 定义
     * @param member 成员
     * @param args   模板参数
     * @return 排名，如果不存在返回 null
     */
    public Long zrank(RedisKeyDefine<?> key, Object member, Object... args) {
        String formattedKey = key.formatKey(args);
        return redisTemplate.opsForZSet().rank(formattedKey, member);
    }

    /**
     * 获取有序集合的成员排名（从高到低，0开始）
     *
     * @param key    RedisKeyDefine 定义
     * @param member 成员
     * @param args   模板参数
     * @return 排名，如果不存在返回 null
     */
    public Long zrevrank(RedisKeyDefine<?> key, Object member, Object... args) {
        String formattedKey = key.formatKey(args);
        return redisTemplate.opsForZSet().reverseRank(formattedKey, member);
    }

    /**
     * 获取有序集合指定范围的成员（从低到高）
     *
     * @param key   RedisKeyDefine 定义
     * @param start 开始位置（0开始）
     * @param end   结束位置（-1表示到最后）
     * @param args  模板参数
     * @return 成员集合
     */
    public Set<Object> zrange(RedisKeyDefine<?> key, long start, long end, Object... args) {
        String formattedKey = key.formatKey(args);
        return redisTemplate.opsForZSet().range(formattedKey, start, end);
    }

    /**
     * 获取有序集合指定范围的成员（从高到低）
     *
     * @param key   RedisKeyDefine 定义
     * @param start 开始位置（0开始）
     * @param end   结束位置（-1表示到最后）
     * @param args  模板参数
     * @return 成员集合
     */
    public Set<Object> zrevrange(RedisKeyDefine<?> key, long start, long end, Object... args) {
        String formattedKey = key.formatKey(args);
        return redisTemplate.opsForZSet().reverseRange(formattedKey, start, end);
    }

    /**
     * 获取有序集合的成员数量
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @return 成员数量
     */
    public long zcard(RedisKeyDefine<?> key, Object... args) {
        String formattedKey = key.formatKey(args);
        Long count = redisTemplate.opsForZSet().size(formattedKey);
        return count != null ? count : 0;
    }

    /**
     * 获取有序集合中指定分数范围的成员数量
     *
     * @param key      RedisKeyDefine 定义
     * @param minScore 最小分数
     * @param maxScore 最大分数
     * @param args     模板参数
     * @return 成员数量
     */
    public long zcount(RedisKeyDefine<?> key, double minScore, double maxScore, Object... args) {
        String formattedKey = key.formatKey(args);
        Long count = redisTemplate.opsForZSet().count(formattedKey, minScore, maxScore);
        return count != null ? count : 0;
    }

    /**
     * 删除有序集合中指定排名范围的成员
     *
     * @param key   RedisKeyDefine 定义
     * @param start 开始位置
     * @param end   结束位置
     * @param args  模板参数
     * @return 删除的成员数量
     */
    public long zremrangeByRank(RedisKeyDefine<?> key, long start, long end, Object... args) {
        String formattedKey = key.formatKey(args);
        Long removed = redisTemplate.opsForZSet().removeRange(formattedKey, start, end);
        return removed != null ? removed : 0;
    }

    /**
     * 删除有序集合中指定分数范围的成员
     *
     * @param key      RedisKeyDefine 定义
     * @param minScore 最小分数
     * @param maxScore 最大分数
     * @param args     模板参数
     * @return 删除的成员数量
     */
    public long zremrangeByScore(RedisKeyDefine<?> key, double minScore, double maxScore, Object... args) {
        String formattedKey = key.formatKey(args);
        Long removed = redisTemplate.opsForZSet().removeRangeByScore(formattedKey, minScore, maxScore);
        return removed != null ? removed : 0;
    }
}
