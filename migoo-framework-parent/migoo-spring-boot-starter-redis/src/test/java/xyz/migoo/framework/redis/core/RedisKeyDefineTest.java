package xyz.migoo.framework.redis.core;

import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import xyz.migoo.framework.redis.core.RedisKeyDefine.TimeoutType;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link RedisKeyDefine} 单元测试
 * <p>
 * 覆盖 Builder 模式、formatKey 占位符替换、parse 反序列化以及三种 TimeoutType 的超时行为。
 */
class RedisKeyDefineTest {

    /** 测试用简单对象，验证 JSON 序列化 / 反序列化 */
    record User(String name) {}

    // ==================== Builder 默认行为 ====================

    @Test
    void builderDefaultsToFixedTypeWithZeroTimeout() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("user:%s:info")
                .valueType(String.class)
                .build();
        // 未显式设置时，timeout 默认 ZERO、timeoutType 默认 FIXED、memo 默认 null
        assertThat(define.getKeyTemplate()).isEqualTo("user:%s:info");
        assertThat(define.getValueType().getType()).isEqualTo(String.class);
        assertThat(define.getTimeout()).isEqualTo(Duration.ZERO);
        assertThat(define.getTimeoutType()).isEqualTo(TimeoutType.FIXED);
        assertThat(define.getMemo()).isNull();
        // ZERO 超时视为无超时
        assertThat(define.hasTimeout()).isFalse();
        assertThat(define.getTimeoutMillis()).isZero();
    }

    @Test
    void builderSetsAllFields() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .memo("用户信息缓存")
                .keyTemplate("user:%s:info")
                .valueType(String.class)
                .timeout(Duration.ofSeconds(30))
                .timeoutType(TimeoutType.DYNAMIC)
                .build();
        assertThat(define.getMemo()).isEqualTo("用户信息缓存");
        assertThat(define.getKeyTemplate()).isEqualTo("user:%s:info");
        assertThat(define.getTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(define.getTimeoutType()).isEqualTo(TimeoutType.DYNAMIC);
    }

    @Test
    void builderSupportsClassAndTypeReferenceValueType() {
        RedisKeyDefine<String> byClass = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).build();
        assertThat(byClass.getValueType().getType()).isEqualTo(String.class);

        TypeReference<List<String>> listType = new TypeReference<>() {
        };
        RedisKeyDefine<List<String>> byReference = RedisKeyDefine.<List<String>>builder()
                .keyTemplate("k").valueType(listType).build();
        assertThat(byReference.getValueType()).isSameAs(listType);
    }

    @Test
    void builderPermanentClearsTimeout() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("k")
                .valueType(String.class)
                .timeout(Duration.ofHours(1))
                .permanent()
                .build();
        // permanent() 会强制把 timeout 重置为 ZERO
        assertThat(define.getTimeoutType()).isEqualTo(TimeoutType.PERMANENT);
        assertThat(define.getTimeout()).isEqualTo(Duration.ZERO);
        assertThat(define.hasTimeout()).isFalse();
        assertThat(define.isFixedTimeout()).isFalse();
        assertThat(define.isDynamicTimeout()).isFalse();
    }

    @Test
    void builderTimeoutUnitHelperMethods() {
        assertThat(timeoutBy(b -> b.timeoutSeconds(30)).getTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(timeoutBy(b -> b.timeoutMinutes(2)).getTimeout()).isEqualTo(Duration.ofMinutes(2));
        assertThat(timeoutBy(b -> b.timeoutHours(1)).getTimeout()).isEqualTo(Duration.ofHours(1));
        assertThat(timeoutBy(b -> b.timeoutDays(3)).getTimeout()).isEqualTo(Duration.ofDays(3));
        assertThat(timeoutBy(b -> b.timeout(Duration.ofMillis(500))).getTimeout()).isEqualTo(Duration.ofMillis(500));
    }

    @Test
    void builderFixedAndDynamicTimeoutTypeMethods() {
        RedisKeyDefine<String> fixed = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).fixedTimeout().build();
        assertThat(fixed.getTimeoutType()).isEqualTo(TimeoutType.FIXED);

        RedisKeyDefine<String> dynamic = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).dynamicTimeout().build();
        assertThat(dynamic.getTimeoutType()).isEqualTo(TimeoutType.DYNAMIC);
    }

    @Test
    void builderWithoutKeyTemplateThrowsNullPointerException() {
        assertThatThrownBy(() -> RedisKeyDefine.<String>builder().valueType(String.class).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("keyTemplate must not be null");
    }

    @Test
    void builderWithoutValueTypeThrowsNullPointerException() {
        assertThatThrownBy(() -> RedisKeyDefine.<String>builder().keyTemplate("k").build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("valueType must not be null");
    }

    // ==================== formatKey 占位符替换 ====================

    @Test
    void formatKeyReplacesPlaceholders() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("user:%s:info")
                .valueType(String.class)
                .build();
        assertThat(define.formatKey("123")).isEqualTo("user:123:info");
    }

    @Test
    void formatKeyReplacesMultiplePlaceholders() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("%s:%s")
                .valueType(String.class)
                .build();
        assertThat(define.formatKey("a", "b")).isEqualTo("a:b");
    }

    @Test
    void formatKeyWithoutArgsReturnsTemplate() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("user:%s:info")
                .valueType(String.class)
                .build();
        // 无参数或 null 参数时直接返回模板
        assertThat(define.formatKey()).isEqualTo("user:%s:info");
        assertThat(define.formatKey((Object[]) null)).isEqualTo("user:%s:info");
    }

    @Test
    void formatKeyFormatsNonStringArgs() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("user:%s:info")
                .valueType(String.class)
                .build();
        assertThat(define.formatKey(123L)).isEqualTo("user:123:info");
        assertThat(define.formatKey(3.14)).isEqualTo("user:3.14:info");
    }

    // ==================== 超时行为 ====================

    @Test
    void hasTimeoutTrueForFixedWithPositiveDuration() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class)
                .timeout(Duration.ofSeconds(60)).fixedTimeout().build();
        assertThat(define.hasTimeout()).isTrue();
    }

    @Test
    void hasTimeoutTrueForDynamicWithPositiveDuration() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class)
                .timeout(Duration.ofSeconds(60)).dynamicTimeout().build();
        assertThat(define.hasTimeout()).isTrue();
    }

    @Test
    void hasTimeoutFalseForPermanent() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class)
                .timeout(Duration.ofSeconds(60)).permanent().build();
        assertThat(define.hasTimeout()).isFalse();
    }

    @Test
    void hasTimeoutFalseForZeroOrNegativeDuration() {
        RedisKeyDefine<String> zero = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class)
                .timeout(Duration.ZERO).fixedTimeout().build();
        RedisKeyDefine<String> negative = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class)
                .timeout(Duration.ofSeconds(-1)).fixedTimeout().build();
        assertThat(zero.hasTimeout()).isFalse();
        assertThat(negative.hasTimeout()).isFalse();
    }

    @Test
    void isFixedTimeoutOnlyWhenTypeIsFixed() {
        RedisKeyDefine<String> fixed = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).fixedTimeout().build();
        RedisKeyDefine<String> dynamic = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).dynamicTimeout().build();
        RedisKeyDefine<String> permanent = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).permanent().build();
        assertThat(fixed.isFixedTimeout()).isTrue();
        assertThat(dynamic.isFixedTimeout()).isFalse();
        assertThat(permanent.isFixedTimeout()).isFalse();
    }

    @Test
    void isDynamicTimeoutOnlyWhenTypeIsDynamic() {
        RedisKeyDefine<String> fixed = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).fixedTimeout().build();
        RedisKeyDefine<String> dynamic = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).dynamicTimeout().build();
        assertThat(dynamic.isDynamicTimeout()).isTrue();
        assertThat(fixed.isDynamicTimeout()).isFalse();
    }

    @Test
    void getTimeoutMillisReturnsDurationMillis() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class)
                .timeout(Duration.ofMillis(1500)).fixedTimeout().build();
        assertThat(define.getTimeoutMillis()).isEqualTo(1500L);
    }

    @Test
    void getTimeoutMillisReturnsZeroWithoutTimeout() {
        RedisKeyDefine<String> permanent = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).permanent().build();
        RedisKeyDefine<String> zero = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).build();
        assertThat(permanent.getTimeoutMillis()).isZero();
        assertThat(zero.getTimeoutMillis()).isZero();
    }

    // ==================== parse 反序列化 ====================

    @Test
    void parseSimpleStringUsesConvertFallback() {
        // "hello" 不是合法 JSON，parseObject 失败后走 convert 兜底
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).build();
        assertThat(define.parse("hello")).isEqualTo("hello");
    }

    @Test
    void parseJsonStringValue() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).build();
        assertThat(define.parse("\"hello\"")).isEqualTo("hello");
    }

    @Test
    void parseIntegerValue() {
        RedisKeyDefine<Integer> define = RedisKeyDefine.<Integer>builder()
                .keyTemplate("k").valueType(Integer.class).build();
        assertThat(define.parse("123")).isEqualTo(123);
    }

    @Test
    void parseNullReturnsNull() {
        RedisKeyDefine<String> define = RedisKeyDefine.<String>builder()
                .keyTemplate("k").valueType(String.class).build();
        assertThat(define.parse(null)).isNull();
    }

    @Test
    void parseGenericListValue() {
        RedisKeyDefine<List<String>> define = RedisKeyDefine.<List<String>>builder()
                .keyTemplate("k")
                .valueType(new TypeReference<>() {
                })
                .build();
        assertThat(define.parse("[\"a\",\"b\"]")).containsExactly("a", "b");
    }

    @Test
    void parsePojoValue() {
        RedisKeyDefine<User> define = RedisKeyDefine.<User>builder()
                .keyTemplate("k").valueType(User.class).build();
        assertThat(define.parse("{\"name\":\"alice\"}")).isEqualTo(new User("alice"));
    }

    // ==================== 私有工具 ====================

    /** 构造一个仅设置 keyTemplate/valueType，并通过 builder 回调应用超时配置的实例 */
    private static RedisKeyDefine<String> timeoutBy(java.util.function.Consumer<RedisKeyDefine.Builder<String>> configurer) {
        RedisKeyDefine.Builder<String> builder = RedisKeyDefine.<String>builder()
                .keyTemplate("k")
                .valueType(String.class);
        configurer.accept(builder);
        return builder.build();
    }
}
