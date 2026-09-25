package xyz.migoo.framework.security.core.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import xyz.migoo.framework.common.exception.ServiceException;
import xyz.migoo.framework.common.observability.AccountLockedEvent;
import xyz.migoo.framework.common.observability.AuthenticationFailedEvent;
import xyz.migoo.framework.common.observability.TokenRevokedEvent;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.TestAuthUser;
import xyz.migoo.framework.security.core.lockout.LoginLockManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link DefaultJwtAuthenticator} 单元测试
 * <p>
 * 覆盖 verifyToken 分层异常处理（预期无效 → 401、非预期 → 500）、refreshToken 状态码映射（400/401）、
 * 撤销拦截，以及登录失败锁定集成（423）
 */
class DefaultJwtAuthenticatorTest {

    private JwtTokenProvider tokenProvider;
    private UserDetailsBridge bridge;
    private AuthenticationManager authenticationManager;
    private SecurityProperties properties;
    private LoginLockManager lockManager;
    private ApplicationEventPublisher eventPublisher;
    private DefaultJwtAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(JwtTokenProvider.class);
        bridge = mock(UserDetailsBridge.class);
        authenticationManager = mock(AuthenticationManager.class);
        properties = new SecurityProperties();
        lockManager = mock(LoginLockManager.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        authenticator = new DefaultJwtAuthenticator(tokenProvider, bridge, authenticationManager,
                properties, lockManager, eventPublisher);
    }

    // ==================== verifyToken: 分层异常处理 ====================

    @Test
    void verifyTokenInvalidOrExpiredTokenReturnsNull() {
        // 过期（JwtException）与非法入参（IllegalArgumentException）→ 未认证 401
        when(tokenProvider.parseToken("expired")).thenThrow(new JwtException("Jwt expired at ..."));
        assertThat(authenticator.verifyToken("expired")).isNull();

        when(tokenProvider.parseToken("malformed")).thenThrow(new IllegalArgumentException("empty"));
        assertThat(authenticator.verifyToken("malformed")).isNull();
    }

    @Test
    void verifyTokenUnexpectedExceptionPropagates() {
        // 非预期异常（自定义 provider 实现缺陷）→ 不吞，上抛 → 500
        when(tokenProvider.parseToken("boom")).thenThrow(new IllegalStateException("boom"));
        assertThatThrownBy(() -> authenticator.verifyToken("boom"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    void verifyTokenMissingUserIdClaimReturnsNull() {
        Jwt jwt = mock(Jwt.class);
        when(tokenProvider.parseToken("t")).thenReturn(jwt);
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn(null);
        assertThat(authenticator.verifyToken("t")).isNull();
        verify(bridge, never()).loadByUserId(any());
    }

    @Test
    void verifyTokenRevokedTokenReturnsNull() {
        Jwt jwt = mock(Jwt.class);
        when(tokenProvider.parseToken("t")).thenReturn(jwt);
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn("1");
        when(jwt.getClaimAsString("type")).thenReturn("access");
        when(bridge.isTokenRevoked("t")).thenReturn(true);
        assertThat(authenticator.verifyToken("t")).isNull();
        verify(bridge, never()).loadByUserId(any());
    }

    @Test
    void verifyTokenRevokedUserReturnsNull() {
        Jwt jwt = mock(Jwt.class);
        when(tokenProvider.parseToken("t")).thenReturn(jwt);
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn("1");
        when(jwt.getClaimAsString("type")).thenReturn("access");
        when(bridge.isTokenRevoked("t")).thenReturn(false);
        when(bridge.isUserRevoked("1")).thenReturn(true);
        assertThat(authenticator.verifyToken("t")).isNull();
        verify(bridge, never()).loadByUserId(any());
    }

    @Test
    void verifyTokenReturnsUserWhenValid() {
        Jwt jwt = mock(Jwt.class);
        var user = TestAuthUser.of(1L, "admin");
        when(tokenProvider.parseToken("t")).thenReturn(jwt);
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn("1");
        when(jwt.getClaimAsString("type")).thenReturn("access");
        when(bridge.isTokenRevoked("t")).thenReturn(false);
        when(bridge.isUserRevoked("1")).thenReturn(false);
        doReturn(user).when(bridge).loadByUserId("1");
        assertThat(authenticator.verifyToken("t")).isSameAs(user);
    }

    @Test
    void verifyTokenInfrastructureExceptionPropagates() {
        // 撤销检查/用户加载的基础设施故障不被吞掉 → 500（而非伪装成 401）
        Jwt jwt = mock(Jwt.class);
        when(tokenProvider.parseToken("t")).thenReturn(jwt);
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn("1");
        when(jwt.getClaimAsString("type")).thenReturn("access");
        when(bridge.isTokenRevoked("t")).thenReturn(false);
        when(bridge.isUserRevoked("1")).thenReturn(false);
        when(bridge.loadByUserId("1")).thenThrow(new IllegalStateException("db down"));
        assertThatThrownBy(() -> authenticator.verifyToken("t"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("db down");
    }

    // ==================== refreshToken: 400/401 映射 ====================

    @Test
    void verifyTokenRejectsRefreshTypeToken() {
        // refresh token 不得作为访问凭证（type 用途隔离）→ null → 401
        Jwt jwt = mock(Jwt.class);
        when(tokenProvider.parseToken("rt")).thenReturn(jwt);
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn("1");
        when(jwt.getClaimAsString("type")).thenReturn("refresh");

        assertThat(authenticator.verifyToken("rt")).isNull();
        verify(bridge, never()).loadByUserId(any());
    }

    @Test
    void refreshTokenUserNotFoundThrows401() {
        // refresh token 合法但用户已被删除 → 401（而非 loadByUserId(null) NPE → 500）
        Jwt jwt = mock(Jwt.class);
        when(tokenProvider.parseToken("rt")).thenReturn(jwt);
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn("9");
        when(jwt.getClaimAsString("type")).thenReturn("refresh");
        when(bridge.isTokenRevoked("rt")).thenReturn(false);
        when(bridge.isUserRevoked("9")).thenReturn(false);
        when(bridge.loadByUserId("9")).thenReturn(null);

        assertThatThrownBy(() -> authenticator.refreshToken("rt"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 401);
    }

    @Test
    void refreshTokenInvalidOrExpiredThrows400() {
        when(tokenProvider.parseToken("bad-rt")).thenThrow(new JwtException("Jwt expired at ..."));
        assertThatThrownBy(() -> authenticator.refreshToken("bad-rt"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 400);
    }

    @Test
    void refreshTokenTypeMismatchThrows400() {
        Jwt jwt = mock(Jwt.class);
        when(tokenProvider.parseToken("rt")).thenReturn(jwt);
        when(jwt.getClaimAsString("type")).thenReturn("access");
        assertThatThrownBy(() -> authenticator.refreshToken("rt"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 400);
    }

    @Test
    void refreshTokenMissingUserIdThrows400() {
        Jwt jwt = mock(Jwt.class);
        when(tokenProvider.parseToken("rt")).thenReturn(jwt);
        when(jwt.getClaimAsString("type")).thenReturn("refresh");
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn(null);
        assertThatThrownBy(() -> authenticator.refreshToken("rt"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 400);
    }

    @Test
    void refreshTokenRevokedThrows401() {
        Jwt jwt = mock(Jwt.class);
        when(tokenProvider.parseToken("rt")).thenReturn(jwt);
        when(jwt.getClaimAsString("type")).thenReturn("refresh");
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn("1");
        when(bridge.isTokenRevoked("rt")).thenReturn(true);
        assertThatThrownBy(() -> authenticator.refreshToken("rt"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 401);
    }

    @Test
    void refreshTokenReturnsNewLoginResult() {
        Jwt jwt = mock(Jwt.class);
        var user = TestAuthUser.of(1L, "admin");
        when(tokenProvider.parseToken("rt")).thenReturn(jwt);
        when(jwt.getClaimAsString("type")).thenReturn("refresh");
        when(tokenProvider.getUserIdFromToken(jwt)).thenReturn("1");
        when(bridge.isTokenRevoked("rt")).thenReturn(false);
        when(bridge.isUserRevoked("1")).thenReturn(false);
        doReturn(user).when(bridge).loadByUserId("1");
        when(tokenProvider.createAccessToken(user)).thenReturn("new-at");
        when(tokenProvider.createRefreshToken(user)).thenReturn("new-rt");

        var result = authenticator.refreshToken("rt");
        assertThat(result.getAccessToken()).isEqualTo("new-at");
        assertThat(result.getRefreshToken()).isEqualTo("new-rt");
        assertThat(result.getUser()).isSameAs(user);
    }

    // ==================== authenticate: 登录失败锁定 ====================

    @Test
    void authenticateLockedAccountThrows423WithoutAuthenticating() {
        when(lockManager.isLocked("admin")).thenReturn(true);
        assertThatThrownBy(() -> authenticator.authenticate("admin", "pwd"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 423);
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void authenticateFailureHittingLockThrows423() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(lockManager.onFailure("admin")).thenReturn(true);
        assertThatThrownBy(() -> authenticator.authenticate("admin", "pwd"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 423);
    }

    @Test
    void authenticateFailureNotHittingLockThrows401() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(lockManager.onFailure("admin")).thenReturn(false);
        assertThatThrownBy(() -> authenticator.authenticate("admin", "pwd"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 401);
    }

    @Test
    void authenticateSuccessClearsFailureCount() {
        var user = TestAuthUser.full(1L, "admin", "pwd");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.createAccessToken(user)).thenReturn("at");
        when(tokenProvider.createRefreshToken(user)).thenReturn("rt");

        var result = authenticator.authenticate("admin", "pwd");
        assertThat(result.getAccessToken()).isEqualTo("at");
        assertThat(result.getUser()).isSameAs(user);
        verify(lockManager).onSuccess("admin");
    }

    @Test
    void authenticateWithLockDisabledSkipsLockManager() {
        properties.getLoginLock().setEnabled(false);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        assertThatThrownBy(() -> authenticator.authenticate("admin", "pwd"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 401);
        verifyNoInteractions(lockManager);
    }

    // ==================== authenticate / 撤销: 可观测性信号事件 ====================

    @Test
    void authenticateFailurePublishesAuthenticationFailedEvent() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(lockManager.onFailure("admin")).thenReturn(false);

        assertThatThrownBy(() -> authenticator.authenticate("admin", "pwd"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 401);

        verify(eventPublisher).publishEvent(new AuthenticationFailedEvent("BadCredentialsException"));
        verify(eventPublisher, never()).publishEvent(any(AccountLockedEvent.class));
    }

    @Test
    void authenticateLockedAccountPublishesAlreadyLockedEvent() {
        when(lockManager.isLocked("admin")).thenReturn(true);

        assertThatThrownBy(() -> authenticator.authenticate("admin", "pwd"))
                .isInstanceOf(ServiceException.class);

        verify(eventPublisher).publishEvent(new AccountLockedEvent("already_locked"));
        verify(eventPublisher, never()).publishEvent(any(AuthenticationFailedEvent.class));
        verifyNoInteractions(authenticationManager);
    }

    @Test
    void authenticateFailureHittingLockPublishesBothEvents() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(lockManager.onFailure("admin")).thenReturn(true);

        assertThatThrownBy(() -> authenticator.authenticate("admin", "pwd"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 423);

        verify(eventPublisher).publishEvent(new AuthenticationFailedEvent("BadCredentialsException"));
        verify(eventPublisher).publishEvent(new AccountLockedEvent("failure_threshold"));
    }

    @Test
    void cleanPublishesTokenRevokedWithTokenScope() {
        authenticator.clean("some-token");

        verify(bridge).clean("some-token");
        verify(eventPublisher).publishEvent(new TokenRevokedEvent("token"));
    }

    @Test
    void revokeUserTokensPublishesTokenRevokedWithUserScope() {
        authenticator.revokeUserTokens("1024");

        verify(bridge).revokeByUserId("1024");
        verify(eventPublisher).publishEvent(new TokenRevokedEvent("user"));
    }

    @Test
    void publishFailureDoesNotBreakAuthentication() {
        // 监听器异常经发布器抛回 → 认证流程照常按 401 收场（观测不得影响业务）
        doThrow(new IllegalStateException("listener failed")).when(eventPublisher).publishEvent(any());
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        when(lockManager.onFailure("admin")).thenReturn(false);

        assertThatThrownBy(() -> authenticator.authenticate("admin", "pwd"))
                .isInstanceOf(ServiceException.class)
                .hasFieldOrPropertyWithValue("code", 401);
    }
}
