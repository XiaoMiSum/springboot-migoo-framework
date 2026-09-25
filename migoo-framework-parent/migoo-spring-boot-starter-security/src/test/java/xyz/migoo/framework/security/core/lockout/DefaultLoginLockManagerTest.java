package xyz.migoo.framework.security.core.lockout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;
import xyz.migoo.framework.web.core.store.StateStore;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * {@link DefaultLoginLockManager} 单元测试
 * <p>
 * 验证多策略组合"命中取最严"、已锁定不延长、unlock 清零计数等语义
 */
class DefaultLoginLockManagerTest {

    private static final String LOCK_KEY = "migoo:security:login:lock:user1";

    private StateStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryStateStore();
    }

    @Test
    void takesLongestDurationAmongHitStrategies() {
        // fixed 阈值 1 次 → 锁 5m；incremental 阈值 1 次 → 锁 1m；组合必须取最严的 5m
        var fixed = new FixedDurationLockStrategy(store, 1, Duration.ofMinutes(30), Duration.ofMinutes(5));
        var incremental = new IncrementalDurationLockStrategy(store, 1, Duration.ofMinutes(30),
                Duration.ofMinutes(1), 2, Duration.ofHours(1));
        var manager = new DefaultLoginLockManager(store, List.of(fixed, incremental));

        long before = System.currentTimeMillis();
        assertThat(manager.onFailure("user1")).isTrue();
        long after = System.currentTimeMillis();

        long lockUntil = store.get(LOCK_KEY);
        assertThat(lockUntil)
                .isBetween(before + Duration.ofMinutes(5).toMillis(), after + Duration.ofMinutes(5).toMillis());
        assertThat(manager.isLocked("user1")).isTrue();
    }

    @Test
    void alreadyLockedDoesNotExtendLock() {
        var fixed = new FixedDurationLockStrategy(store, 1, Duration.ofMinutes(30), Duration.ofMinutes(5));
        var manager = new DefaultLoginLockManager(store, List.of(fixed));

        manager.onFailure("user1");
        long lockUntil = store.get(LOCK_KEY);

        // 已锁定: 仍返回 true，但不再累计、不延长锁定
        assertThat(manager.onFailure("user1")).isTrue();
        assertThat(store.get(LOCK_KEY)).isEqualTo(lockUntil);
    }

    @Test
    void unlockClearsLockAndStrategyCounters() {
        // 仅 incremental: 若 unlock 未清零计数，再次失败会锁定 2m（递增）而非 1m
        var incremental = new IncrementalDurationLockStrategy(store, 1, Duration.ofMinutes(30),
                Duration.ofMinutes(1), 2, Duration.ofHours(1));
        var manager = new DefaultLoginLockManager(store, List.of(incremental));

        assertThat(manager.onFailure("user1")).isTrue();
        manager.unlock("user1");
        assertThat(manager.isLocked("user1")).isFalse();

        long before = System.currentTimeMillis();
        assertThat(manager.onFailure("user1")).isTrue();
        long lockUntil = store.get(LOCK_KEY);
        // 计数已清零 → 重新从首次锁定时长 1m 开始（若未清零则为 2m）
        assertThat(lockUntil).isLessThan(before + Duration.ofMinutes(2).toMillis());
    }

    @Test
    void emptyStrategiesNeverLock() {
        var manager = new DefaultLoginLockManager(store, List.of());
        assertThat(manager.onFailure("user1")).isFalse();
        assertThat(manager.isLocked("user1")).isFalse();
    }

    @Test
    void onSuccessDelegatesToAllStrategies() {
        LoginLockStrategy strategy = mock(LoginLockStrategy.class);
        var manager = new DefaultLoginLockManager(store, List.of(strategy));

        manager.onSuccess("user1");
        verify(strategy).onSuccess("user1");
    }
}
