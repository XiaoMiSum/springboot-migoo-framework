package xyz.migoo.examples.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.security.core.authentication.AuthUserDetailsFetcher;

/**
 * JWT 模式登录示例
 *
 * <p>框架不内置登录接口（登录协议由应用定义），应用只须注入
 * {@link AuthUserDetailsFetcher}（JWT 模式下框架默认注册 DefaultJwtAuthenticator，
 * 需整体替换时实现自己的 AuthUserDetailsFetcher Bean 即可）。</p>
 *
 * <p>本框架约定：响应始终 HTTP 200，业务状态由 {@code Result.code} 表达
 * （200 成功 / 401 认证失败 / 403 无权限 / 423 账号锁定）。</p>
 *
 * @author xiaomi
 */
@RestController
public class AuthController {

    private final AuthUserDetailsFetcher<DemoUserDetails> authFetcher;

    public AuthController(AuthUserDetailsFetcher<DemoUserDetails> authFetcher) {
        this.authFetcher = authFetcher;
    }

    /**
     * 登录请求体
     */
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    /**
     * POST /auth/login {"username":"admin","password":"demo123"}
     * <p>成功 → data 含 accessToken / refreshToken / 过期时间 / 用户信息；失败 → code 401（密码错误）或 423（锁定）</p>
     */
    @PostMapping("/auth/login")
    public Result<AuthUserDetailsFetcher.LoginResult<DemoUserDetails>> login(
            @RequestBody @Valid LoginRequest request) {
        return Result.ok(authFetcher.authenticate(request.username(), request.password()));
    }

    /**
     * POST /auth/refresh（请求头 X-Refresh-Token 携带 refresh token）
     * <p>refresh token 与 access token 用途隔离，不可用于访问业务接口</p>
     */
    @PostMapping("/auth/refresh")
    public Result<AuthUserDetailsFetcher.LoginResult<DemoUserDetails>> refresh(
            @RequestHeader(name = "X-Refresh-Token", required = false) String refreshToken) {
        return Result.ok(authFetcher.refreshToken(refreshToken));
    }

    /**
     * GET /api/me —— 需要登录，@AuthenticationPrincipal 即 JwtAuthenticationFilter 放入安全上下文的用户
     */
    @GetMapping("/api/me")
    public Result<DemoUserDetails> me(@AuthenticationPrincipal DemoUserDetails user) {
        return Result.ok(user);
    }

    /**
     * GET /api/admin —— 需要 ROLE_ADMIN（方法级校验，@EnableMethodSecurity 已由框架启用）
     */
    @GetMapping("/api/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> admin() {
        return Result.ok("admin-only-content");
    }

    /**
     * GET /public/ping —— 免登录（已在 permit-all-urls 中放行）
     */
    @GetMapping("/public/ping")
    public Result<String> ping() {
        return Result.ok("pong");
    }
}
