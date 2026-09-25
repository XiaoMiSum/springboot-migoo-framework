package xyz.migoo.framework.security.core.lockout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FixedDurationLockStrategy} 单元测试
 */
class FixedDurationLockStrategyTest {

    private FixedDurationLockStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FixedDurationLockStrategy(new InMemoryStateStore(), 3,
                Duration.ofMinutes(30), Duration.ofMinutes(10));
    }

    @Test
    void locksAfterThresholdConsecutiveFailures() {
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).isEmpty();
        // 第 3 次连续失败 → 锁定 10 分钟
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(10));
    }

    @Test
    void counterClearsAfterLockTriggered() {
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        // 命中后计数清零，下一轮需要重新累计 3 次
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(10));
    }

    @Test
    void successResetsCounter() {
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        strategy.onSuccess("user1");
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(10));
    }

    @Test
    void resetClearsCounter() {
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        strategy.reset("user1");
        assertThat(strategy.onFailure("user1")).isEmpty();
    }

    @Test
    void subjectsAreIsolated() {
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(10));
        // 其他用户不受影响
        assertThat(strategy.onFailure("user2")).isEmpty();
    }
}
