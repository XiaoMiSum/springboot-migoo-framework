package xyz.migoo.framework.redis.core;

import lombok.Getter;
import tools.jackson.core.type.TypeReference;
import xyz.migoo.framework.common.util.json.JsonUtils;

import java.time.Duration;
import java.util.Objects;

/**
 * Redis Key 定义类
 * <p>
 * 用于定义 Redis 键的模板、类型、值类型、超时等属性。
 * 支持泛型类型安全，通过 {@link TypeReference} 支持复杂的泛型类型（如 List&lt;User&gt;、Map&lt;String, Object&gt; 等）。
 *
 * @author xiaomi
 * Created on 2021/11/21 16:12
 */
@Getter
public class RedisKeyDefine<T> {

    /**
     * Key 模板
     * <p>
     * 支持动态参数，使用 {@link String#format(String, Object...)} 格式，如："user:%s:info"
     */
    private final String keyTemplate;
    /**
     * Value 类型
     * <p>
     * 用于序列化和反序列化时的类型信息。如果是使用分布式锁，设置为 {@link java.util.concurrent.locks.Lock} 类型。
     * 对于简单类型，可以直接使用 {@link Class}；对于复杂泛型类型，使用 {@link TypeReference}。
     */
    private final TypeReference<T> valueType;
    /**
     * 过期时间
     */
    private final Duration timeout;
    /**
     * 过期类型
     */
    private final TimeoutType timeoutType;
    /**
     * 备注
     */
    private final String memo;

    /**
     * 私有构造函数，使用 Builder 模式创建实例
     */
    private RedisKeyDefine(Builder<T> builder) {
        this.memo = builder.memo;
        this.keyTemplate = Objects.requireNonNull(builder.keyTemplate, "keyTemplate must not be null");
        this.valueType = Objects.requireNonNull(builder.valueType, "valueType must not be null");
        this.timeout = builder.timeout != null ? builder.timeout : Duration.ZERO;
        this.timeoutType = builder.timeoutType != null ? builder.timeoutType : TimeoutType.FIXED;
    }

    /**
     * 创建 Builder 实例
     *
     * @param <T> 值类型
     * @return Builder 实例
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * 格式化键，将参数填充到键模板中
     * <p>
     * 例如：keyTemplate 为 "user:%s:info"，args 为 "123"，则返回 "user:123:info"
     *
     * @param args 格式化参数
     * @return 格式化后的键
     */
    public String formatKey(Object... args) {
        if (args == null || args.length == 0) {
            return keyTemplate;
        }
        return String.format(keyTemplate, args);
    }

    // ==================== 业务方法 ====================

    public T parse(String json) {
        return JsonUtils.parseObject(json, valueType);
    }

    /**
     * 检查是否设置了超时时间
     *
     * @return 如果 timeoutType 不为 PERMANENT 且 timeout 大于 0，返回 true
     */
    public boolean hasTimeout() {
        return timeoutType != TimeoutType.PERMANENT && !timeout.isZero() && !timeout.isNegative();
    }

    /**
     * 检查是否为固定过期时间类型
     *
     * @return 如果是 FIXED 类型返回 true
     */
    public boolean isFixedTimeout() {
        return timeoutType == TimeoutType.FIXED;
    }

    /**
     * 检查是否为动态过期时间类型
     *
     * @return 如果是 DYNAMIC 类型返回 true
     */
    public boolean isDynamicTimeout() {
        return timeoutType == TimeoutType.DYNAMIC;
    }

    /**
     * 获取超时时间（毫秒）
     *
     * @return 超时毫秒数，如果没有设置超时返回 0
     */
    public long getTimeoutMillis() {
        return hasTimeout() ? timeout.toMillis() : 0;
    }

    // ==================== Builder 模式 ====================

    /**
     * 过期类型枚举
     */
    public enum TimeoutType {
        /**
         * 永久有效，不设置过期时间
         */
        PERMANENT,
        /**
         * 固定过期时间，仅在首次设置时设置过期时间
         */
        FIXED,
        /**
         * 动态过期时间，每次设置都重新计算过期时间
         */
        DYNAMIC
    }

    /**
     * Builder 类，用于构建 RedisKeyDefine 实例
     *
     * @param <T> 值类型
     */
    public static class Builder<T> {
        private String memo;
        private String keyTemplate;
        private TypeReference<T> valueType;
        private Duration timeout = Duration.ZERO;
        private TimeoutType timeoutType;

        public Builder<T> memo(String memo) {
            this.memo = memo;
            return this;
        }

        public Builder<T> keyTemplate(String keyTemplate) {
            this.keyTemplate = keyTemplate;
            return this;
        }

        public Builder<T> valueType(Class<T> valueType) {
            this.valueType = new TypeReference<>() {
                @Override
                public Class<T> getType() {
                    return valueType;
                }
            };
            return this;
        }

        public Builder<T> valueType(TypeReference<T> valueType) {
            this.valueType = valueType;
            return this;
        }

        public Builder<T> timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder<T> timeoutSeconds(long seconds) {
            this.timeout = Duration.ofSeconds(seconds);
            return this;
        }

        public Builder<T> timeoutMinutes(long minutes) {
            this.timeout = Duration.ofMinutes(minutes);
            return this;
        }

        public Builder<T> timeoutHours(long hours) {
            this.timeout = Duration.ofHours(hours);
            return this;
        }

        public Builder<T> timeoutDays(long days) {
            this.timeout = Duration.ofDays(days);
            return this;
        }

        /**
         * 设置过期类型为永久有效
         */
        public Builder<T> permanent() {
            this.timeoutType = TimeoutType.PERMANENT;
            this.timeout = Duration.ZERO;
            return this;
        }

        /**
         * 设置过期类型为固定过期时间（仅首次设置时生效）
         */
        public Builder<T> fixedTimeout() {
            this.timeoutType = TimeoutType.FIXED;
            return this;
        }

        /**
         * 设置过期类型为动态过期时间（每次设置都重新计算）
         */
        public Builder<T> dynamicTimeout() {
            this.timeoutType = TimeoutType.DYNAMIC;
            return this;
        }

        /**
         * 设置过期类型
         *
         * @param timeoutType 过期类型
         */
        public Builder<T> timeoutType(TimeoutType timeoutType) {
            this.timeoutType = timeoutType;
            return this;
        }

        public RedisKeyDefine<T> build() {
            return new RedisKeyDefine<>(this);
        }
    }
}
