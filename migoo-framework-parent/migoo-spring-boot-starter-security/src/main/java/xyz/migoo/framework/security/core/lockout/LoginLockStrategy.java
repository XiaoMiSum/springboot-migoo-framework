package xyz.migoo.framework.security.core.lockout;

import java.time.Duration;
import java.util.Optional;

/**
 * 登录失败锁定策略接口
 * <p>
 * 每个策略自行维护失败计数状态（存储于 {@code SecurityStateStore}），
 * 在每次登录失败时评估是否命中锁定、锁定多久。
 * <p>
 * 框架内置固定时长、递增时长、滑动窗口累计三种策略，
 * 所有生效的策略 Bean 会被 {@link DefaultLoginLockManager} 组合调用（命中取最严），
 * 应用也可注册自定义策略 Bean 参与组合。
 *
 * @author xiaomi
 */
public interface LoginLockStrategy {

    /**
     * 策略标识（用于统计键区分，建议唯一）
     *
     * @return 策略标识
     */
    String getName();

    /**
     * 记录一次登录失败并评估是否命中锁定
     * <p>
     * 注意: 已处于锁定状态时不会调用本方法（由 {@link LoginLockManager} 保证）。
     *
     * @param subject 锁定主体（登录用户名）
     * @return 命中锁定返回锁定时长，未命中返回 empty
     */
    Optional<Duration> onFailure(String subject);

    /**
     * 登录成功回调（默认空实现）
     * <p>
     * 需要「连续失败」语义的策略应在此清零计数。
     *
     * @param subject 锁定主体
     */
    default void onSuccess(String subject) {
        // 默认空实现: 滑动窗口累计类策略成功不重置
    }

    /**
     * 强制清零计数（用于管理员解锁、重置密码等场景，与登录成功区分）
     *
     * @param subject 锁定主体
     */
    default void reset(String subject) {
        // 默认空实现
    }
}
