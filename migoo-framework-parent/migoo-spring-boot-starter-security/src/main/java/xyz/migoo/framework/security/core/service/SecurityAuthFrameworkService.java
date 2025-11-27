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

    Authenticated<T> authenticate(String username, String password);

    /**
     * 校验 token 的有效性，并获取用户信息
     * 通过后，刷新 token 的过期时间
     *
     * @param token token
     * @return 用户信息
     */
    T verify(String token);

    /**
     * 基于 token 清理登录
     *
     * @param token token
     */
    void clean(String token);

}
