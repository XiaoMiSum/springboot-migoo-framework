package xyz.migoo.framework.infra.service.login;

import xyz.migoo.framework.infra.controller.login.vo.CaptchaImageRespVO;

public interface CaptchaService {
    CaptchaImageRespVO getCaptchaImage();

    String getCaptchaCode(String uuid);

    void deleteCaptchaCode(String uuid);
}
