package xyz.migoo.framework.infra.service.login;

import cn.hutool.core.util.IdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.service.LoginUserCacheService;
import xyz.migoo.framework.security.core.service.SecuritySessionAuthService;

import java.util.Date;

@Service
@Slf4j
public class SecuritySessionAuthServiceImpl implements SecuritySessionAuthService {

    @Resource
    private LoginUserCacheService loginUserCacheService;
    @Resource
    private SecurityProperties securityProperties;

    /**
     * 生成 Session 编号，目前采用 UUID 算法
     *
     * @return Session 编号
     */
    private static String generateSessionId() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    public String createUserSession(AuthUserDetails authUserDetails, String... params) {
        // 生成 Session 编号
        String sessionId = generateSessionId();
        // 写入 缓存
        authUserDetails.setUpdateTime(new Date());
        loginUserCacheService.set(sessionId, authUserDetails);
        // 返回 Session 编号
        return sessionId;
    }

    @Override
    public void refreshUserSession(String sessionId, AuthUserDetails authUserDetails) {
        authUserDetails.setUpdateTime(new Date());
        loginUserCacheService.set(sessionId, authUserDetails);
    }

    @Override
    public void deleteUserSession(String sessionId) {
        loginUserCacheService.delete(sessionId);
    }

    @Override
    public AuthUserDetails getLoginUser(String sessionId) {
        return loginUserCacheService.get(sessionId);
    }

    @Override
    public Long getSessionTimeoutMillis() {
        return securityProperties.getToken().getTimeout().toMillis();
    }
}
