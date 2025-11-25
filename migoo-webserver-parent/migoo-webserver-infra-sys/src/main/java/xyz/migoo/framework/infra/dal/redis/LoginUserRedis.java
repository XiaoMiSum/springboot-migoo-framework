package xyz.migoo.framework.infra.dal.redis;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.AuthUserDetails;

import java.time.Duration;

import static xyz.migoo.framework.infra.dal.redis.RedisKeyConstants.LOGIN_USER;

@Repository
public class LoginUserRedis {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SecurityProperties securityProperties;

    private static String formatKey(String sessionId) {
        return String.format(LOGIN_USER.getKeyTemplate(), sessionId);
    }

    public AuthUserDetails get(String sessionId) {
        String redisKey = formatKey(sessionId);
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), LOGIN_USER.getValueType());
    }

    public void set(String sessionId, AuthUserDetails authUserDetails) {
        String redisKey = formatKey(sessionId);
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(authUserDetails),
                Duration.ofMillis(securityProperties.getToken().getTimeout().toMillis()));
    }

    public void remove(String sessionId) {
        String redisKey = formatKey(sessionId);
        stringRedisTemplate.delete(redisKey);
    }
}
