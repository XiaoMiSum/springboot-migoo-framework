package xyz.migoo.framework.web.core.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.web.core.store.InMemoryStateStore;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultRateLimiter} 单元测试
 */
class DefaultRateLimiterTest {

    private DefaultRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new DefaultRateLimiter(new InMemoryStateStore());
    }

    @Test
    void allowsUpToLimitThenRejects() {
        Duration window = Duration.ofMinutes(1);
        assertThat(rateLimiter.tryAcquire("api:list:1.1.1.1", 2, window)).isTrue();
        assertThat(rateLimiter.tryAcquire("api:list:1.1.1.1", 2, window)).isTrue();
        assertThat(rateLimiter.tryAcquire("api:list:1.1.1.1", 2, window)).isFalse();
    }

    @Test
    void distinctKeysAreIsolated() {
        Duration window = Duration.ofMinutes(1);
        assertThat(rateLimiter.tryAcquire("api:list:1.1.1.1", 1, window)).isTrue();
        assertThat(rateLimiter.tryAcquire("api:list:1.1.1.1", 1, window)).isFalse();
        // 其他 key 不受影响
        assertThat(rateLimiter.tryAcquire("api:list:2.2.2.2", 1, window)).isTrue();
    }

    @Test
    void nonPositiveLimitAlwaysRejects() {
        assertThat(rateLimiter.tryAcquire("api:x", 0, Duration.ofMinutes(1))).isFalse();
    }
}
