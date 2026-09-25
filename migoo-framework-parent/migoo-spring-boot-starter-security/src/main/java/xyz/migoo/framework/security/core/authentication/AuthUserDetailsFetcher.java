package xyz.migoo.framework.security.core.authentication;

import lombok.Data;
import xyz.migoo.framework.security.core.AuthUserDetails;

import java.time.LocalDateTime;

/**
 * 用户详情获取接口
 * <p>
 * 定义 security 组件需要的认证能力（登录、token 校验、刷新、登出）。
 * 与 {@link org.springframework.security.core.userdetails.UserDetailsService} 解耦，
 * 用户加载由 {@link UserDetailsBridge} 负责。
 *
 * @author xiaomi
 */
public interface AuthUserDetailsFetcher<T extends AuthUserDetails<T, ?>> {

    /**
     * 用户认证
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证信息
     */
    LoginResult<T> authenticate(String username, String password);

    /**
     * 校验 accessToken 的有效性，并获取用户信息
     * <p>
     * 返回 null 表示未认证（调用方据此返回 401），包括四种情况:
     * token 无效（签名错误/过期/exp 缺失/格式非法）、token 类型不符（access 与 refresh 用途隔离，
     * refresh token 不得作为访问凭证）、token 已撤销（{@link UserDetailsBridge#isTokenRevoked}）/
     * 用户已被踢出（{@link UserDetailsBridge#isUserRevoked}）、用户不存在。
     * <p>
     * 基础设施异常（撤销检查、{@link UserDetailsBridge#loadByUserId} 中的用户存储故障）不在此捕获，
     * 会原样抛出，由全局异常处理返回 500，避免服务端故障伪装成 401。
     *
     * @param accessToken accessToken
     * @return 用户信息；无效/类型不符/已撤销/用户不存在时返回 null
     */
    T verifyToken(String accessToken);

    /**
     * 刷新 token
     *
     * @param refreshToken refreshToken
     * @return 认证信息
     */
    LoginResult<T> refreshToken(String refreshToken);

    /**
     * 基于 accessToken 清理登录
     *
     * @param accessToken token
     */
    void clean(String accessToken);

    /**
     * 撤销指定 token（主动踢出该次登录）
     * <p>
     * 默认委托给 {@link #clean(String)}，撤销后通过 {@link UserDetailsBridge#isTokenRevoked(String)} 校验生效。
     * 注意: access token 与 refresh token 相互独立，如需彻底踢除，请同时撤销对应的 refreshToken，
     * 或直接使用 {@link #revokeUserTokens(String)} 按用户撤销。
     *
     * @param accessToken 需要撤销的 token（accessToken 或 refreshToken）
     */
    default void revokeToken(String accessToken) {
        clean(accessToken);
    }

    /**
     * 撤销指定用户的全部 token（踢出已登录用户）
     * <p>
     * 默认空实现（预留方法）。
     * 框架默认实现 {@link DefaultJwtAuthenticator} 会委托给 {@link UserDetailsBridge#revokeByUserId(String)}，
     * 并在 {@link #verifyToken(String)} / {@link #refreshToken(String)} 中通过
     * {@link UserDetailsBridge#isUserRevoked(String)} 拦截被踢用户的请求。
     *
     * @param userId 用户编号
     */
    default void revokeUserTokens(String userId) {
        // 默认空实现，由具体认证器决定是否支持按用户撤销
    }

    /**
     * 登录/刷新 token 返回结果
     */
    @Data
    class LoginResult<T extends AuthUserDetails<T, ?>> {

        private String accessToken;

        private LocalDateTime accessExpiry;

        private String refreshToken;

        private LocalDateTime refreshExpiry;

        private T user;
    }

}
