package xyz.migoo.framework.security.core.lockout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link IncrementalDurationLockStrategy} 单元测试
 */
class IncrementalDurationLockStrategyTest {

    private IncrementalDurationLockStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new IncrementalDurationLockStrategy(new InMemoryStateStore(), 3,
                Duration.ofMinutes(30), Duration.ofMinutes(1), 2, Duration.ofMinutes(4));
    }

    @Test
    void lockDurationEscalatesExponentially() {
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).isEmpty();
        // 第 3 次: 首次锁定 1 分钟
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(1));
        // 第 4 次: 2 分钟
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(2));
        // 第 5 次: 4 分钟
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(4));
        // 第 6 次: 封顶 4 分钟
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(4));
    }

    @Test
    void counterSurvivesLockTriggeredForEscalation() {
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        // 命中不清零（递增语义的关键），下次失败直接进入下一级
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(2));
    }

    @Test
    void successResetsEscalation() {
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        strategy.onFailure("user1");
        strategy.onSuccess("user1");
        // 清零后从头累计
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).isEmpty();
        assertThat(strategy.onFailure("user1")).contains(Duration.ofMinutes(1));
    }
}
