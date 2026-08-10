package xyz.migoo.framework.redis.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RedisKit} 单元测试
 * <p>
 * 全部使用 Mockito mock {@link RedisTemplate} 及各 Operation，不依赖真实 Redis。
 * 覆盖 String 操作、原子计数、分布式锁、Sorted Set、Hash、List 全部公开方法。
 */
@ExtendWith(MockitoExtension.class)
class RedisKitTest {

    /** 测试用简单对象，验证 JSON 序列化 / 反序列化 */
    record User(String name) {}

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private ZSetOperations<String, Object> zSetOperations;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private ListOperations<String, Object> listOperations;

    @InjectMocks
    private RedisKit redisKit;

    @BeforeEach
    void setUp() {
        // lenient：各 @Nested 测试只会用到其中部分 Operation，避免 UnnecessaryStubbing
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    // ==================== 测试辅助方法 ====================

    /** 无超时（默认 FIXED + ZERO）的 String 类型 key */
    private static RedisKeyDefine<String> stringKey(String template) {
        return RedisKeyDefine.<String>builder().keyTemplate(template).valueType(String.class).build();
    }

    /** 永久有效的 String 类型 key */
    private static RedisKeyDefine<String> permanentKey(String template) {
        return RedisKeyDefine.<String>builder().keyTemplate(template).valueType(String.class).permanent().build();
    }

    /** FIXED 超时类型的 String 类型 key */
    private static RedisKeyDefine<String> fixedKey(String template, Duration timeout) {
        return RedisKeyDefine.<String>builder().keyTemplate(template).valueType(String.class)
                .timeout(timeout).fixedTimeout().build();
    }

    /** DYNAMIC 超时类型的 String 类型 key */
    private static RedisKeyDefine<String> dynamicKey(String template, Duration timeout) {
        return RedisKeyDefine.<String>builder().keyTemplate(template).valueType(String.class)
                .timeout(timeout).dynamicTimeout().build();
    }

    /** Integer 类型 key，验证非 String 值的 convert 路径 */
    private static RedisKeyDefine<Integer> intKey(String template) {
        return RedisKeyDefine.<Integer>builder().keyTemplate(template).valueType(Integer.class).build();
    }

    /** User 对象类型 key，验证 JSON 反序列化路径 */
    private static RedisKeyDefine<User> userKey(String template) {
        return RedisKeyDefine.<User>builder().keyTemplate(template).valueType(User.class).build();
    }

    // ==================== get ====================

    @Nested
    class GetTests {

        @Test
        void getFormatsKeyAndParsesStringValue() {
            RedisKeyDefine<String> key = stringKey("user:%s:info");
            when(valueOperations.get("user:1:info")).thenReturn("hello");
            String result = redisKit.get(key, 1);
            // 非 JSON 字符串，走 convert 兜底
            assertThat(result).isEqualTo("hello");
            verify(valueOperations).get("user:1:info");
        }

        @Test
        void getParsesQuotedJsonStringValue() {
            RedisKeyDefine<String> key = stringKey("k");
            when(valueOperations.get("k")).thenReturn("\"hello\"");
            assertThat(redisKit.get(key)).isEqualTo("hello");
        }

        @Test
        void getParsesJsonStringIntoPojo() {
            RedisKeyDefine<User> key = userKey("k");
            when(valueOperations.get("k")).thenReturn("{\"name\":\"alice\"}");
            assertThat(redisKit.get(key)).isEqualTo(new User("alice"));
        }

        @Test
        void getReturnsNullWhenValueIsNull() {
            RedisKeyDefine<String> key = stringKey("k");
            when(valueOperations.get("k")).thenReturn(null);
            assertThat(redisKit.get(key)).isNull();
        }

        @Test
        void getConvertsNonStringValue() {
            RedisKeyDefine<Integer> key = intKey("k");
            // 非 String 存储值（如默认 JDK 序列化产物）走 convert 路径
            when(valueOperations.get("k")).thenReturn(42);
            assertThat(redisKit.get(key)).isEqualTo(42);
        }

        @Test
        void getWithNoArgsReturnsTemplateValue() {
            RedisKeyDefine<String> key = stringKey("cache:info");
            when(valueOperations.get("cache:info")).thenReturn("value");
            assertThat(redisKit.get(key)).isEqualTo("value");
            verify(valueOperations).get("cache:info");
        }
    }

    // ==================== set ====================

    @Nested
    class SetTests {

        @Test
        void setWithoutTimeoutCallsSetWithJson() {
            redisKit.set(permanentKey("k"), "hello");
            verify(valueOperations).set("k", "\"hello\"");
        }

        @Test
        void setStoresPojoAsJson() {
            redisKit.set(userKey("k"), new User("alice"));
            verify(valueOperations).set("k", "{\"name\":\"alice\"}");
        }

        @Test
        void setWithNullValueStoresEmptyJsonObject() {
            // JsonUtils.toJsonString(null) 返回 "{}"
            redisKit.set(permanentKey("k"), null);
            verify(valueOperations).set("k", "{}");
        }

        @Test
        void setWithFixedTimeoutExecutesLuaScriptWithSeconds() {
            RedisKeyDefine<String> key = fixedKey("user:%s:info", Duration.ofSeconds(60));
            redisKit.set(key, "hello", 1);
            // 期望脚本参数：value、timeout 秒数
            verify(redisTemplate).execute(any(RedisScript.class),
                    eq(java.util.Collections.singletonList("user:1:info")), eq("\"hello\""), eq("60"));
            // 校验使用的确为 SET_FIXED 脚本（包含 EXISTS 判断）
            ArgumentCaptor<RedisScript> captor = ArgumentCaptor.forClass(RedisScript.class);
            verify(redisTemplate).execute(captor.capture(),
                    eq(java.util.Collections.singletonList("user:1:info")), eq("\"hello\""), eq("60"));
            assertThat(captor.getValue().getScriptAsString())
                    .contains("EXISTS").contains("EXPIRE");
        }

        @Test
        void setWithDynamicTimeoutSetsDuration() {
            RedisKeyDefine<String> key = dynamicKey("k", Duration.ofSeconds(90));
            redisKit.set(key, "hello");
            verify(valueOperations).set("k", "\"hello\"", Duration.ofSeconds(90));
        }
    }

    // ==================== setIfAbsent ====================

    @Nested
    class SetIfAbsentTests {

        @Test
        void setIfAbsentWithoutTimeout() {
            when(valueOperations.setIfAbsent("k", "\"hello\"")).thenReturn(true);
            assertThat(redisKit.setIfAbsent(permanentKey("k"), "hello")).isTrue();
            verify(valueOperations).setIfAbsent("k", "\"hello\"");
        }

        @Test
        void setIfAbsentReturnsFalseWhenResultIsNull() {
            when(valueOperations.setIfAbsent("k", "\"hello\"")).thenReturn(null);
            assertThat(redisKit.setIfAbsent(permanentKey("k"), "hello")).isFalse();
        }

        @Test
        void setIfAbsentWithTimeoutPassesKeyTimeout() {
            RedisKeyDefine<String> key = fixedKey("k", Duration.ofSeconds(60));
            when(valueOperations.setIfAbsent("k", "\"hello\"", Duration.ofSeconds(60))).thenReturn(true);
            assertThat(redisKit.setIfAbsent(key, "hello")).isTrue();
            verify(valueOperations).setIfAbsent("k", "\"hello\"", Duration.ofSeconds(60));
        }

        @Test
        void setIfAbsentWithExplicitTimeout() {
            when(valueOperations.setIfAbsent("k", "\"hello\"", Duration.ofMinutes(5))).thenReturn(true);
            assertThat(redisKit.setIfAbsent(permanentKey("k"), "hello", Duration.ofMinutes(5))).isTrue();
            verify(valueOperations).setIfAbsent("k", "\"hello\"", Duration.ofMinutes(5));
        }
    }

    // ==================== delete / hasKey / expire ====================

    @Nested
    class KeyOpsTests {

        @Test
        void deleteFormatsKey() {
            redisKit.delete(stringKey("user:%s:info"), 1);
            verify(redisTemplate).delete("user:1:info");
        }

        @Test
        void hasKeyReturnsTrueWhenExists() {
            when(redisTemplate.hasKey("k")).thenReturn(true);
            assertThat(redisKit.hasKey(stringKey("k"))).isTrue();
        }

        @Test
        void hasKeyReturnsFalseWhenResultIsNull() {
            when(redisTemplate.hasKey("k")).thenReturn(null);
            assertThat(redisKit.hasKey(stringKey("k"))).isFalse();
        }

        @Test
        void expireConvertsMillisToDuration() {
            when(redisTemplate.expire("k", Duration.ofMillis(1000))).thenReturn(true);
            assertThat(redisKit.expire(stringKey("k"), 1000)).isTrue();
            verify(redisTemplate).expire("k", Duration.ofMillis(1000));
        }
    }

    // ==================== 原子计数 ====================

    @Nested
    class IncrementTests {

        @Test
        void incrementWithoutTimeoutCallsOpsForValue() {
            when(valueOperations.increment("k")).thenReturn(5L);
            assertThat(redisKit.increment(stringKey("k"))).isEqualTo(5L);
            verify(valueOperations).increment("k");
        }

        @Test
        void incrementWithoutTimeoutReturnsZeroWhenNull() {
            when(valueOperations.increment("k")).thenReturn(null);
            assertThat(redisKit.increment(stringKey("k"))).isZero();
        }

        @Test
        void incrementFixedTimeoutUsesFixedScriptWithSeconds() {
            RedisKeyDefine<String> key = fixedKey("k", Duration.ofSeconds(60));
            when(redisTemplate.execute(any(RedisScript.class),
                    eq(java.util.Collections.singletonList("k")), eq("60"))).thenReturn(7L);
            assertThat(redisKit.increment(key)).isEqualTo(7L);
            ArgumentCaptor<RedisScript> captor = ArgumentCaptor.forClass(RedisScript.class);
            verify(redisTemplate).execute(captor.capture(),
                    eq(java.util.Collections.singletonList("k")), eq("60"));
            // FIXED 脚本仅在 key 不存在时设置过期
            assertThat(captor.getValue().getScriptAsString())
                    .contains("EXISTS").contains("INCR").contains("EXPIRE");
        }

        @Test
        void incrementDynamicTimeoutUsesDynamicScript() {
            RedisKeyDefine<String> key = dynamicKey("k", Duration.ofSeconds(60));
            when(redisTemplate.execute(any(RedisScript.class),
                    eq(java.util.Collections.singletonList("k")), eq("60"))).thenReturn(9L);
            assertThat(redisKit.increment(key)).isEqualTo(9L);
            ArgumentCaptor<RedisScript> captor = ArgumentCaptor.forClass(RedisScript.class);
            verify(redisTemplate).execute(captor.capture(),
                    eq(java.util.Collections.singletonList("k")), eq("60"));
            // DYNAMIC 脚本每次都设置过期，不做 EXISTS 判断
            assertThat(captor.getValue().getScriptAsString())
                    .doesNotContain("EXISTS").contains("INCR").contains("EXPIRE");
        }

        @Test
        void incrementByDoubleWithoutTimeout() {
            when(valueOperations.increment("k", 1.5)).thenReturn(2.5);
            assertThat(redisKit.increment(stringKey("k"), 1.5)).isEqualTo(2.5);
            verify(valueOperations).increment("k", 1.5);
        }

        @Test
        void incrementByDoubleReturnsZeroWhenNull() {
            when(valueOperations.increment("k", 1.5)).thenReturn(null);
            assertThat(redisKit.increment(stringKey("k"), 1.5)).isZero();
        }

        @Test
        void incrementByDoubleFixedTimeoutUsesFloatScript() {
            RedisKeyDefine<String> key = fixedKey("k", Duration.ofSeconds(60));
            when(redisTemplate.execute(any(RedisScript.class),
                    eq(java.util.Collections.singletonList("k")), eq("1.5"), eq("60"))).thenReturn(2.5);
            assertThat(redisKit.increment(key, 1.5)).isEqualTo(2.5);
            ArgumentCaptor<RedisScript> captor = ArgumentCaptor.forClass(RedisScript.class);
            verify(redisTemplate).execute(captor.capture(),
                    eq(java.util.Collections.singletonList("k")), eq("1.5"), eq("60"));
            assertThat(captor.getValue().getScriptAsString()).contains("INCRBYFLOAT").contains("EXISTS");
        }

        @Test
        void incrementByLongWithoutTimeout() {
            when(valueOperations.increment("k", 3L)).thenReturn(8L);
            assertThat(redisKit.increment(stringKey("k"), 3L)).isEqualTo(8L);
            verify(valueOperations).increment("k", 3L);
        }

        @Test
        void incrementByLongReturnsZeroWhenNull() {
            when(valueOperations.increment("k", 3L)).thenReturn(null);
            assertThat(redisKit.increment(stringKey("k"), 3L)).isZero();
        }

        @Test
        void incrementByLongFixedTimeoutUsesIncrByScript() {
            RedisKeyDefine<String> key = fixedKey("k", Duration.ofSeconds(60));
            when(redisTemplate.execute(any(RedisScript.class),
                    eq(java.util.Collections.singletonList("k")), eq("3"), eq("60"))).thenReturn(8L);
            assertThat(redisKit.increment(key, 3L)).isEqualTo(8L);
            ArgumentCaptor<RedisScript> captor = ArgumentCaptor.forClass(RedisScript.class);
            verify(redisTemplate).execute(captor.capture(),
                    eq(java.util.Collections.singletonList("k")), eq("3"), eq("60"));
            assertThat(captor.getValue().getScriptAsString()).contains("INCRBY").contains("EXISTS");
        }

        @Test
        void decrementWithoutTimeout() {
            when(valueOperations.decrement("k", 2L)).thenReturn(3L);
            assertThat(redisKit.decrement(stringKey("k"), 2L)).isEqualTo(3L);
            verify(valueOperations).decrement("k", 2L);
        }

        @Test
        void decrementReturnsZeroWhenNull() {
            when(valueOperations.decrement("k", 2L)).thenReturn(null);
            assertThat(redisKit.decrement(stringKey("k"), 2L)).isZero();
        }

        @Test
        void decrementFixedTimeoutUsesDecrByScript() {
            RedisKeyDefine<String> key = fixedKey("k", Duration.ofSeconds(60));
            when(redisTemplate.execute(any(RedisScript.class),
                    eq(java.util.Collections.singletonList("k")), eq("2"), eq("60"))).thenReturn(3L);
            assertThat(redisKit.decrement(key, 2L)).isEqualTo(3L);
            ArgumentCaptor<RedisScript> captor = ArgumentCaptor.forClass(RedisScript.class);
            verify(redisTemplate).execute(captor.capture(),
                    eq(java.util.Collections.singletonList("k")), eq("2"), eq("60"));
            assertThat(captor.getValue().getScriptAsString()).contains("DECRBY").contains("EXISTS");
        }
    }

    // ==================== 分布式锁 ====================

    @Nested
    class LockTests {

        @Test
        void tryLockStringPassesDuration() {
            when(valueOperations.setIfAbsent("lock:key", "uuid", Duration.ofSeconds(10))).thenReturn(true);
            assertThat(redisKit.tryLock("lock:key", "uuid", Duration.ofSeconds(10))).isTrue();
            verify(valueOperations).setIfAbsent("lock:key", "uuid", Duration.ofSeconds(10));
        }

        @Test
        void tryLockStringReturnsFalseWhenNull() {
            when(valueOperations.setIfAbsent("lock:key", "uuid", Duration.ofSeconds(10))).thenReturn(null);
            assertThat(redisKit.tryLock("lock:key", "uuid", Duration.ofSeconds(10))).isFalse();
        }

        @Test
        void tryLockWithKeyDefineUsesKeyTimeout() {
            RedisKeyDefine<String> key = fixedKey("lock:%s", Duration.ofSeconds(30));
            when(valueOperations.setIfAbsent("lock:1", "uuid", Duration.ofSeconds(30))).thenReturn(true);
            assertThat(redisKit.tryLock(key, "uuid", 1)).isTrue();
            verify(valueOperations).setIfAbsent("lock:1", "uuid", Duration.ofSeconds(30));
        }

        @Test
        void tryLockWithKeyDefineWithoutTimeoutUsesThirtySeconds() {
            RedisKeyDefine<String> key = permanentKey("lock:%s");
            when(valueOperations.setIfAbsent("lock:1", "uuid", Duration.ofSeconds(30))).thenReturn(true);
            assertThat(redisKit.tryLock(key, "uuid", 1)).isTrue();
            verify(valueOperations).setIfAbsent("lock:1", "uuid", Duration.ofSeconds(30));
        }

        @Test
        void unlockExecutesLuaAndReturnsTrueWhenResultIsOne() {
            when(redisTemplate.execute(any(RedisScript.class),
                    eq(java.util.Collections.singletonList("lock:key")), eq("uuid"))).thenReturn(1L);
            assertThat(redisKit.unlock("lock:key", "uuid")).isTrue();
            ArgumentCaptor<RedisScript> captor = ArgumentCaptor.forClass(RedisScript.class);
            verify(redisTemplate).execute(captor.capture(),
                    eq(java.util.Collections.singletonList("lock:key")), eq("uuid"));
            // 校验解锁脚本内容：先 get 比较持有者，再 del
            assertThat(captor.getValue().getScriptAsString())
                    .contains("redis.call('get'").contains("redis.call('del'");
        }

        @Test
        void unlockReturnsFalseWhenResultIsZero() {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(0L);
            assertThat(redisKit.unlock("lock:key", "uuid")).isFalse();
        }

        @Test
        void unlockReturnsFalseWhenResultIsNull() {
            when(redisTemplate.execute(any(RedisScript.class), anyList(), anyString())).thenReturn(null);
            assertThat(redisKit.unlock("lock:key", "uuid")).isFalse();
        }

        @Test
        void unlockWithKeyDefineFormatsKey() {
            RedisKeyDefine<String> key = stringKey("lock:%s");
            when(redisTemplate.execute(any(RedisScript.class),
                    eq(java.util.Collections.singletonList("lock:1")), eq("uuid"))).thenReturn(1L);
            assertThat(redisKit.unlock(key, "uuid", 1)).isTrue();
            verify(redisTemplate).execute(any(RedisScript.class),
                    eq(java.util.Collections.singletonList("lock:1")), eq("uuid"));
        }
    }

    // ==================== Sorted Set 操作 ====================

    @Nested
    class ZSetTests {

        @Test
        void zaddAddsMemberWithScore() {
            when(zSetOperations.add("k", "member", 1.5)).thenReturn(true);
            assertThat(redisKit.zadd(stringKey("k"), "member", 1.5)).isTrue();
            verify(zSetOperations).add("k", "member", 1.5);
        }

        @Test
        void zaddReturnsFalseWhenResultIsNull() {
            when(zSetOperations.add("k", "member", 1.5)).thenReturn(null);
            assertThat(redisKit.zadd(stringKey("k"), "member", 1.5)).isFalse();
        }

        @Test
        void zremRemovesSingleMember() {
            when(zSetOperations.remove("k", "member")).thenReturn(1L);
            assertThat(redisKit.zrem(stringKey("k"), "member")).isEqualTo(1L);
            verify(zSetOperations).remove("k", "member");
        }

        @Test
        void zremRemovesMultipleMembers() {
            Object[] members = {"a", "b"};
            when(zSetOperations.remove("k", members)).thenReturn(2L);
            assertThat(redisKit.zrem(stringKey("k"), members)).isEqualTo(2L);
            verify(zSetOperations).remove("k", members);
        }

        @Test
        void zremReturnsZeroWhenNull() {
            when(zSetOperations.remove("k", "member")).thenReturn(null);
            assertThat(redisKit.zrem(stringKey("k"), "member")).isZero();
        }

        @Test
        void zrankReturnsRank() {
            when(zSetOperations.rank("k", "member")).thenReturn(2L);
            assertThat(redisKit.zrank(stringKey("k"), "member")).isEqualTo(2L);
        }

        @Test
        void zrevrankReturnsReverseRank() {
            when(zSetOperations.reverseRank("k", "member")).thenReturn(1L);
            assertThat(redisKit.zrevrank(stringKey("k"), "member")).isEqualTo(1L);
        }

        @Test
        void zrangeReturnsMembers() {
            when(zSetOperations.range("k", 0L, -1L)).thenReturn(Set.of("a", "b"));
            assertThat(redisKit.zrange(stringKey("k"), 0, -1)).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        void zrevrangeReturnsMembersDesc() {
            when(zSetOperations.reverseRange("k", 0L, 1L)).thenReturn(Set.of("b", "a"));
            assertThat(redisKit.zrevrange(stringKey("k"), 0, 1)).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        void zcardReturnsSize() {
            when(zSetOperations.size("k")).thenReturn(3L);
            assertThat(redisKit.zcard(stringKey("k"))).isEqualTo(3L);
        }

        @Test
        void zcardReturnsZeroWhenNull() {
            when(zSetOperations.size("k")).thenReturn(null);
            assertThat(redisKit.zcard(stringKey("k"))).isZero();
        }

        @Test
        void zcountCountsByScoreRange() {
            when(zSetOperations.count("k", 1.0, 5.0)).thenReturn(4L);
            assertThat(redisKit.zcount(stringKey("k"), 1.0, 5.0)).isEqualTo(4L);
        }

        @Test
        void zremrangeByRankRemovesByRankRange() {
            when(zSetOperations.removeRange("k", 0L, 1L)).thenReturn(2L);
            assertThat(redisKit.zremrangeByRank(stringKey("k"), 0, 1)).isEqualTo(2L);
        }

        @Test
        void zremrangeByScoreRemovesByScoreRange() {
            when(zSetOperations.removeRangeByScore("k", 1.0, 5.0)).thenReturn(3L);
            assertThat(redisKit.zremrangeByScore(stringKey("k"), 1.0, 5.0)).isEqualTo(3L);
        }
    }

    // ==================== Hash 操作 ====================

    @Nested
    class HashTests {

        @Test
        void hgetReturnsFieldValue() {
            when(hashOperations.get("k", "field")).thenReturn("value");
            String value = redisKit.hget(stringKey("k"), "field");
            assertThat(value).isEqualTo("value");
        }

        @Test
        void hgetReturnsNullWhenFieldMissing() {
            when(hashOperations.get("k", "field")).thenReturn(null);
            String value = redisKit.hget(stringKey("k"), "field");
            assertThat(value).isNull();
        }

        @Test
        void hsetPutsFieldValue() {
            redisKit.hset(stringKey("k"), "field", "value");
            verify(hashOperations).put("k", "field", "value");
        }

        @Test
        void hmsetPutsAllEntries() {
            Map<String, Object> map = new HashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            redisKit.hmset(stringKey("k"), map);
            verify(hashOperations).putAll("k", map);
        }

        @Test
        void hmgetMultiGetsFields() {
            when(hashOperations.multiGet("k", List.of("f1", "f2"))).thenReturn(List.of("v1", "v2"));
            assertThat(redisKit.hmget(stringKey("k"), List.of("f1", "f2"))).containsExactly("v1", "v2");
            verify(hashOperations).multiGet("k", List.of("f1", "f2"));
        }

        @Test
        void hdelDeletesFields() {
            when(hashOperations.delete("k", Set.of("a", "b"))).thenReturn(2L);
            assertThat(redisKit.hdel(stringKey("k"), Set.of("a", "b"))).isEqualTo(2L);
        }

        @Test
        void hdelReturnsZeroWhenNull() {
            when(hashOperations.delete("k", Set.of("a"))).thenReturn(null);
            assertThat(redisKit.hdel(stringKey("k"), Set.of("a"))).isZero();
        }

        @Test
        void hexistsChecksFieldExistence() {
            when(hashOperations.hasKey("k", "field")).thenReturn(true);
            assertThat(redisKit.hexists(stringKey("k"), "field")).isTrue();
        }

        @Test
        void hkeysReturnsFieldSet() {
            when(hashOperations.keys("k")).thenReturn(Set.of("a", "b"));
            assertThat(redisKit.hkeys(stringKey("k"))).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        void hvalsReturnsValueList() {
            when(hashOperations.values("k")).thenReturn(List.of(1, 2));
            assertThat(redisKit.hvals(stringKey("k"))).containsExactly(1, 2);
        }

        @Test
        void hgetAllReturnsEntries() {
            Map<Object, Object> entries = new HashMap<>();
            entries.put("a", 1);
            when(hashOperations.entries("k")).thenReturn(entries);
            assertThat(redisKit.hgetAll(stringKey("k"))).isEqualTo(entries);
        }

        @Test
        void hincrByIncrementsField() {
            when(hashOperations.increment("k", "field", 2L)).thenReturn(5L);
            assertThat(redisKit.hincrBy(stringKey("k"), "field", 2)).isEqualTo(5L);
        }
    }

    // ==================== List 操作 ====================

    @Nested
    class ListTests {

        @Test
        void lpushLeftPushesAll() {
            when(listOperations.leftPushAll("k", List.of("a", "b"))).thenReturn(2L);
            assertThat(redisKit.lpush(stringKey("k"), List.of("a", "b"))).isEqualTo(2L);
        }

        @Test
        void lpushReturnsZeroWhenNull() {
            when(listOperations.leftPushAll("k", List.of("a"))).thenReturn(null);
            assertThat(redisKit.lpush(stringKey("k"), List.of("a"))).isZero();
        }

        @Test
        void rpushRightPushesAll() {
            when(listOperations.rightPushAll("k", List.of("a", "b"))).thenReturn(2L);
            assertThat(redisKit.rpush(stringKey("k"), List.of("a", "b"))).isEqualTo(2L);
        }

        @Test
        void lpopLeftPopsElement() {
            when(listOperations.leftPop("k")).thenReturn("a");
            String value = redisKit.lpop(stringKey("k"));
            assertThat(value).isEqualTo("a");
        }

        @Test
        void rpopRightPopsElement() {
            when(listOperations.rightPop("k")).thenReturn("b");
            String value = redisKit.rpop(stringKey("k"));
            assertThat(value).isEqualTo("b");
        }

        @Test
        void lrangeReturnsElements() {
            when(listOperations.range("k", 0L, -1L)).thenReturn(List.of("a", "b"));
            assertThat(redisKit.lrange(stringKey("k"), 0, -1)).containsExactly("a", "b");
        }

        @Test
        void llenReturnsSize() {
            when(listOperations.size("k")).thenReturn(4L);
            assertThat(redisKit.llen(stringKey("k"))).isEqualTo(4L);
        }

        @Test
        void llenReturnsZeroWhenNull() {
            when(listOperations.size("k")).thenReturn(null);
            assertThat(redisKit.llen(stringKey("k"))).isZero();
        }

        @Test
        void lindexReturnsElementAtPosition() {
            when(listOperations.index("k", 1L)).thenReturn("b");
            String value = redisKit.lindex(stringKey("k"), 1);
            assertThat(value).isEqualTo("b");
        }

        @Test
        void lsetSetsElementAtPosition() {
            redisKit.lset(stringKey("k"), 0, "x");
            verify(listOperations).set("k", 0L, "x");
        }

        @Test
        void lremRemovesCountElements() {
            when(listOperations.remove("k", 2L, "a")).thenReturn(2L);
            assertThat(redisKit.lrem(stringKey("k"), 2, "a")).isEqualTo(2L);
        }

        @Test
        void blPopLeftPopsWithTimeout() {
            when(listOperations.leftPop("k", Duration.ofSeconds(1))).thenReturn("a");
            String value = redisKit.blPop(stringKey("k"), Duration.ofSeconds(1));
            assertThat(value).isEqualTo("a");
        }

        @Test
        void blPopReturnsNullWhenEmpty() {
            when(listOperations.leftPop("k", Duration.ofSeconds(1))).thenReturn(null);
            String value = redisKit.blPop(stringKey("k"), Duration.ofSeconds(1));
            assertThat(value).isNull();
        }

        @Test
        void brPopRightPopsWithTimeout() {
            when(listOperations.rightPop("k", Duration.ofSeconds(1))).thenReturn("b");
            String value = redisKit.brPop(stringKey("k"), Duration.ofSeconds(1));
            assertThat(value).isEqualTo("b");
        }
    }
}
