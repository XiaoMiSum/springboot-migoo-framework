# migoo-spring-boot-starter-security

安全组件，支持 JWT 和 OAuth2 Resource Server 两种认证模式，内置 TOTP 二次验证。

## 快速开始

| 步骤 | JWT 模式 | OAuth2 模式 |
|------|----------|-------------|
| 1. 引入依赖 | 添加 `migoo-spring-boot-starter-security` | 同左 |
| 2. 配置模式 | `migoo.security.mode=jwt` + `jwt.secret-key` | `migoo.security.mode=oauth2` + `oauth2.issuer-uri` |
| 3. 实现接口 | 实现 `UserDetailsBridge`（必须） | 无需实现 |
| 4. 获取用户 | `@AuthUser AuthUserDetails user` | 从 `Jwt` claims 获取 |
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
// 获取当前用户
@GetMapping("/profile")
public UserVO getProfile(@AuthUser AuthUserDetails user) {
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
```

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
  │                             │ verifyToken() 验签+解析       │
  │                             │ ────────────────────────────> │
  │                             │ <──── 返回 AuthUserDetails ── │
  │                             │ 设置 SecurityContext          │
  │ <── 200 OK ──────────────── │                              │
```

### 应用层对接

#### 1. 实现 UserDetailsBridge（必须）

```java
@Component
public class UserDetailsBridgeImpl implements UserDetailsBridge {

    @Resource
    private UserMapper userMapper;

    @Override
    public AuthUserDetails<?, ?> loadByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public AuthUserDetails<?, ?> loadByUserId(String userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void clean(String token) {
        // 可选：Token 黑名单
        redisTemplate.opsForValue().set("token:blacklist:" + token, "1", 30, TimeUnit.MINUTES);
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
        // 自定义 Token 验证
    }

    @Override
    public LoginResult<CustomUserDetails> refreshToken(String refreshToken) {
        // 自定义刷新逻辑
    }
}
```

#### 3. 获取当前用户

```java
// 方式一：@AuthUser 注解（推荐）
@GetMapping("/profile")
public UserVO getProfile(@AuthUser AuthUserDetails user) {
    return userMapper.selectById(user.getId());
}

// 方式二：工具类
String userId = SecurityFrameworkUtils.getLoginUserId();
```

#### 4. 权限校验

```java
// 方法级别
@PreAuthorize("hasRole('ADMIN')")
@Secured("ROLE_ADMIN")
```

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
public TotpAuthenticator.TotpBinding bindTotp(@AuthUser AuthUserDetails user) {
    return totpAuthenticator.generateBinding(user.getUsername(), "MyApp");
}

// 通过 @AuthUser(false) 获取用户（不强制要求 TOTP 已验证）
@GetMapping("/auth/totp-status")
public Map<String, Object> totpStatus(@AuthUser(false) AuthUserDetails user) {
    return Map.of("enabled", user.isTwoFactorEnabled(), "bound", user.isTwoFactorBound());
}
```

---

## 核心接口

### AuthUserDetailsFetcher

认证服务接口：

| 方法 | 说明 |
|------|------|
| `loadUserByUsername(String)` | 根据用户名加载用户（Spring Security 标准接口） |
| `authenticate(String, String)` | 用户名密码认证，返回 `LoginResult` |
| `verifyToken(String)` | 校验 accessToken 有效性，返回用户信息 |
| `refreshToken(String)` | 刷新 token，返回新的 `LoginResult` |
| `clean(String)` | 清理 token（登出/黑名单） |

### UserDetailsBridge

用户加载桥接接口（JWT 模式必须实现）：

| 方法 | 说明 |
|------|------|
| `loadByUsername(String)` | 根据用户名查数据库 |
| `loadByUserId(String)` | 根据用户编号查数据库（每次请求调用） |
| `clean(String)` | 清理 token（默认空实现） |

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

认证失败统一抛出 `ServiceException`，错误码 `401`（`GlobalErrorCodeConstants.INVALID_AUTHORIZED`）。
