---
layout: default
---

# migoo-spring-boot-starter-security

安全组件，支持 JWT 和 OAuth2 Resource Server 两种认证模式，内置 TOTP 二次验证、登录失败锁定、token 撤销（踢人）能力。

## 快速开始

| 步骤 | JWT 模式 | OAuth2 模式 |
|------|----------|-------------|
| 1. 引入依赖 | 添加 `migoo-spring-boot-starter-security` | 同左 |
| 2. 配置模式 | `migoo.security.mode=jwt` + `jwt.secret-key` | `migoo.security.mode=oauth2` + `oauth2.issuer-uri` |
| 3. 实现接口 | 实现 `UserDetailsBridge`（必须） | 无需实现 |
| 4. 获取用户 | `@AuthenticationPrincipal AuthUserDetails user` | 从 `Jwt` claims 获取 |
| 5. 权限校验 | `@PreAuthorize` / `@Secured` | 同左 |

```yaml
# JWT 最小配置
migoo:
  security:
    mode: jwt
    jwt:
      secret-key: your-secret-key-at-least-32-bytes
```

```java
import org.springframework.security.core.annotation.AuthenticationPrincipal;

// 获取当前用户（@AuthenticationPrincipal 由 Spring Security 自动解析，未认证时为 null）
@GetMapping("/profile")
public UserVO getProfile(@AuthenticationPrincipal AuthUserDetails<?, ?> user) {
    return userMapper.selectById(user.getId());
}
```

## 依赖

```xml
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-security</artifactId>
</dependency>
```

## 配置项

```yaml
migoo:
  security:
    # 安全模式: jwt 或 oauth2
    mode: jwt
    # 登出 URL
    logout-url: /auth/logout
    # 免认证 URL
    permit-all-urls:
      - /auth/login
      - /auth/register
      - /public/**

    # ========== JWT 模式配置 ==========
    jwt:
      # JWT 签名密钥（HMAC-SHA256，必填）
      secret-key: your-jwt-secret-key
      # Token 请求头（默认 Authorization）
      header-name: Authorization
      # Access Token 过期时间（默认 30 分钟）
      access-token-expires: PT30M
      # Refresh Token 过期时间（默认 7 天）
      refresh-token-expires: P7D
      # Refresh Token 请求头（默认 X-Refresh-Token）
      refresh-header-name: X-Refresh-Token

    # ========== OAuth2 模式配置 ==========
    oauth2:
      # 授权服务器 issuer URI（自动发现 JWK Set）
      issuer-uri: https://auth.example.com
      # 或直接指定 JWK Set URI（与 issuer-uri 二选一）
      # jwk-set-uri: https://auth.example.com/.well-known/jwks.json

    # ========== 登录失败锁定（仅 JWT 模式） ==========
    login-lock:
      # 总开关（默认 true；关闭后不检查也不计数）
      enabled: true
      # 连续失败计数的空闲重置窗口（默认 30 分钟，超时未再失败则计数清零）
      failure-window: PT30M
      # 固定时长策略: 连续失败 5 次 → 锁定 10 分钟
      fixed:
        enabled: true
        threshold: 5
        duration: PT10M
      # 递增时长策略: 连续失败 5 次起锁，首次锁 1 分钟，之后每次 ×2，单次上限 1 小时
      incremental:
        enabled: true
        threshold: 5
        initial-duration: PT1M
        multiplier: 2
        max-duration: PT1H
      # 滑动窗口策略: 15 分钟内累计失败 5 次（成功也计入）→ 锁定 10 分钟
      sliding-window:
        enabled: true
        threshold: 5
        window: PT15M
        duration: PT10M

    # ========== 安全响应头（默认启用，见下文「安全响应头」） ==========
    headers:
      # 总开关（false = 完全禁用，等同旧版 .headers(disable)）
      enabled: true
      # Spring Security 默认头集，逐项可关
      content-type-options: true         # X-Content-Type-Options: nosniff
      frame-options: true                # X-Frame-Options: DENY（防点击劫持）
      hsts: true                         # HSTS（仅 HTTPS 请求携带）
      cache-control: true                # Cache-Control: no-cache/no-store
      xss-protection: true               # X-XSS-Protection（Spring 默认写 0）
      # 非 Spring 默认头
      referrer-policy: true              # Referrer-Policy: strict-origin-when-cross-origin
      # content-security-policy: default-src 'self'   # CSP（默认不下发，按需显式给出）
```

---

## 安全响应头

过滤链默认**启用安全响应头**（旧版本为显式 `.headers(disable)` 全关，这是一次行为变化）：

| 头 | 默认值 | 防什么 | 开关（`migoo.security.headers.*`） |
|---|---|---|---|
| `X-Content-Type-Options` | `nosniff` | 上传内容被浏览器当脚本执行 | `content-type-options` |
| `X-Frame-Options` | `DENY` | 点击劫持（第三方页面 iframe 套壳诱导点击） | `frame-options` |
| `Strict-Transport-Security` | `max-age=3153653600; includeSubDomains` | HTTPS 降级剥离；**仅 HTTPS 请求携带**，本地 HTTP 调试不受影响 | `hsts` |
| `Cache-Control` | `no-cache, no-store, ...` | 敏感响应被中间层/浏览器缓存 | `cache-control` |
| `X-XSS-Protection` | `0`（Spring Security 默认关闭旧过滤器） | 旧版浏览器 XSS 过滤器 | `xss-protection` |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | URL 参数/token 随 Referer 泄露第三方（框架补充，非 Spring 默认） | `referrer-policy` |
| `Content-Security-Policy` | 不下发 | XSS（误配会阻断页面资源，须显式给出指令才开启） | `content-security-policy` |

装配策略是**「要关的才碰」**：仅对显式关闭的头调用 `disable`，其余保持 Spring Security 默认头集。

- `migoo.security.headers.enabled=false` → 完全禁用响应头（等同旧版行为，纯内网无浏览器交互的 API 可关闭）；
- 需要 COOP/COEP/CORP 等其他头时，可自定义 `SecurityFilterChain` Bean 覆盖（本组件的链带 `@ConditionalOnMissingBean`）。

---

## JWT 模式

框架自建 token 签发/验证体系，应用层通过实现接口对接。

### 流程

```
客户端                         框架                           应用层
  │                             │                              │
  │ POST /auth/login            │                              │
  │ ──────────────────────────> │                              │
  │                             │ 调用 authenticate()          │
  │                             │ ────────────────────────────> │
  │                             │ <──── 返回 LoginResult ────── │
  │ <── 返回 access+refresh ─── │                              │
  │                             │                              │
  │ GET /api/xxx                │                              │
  │ Authorization: Bearer xxx   │                              │
  │ ──────────────────────────> │                              │
  │                             │ verifyToken() 统一认证管线    │
  │                             │ ────────────────────────────> │
  │                             │ <──── 返回 AuthUserDetails ── │
  │                             │ 设置 SecurityContext          │
  │ <── 200 OK ──────────────── │                              │
```

**统一认证管线（`verifyAndLoadUser`，verifyToken 与 refreshToken 共用）逐步说明:**

1. **机械校验**（签名/过期/exp 缺失）: 由 `JwtTokenProvider.parseToken` 完成。默认实现经 `NimbusJwtDecoder` 内置的 `JwtTimestampValidator` 校验过期时间（默认 60 秒时钟偏移，当前时间超过 `exp + 60s` 即拒绝）；`exp` claim 缺失的 token 由实现兜底拒绝，防止无 `exp` 的 token 永久有效。
2. **userId 提取**: 缺少 `userId` claim 视为无效 token。
3. **类型校验（access/refresh 用途隔离）**: refresh token 不得作为访问凭证（防止 7 天有效期的 token 打 API），access token 不得用于刷新。
4. **撤销校验**: 通过 `UserDetailsBridge.isTokenRevoked` / `isUserRevoked` 拦截已撤销的 token / 已被踢出的用户。
5. **加载用户**: `loadByUserId` 每次请求执行，用户状态实时（禁用/权限变更立即生效）。

**错误分层**: 预期失败（无效/类型不符/已撤销/用户不存在）由调用方映射为 401/400；基础设施异常（用户存储故障）不拦截，原样上抛返回 500，避免服务端故障伪装成认证失败。请求无 token 或校验返回 null 时，`JwtAuthenticationFilter` 立即返回 401，不依赖下游授权层兜底。

### 应用层对接

#### 1. 实现 UserDetailsBridge（必须）

```java
@Component
public class UserDetailsBridgeImpl implements UserDetailsBridge {

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public AuthUserDetails<?, ?> loadByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public AuthUserDetails<?, ?> loadByUserId(String userId) {
        return userMapper.selectById(userId);
    }

    // ===== 以下四个撤销钩子为可选（不实现则不拦截） =====

    @Override
    public void clean(String token) {
        // 单 token 黑名单: 登出 / revokeToken 时写入
        redisTemplate.opsForValue().set("security:token:blacklist:" + token, "1",
                Duration.ofMinutes(30));
    }

    @Override
    public void revokeByUserId(String userId) {
        // 用户级黑名单: revokeUserTokens 踢出该用户全部 token
        // TTL 建议不小于 refresh token 剩余有效期
        redisTemplate.opsForValue().set("security:user:blacklist:" + userId, "1",
                Duration.ofDays(7));
    }

    @Override
    public boolean isTokenRevoked(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("security:token:blacklist:" + token));
    }

    @Override
    public boolean isUserRevoked(String userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("security:user:blacklist:" + userId));
    }
}
```

#### 2. 自定义认证器（可选）

框架提供 `DefaultJwtAuthenticator`，覆盖默认行为只需实现 `AuthUserDetailsFetcher`：

```java
@Component
public class CustomAuthFetcher implements AuthUserDetailsFetcher<CustomUserDetails> {

    @Override
    public LoginResult<CustomUserDetails> authenticate(String username, String password) {
        // 自定义认证逻辑
    }

    @Override
    public CustomUserDetails verifyToken(String accessToken) {
        // 自定义 Token 验证（返回 null 表示未认证 → 401）
    }

    @Override
    public LoginResult<CustomUserDetails> refreshToken(String refreshToken) {
        // 自定义刷新逻辑
    }
}
```

#### 3. 获取当前用户

```java
import org.springframework.security.core.annotation.AuthenticationPrincipal;

// 方式一：@AuthenticationPrincipal 注解（推荐，认证通过后 principal 即 AuthUserDetails）
@GetMapping("/profile")
public UserVO getProfile(@AuthenticationPrincipal AuthUserDetails<?, ?> user) {
    return userMapper.selectById(user.getId());
}

// 方式二：工具类
AuthUserDetails<?, ?> user = SecurityFrameworkUtils.getLoginUser();
String userId = SecurityFrameworkUtils.getLoginUserId();
```

#### 4. 权限校验

```java
// 方法级别
@PreAuthorize("hasRole('ADMIN')")
@Secured("ROLE_ADMIN")
```

---

## 撤销 Token / 踢出用户

通过 `AuthUserDetailsFetcher` 主动失效已签发的 token，配合 `UserDetailsBridge` 的撤销钩子在**每次请求校验时**生效（命中 → 401）。

| 方法 | 场景 | 生效方式 |
|------|------|----------|
| `revokeToken(accessToken)` | 撤销单次登录（登出），默认委托 `clean(token)` | `isTokenRevoked` 拦截该 token |
| `revokeUserTokens(userId)` | 踢出该用户全部登录（改密/封禁/多端管理） | `isUserRevoked` 拦截该用户所有 token |

```java
@Service
public class AuthService {

    @Resource
    private AuthUserDetailsFetcher<? extends AuthUserDetails<?, ?>> fetcher;

    /** 登出: 撤销本次登录的 token，后续请求 401 */
    public void logout(String accessToken, String refreshToken) {
        fetcher.revokeToken(accessToken);
        fetcher.revokeToken(refreshToken);
    }

    /** 踢人: 该用户全部 token 立即失效（所有设备） */
    public void kickOut(String userId) {
        fetcher.revokeUserTokens(userId);
    }
}
```

> 撤销要生效，需实现 `UserDetailsBridge` 的 `clean` / `revokeByUserId` / `isTokenRevoked` / `isUserRevoked` 钩子（见上方 Bridge 示例，建议用 Redis 黑名单，TTL 不小于 token 剩余有效期）。

---

## 登录失败锁定

认证前后两道防线：

- **认证前**: 已锁定账号直接拒绝（423），不再进入密码校验；
- **认证时**: 记录连续失败次数并评估策略，认证成功清零计数；
- **认证接口防爆破**: 建议同时在登录接口标注 web 组件的 `@RateLimit`（见 [web 文档](web.md)），未达锁定阈值前就拒绝（429），两道防线互补。

### 策略

三种策略**同时生效、命中取最严**（锁定截止时间取最晚者），可单独关闭：

| 策略 | 配置键 | 规则 | 默认 |
|------|--------|------|------|
| 固定时长 | `login-lock.fixed` | 连续失败达阈值 → 锁定固定时长 | 5 次 → 锁 10 分钟 |
| 递增时长 | `login-lock.incremental` | 达阈值后每次锁定时长 ×multiplier（封顶 max-duration） | 5 次起锁，1 分钟起每次 ×2，单次上限 1 小时 |
| 滑动窗口 | `login-lock.sliding-window` | 窗口内累计失败（成功也计入）达阈值 → 锁定 | 15 分钟内 5 次 → 锁 10 分钟 |

- 连续计数（固定/递增）在 `failure-window` 空闲窗口后自动清零（默认 30 分钟）；
- 锁定状态与失败计数存于 `StateStore`（默认内存实现，检测到 Redis 自动切换共享，多实例一致）；
- 锁定命中返回 **423**（`GlobalErrorCodeConstants.ACCOUNT_LOCKED`），i18n key `common.account.locked`；
- 总开关 `migoo.security.login-lock.enabled=false` 完全关闭（不检查也不计数）。

完整配置见「配置项」一节的 `login-lock` 块。

---

## OAuth2 模式

委托外部授权服务器签发 token，框架不参与 token 签发/验证。

### 流程

```
客户端                         授权服务器                      框架                          应用层
  │                             │                              │                              │
  │ POST /oauth2/token          │                              │                              │
  │ ──────────────────────────> │                              │                              │
  │ <── 返回 access token ───── │                              │                              │
  │                             │                              │                              │
  │ GET /api/xxx                │                              │                              │
  │ Authorization: Bearer xxx   │                              │                              │
  │ ──────────────────────────────────────────────────────────>│                              │
  │                             │    JwtDecoder 验签（JWK Set）│                              │
  │                             │ <──────────────────────────── │                              │
  │                             │ ───────────────────────────> │                              │
  │                             │                              │ 解析 JWT claims              │
  │                             │                              │ 设置 SecurityContext          │
  │ <── 200 OK ──────────────────────────────────────────────── │                              │
```

### 应用层对接

#### 1. 无需实现任何接口

OAuth2 模式下，`UserDetailsBridge`、`AuthUserDetailsFetcher` 均**不注册**。

#### 2. 获取当前用户

```java
@GetMapping("/profile")
public Map<String, Object> getProfile() {
    Jwt jwt = (Jwt) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
    String userId = jwt.getClaimAsString("sub");
    // 业务查询...
}
```

#### 3. 权限校验

与 JWT 模式相同，`@PreAuthorize` / `@Secured` 正常使用。

---

## TOTP 二次验证

### 流程

```java
// 1. 生成绑定信息
TotpAuthenticator.TotpBinding binding = totpAuthenticator.generateBinding("user@example.com", "MyApp");
// binding.getTotpSecret() → Base32 编码的密钥（存入数据库 two_factor_secret 字段）
// binding.getOtpAuthUri()  → otpauth:// URI（用于生成 QR 码）

// 2. 用户扫描 QR 码后，验证验证码
totpAuthenticator.verify("123456");

// 3. 登录时验证（需设置 twoFactorEnabled=true, twoFactorBound=true）
```

### 注解使用

```java
// 需要 TOTP 验证的接口
@PostMapping("/auth/bind-totp")
@RequiresTotp
public TotpAuthenticator.TotpBinding bindTotp(@AuthenticationPrincipal AuthUserDetails<?, ?> user) {
    return totpAuthenticator.generateBinding(user.getUsername(), "MyApp");
}

// 获取当前用户（未标注 @RequiresTotp 的接口不强制 TOTP 已验证）
@GetMapping("/auth/totp-status")
public Map<String, Object> totpStatus(@AuthenticationPrincipal AuthUserDetails<?, ?> user) {
    return Map.of("enabled", user.isTwoFactorEnabled(), "bound", user.isTwoFactorBound());
}
```

---

## 核心接口

### AuthUserDetailsFetcher

认证服务接口（登录、token 校验、刷新、撤销）：

| 方法 | 说明 |
|------|------|
| `authenticate(String, String)` | 用户名密码认证，返回 `LoginResult`；锁定命中抛 423 |
| `verifyToken(String)` | 校验 accessToken，返回用户信息。**返回 null 表示未认证**（无效/类型不符/已撤销/用户不存在），调用方据此返回 401；基础设施异常不上抛为 401，而是原样抛出 → 500 |
| `refreshToken(String)` | 刷新 token，返回新 `LoginResult`。格式/过期/类型不符 → 400，撤销/用户不存在 → 401 |
| `clean(String)` | 清理 token（登出/黑名单） |
| `revokeToken(String)` | 撤销指定 token（默认委托 `clean`） |
| `revokeUserTokens(String)` | 撤销用户全部 token（默认空实现，`DefaultJwtAuthenticator` 委托 `UserDetailsBridge#revokeByUserId`） |

### UserDetailsBridge

用户加载桥接接口（JWT 模式必须实现前两个方法，撤销钩子可选）：

| 方法 | 说明 |
|------|------|
| `loadByUsername(String)` | 登录时根据用户名查数据库 |
| `loadByUserId(String)` | 每次请求按 userId 查数据库（状态实时） |
| `clean(String)` | 单 token 黑名单（可选，默认空实现） |
| `revokeByUserId(String)` | 用户级黑名单，踢出全部 token（可选，默认空实现） |
| `isTokenRevoked(String)` | 该 token 是否已撤销（可选，默认 false） |
| `isUserRevoked(String)` | 该用户是否已被踢出（可选，默认 false） |

### JwtTokenProvider

JWT 机械操作接口（创建、解析、验证），默认实现 `JJwtTokenProvider`（HMAC-SHA256）：

| 方法 | 说明 |
|------|------|
| `createAccessToken(user)` / `createRefreshToken(user)` | 签发 token（含 `type`/`iat`/`exp` claims） |
| `parseToken(String)` | **契约: 在此完成全部有效性校验**（签名、过期、exp 缺失），无效必须抛 `JwtException`，禁止返回未经校验的 Jwt |
| `isTokenValid(String)` | `parseToken` 的布尔包装，判定与之完全一致 |
| `getUserIdFromToken(Jwt)` | 提取 userId（统一存 String，应用层在 `loadByUserId` 转回自身 ID 类型） |

> 过期校验由 `NimbusJwtDecoder` 内置的 `JwtTimestampValidator` 完成（60 秒时钟偏移）；`exp` 缺失的 token 因该 validator 默认放行，由 `JJwtTokenProvider` 兜底拒绝。自定义实现须遵守 `parseToken` 契约，否则会绕过 verifyToken/refreshToken 的 401/400 防线。

### AuthUserDetails

用户认证信息基类：

| 字段 | 说明 |
|------|------|
| `id` | 用户 ID |
| `authorities` | 权限列表 |
| `totpSecret` | TOTP 密钥（Base32） |
| `twoFactorEnabled` | 是否启用 2FA |
| `twoFactorBound` | TOTP 是否已绑定 |

## 错误码

认证相关失败统一抛出 `ServiceException`：

| 错误码 | 常量 | 场景 |
|--------|------|------|
| 401 | `UNAUTHORIZED` | 密码错误、token 无效/已撤销（`JwtAuthenticationFilter` 立即返回） |
| 401 | `INVALID_AUTHORIZED` | 用户不存在等授权无效 |
| 400 | `INVALID_REFRESH_TOKEN` | refresh token 无效/过期/类型不符 |
| 423 | `ACCOUNT_LOCKED` | 登录失败次数过多，账号被锁定 |
| 429 | `TOO_MANY_REQUESTS` | 触发 `@RateLimit` 限流（web 组件，见 [web 文档](web.md)） |

i18n key 由应用提供对应语言翻译。
