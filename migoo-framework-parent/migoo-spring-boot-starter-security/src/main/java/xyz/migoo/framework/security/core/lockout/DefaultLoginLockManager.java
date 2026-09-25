package xyz.migoo.framework.security.core.lockout;

import xyz.migoo.framework.web.core.store.StateStore;

import java.time.Duration;
import java.util.List;

/**
 * 默认登录失败锁定管理实现
 * <p>
 * 组合全部 {@link LoginLockStrategy} Bean: 每次失败逐个策略评估，
 * 任一策略命中即锁定，锁定时长取所有命中策略中的最大值（命中取最严）。
 * <p>
 * 锁定状态以 {@code 锁定截止时间戳} 存储于 {@link StateStore}，
 * TTL 与锁定时长一致，到期自动解锁。
 *
 * @author xiaomi
 */
public class DefaultLoginLockManager implements LoginLockManager {

    /**
     * 锁定状态键前缀
     */
    private static final String LOCK_KEY_PREFIX = "migoo:security:login:lock:";

    private final StateStore store;

    private final List<LoginLockStrategy> strategies;

    public DefaultLoginLockManager(StateStore store, List<LoginLockStrategy> strategies) {
        this.store = store;
        this.strategies = strategies;
    }

    @Override
    public boolean isLocked(String subject) {
        return lockRemainMillis(subject) > 0;
    }

    @Override
    public boolean onFailure(String subject) {
        if (isLocked(subject)) {
            // 已锁定: 不再累计失败，也不延长锁定
            return true;
        }
        Duration maxDuration = null;
        for (LoginLockStrategy strategy : strategies) {
            Duration duration = strategy.onFailure(subject).orElse(null);
            if (duration != null && (maxDuration == null || duration.compareTo(maxDuration) > 0)) {
                maxDuration = duration;
            }
        }
        if (maxDuration == null) {
            return false;
        }
        // 命中: 锁定截止时间取新锁定与已有锁定中的较晚者（不会缩短已有锁定）
        long now = System.currentTimeMillis();
        long lockUntil = now + maxDuration.toMillis();
        long existing = store.get(lockKey(subject));
        if (existing > lockUntil) {
            lockUntil = existing;
        }
        store.put(lockKey(subject), lockUntil, Duration.ofMillis(Math.max(lockUntil - now, 1)));
        return true;
    }

    @Override
    public void onSuccess(String subject) {
        strategies.forEach(strategy -> strategy.onSuccess(subject));
    }

    @Override
    public void unlock(String subject) {
        store.delete(lockKey(subject));
        strategies.forEach(strategy -> strategy.reset(subject));
    }

    private long lockRemainMillis(String subject) {
        long lockUntil = store.get(lockKey(subject));
        return lockUntil - System.currentTimeMillis();
    }

    private String lockKey(String subject) {
        return LOCK_KEY_PREFIX + subject;
    }
}
