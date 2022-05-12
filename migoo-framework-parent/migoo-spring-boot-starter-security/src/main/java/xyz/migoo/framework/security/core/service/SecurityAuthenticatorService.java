package xyz.migoo.framework.security.core.service;

import org.springframework.stereotype.Component;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.security.core.LoginUser;
import xyz.migoo.framework.security.core.service.dto.AuthenticatorDTO;
import xyz.migoo.framework.security.core.util.GoogleAuthenticator;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;

import java.util.Objects;

/**
 * @author xiaomi
 * Created on 2021/11/21 15:41
 */
@Component
public class SecurityAuthenticatorService {

    public void verify(String code) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        assert Objects.nonNull(loginUser);
        if (!GoogleAuthenticator.verify(loginUser.getSecurityCode(), code)) {
            throw ServiceExceptionUtil.get(new ErrorCode(999, "身份验证失败!"));
        }
    }

    public AuthenticatorDTO generate(String user, String host) {
        String secret = GoogleAuthenticator.generateSecretKey();
        String quickMark = String.format("otpauth://totp/%s@%s%%3Fsecret%%3D%s&issuer=%s", user, host, secret, user);
        return new AuthenticatorDTO(secret, quickMark);
    }
}