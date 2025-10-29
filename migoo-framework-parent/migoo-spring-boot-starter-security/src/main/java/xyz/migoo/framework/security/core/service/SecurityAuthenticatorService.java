package xyz.migoo.framework.security.core.service;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Strings;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.LoginUser;
import xyz.migoo.framework.security.core.service.dto.AuthenticatorDTO;
import xyz.migoo.framework.security.core.util.GoogleAuthenticator;
import xyz.migoo.framework.security.core.util.SecurityFrameworkUtils;

/**
 * @author xiaomi
 * Created on 2021/11/21 15:41
 */
@Component
public class SecurityAuthenticatorService {

    @Resource
    private SecuritySessionAuthService loginUserService;
    @Resource
    private SecurityProperties properties;

    public void verify(HttpServletRequest request) {
        String code = request.getParameter("_code");
        String token = SecurityFrameworkUtils.obtainAuthorization(request, properties.getToken().getHeaderName());
        // 兼容绑定身份验证器时，token 在请求参数中
        LoginUser loginUser = loginUserService.getLoginUser(!Strings.isNullOrEmpty(token) ? token :
                request.getParameter("_token"));
        // assert Objects.nonNull(loginUser);
        if (!loginUser.isRequiredVerifyAuthenticator()) {
            return;
        }
        if ((StrUtil.isBlankIfStr(code)) || !GoogleAuthenticator.verify(loginUser.getSecurityCode(), code)) {
            throw ServiceExceptionUtil.get(new ErrorCode(999, "2fa.failure"));
        }
    }

    public AuthenticatorDTO generate(String user, String host) {
        String secret = GoogleAuthenticator.generateSecretKey();
        String quickMark = String.format("otpauth://totp/%s@%s%%3Fsecret%%3D%s&issuer=%s", user, host, secret, user);
        return new AuthenticatorDTO(secret, quickMark);
    }
}