package xyz.migoo.framework.security.core.authentication;

import lombok.Data;
import org.springframework.security.core.userdetails.UserDetailsService;
import xyz.migoo.framework.security.core.AuthUserDetails;

import java.time.LocalDateTime;

/**
 * 用户详情获取接口
 * <p>
 * 定义 security 组件需要的用户加载与认证能力
 *
 * @author xiaomi
 */
public interface AuthUserDetailsFetcher<T extends AuthUserDetails<T, ?>> extends UserDetailsService {

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
