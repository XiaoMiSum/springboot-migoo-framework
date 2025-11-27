package xyz.migoo.framework.security.core.service;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.security.core.service.dto.AuthenticatorDTO;
import xyz.migoo.framework.security.core.util.GoogleAuthenticator;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;

import java.util.Objects;

/**
 * @author xiaomi
 * Created on 2021/11/21 15:41
 */
public class SecurityAuthenticatorService {

    public void verify(HttpServletRequest request) {
        String code = request.getParameter("_code");
        var authUserDetails = SecurityFrameworkUtils.getLoginUser();
        assert Objects.nonNull(authUserDetails);
        if (!authUserDetails.isRequiredVerifyAuthenticator()) {
            return;
        }
        if ((StrUtil.isBlankIfStr(code)) || !GoogleAuthenticator.verify(authUserDetails.getSecurityCode(), code)) {
            throw ServiceExceptionUtil.get(new ErrorCode(999, "2fa.failure"));
        }
    }

    public AuthenticatorDTO generate(String user, String host) {
        String secret = GoogleAuthenticator.generateSecretKey();
        String quickMark = String.format("otpauth://totp/%s@%s%%3Fsecret%%3D%s&issuer=%s", user, host, secret, user);
        return new AuthenticatorDTO(secret, quickMark);
    }
}