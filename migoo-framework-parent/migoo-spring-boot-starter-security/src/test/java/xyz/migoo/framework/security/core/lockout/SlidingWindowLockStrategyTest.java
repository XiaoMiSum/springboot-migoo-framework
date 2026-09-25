package xyz.migoo.framework.security.core.lockout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SlidingWindowLockStrategy} 单元测试
 */
class SlidingWindowLockStrategyTest {

    private SlidingWindowLockStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SlidingWindowLockStrategy(new InMemoryStateStore(), 3,
                Duration.ofMinutes(10), Duration.ofMinutes(5));
    }

    @Test
    void locksAfterThresholdFailuresWithinWindow() {
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(5));
    }

    @Test
    void successDoesNotResetAccumulatedCount() {
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        // 累计语义: 窗口内登录成功不重置失败计数
        strategy.onSuccess("user1");
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(5));
    }

    @Test
    void windowClearsAfterLockTriggered() {
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        // 命中后窗口清空，锁定结束后重新累计
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(5));
    }

    @Test
    void resetClearsWindow() {
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        strategy.reset("user1");
        assertThat(strategy.onFailure("user1")).isEmpty();
    }
}
