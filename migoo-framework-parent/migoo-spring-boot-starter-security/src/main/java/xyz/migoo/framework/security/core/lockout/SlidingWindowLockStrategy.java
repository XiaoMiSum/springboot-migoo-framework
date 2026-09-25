package xyz.migoo.framework.security.core.lockout;

import xyz.migoo.framework.web.core.store.StateStore;

import java.time.Duration;
import java.util.Optional;

/**
 * 滑动窗口累计锁定策略
 * <p>
 * 窗口内累计失败次数达到阈值即锁定（不限连续，中间登录成功也计数）。
 * 实现上将窗口划分为 {@value #BUCKETS} 个时间片滚动统计，统计误差不超过一个时间片。
 * <p>
 * 命中锁定时清空窗口内计数（锁定期间的失败由 {@link LoginLockManager} 拦截不再累计，
 * 锁定结束后从零开始重新统计，避免反复立即触发）；登录成功不清零（累计语义）。
 *
 * @author xiaomi
 */
public class SlidingWindowLockStrategy implements LoginLockStrategy {

    /**
     * 策略标识
     */
    public static final String NAME = "sliding-window";

    /**
     * 窗口时间片数量
     */
    private static final int BUCKETS = 10;

    /**
     * 失败计数键前缀
     */
    private static final String KEY_PREFIX = "migoo:security:login:fail:";

    private final StateStore store;

    private final int threshold;

    private final Duration window;

    private final Duration lockDuration;

    public SlidingWindowLockStrategy(StateStore store, int threshold,
                                     Duration window, Duration lockDuration) {
        if (threshold < 1) {
            throw new IllegalStateException("登录失败锁定策略 " + NAME + " 的 threshold 必须大于 0");
        }
        this.store = store;
        this.threshold = threshold;
        this.window = window;
        this.lockDuration = lockDuration;
    }

    @Override
    public Optional<Duration> onFailure(String subject) {
        long now = System.currentTimeMillis();
        long bucketMs = Math.max(window.toMillis() / BUCKETS, 1);
        long currentBucket = now / bucketMs;
        String keyPrefix = keyPrefix(subject);

        long total = store.increment(keyPrefix + currentBucket, window);
        for (int i = 1; i < BUCKETS; i++) {
            total += store.get(keyPrefix + (currentBucket - i));
        }
        if (total >= threshold) {
            clearWindow(subject, currentBucket);
            return Optional.of(lockDuration);
        }
        return Optional.empty();
    }

    @Override
    public void onSuccess(String subject) {
        // 累计语义: 窗口内登录成功不重置失败计数
    }

    @Override
    public void reset(String subject) {
        long bucketMs = Math.max(window.toMillis() / BUCKETS, 1);
        clearWindow(subject, System.currentTimeMillis() / bucketMs);
    }

    /**
     * 清空当前窗口内所有时间片计数
     */
    private void clearWindow(String subject, long currentBucket) {
        String keyPrefix = keyPrefix(subject);
        for (int i = 0; i < BUCKETS; i++) {
            store.delete(keyPrefix + (currentBucket - i));
        }
    }

    private String keyPrefix(String subject) {
        return KEY_PREFIX + NAME + ":" + subject + ":";
    }

    @Override
    public String getName() {
        return NAME;
    }
}
