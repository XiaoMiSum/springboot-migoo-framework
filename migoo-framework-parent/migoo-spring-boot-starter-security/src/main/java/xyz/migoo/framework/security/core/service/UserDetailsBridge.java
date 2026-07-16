package xyz.migoo.framework.security.core.service;

import xyz.migoo.framework.security.core.AuthUserDetails;

/**
 * 应用层回调: 用户加载桥接接口
 * <p>
 * 应用必须实现此接口，提供从数据库加载用户的逻辑。
 * 框架通过此接口将 token 操作与用户存储解耦。
 *
 * @author xiaomi
 */
public interface UserDetailsBridge {

    /**
     * 根据用户名加载用户 (用于 loadUserByUsername / authenticate)
     * <p>
     * 应用在这里查数据库，返回完整的 AuthUserDetails
     *
     * @param username 用户名
     * @return 用户信息
     */
    AuthUserDetails<?, ?> loadByUsername(String username);

    /**
     * 根据用户编号加载用户 (用于 verifyToken / refreshToken)
     * <p>
     * 应用在这里根据 userId 查数据库，获取最新用户状态。
     * 每次请求都会调用，确保用户状态实时性（如: 账号是否被禁用、权限是否变更）。
     *
     * @param userId 用户编号 (String 类型，应用层自行转换)
     * @return 用户信息
     */
    AuthUserDetails<?, ?> loadByUserId(String userId);

    /**
     * 清理 token (用于 clean / 登出)
     * <p>
     * 应用在这里实现 token 黑名单逻辑（如: Redis 删除 token）。
     * JWT 天然无状态，若应用需要主动失效 token，实现此方法。
     *
     * @param token 需要清理的 token
     */
    default void clean(String token) {
        // 默认空实现，JWT 无状态场景无需清理
    }
}
