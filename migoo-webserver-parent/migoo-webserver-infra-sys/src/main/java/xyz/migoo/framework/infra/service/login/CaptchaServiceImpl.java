package xyz.migoo.framework.infra.service.login;

import xyz.migoo.framework.infra.controller.login.vo.CaptchaImageRespVO;
import xyz.migoo.framework.infra.dal.redis.CaptchaRedis;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.captcha.config.CaptchaProperties;

/**
 * 验证码 Service 实现类
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Resource
    private CaptchaProperties captchaProperties;

    @Resource
    private CaptchaRedis redis;

    @Override
    public CaptchaImageRespVO getCaptchaImage() {
        // 生成验证码
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(captchaProperties.getWidth(), captchaProperties.getHeight());
        // 缓存到 Redis 中
        String uuid = IdUtil.fastSimpleUUID();
        redis.set(uuid, captcha.getCode(), captchaProperties.getTimeout());
        // 返回
        return CaptchaImageRespVO.builder().uuid(uuid).img(captcha.getImageBase64()).build();
    }

    @Override
    public String getCaptchaCode(String uuid) {
        return redis.get(uuid);
    }

    @Override
    public void deleteCaptchaCode(String uuid) {
        redis.delete(uuid);
    }

}
