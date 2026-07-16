package xyz.migoo.framework.security.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.service.JwtTokenProvider;
import xyz.migoo.framework.security.core.service.AuthUserDetailsService;
import xyz.migoo.framework.security.core.service.UserDetailsBridge;
import xyz.migoo.framework.security.core.service.dto.LoginResult;

import java.time.LocalDateTime;

/**
 * 默认 JWT 认证服务实现
 * <p>
 * 组合 {@link JwtTokenProvider} + {@link UserDetailsBridge}，实现 {@link AuthUserDetailsService}。
 * 当应用未自行实现 AuthUserDetailsService 时，此 Bean 自动注册。
 *
 * @author xiaomi
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RequiredArgsConstructor
public class DefaultJwtAuthService implements AuthUserDetailsService {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsBridge userBridge;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties properties;

    @Override
    @NonNull
    public AuthUserDetails loadUserByUsername(@NonNull String username) {
        AuthUserDetails user = userBridge.loadByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return user;
    }

    @Override
    public LoginResult authenticate(String username, String password) {
        AuthUserDetails user = loadUserByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }
        return buildLoginResult(user);
    }

    @Override
    public AuthUserDetails verifyToken(String token) {
        if (!tokenProvider.isTokenValid(token)) {
            return null;
        }
        Jwt jwt = tokenProvider.parseToken(token);
        String userId = tokenProvider.getUserIdFromToken(jwt);
        return userBridge.loadByUserId(userId);
    }

    @Override
    public LoginResult refreshToken(String refreshToken) {
        Jwt jwt = tokenProvider.parseToken(refreshToken);
        String type = jwt.getClaimAsString("type");
        if (!"refresh".equals(type)) {
            throw new IllegalArgumentException("不是 refresh token");
        }
        String userId = tokenProvider.getUserIdFromToken(jwt);
        AuthUserDetails user = userBridge.loadByUserId(userId);
        return buildLoginResult(user);
    }

    @Override
    public void clean(String token) {
        userBridge.clean(token);
    }

    private LoginResult buildLoginResult(AuthUserDetails user) {
        String accessToken = tokenProvider.createAccessToken(user);
        String refreshToken = tokenProvider.createRefreshToken(user);

        LoginResult result = new LoginResult();
        result.setAccessToken(accessToken);
        result.setRefreshToken(refreshToken);
        result.setAccessExpiry(LocalDateTime.now().plus(properties.getAuthorization().getAccessTokenExpires()));
        result.setRefreshExpiry(LocalDateTime.now().plus(properties.getAuthorization().getRefreshTokenExpires()));
        result.setUserInfo(user);
        return result;
    }
}
