package xyz.migoo.framework.redis.core;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.migoo.framework.common.util.json.JsonUtils;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisKit {

    private final RedisTemplate<String, Object> redisTemplate;

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
}
