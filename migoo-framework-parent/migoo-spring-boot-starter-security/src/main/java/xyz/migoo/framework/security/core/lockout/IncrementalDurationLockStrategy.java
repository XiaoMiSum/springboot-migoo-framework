package xyz.migoo.framework.security.core.lockout;

import xyz.migoo.framework.web.core.store.StateStore;

import java.time.Duration;
import java.util.Optional;

/**
 * 递增时长锁定策略
 * <p>
 * 连续失败次数达到阈值后开始锁定，此后每多失败一次，锁定时长按倍数递增:
 * 第 threshold 次锁 initialDuration，第 threshold+1 次锁 initialDuration × multiplier，
 * 依次递增，最长不超过 maxDuration。
 * <p>
 * 命中锁定时不清零计数（这是实现「越锁越久」递增语义的关键），
 * 计数在最后一次失败后经过 failureWindow 自动过期；登录成功时清零。
 * 建议 failureWindow 大于最长锁定时长，保证跨锁定周期的递增可延续。
 *
 * @author xiaomi
 */
public class IncrementalDurationLockStrategy extends AbstractConsecutiveLockStrategy {

    /**
     * 策略标识
     */
    public static final String NAME = "incremental";

    private final Duration initialDuration;

    private final long multiplier;

    private final Duration maxDuration;

    public IncrementalDurationLockStrategy(StateStore store, int threshold,
                                           Duration failureWindow, Duration initialDuration,
                                           long multiplier, Duration maxDuration) {
        super(store, NAME, threshold, failureWindow);
        if (multiplier < 2) {
            throw new IllegalStateException("登录失败锁定策略 " + NAME + " 的 multiplier 必须大于等于 2");
        }
        this.initialDuration = initialDuration;
        this.multiplier = multiplier;
        this.maxDuration = maxDuration;
    }

    @Override
    public Optional<Duration> onFailure(String subject) {
        long count = increaseFailure(subject);
        if (count < getThreshold()) {
            return Optional.empty();
        }
        // 第 threshold 次: initial，之后每次 × multiplier，封顶 max
        Duration duration = initialDuration;
        long escalateCount = count - getThreshold();
        for (long i = 0; i < escalateCount && duration.compareTo(maxDuration) < 0; i++) {
            duration = duration.multipliedBy(multiplier);
        }
        return Optional.of(duration.compareTo(maxDuration) > 0 ? maxDuration : duration);
    }
}
