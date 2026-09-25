package xyz.migoo.framework.security.core.lockout;

/**
 * 登录失败锁定管理接口
 * <p>
 * 负责锁定状态的查询与维护，并组合调用所有 {@link LoginLockStrategy}（命中取最严）。
 *
 * @author xiaomi
 */
public interface LoginLockManager {

    /**
     * 判断主体是否处于锁定状态
     *
     * @param subject 锁定主体（登录用户名）
     * @return true-锁定中，登录请求应被拒绝（423）
     */
    boolean isLocked(String subject);

    /**
     * 记录一次登录失败，组合评估所有锁定策略
     * <p>
     * 任一策略命中即锁定，锁定时长取所有命中策略中最长的（命中取最严）。
     *
     * @param subject 锁定主体
     * @return true-本次失败后主体已被锁定
     */
    boolean onFailure(String subject);

    /**
     * 登录成功回调（委托各策略清零连续失败计数）
     *
     * @param subject 锁定主体
     */
    void onSuccess(String subject);

    /**
     * 解除锁定并清零全部策略计数（预留: 管理端强制解锁、重置密码后解锁等场景）
     *
     * @param subject 锁定主体
     */
    void unlock(String subject);
}
