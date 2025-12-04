package xyz.migoo.framework.security.core.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.service.dto.Authenticated;

/**
 * Security 框架 Auth Service 接口，定义 security 组件需要的功能
 *
 * @author xiaomi
 */
public interface SecurityAuthFrameworkService<T extends AuthUserDetails<T>> extends UserDetailsService {

    /**
     * 用户认证
     *
     * @param username 用户名
     * @param password 密码
     * @return 认证信息
     */
    Authenticated<T> authenticate(String username, String password);

    /**
     * 校验 accessToken 的有效性，并获取用户信息
     * 通过后，刷新 token 的过期时间
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
    Authenticated<T> refreshToken(String refreshToken);

    /**
     * 基于 accessToken 清理登录
     *
     * @param accessToken token
     */
    void clean(String accessToken);

}
