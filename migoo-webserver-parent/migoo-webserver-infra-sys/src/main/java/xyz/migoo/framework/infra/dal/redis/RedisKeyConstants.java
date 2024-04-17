package xyz.migoo.framework.infra.dal.redis;

import xyz.migoo.framework.redis.core.RedisKeyDefine;
import xyz.migoo.framework.security.core.LoginUser;

import java.time.Duration;

import static xyz.migoo.framework.redis.core.RedisKeyDefine.KeyTypeEnum.STRING;

public interface RedisKeyConstants {

    RedisKeyDefine<LoginUser> LOGIN_USER = new RedisKeyDefine<>("登录用户的缓存",
            "login_user:%s", // 参数为 sessionId
            STRING, LoginUser.class, RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);

    RedisKeyDefine<String> CAPTCHA_CODE = new RedisKeyDefine<>("验证码的缓存",
            "captcha_code:%s", // 参数为 uuid
            STRING, String.class, RedisKeyDefine.TimeoutTypeEnum.DYNAMIC);

    RedisKeyDefine<String> RUNNER_USERID = new RedisKeyDefine<>("三方应用UserId的缓存",
            "runner_userid:%s", // 参数为 uuid
            STRING, String.class, Duration.ofDays(1L));
}