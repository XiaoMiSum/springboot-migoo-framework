package xyz.migoo.framework.web.core.ratelimit;

import xyz.migoo.framework.web.core.store.StateStore;

import java.time.Duration;

/**
 * 默认限流器实现（固定窗口计数）
 * <p>
 * 基于 {@link StateStore} 计数，窗口按时间片对齐，
 * 每个窗口使用独立的统计键（{@code 前缀:key:窗口序号}），窗口切换后自动重新计数。
 *
 * @author xiaomi
 */
public class DefaultRateLimiter implements RateLimiter {

    /**
     * 统计键前缀
     */
    private static final String KEY_PREFIX = "migoo:web:rate:";

    private final StateStore stateStore;

    public DefaultRateLimiter(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    @Override
    public boolean tryAcquire(String key, int limit, Duration window) {
        if (limit <= 0) {
            return false;
        }
        long windowMs = Math.max(window.toMillis(), 1);
        long windowIndex = System.currentTimeMillis() / windowMs;
        long count = stateStore.increment(KEY_PREFIX + key + ":" + windowIndex, window);
        return count <= limit;
    }
}
