package xyz.migoo.framework.infra.service.login;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.infra.dal.redis.LoginUserRedis;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.service.LoginUserCacheService;

@Slf4j
@Service
public class LoginUserCacheServiceImpl implements LoginUserCacheService {

    @Resource
    private LoginUserRedis usersCache;

    @Override
    public AuthUserDetails get(String sessionId) {
        return usersCache.get(sessionId);
    }

    @Override
    public void set(String sessionId, AuthUserDetails authUserDetails) {
        usersCache.set(sessionId, authUserDetails);
    }

    @Override
    public void delete(String sessionId) {
        usersCache.remove(sessionId);
    }
}
