package xyz.migoo.framework.security.core.lockout;

import xyz.migoo.framework.web.core.store.StateStore;

import java.time.Duration;

/**
 * 「连续失败计数」锁定策略抽象基类
 * <p>
 * 维护按主体隔离的连续失败计数器，计数键在最后一次失败后经过
 * {@code failureWindow} 未再发生失败则自动过期清零（存储侧每次自增刷新 TTL）。
 * 登录成功（{@link #onSuccess}）或强制重置（{@link #reset}）时清零计数。
 *
 * @author xiaomi
 */
public abstract class AbstractConsecutiveLockStrategy implements LoginLockStrategy {

    /**
     * 失败计数键前缀
     */
    protected static final String KEY_PREFIX = "migoo:security:login:fail:";

    private final StateStore store;

    private final String name;

    private final int threshold;

    private final Duration failureWindow;

    protected AbstractConsecutiveLockStrategy(StateStore store, String name,
                                              int threshold, Duration failureWindow) {
        if (threshold < 1) {
            throw new IllegalStateException("登录失败锁定策略 " + name + " 的 threshold 必须大于 0");
        }
        this.store = store;
        this.name = name;
        this.threshold = threshold;
        this.failureWindow = failureWindow;
    }

    /**
     * 失败计数 +1，并返回计数后的值
     *
     * @param subject 锁定主体
     * @return 连续失败次数
     */
    protected long increaseFailure(String subject) {
        return store.increment(key(subject), failureWindow);
    }

    /**
     * 清零失败计数
     *
     * @param subject 锁定主体
     */
    protected void clearFailure(String subject) {
        store.delete(key(subject));
    }

    protected int getThreshold() {
        return threshold;
    }

    private String key(String subject) {
        return KEY_PREFIX + name + ":" + subject;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onSuccess(String subject) {
        clearFailure(subject);
    }

    @Override
    public void reset(String subject) {
        clearFailure(subject);
    }
}
