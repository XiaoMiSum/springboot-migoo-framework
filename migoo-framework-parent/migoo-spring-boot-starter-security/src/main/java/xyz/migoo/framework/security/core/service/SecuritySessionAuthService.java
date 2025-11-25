package xyz.migoo.framework.security.core.service;

import xyz.migoo.framework.security.core.AuthUserDetails;

/**
 * 在线用户 Session Core Service 接口
 *
 * @author xiaomi
 * Created on 2021/11/21 15:34
 */
public interface SecuritySessionAuthService {

    /**
     * 创建在线用户 Session
     *
     * @param authUserDetails 登录用户
     * @param params          扩展参数
     * @return Session 编号
     */
    String createUserSession(AuthUserDetails authUserDetails, String... params);

    /**
     * 刷新在线用户 Session 的更新时间
     *
     * @param sessionId       Session 编号
     * @param authUserDetails 登录用户
     */
    void refreshUserSession(String sessionId, AuthUserDetails authUserDetails);

    /**
     * 删除在线用户 Session
     *
     * @param sessionId Session 编号
     */
    void deleteUserSession(String sessionId);

    /**
     * 获得 Session 编号对应的在线用户
     *
     * @param sessionId Session 编号
     * @return 在线用户
     */
    AuthUserDetails getLoginUser(String sessionId);

    /**
     * 获得 Session 超时时间，单位：毫秒
     *
     * @return 超时时间
     */
    Long getSessionTimeoutMillis();
}
