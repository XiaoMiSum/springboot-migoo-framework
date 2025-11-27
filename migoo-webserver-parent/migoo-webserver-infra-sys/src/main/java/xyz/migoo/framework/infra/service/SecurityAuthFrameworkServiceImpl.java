package xyz.migoo.framework.infra.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSignerUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.infra.enums.SysErrorCodeConstants;
import xyz.migoo.framework.infra.service.sys.user.UserService;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.MiGooUserDetails;
import xyz.migoo.framework.security.core.service.SecurityAuthFrameworkService;
import xyz.migoo.framework.security.core.service.dto.Authenticated;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;

@Slf4j
@Service
public class SecurityAuthFrameworkServiceImpl implements SecurityAuthFrameworkService<MiGooUserDetails> {

    @Resource
    @Lazy
    private AuthenticationManager authenticationManager;

    @Resource
    private UserService userService;

    @Resource
    private SecurityProperties properties;

    @Override
    public MiGooUserDetails loadUserByUsername(String username) {
        return userService.toLoginUser(userService.get(username));
    }

    @Override
    public MiGooUserDetails verify(String token) {
        try {
            var jwt = JWTUtil.parseToken(token);
            if (!jwt.setKey(properties.getToken().getSecret().getBytes(StandardCharsets.UTF_8)).validate(0)) {
                throw ServiceExceptionUtil.get(UNAUTHORIZED);
            }
            var payload = jwt.getPayload().getClaimsJson();
            var aud = payload.get("aud", List.class);
            if (!aud.contains("Authenticated")) {
                throw ServiceExceptionUtil.get(UNAUTHORIZED);
            }
            var issuer = payload.get("iss", String.class);
            if (!StrUtil.equals("migoo", issuer)) {
                throw ServiceExceptionUtil.get(UNAUTHORIZED);
            }
            var user = JsonUtils.parseObject(payload.get("user").toString(), MiGooUserDetails.class);
            assert user != null;
            return user.setSecurityCode(SecureUtil.aes(properties.getToken().getSecret().getBytes()).encryptBase64(user.getSecurityCode()));
        } catch (Exception e) {
            throw ServiceExceptionUtil.get(UNAUTHORIZED);
        }
    }

    @Override
    public void clean(String token) {
        // todo 如果有缓存 token 则可以删除这个token
    }

    @Override
    public Authenticated<MiGooUserDetails> authenticate(String username, String password) {
        try {
            var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            var user = (MiGooUserDetails) authentication.getPrincipal();
            user.setSecurityCode(SecureUtil.aes(properties.getToken().getSecret().getBytes()).encryptBase64(user.getSecurityCode()));
            var now = DateTime.now();
            var assessExpireAt = new Date(System.currentTimeMillis() + properties.getToken().getTimeout().toMillis());
            Map<String, Object> payload = BeanUtil.beanToMap(user, false, false);
            var signer = JWTSignerUtil.hs256(properties.getToken().getSecret().getBytes());
            var accessToken = JWT.create().setSigner(signer).setPayload("user", payload)
                    .setIssuedAt(now).setIssuer("migoo").setNotBefore(now)
                    .setAudience("Authenticated").setExpiresAt(assessExpireAt).sign();
            var refreshExpireAt = new Date(System.currentTimeMillis() + properties.getToken().getRefreshTimeout().toMillis());
            var refreshToken = JWT.create().setSigner(signer)
                    .setIssuedAt(now).setIssuer("migoo").setNotBefore(now)
                    .setAudience("Authenticated").setExpiresAt(refreshExpireAt).sign(signer);
            return new Authenticated<MiGooUserDetails>().setUser(user)
                    .setAccessToken(accessToken).setAccessTokenExpiresAt(assessExpireAt)
                    .setRefreshToken(refreshToken).setRefreshTokenExpiresAt(refreshExpireAt);
        } catch (BadCredentialsException badCredentialsException) {
            throw ServiceExceptionUtil.get(SysErrorCodeConstants.AUTH_LOGIN_BAD_CREDENTIALS);
        } catch (DisabledException disabledException) {
            throw ServiceExceptionUtil.get(SysErrorCodeConstants.AUTH_LOGIN_USER_DISABLED);
        } catch (AuthenticationException authenticationException) {
            log.error("[authenticate][user({}) 发生未知异常]", username, authenticationException);
            throw ServiceExceptionUtil.get(SysErrorCodeConstants.AUTH_LOGIN_FAIL_UNKNOWN);
        }
    }
}