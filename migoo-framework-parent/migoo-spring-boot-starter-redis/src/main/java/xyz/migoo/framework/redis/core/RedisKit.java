package xyz.migoo.framework.redis.core;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import xyz.migoo.framework.common.util.json.JsonUtils;

import java.time.Duration;
import java.util.*;

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

    /**
     * 递增 + FIXED 过期（仅 key 不存在时设置）Lua 脚本
     * KEYS[1]: key
     * ARGV[1]: 过期秒数
     * 返回: 递增后的值
     */
    private static final String INCR_FIXED_SCRIPT = """
            local existed = redis.call('EXISTS', KEYS[1])
            local v = redis.call('INCR', KEYS[1])
            if existed == 0 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return v
            """;

    /**
     * 递增 + DYNAMIC 过期（每次重设）Lua 脚本
     * KEYS[1]: key
     * ARGV[1]: 过期秒数
     * 返回: 递增后的值
     */
    private static final String INCR_DYNAMIC_SCRIPT = """
            local v = redis.call('INCR', KEYS[1])
            redis.call('EXPIRE', KEYS[1], ARGV[1])
            return v
            """;

    /**
     * 整数递增指定增量 + FIXED 过期 Lua 脚本
     * KEYS[1]: key
     * ARGV[1]: 增量值
     * ARGV[2]: 过期秒数
     * 返回: 递增后的值
     */
    private static final String INCRBY_FIXED_SCRIPT = """
            local existed = redis.call('EXISTS', KEYS[1])
            local v = redis.call('INCRBY', KEYS[1], ARGV[1])
            if existed == 0 then
                redis.call('EXPIRE', KEYS[1], ARGV[2])
            end
            return v
            """;

    /**
     * 整数递增指定增量 + DYNAMIC 过期 Lua 脚本
     * KEYS[1]: key
     * ARGV[1]: 增量值
     * ARGV[2]: 过期秒数
     * 返回: 递增后的值
     */
    private static final String INCRBY_DYNAMIC_SCRIPT = """
            local v = redis.call('INCRBY', KEYS[1], ARGV[1])
            redis.call('EXPIRE', KEYS[1], ARGV[2])
            return v
            """;

    /**
     * 浮点递增指定增量 + FIXED 过期 Lua 脚本
     * KEYS[1]: key
     * ARGV[1]: 增量值
     * ARGV[2]: 过期秒数
     * 返回: 递增后的值
     */
    private static final String INCRBY_FLOAT_FIXED_SCRIPT = """
            local existed = redis.call('EXISTS', KEYS[1])
            local v = redis.call('INCRBYFLOAT', KEYS[1], ARGV[1])
            if existed == 0 then
                redis.call('EXPIRE', KEYS[1], ARGV[2])
            end
            return v
            """;

    /**
     * 浮点递增指定增量 + DYNAMIC 过期 Lua 脚本
     * KEYS[1]: key
     * ARGV[1]: 增量值
     * ARGV[2]: 过期秒数
     * 返回: 递增后的值
     */
    private static final String INCRBY_FLOAT_DYNAMIC_SCRIPT = """
            local v = redis.call('INCRBYFLOAT', KEYS[1], ARGV[1])
            redis.call('EXPIRE', KEYS[1], ARGV[2])
            return v
            """;

    /**
     * 递减 + FIXED 过期 Lua 脚本
     * KEYS[1]: key
     * ARGV[1]: 减量值
     * ARGV[2]: 过期秒数
     * 返回: 递减后的值
     */
    private static final String DECRBY_FIXED_SCRIPT = """
            local existed = redis.call('EXISTS', KEYS[1])
            local v = redis.call('DECRBY', KEYS[1], ARGV[1])
            if existed == 0 then
                redis.call('EXPIRE', KEYS[1], ARGV[2])
            end
            return v
            """;

    /**
     * 递减 + DYNAMIC 过期 Lua 脚本
     * KEYS[1]: key
     * ARGV[1]: 减量值
     * ARGV[2]: 过期秒数
     * 返回: 递减后的值
     */
    private static final String DECRBY_DYNAMIC_SCRIPT = """
            local v = redis.call('DECRBY', KEYS[1], ARGV[1])
            redis.call('EXPIRE', KEYS[1], ARGV[2])
            return v
            """;
    /**
     * SET + FIXED 过期（仅 key 不存在时设置 TTL）Lua 脚本
     * KEYS[1]: key
     * ARGV[1]: value
     * ARGV[2]: 过期秒数
     * 返回: 1-设置成功, 0-key已存在（仅更新value，不修改TTL）
     */
    private static final String SET_FIXED_SCRIPT = """
            local existed = redis.call('EXISTS', KEYS[1])
            redis.call('SET', KEYS[1], ARGV[1])
            if existed == 0 then
                redis.call('EXPIRE', KEYS[1], ARGV[2])
            end
            return existed
            """;
    // ==================== Lua 脚本 ====================
    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== String 操作 ====================

    /**
     * 获取缓存值
     * <p>
     * 支持所有泛型类型，包括：
     * <ul>
     *   <li>简单类型：String、Integer、Boolean、Long、Double 等</li>
     *   <li>单层泛型：List&lt;User&gt;、Map&lt;String, User&gt;、Set&lt;String&gt;</li>
     *   <li>多层泛型：List&lt;Map&lt;String, User&gt;&gt;、Map&lt;String, List&lt;User&gt;&gt;</li>
     *   <li>嵌套类型：PageResult&lt;UserDTO&gt;、UserDTO&lt;UserDTO.Address&gt;</li>
     * </ul>
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @param <V>  值类型
     * @return 缓存值，如果不存在返回 null
     */
    public <V> V get(RedisKeyDefine<V> key, Object... args) {
        Object value = redisTemplate.opsForValue().get(key.formatKey(args));
        if (value == null) {
            return null;
        }

        if (value instanceof String strValue) {
            return key.parse(strValue);
        }

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
        String jsonValue = JsonUtils.toJsonString(value);
        if (!key.hasTimeout()) {
            redisTemplate.opsForValue().set(formattedKey, jsonValue);
            return;
        }

        if (key.isFixedTimeout()) {
            long seconds = key.getTimeout().getSeconds();
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(SET_FIXED_SCRIPT, Long.class);
            redisTemplate.execute(script, Collections.singletonList(formattedKey), jsonValue, String.valueOf(seconds));
            return;
        }
        // 动态过期时间：每次设置都重新计算过期时间
        redisTemplate.opsForValue().set(formattedKey, jsonValue, key.getTimeout());
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
        // 序列化为 JSON 字符串存储
        String jsonValue = JsonUtils.toJsonString(value);
        Boolean success;
        if (key.hasTimeout()) {
            success = redisTemplate.opsForValue().setIfAbsent(formattedKey, jsonValue, key.getTimeout());
        } else {
            success = redisTemplate.opsForValue().setIfAbsent(formattedKey, jsonValue);
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
        // 序列化为 JSON 字符串存储
        String jsonValue = JsonUtils.toJsonString(value);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(formattedKey, jsonValue, timeout);
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
        return Boolean.TRUE.equals(redisTemplate.expire(key.formatKey(args), Duration.ofMillis(timeout)));
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

        long seconds = key.getTimeout().getSeconds();
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(
                key.isFixedTimeout() ? INCR_FIXED_SCRIPT : INCR_DYNAMIC_SCRIPT, Long.class);
        return redisTemplate.execute(script, Collections.singletonList(formattedKey), String.valueOf(seconds));
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

        long seconds = key.getTimeout().getSeconds();
        DefaultRedisScript<Double> script = new DefaultRedisScript<>(
                key.isFixedTimeout() ? INCRBY_FLOAT_FIXED_SCRIPT : INCRBY_FLOAT_DYNAMIC_SCRIPT, Double.class);
        return redisTemplate.execute(script, Collections.singletonList(formattedKey),
                String.valueOf(delta), String.valueOf(seconds));
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

        long seconds = key.getTimeout().getSeconds();
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(
                key.isFixedTimeout() ? INCRBY_FIXED_SCRIPT : INCRBY_DYNAMIC_SCRIPT, Long.class);
        return redisTemplate.execute(script, Collections.singletonList(formattedKey),
                String.valueOf(delta), String.valueOf(seconds));
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

        long seconds = key.getTimeout().getSeconds();
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(
                key.isFixedTimeout() ? DECRBY_FIXED_SCRIPT : DECRBY_DYNAMIC_SCRIPT, Long.class);
        return redisTemplate.execute(script, Collections.singletonList(formattedKey),
                String.valueOf(delta), String.valueOf(seconds));
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

    // ==================== Hash 操作 ====================

    /**
     * 获取 Hash 中指定字段的值
     *
     * @param key   RedisKeyDefine 定义
     * @param field 字段名
     * @param args  模板参数
     * @param <V>   值类型
     * @return 字段值，不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <V> V hget(RedisKeyDefine<?> key, String field, Object... args) {
        String formattedKey = key.formatKey(args);
        Object value = redisTemplate.opsForHash().get(formattedKey, field);
        if (value == null) {
            return null;
        }
        return (V) value;
    }

    /**
     * 设置 Hash 中指定字段的值
     *
     * @param key   RedisKeyDefine 定义
     * @param field 字段名
     * @param value 字段值
     * @param args  模板参数
     */
    public void hset(RedisKeyDefine<?> key, String field, Object value, Object... args) {
        String formattedKey = key.formatKey(args);
        redisTemplate.opsForHash().put(formattedKey, field, value);
    }

    /**
     * 设置 Hash 中多个字段的值
     *
     * @param key  RedisKeyDefine 定义
     * @param map  字段-值映射
     * @param args 模板参数
     */
    public void hmset(RedisKeyDefine<?> key, Map<String, Object> map, Object... args) {
        String formattedKey = key.formatKey(args);
        redisTemplate.opsForHash().putAll(formattedKey, map);
    }

    /**
     * 获取 Hash 中多个字段的值
     *
     * @param key    RedisKeyDefine 定义
     * @param fields 字段名列表
     * @param args   模板参数
     * @return 字段值列表
     */
    public List<Object> hmget(RedisKeyDefine<?> key, Collection<String> fields, Object... args) {
        String formattedKey = key.formatKey(args);
        return redisTemplate.opsForHash().multiGet(formattedKey, new ArrayList<>(fields));
    }

    /**
     * 删除 Hash 中指定字段
     *
     * @param key    RedisKeyDefine 定义
     * @param fields 字段名
     * @param args   模板参数
     * @return 删除的字段数量
     */
    public long hdel(RedisKeyDefine<?> key, Collection<String> fields, Object... args) {
        String formattedKey = key.formatKey(args);
        Long removed = redisTemplate.opsForHash().delete(formattedKey, fields);
        return removed != null ? removed : 0;
    }

    /**
     * 检查 Hash 中指定字段是否存在
     *
     * @param key   RedisKeyDefine 定义
     * @param field 字段名
     * @param args  模板参数
     * @return 是否存在
     */
    public boolean hexists(RedisKeyDefine<?> key, String field, Object... args) {
        String formattedKey = key.formatKey(args);
        return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(formattedKey, field));
    }

    /**
     * 获取 Hash 中所有字段
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @return 所有字段
     */
    public Set<Object> hkeys(RedisKeyDefine<?> key, Object... args) {
        String formattedKey = key.formatKey(args);
        return redisTemplate.opsForHash().keys(formattedKey);
    }

    /**
     * 获取 Hash 中所有值
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @return 所有值
     */
    public List<Object> hvals(RedisKeyDefine<?> key, Object... args) {
        String formattedKey = key.formatKey(args);
        return redisTemplate.opsForHash().values(formattedKey);
    }

    /**
     * 获取 Hash 中所有字段和值
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @return 字段-值映射
     */
    public Map<Object, Object> hgetAll(RedisKeyDefine<?> key, Object... args) {
        String formattedKey = key.formatKey(args);
        return redisTemplate.opsForHash().entries(formattedKey);
    }

    /**
     * Hash 字段自增
     *
     * @param key   RedisKeyDefine 定义
     * @param field 字段名
     * @param delta 增量值
     * @param args  模板参数
     * @return 自增后的值
     */
    public long hincrBy(RedisKeyDefine<?> key, String field, long delta, Object... args) {
        String formattedKey = key.formatKey(args);
        Long value = redisTemplate.opsForHash().increment(formattedKey, field, delta);
        return value != null ? value : 0;
    }

    // ==================== List 操作 ====================

    /**
     * 从左侧推入元素
     *
     * @param key    RedisKeyDefine 定义
     * @param values 元素
     * @param args   模板参数
     * @return 列表长度
     */
    public long lpush(RedisKeyDefine<?> key, Collection<Object> values, Object... args) {
        String formattedKey = key.formatKey(args);
        Long size = redisTemplate.opsForList().leftPushAll(formattedKey, values);
        return size != null ? size : 0;
    }

    /**
     * 从右侧推入元素
     *
     * @param key    RedisKeyDefine 定义
     * @param values 元素
     * @param args   模板参数
     * @return 列表长度
     */
    public long rpush(RedisKeyDefine<?> key, Collection<Object> values, Object... args) {
        String formattedKey = key.formatKey(args);
        Long size = redisTemplate.opsForList().rightPushAll(formattedKey, values);
        return size != null ? size : 0;
    }

    /**
     * 从左侧弹出元素
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @param <V>  值类型
     * @return 弹出的元素，列表为空返回 null
     */
    @SuppressWarnings("unchecked")
    public <V> V lpop(RedisKeyDefine<?> key, Object... args) {
        String formattedKey = key.formatKey(args);
        return (V) redisTemplate.opsForList().leftPop(formattedKey);
    }

    /**
     * 从右侧弹出元素
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @param <V>  值类型
     * @return 弹出的元素，列表为空返回 null
     */
    @SuppressWarnings("unchecked")
    public <V> V rpop(RedisKeyDefine<?> key, Object... args) {
        String formattedKey = key.formatKey(args);
        return (V) redisTemplate.opsForList().rightPop(formattedKey);
    }

    /**
     * 获取列表指定范围的元素
     *
     * @param key   RedisKeyDefine 定义
     * @param start 开始位置（0 开始）
     * @param end   结束位置（-1 表示到最后）
     * @param args  模板参数
     * @return 元素列表
     */
    public List<Object> lrange(RedisKeyDefine<?> key, long start, long end, Object... args) {
        String formattedKey = key.formatKey(args);
        return redisTemplate.opsForList().range(formattedKey, start, end);
    }

    /**
     * 获取列表长度
     *
     * @param key  RedisKeyDefine 定义
     * @param args 模板参数
     * @return 列表长度
     */
    public long llen(RedisKeyDefine<?> key, Object... args) {
        String formattedKey = key.formatKey(args);
        Long size = redisTemplate.opsForList().size(formattedKey);
        return size != null ? size : 0;
    }

    /**
     * 获取列表指定位置的元素
     *
     * @param key   RedisKeyDefine 定义
     * @param index 索引（0 开始，负数表示从末尾算起）
     * @param args  模板参数
     * @param <V>   值类型
     * @return 元素值，不存在返回 null
     */
    @SuppressWarnings("unchecked")
    public <V> V lindex(RedisKeyDefine<?> key, long index, Object... args) {
        String formattedKey = key.formatKey(args);
        return (V) redisTemplate.opsForList().index(formattedKey, index);
    }

    /**
     * 设置列表指定位置的元素
     *
     * @param key   RedisKeyDefine 定义
     * @param index 索引
     * @param value 元素值
     * @param args  模板参数
     */
    public void lset(RedisKeyDefine<?> key, long index, Object value, Object... args) {
        String formattedKey = key.formatKey(args);
        redisTemplate.opsForList().set(formattedKey, index, value);
    }

    /**
     * 从列表中移除指定数量的元素
     *
     * @param key   RedisKeyDefine 定义
     * @param count 移除数量（正数从左到右，负数从右到左，0 移除所有）
     * @param value 元素值
     * @param args  模板参数
     * @return 实际移除的数量
     */
    public long lrem(RedisKeyDefine<?> key, long count, Object value, Object... args) {
        String formattedKey = key.formatKey(args);
        Long removed = redisTemplate.opsForList().remove(formattedKey, count, value);
        return removed != null ? removed : 0;
    }

    /**
     * 阻塞弹出左侧元素
     *
     * @param key     RedisKeyDefine 定义
     * @param timeout 超时时间
     * @param args    模板参数
     * @param <V>     值类型
     * @return 弹出的元素，超时返回 null
     */
    @SuppressWarnings("unchecked")
    public <V> V blPop(RedisKeyDefine<?> key, Duration timeout, Object... args) {
        String formattedKey = key.formatKey(args);
        var result = redisTemplate.opsForList().leftPop(formattedKey, timeout);
        return result != null ? (V) result : null;
    }

    /**
     * 阻塞弹出右侧元素
     *
     * @param key     RedisKeyDefine 定义
     * @param timeout 超时时间
     * @param args    模板参数
     * @param <V>     值类型
     * @return 弹出的元素，超时返回 null
     */
    @SuppressWarnings("unchecked")
    public <V> V brPop(RedisKeyDefine<?> key, Duration timeout, Object... args) {
        String formattedKey = key.formatKey(args);
        var result = redisTemplate.opsForList().rightPop(formattedKey, timeout);
        return result != null ? (V) result : null;
    }
}
