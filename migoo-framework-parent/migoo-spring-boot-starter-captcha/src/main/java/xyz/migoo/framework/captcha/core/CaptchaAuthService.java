package xyz.migoo.framework.captcha.core;

/**
 * @author xiaomi
 * Created on 2021/11/21 15:55
 */
public interface CaptchaAuthService {

    /**
     * 获得验证码图片
     *
     * @return 验证码图片
     */
    CaptchaImage getCaptchaImage();

    /**
     * 获得 uuid 对应的验证码
     *
     * @param uuid 验证码编号
     * @return 验证码
     */
    String getCaptchaCode(String uuid);

    /**
     * 删除 uuid 对应的验证码
     *
     * @param uuid 验证码编号
     */
    void deleteCaptchaCode(String uuid);
}
