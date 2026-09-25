package xyz.migoo.framework.security.core.lockout;

import xyz.migoo.framework.web.core.store.StateStore;

import java.time.Duration;
import java.util.Optional;

/**
 * 固定时长锁定策略
 * <p>
 * 连续失败次数达到阈值后，锁定固定时长。
 * 命中锁定时计数清零（锁定结束后需要重新累计阈值次失败才会再次触发）。
 *
 * @author xiaomi
 */
public class FixedDurationLockStrategy extends AbstractConsecutiveLockStrategy {

    /**
     * 策略标识
     */
    public static final String NAME = "fixed";

    private final Duration lockDuration;

    public FixedDurationLockStrategy(StateStore store, int threshold,
                                     Duration failureWindow, Duration lockDuration) {
        super(store, NAME, threshold, failureWindow);
        this.lockDuration = lockDuration;
    }

    @Override
    public Optional<Duration> onFailure(String subject) {
        long count = increaseFailure(subject);
        if (count >= getThreshold()) {
            // 命中后清零，下一轮锁定需重新累计
            clearFailure(subject);
            return Optional.of(lockDuration);
        }
        return Optional.empty();
    }
}
