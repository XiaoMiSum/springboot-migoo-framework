package xyz.migoo.framework.security.core.service;

import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.security.core.service.dto.AuthenticatorDTO;
import xyz.migoo.framework.security.core.util.GoogleAuthenticator;

/**
 * @author xiaomi
 * Created on 2021/11/21 15:41
 */
public interface SecurityAuthenticatorService {
    /**
     * 1. 获取不到登录用户，返回token过期
     * 2. 获取不到登录用户的SecurityCode，返回身份验证失败
     */
    String getSecurityCode(String token);

    default void verify(String securityCode, String code) {
        if (!GoogleAuthenticator.verify(securityCode, code)) {
            throw ServiceExceptionUtil.get(new ErrorCode(999, "身份验证失败!"));
        }
    }

    default AuthenticatorDTO generate(String user, String host) {
        String secret = GoogleAuthenticator.generateSecretKey();
        String quickMark = String.format("otpauth://totp/%s@%s%%3Fsecret%%3D%s&issuer=%s", user, host, secret, user);
        return new AuthenticatorDTO(secret, quickMark);
    }
}