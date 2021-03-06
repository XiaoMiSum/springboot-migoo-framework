package xyz.migoo.framework.security.core.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import xyz.migoo.framework.security.core.LoginUser;

/**
 * Security 框架 Auth Service 接口，定义 security 组件需要的功能
 *
 * @author xiaomi
 */
public interface SecurityAuthFrameworkService extends UserDetailsService {

    /**
     * 校验 token 的有效性，并获取用户信息
     * 通过后，刷新 token 的过期时间
     *
     * @param token token
     * @return 用户信息
     */
    LoginUser verifyTokenAndRefresh(String token);

    /**
     * 基于 token 退出登录
     *
     * @param token token
     */
    void signOut(String token);

}
