package xyz.migoo.framework.security.core.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import xyz.migoo.framework.common.exception.GlobalErrorCodeConstants;
import xyz.migoo.framework.common.exception.ServiceExceptionUtil;
import xyz.migoo.framework.common.observability.AccountLockedEvent;
import xyz.migoo.framework.common.observability.AuthenticationFailedEvent;
import xyz.migoo.framework.common.observability.TokenRevokedEvent;
import xyz.migoo.framework.security.config.SecurityProperties;
import xyz.migoo.framework.security.core.AuthUserDetails;
import xyz.migoo.framework.security.core.lockout.LoginLockManager;

import java.time.LocalDateTime;

import static xyz.migoo.framework.common.exception.GlobalErrorCodeConstants.INVALID_REFRESH_TOKEN;
import static xyz.migoo.framework.common.exception.GlobalErrorCodeConstants.UNAUTHORIZED;

/**
 * 默认 JWT 认证器实现
 * <p>
 * 组合 {@link JwtTokenProvider} + {@link UserDetailsBridge}，实现 {@link AuthUserDetailsFetcher}。
 * 当应用未自行实现 AuthUserDetailsFetcher 时，此 Bean 自动注册。
 * <p>
 * 三块职责:
 * <ul>
 *     <li>登录 {@link #authenticate}: 组合 AuthenticationManager，集成 {@link LoginLockManager}
 *         （由 {@code migoo.security.login-lock} 控制，总开关关闭时不锁定）</li>
 *     <li>token 校验 {@link #verifyToken} / {@link #refreshToken}: 共用 {@link #verifyAndLoadUser}
 *         统一管线（机械校验 → 类型校验 → 撤销拦截 → 加载用户），差异只在错误映射</li>
 *     <li>撤销 {@link #clean} / {@link #revokeUserTokens}: 委托 {@link UserDetailsBridge} 钩子</li>
 * </ul>
 *
 * @author xiaomi
 */
@Slf4j
@SuppressWarnings({"unchecked", "rawtypes"})
public class DefaultJwtAuthenticator implements AuthUserDetailsFetcher {

    /**
     * token 类型: 访问令牌（verifyToken 只接受该类型）
     */
    private static final String TYPE_ACCESS = "access";

    /**
     * token 类型: 刷新令牌（refreshToken 只接受该类型）
     */
    private static final String TYPE_REFRESH = "refresh";

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsBridge userBridge;
    private final AuthenticationManager authenticationManager;
    private final SecurityProperties properties;
    private final LoginLockManager lockManager;

    /**
     * 事件发布器（可观测性信号，可空：直连构造时允许不发布）
     */
    private final ApplicationEventPublisher eventPublisher;

    public DefaultJwtAuthenticator(JwtTokenProvider tokenProvider,
                                   UserDetailsBridge userBridge,
                                   AuthenticationManager authenticationManager,
                                   SecurityProperties properties,
                                   LoginLockManager lockManager) {
        this(tokenProvider, userBridge, authenticationManager, properties, lockManager, null);
    }

    public DefaultJwtAuthenticator(JwtTokenProvider tokenProvider,
                                   UserDetailsBridge userBridge,
                                   AuthenticationManager authenticationManager,
                                   SecurityProperties properties,
                                   LoginLockManager lockManager,
                                   ApplicationEventPublisher eventPublisher) {
        this.tokenProvider = tokenProvider;
        this.userBridge = userBridge;
        this.authenticationManager = authenticationManager;
        this.properties = properties;
        this.lockManager = lockManager;
        this.eventPublisher = eventPublisher;
    }

    // ==================== 登录 ====================

    @Override
    public AuthUserDetailsFetcher.LoginResult authenticate(String username, String password) {
        boolean lockEnabled = properties.getLoginLock().isEnabled();
        // 认证前: 检查账号是否已被登录失败策略锁定（423）
        if (lockEnabled && lockManager.isLocked(username)) {
            publishEvent(new AccountLockedEvent("already_locked"));
            throw ServiceExceptionUtil.get(GlobalErrorCodeConstants.ACCOUNT_LOCKED);
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            // 认证成功: 清零连续失败计数
            if (lockEnabled) {
                lockManager.onSuccess(username);
            }
            return buildLoginResult((AuthUserDetails) authentication.getPrincipal());
        } catch (AuthenticationException e) {
            publishEvent(new AuthenticationFailedEvent(e.getClass().getSimpleName()));
            // 认证失败: 记录失败次数并评估锁定策略，命中则抛出 423
            if (lockEnabled && lockManager.onFailure(username)) {
                publishEvent(new AccountLockedEvent("failure_threshold"));
                throw ServiceExceptionUtil.get(GlobalErrorCodeConstants.ACCOUNT_LOCKED);
            }
            throw ServiceExceptionUtil.get(UNAUTHORIZED);
        }
    }

    // ==================== token 校验 ====================

    @Override
    public AuthUserDetails verifyToken(String token) {
        try {
            return verifyAndLoadUser(token, TYPE_ACCESS);
        } catch (TokenVerifyException ignored) {
            return null;
        }
    }

    @Override
    public AuthUserDetailsFetcher.LoginResult refreshToken(String refreshToken) {
        try {
            AuthUserDetails user = verifyAndLoadUser(refreshToken, TYPE_REFRESH);
            // 用户已被删除 → 401（而非 buildLoginResult(null) 触发 NPE → 500）
            if (user == null) {
                throw ServiceExceptionUtil.get(UNAUTHORIZED);
            }
            return buildLoginResult(user);
        } catch (TokenVerifyException e) {
            // 撤销/被踢出 → 401；其余（格式/过期/exp 缺失/类型不符/缺 userId）→ 400
            throw ServiceExceptionUtil.get(e.isRevoked() ? UNAUTHORIZED : INVALID_REFRESH_TOKEN);
        }

    }

    // ==================== 撤销 / 登出 ====================

    @Override
    public void clean(String token) {
        userBridge.clean(token);
        publishEvent(new TokenRevokedEvent("token"));
    }

    @Override
    public void revokeUserTokens(String userId) {
        userBridge.revokeByUserId(userId);
        publishEvent(new TokenRevokedEvent("user"));
    }

    // ==================== 内部实现 ====================

    /**
     * 发布可观测性信号事件（观测不得影响业务：发布异常只记日志）
     *
     * @param event common 中的信号事件 record
     */
    private void publishEvent(Object event) {
        if (eventPublisher == null) {
            return;
        }
        try {
            eventPublisher.publishEvent(event);
        } catch (Exception ex) {
            log.warn("[publishEvent][发布可观测性事件失败] event({})", event, ex);
        }
    }

    /**
     * 统一 token 认证管线（verifyToken 与 refreshToken 共用，相似校验收敛于此）
     * <p>
     * 逐步校验: 机械校验 → userId → 类型（用途隔离）→ 撤销 → 加载用户。
     * 预期失败抛 {@link TokenVerifyException}，由调用方映射各自的状态码
     * （verifyToken → null/401，refreshToken → 400/401）；
     * 撤销检查、用户加载的基础设施异常不拦截，原样上抛 → 500，避免服务端故障伪装成认证失败。
     *
     * @param token        待校验 token
     * @param requiredType 要求的类型（{@code access} / {@code refresh}），用途隔离
     * @return 加载到的用户；用户不存在返回 null
     */
    private AuthUserDetails verifyAndLoadUser(String token, String requiredType) {
        Jwt jwt = requireParseable(token);
        String userId = requireUserId(jwt, token);
        requireType(jwt, requiredType, token);
        requireNotRevoked(token, userId);
        return userBridge.loadByUserId(userId);
    }

    /**
     * 机械校验: 签名/过期/exp 缺失，由 {@link JwtTokenProvider#parseToken} 完成
     * <p>
     * 预期的无效 token → 控制流异常（调用方映射 401/400）；
     * 自定义 JwtTokenProvider 实现缺陷 → 不吞，记 error 后上抛 → 500
     */
    private Jwt requireParseable(String token) {
        try {
            return tokenProvider.parseToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("[verifyAndLoadUser][token 无效({}) token({})]", e.getMessage(), maskToken(token));
            throw TokenVerifyException.invalid(e.getMessage());
        } catch (Exception e) {
            log.error("[verifyAndLoadUser][解析 token 非预期异常 token({})]", maskToken(token), e);
            throw e;
        }
    }

    /**
     * 提取 userId claim（缺失视为无效 token，避免穿透到 loadByUserId(null) → NPE 500）
     */
    private String requireUserId(Jwt jwt, String token) {
        String userId = tokenProvider.getUserIdFromToken(jwt);
        if (userId == null) {
            log.warn("[verifyAndLoadUser][token 缺少 userId claim token({})]", maskToken(token));
            throw TokenVerifyException.invalid("缺少 userId claim");
        }
        return userId;
    }

    /**
     * 类型校验（access/refresh 用途隔离）: refresh 不得作为访问凭证，access 不得用于刷新
     */
    private void requireType(Jwt jwt, String requiredType, String token) {
        if (!requiredType.equals(jwt.getClaimAsString("type"))) {
            log.debug("[verifyAndLoadUser][token 类型不符，要求({}) token({})]", requiredType, maskToken(token));
            throw TokenVerifyException.invalid("token 类型不符");
        }
    }

    /**
     * 撤销校验: token 已撤销 / 用户已被踢出（info 日志留作踢人审计线索）
     */
    private void requireNotRevoked(String token, String userId) {
        if (userBridge.isTokenRevoked(token) || userBridge.isUserRevoked(userId)) {
            log.info("[verifyAndLoadUser][token 已撤销或用户已被踢出 userId({})]", userId);
            throw TokenVerifyException.revoked();
        }
    }

    /**
     * 脱敏 token（避免 bearer token 明文进入日志）
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 16) {
            return "***";
        }
        return token.substring(0, 16) + "***";
    }

    private AuthUserDetailsFetcher.LoginResult buildLoginResult(AuthUserDetails user) {
        String accessToken = tokenProvider.createAccessToken(user);
        String refreshToken = tokenProvider.createRefreshToken(user);

        AuthUserDetailsFetcher.LoginResult result = new AuthUserDetailsFetcher.LoginResult();
        result.setAccessToken(accessToken);
        result.setRefreshToken(refreshToken);
        result.setAccessExpiry(LocalDateTime.now().plus(properties.getJwt().getAccessTokenExpires()));
        result.setRefreshExpiry(LocalDateTime.now().plus(properties.getJwt().getRefreshTokenExpires()));
        result.setUser(user);
        return result;
    }

    /**
     * 预期的 token 校验失败（控制流异常，不填充堆栈）
     */
    private static final class TokenVerifyException extends RuntimeException {

        /**
         * true-撤销/被踢出 → 两个调用方均映射 401；
         * false-校验类失败 → verifyToken 映射 null、refreshToken 映射 400
         */
        private final boolean revoked;

        private TokenVerifyException(boolean revoked, String message) {
            super(message, null, false, false);
            this.revoked = revoked;
        }

        /**
         * 校验类失败（无效/类型不符/缺 claim）
         */
        static TokenVerifyException invalid(String reason) {
            return new TokenVerifyException(false, reason);
        }

        /**
         * 撤销/被踢出
         */
        static TokenVerifyException revoked() {
            return new TokenVerifyException(true, "token 已撤销");
        }

        boolean isRevoked() {
            return revoked;
        }
    }
}
