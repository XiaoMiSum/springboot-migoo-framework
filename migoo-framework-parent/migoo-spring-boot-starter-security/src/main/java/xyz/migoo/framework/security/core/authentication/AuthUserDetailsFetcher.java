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
     *
     * @param accessToken accessToken
     * @return 用户信息
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
     * 登录/刷新 token 返回结果
     */
    @Data
    class LoginResult<T extends AuthUserDetails<T, ?>> {

        private String accessToken;

        private LocalDateTime accessExpiry;

        private String refreshToken;

        private LocalDateTime refreshExpiry;

        private T userInfo;
    }

}
