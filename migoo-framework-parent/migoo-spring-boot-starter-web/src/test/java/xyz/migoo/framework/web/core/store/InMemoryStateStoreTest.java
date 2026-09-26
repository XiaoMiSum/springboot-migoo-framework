package xyz.migoo.framework.web.core.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InMemoryStateStore} 单元测试
 */
class InMemoryStateStoreTest {

    private InMemoryStateStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryStateStore();
    }

    @Test
    void incrementReturnsSequence() {
        Duration ttl = Duration.ofMinutes(1);
        assertThat(store.increment("k1", ttl)).isEqualTo(1);
        assertThat(store.increment("k1", ttl)).isEqualTo(2);
        assertThat(store.increment("k1", ttl)).isEqualTo(3);
        assertThat(store.get("k1")).isEqualTo(3);
    }

    @Test
    void getMissingKeyReturnsZero() {
        assertThat(store.get("missing")).isZero();
    }

    @Test
    void putGetAndDelete() {
        store.put("lock:user1", 100L, Duration.ofMinutes(5));
        assertThat(store.get("lock:user1")).isEqualTo(100L);

        store.delete("lock:user1");
        assertThat(store.get("lock:user1")).isZero();
    }

    @Test
    void entryExpiresAfterTtl() throws InterruptedException {
        store.put("k", 7L, Duration.ofMillis(10));
        Thread.sleep(50);
        assertThat(store.get("k")).isZero();
    }

    @Test
    void incrementAfterExpiryRestartsFromOne() throws InterruptedException {
        assertThat(store.increment("k", Duration.ofMillis(10))).isEqualTo(1);
        Thread.sleep(50);
        assertThat(store.increment("k", Duration.ofMillis(10))).isEqualTo(1);
    }

    @Test
    void incrementRefreshesTtl() throws InterruptedException {
        // 第一次自增: TTL 600ms
        assertThat(store.increment("k", Duration.ofMillis(600))).isEqualTo(1);
        // 400ms 后第二次自增（第一次 TTL 尚未到期），TTL 被刷新为 600ms
        Thread.sleep(400);
        assertThat(store.increment("k", Duration.ofMillis(600))).isEqualTo(2);
        // 再过 400ms（距第一次已 800ms > 600ms，距第二次 400ms < 600ms）: 值仍在，证明 TTL 被刷新
        Thread.sleep(400);
        assertThat(store.get("k")).isEqualTo(2);
        // 再过 400ms（距第二次 800ms > 600ms）: 已过期
        Thread.sleep(400);
        assertThat(store.get("k")).isZero();
    }

    @Test
    void setIfAbsentOnlyFirstClaimWins() {
        Duration ttl = Duration.ofMinutes(1);

        // 首次占位成功，重复占位失败
        assertThat(store.setIfAbsent("claim", ttl)).isTrue();
        assertThat(store.setIfAbsent("claim", ttl)).isFalse();
        assertThat(store.get("claim")).isEqualTo(1);

        // 释放后可重新占位
        store.delete("claim");
        assertThat(store.setIfAbsent("claim", ttl)).isTrue();
    }

    @Test
    void setIfAbsentAllowsReclaimAfterExpiry() throws InterruptedException {
        assertThat(store.setIfAbsent("short", Duration.ofMillis(10))).isTrue();
        assertThat(store.setIfAbsent("short", Duration.ofMillis(10))).isFalse();
        Thread.sleep(50);
        // 过期后可重新占位
        assertThat(store.setIfAbsent("short", Duration.ofMillis(10))).isTrue();
    }

    @Test
    void defaultSetIfAbsentFallsBackToGetThenPut() {
        // 仅实现四个基础方法的自定义存储 → 走接口默认「读-判-写」兜底
        Map<String, Long> backing = new HashMap<>();
        StateStore custom = new StateStore() {
            @Override
            public long increment(String key, Duration ttl) {
                return backing.merge(key, 1L, Long::sum);
            }

            @Override
            public long get(String key) {
                return backing.getOrDefault(key, 0L);
            }

            @Override
            public void put(String key, long value, Duration ttl) {
                backing.put(key, value);
            }

            @Override
            public void delete(String key) {
                backing.remove(key);
            }
        };

        assertThat(custom.setIfAbsent("k", Duration.ofMinutes(1))).isTrue();
        assertThat(custom.setIfAbsent("k", Duration.ofMinutes(1))).isFalse();
        assertThat(backing).containsEntry("k", 1L);
    }
}
