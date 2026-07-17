package xyz.migoo.framework.security.core.authentication;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import xyz.migoo.framework.common.exception.enums.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.AuthUserDetails;

import java.time.LocalDateTime;

/**
 * 默认 JWT 认证器实现
 * <p>
 * 组合 {@link JwtTokenProvider} + {@link UserDetailsBridge}，实现 {@link AuthUserDetailsFetcher}。
 * 当应用未自行实现 AuthUserDetailsFetcher 时，此 Bean 自动注册。
 *
 * @author xiaomi
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RequiredArgsConstructor
public class DefaultJwtAuthenticator implements AuthUserDetailsFetcher {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsBridge userBridge;
    private final AuthenticationManager authenticationManager;
    private final SecurityProperties properties;

    @Override
    @NonNull
    public AuthUserDetails loadUserByUsername(@NonNull String username) {
        AuthUserDetails user = userBridge.loadByUsername(username);
        if (user == null) {
            throw ServiceExceptionUtil.get(GlobalErrorCodeConstants.INVALID_AUTHORIZED);
        }
        return user;
    }

    @Override
    public AuthUserDetailsFetcher.LoginResult authenticate(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            AuthUserDetails user = (AuthUserDetails) authentication.getPrincipal();
            return buildLoginResult(user);
        } catch (Exception e) {
            throw ServiceExceptionUtil.get(GlobalErrorCodeConstants.INVALID_AUTHORIZED);
        }
    }

    @Override
    public AuthUserDetails verifyToken(String token) {
        try {
            Jwt jwt = tokenProvider.parseToken(token);
            String userId = tokenProvider.getUserIdFromToken(jwt);
            return userBridge.loadByUserId(userId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public AuthUserDetailsFetcher.LoginResult refreshToken(String refreshToken) {
        Jwt jwt = tokenProvider.parseToken(refreshToken);
        String type = jwt.getClaimAsString("type");
        if (!"refresh".equals(type)) {
            throw ServiceExceptionUtil.get(GlobalErrorCodeConstants.INVALID_AUTHORIZED);
        }
        String userId = tokenProvider.getUserIdFromToken(jwt);
        AuthUserDetails user = userBridge.loadByUserId(userId);
        return buildLoginResult(user);
    }

    @Override
    public void clean(String token) {
        userBridge.clean(token);
    }

    private AuthUserDetailsFetcher.LoginResult buildLoginResult(AuthUserDetails user) {
        String accessToken = tokenProvider.createAccessToken(user);
        String refreshToken = tokenProvider.createRefreshToken(user);

        AuthUserDetailsFetcher.LoginResult result = new AuthUserDetailsFetcher.LoginResult();
        result.setAccessToken(accessToken);
        result.setRefreshToken(refreshToken);
        result.setAccessExpiry(LocalDateTime.now().plus(properties.getJwt().getAccessTokenExpires()));
        result.setRefreshExpiry(LocalDateTime.now().plus(properties.getJwt().getRefreshTokenExpires()));
        result.setUserInfo(user);
        return result;
    }
}
