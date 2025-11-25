package xyz.migoo.framework.security.core.service;

import xyz.migoo.framework.security.core.AuthUserDetails;

/**
 * @author xiaomi
 * Created on 2021/11/21 15:41
 */
public interface LoginUserCacheService {

    /**
     * 通过sessionId 获取 AuthUserDetails
     *
     * @param sessionId sessionId
     * @return 登录用户
     */
    AuthUserDetails get(String sessionId);

    /**
     * 设置登录用户缓存
     *
     * @param sessionId       sessionId
     * @param authUserDetails 登录用户
     */
    void set(String sessionId, AuthUserDetails authUserDetails);

    /**
     * 通过 sessionId 删除登录用户缓存
     *
     * @param sessionId sessionId
     */
    void delete(String sessionId);
}
