---
layout: default
---

# migoo-spring-boot-starter-web

Web 组件，提供全局异常处理、统一响应封装、TraceId 注入、i18n 国际化、CORS 过滤等。

## 快速开始

| 步骤 | 说明 |
|------|------|
| 1. 引入依赖 | 添加 `migoo-spring-boot-starter-web` |
| 2. 零配置生效 | 全局异常处理、TraceId、请求体缓存自动生效；CORS 过滤器注册但默认不放行任何跨域（安全默认，见 §6） |
| 3. 定义错误码 | 创建 `ErrorCode` 常量，业务异常抛出 `ServiceException` |
| 4. 返回 Result | Controller 返回 `Result.ok(data)` 统一响应格式 |

```java
// 错误码定义
public interface UserErrorCode {
    ErrorCode USER_NOT_FOUND = ErrorCode.of(1001000000, "用户不存在");
}

// 抛出业务异常
throw ServiceExceptionUtil.get(UserErrorCode.USER_NOT_FOUND);

// Controller 返回统一响应
@GetMapping("/user")
public Result<UserVO> getUser() {
    return Result.ok(userVO);
}
```

## 依赖

```xml
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-web</artifactId>
</dependency>
```

## 应用层对接

### 1. 全局异常处理（自动生效）

无需额外配置，框架自动捕获 Controller 层异常并返回统一 `Result` 响应：

```java
// 业务异常 → 返回对应错误码
throw ServiceExceptionUtil.get(UserErrorCode.USER_NOT_FOUND);

// Spring @Valid 参数校验异常 → 400 + 校验错误信息
public UserVO create(@Valid @RequestBody UserCreateReqBody reqBody) { ... }

// 权限不足 → 403
throw new AccessDeniedException("权限不足");

// 未知异常 → 500 + 错误日志
```

### 2. API 错误日志持久化（可选）

当发生 500 错误时，框架会调用 `ApiErrorLogFrameworkService` 记录错误日志。应用层需实现该接口：

```java
@Component
public class ApiErrorLogFrameworkServiceImpl implements ApiErrorLogFrameworkService {

    @Resource
    private ApiErrorLogMapper apiErrorLogMapper;

    @Override
    public void createApiErrorLog(ApiErrorLog apiErrorLog) {
        // Bean 拷贝用 Spring 标准 BeanUtils（org.springframework.beans.BeanUtils），框架不内置转换工具
        ApiErrorLogDO apiErrorLogDO = new ApiErrorLogDO();
        BeanUtils.copyProperties(apiErrorLog, apiErrorLogDO);
        apiErrorLogMapper.insert(apiErrorLogDO);
    }
}
```

`ApiErrorLog` 包含：请求方法、URL、参数、客户端 IP、异常类名、异常堆栈、根因消息等。

### 3. TraceId 链路追踪（自动生效）

每个请求自动生成 `TraceId`，注入到 MDC 和响应头 `X-Trace-Id`：

```java
// 日志中自动包含 traceId
log.info("用户登录: {}", username);

// 前端可通过响应头 X-Trace-Id 获取
```

### 4. i18n 国际化

框架通过 `Accept-Language` 请求头自动解析语言，Controller 返回的 `Result.msg` 会自动国际化。

```java
// 注入 I18NMessage（一般不需要手动调用，Result 自动处理）
@Resource
private I18NMessage i18n;

// 手动获取国际化消息
String message = i18n.getMessage("user.not.found");
// 支持占位符
String message = i18n.getMessage("user.exists", "13800138000");
```

### 5. 请求体缓存（自动生效）

`CacheRequestBodyFilter` 自动将请求体包装为 `CachedBodyHttpServletRequest`，支持重复读取（JSON / XML / text 及无 Content-Type 请求，默认最大 10MB）。GET/HEAD、表单（`application/x-www-form-urlencoded`）、文件上传（`multipart/*`）及二进制类型不缓存。

可通过配置调整缓存行为：

```yaml
migoo:
  web:
    cache-body:
      enabled: true       # 是否开启，默认 true
      max-size: 10485760  # 最大缓存大小（字节），默认 10MB
```

### 6. CORS 跨域配置（四档模式）

**安全默认**：`mode=strict` 且允许来源为空 = **不放行任何跨域来源**；跨域能力必须按场景显式选择模式。
CORS 只做**来源准入**，不做身份认证（登录态校验由 security 组件负责，两者边界不混用）。

| 模式 | 适用场景 | 需要的配置 |
|------|----------|-----------|
| `strict`（默认） | 固定域名的后台/前台 | `allowed-origins` 或 `allowed-origin-patterns` 精确白名单，为空则禁止跨域 |
| `open` | Token 型开放平台 API（无凭证） | 无需来源配置；强制 `allow-credentials=false` |
| `pattern` | 自有子域名生态 | `allowed-origin-patterns`，如 `https://*.example.com` |
| `dynamic` | 动态域名开放平台（域名不可枚举） | 应用注册一个 `CorsOriginPredicate` Bean 逐请求判定 |

```yaml
migoo:
  web:
    cors:
      enabled: true                        # 是否装配 CORS 过滤器，默认 true
      mode: strict                         # strict | open | pattern | dynamic，默认 strict
      allowed-origins:                     # strict 模式：精确来源白名单，默认 []（= 禁止跨域）
        - https://admin.example.com
      allowed-origin-patterns:             # pattern 模式：来源模式，如 https://*.example.com
        - https://*.example.com
      allowed-methods: ["GET", "POST"]     # 默认 ["*"]
      allowed-headers: ["*"]               # 默认 ["*"]
      allow-credentials: false             # 是否携带凭证（Cookie），默认 false
      max-age: 1800                        # 预检缓存时间（秒），默认 1800
```

`dynamic` 模式示例（动态域名开放平台，一次接入后新域名由应用注册表决定）：

```java
// 应用注册来源准入谓词：查询开放平台注册表判定该域名是否已登记
@Bean
public CorsOriginPredicate tenantOriginPredicate(TenantRepository tenants) {
    return origin -> tenants.existsByOrigin(origin);
}
```

```yaml
migoo:
  web:
    cors:
      mode: dynamic
      allow-credentials: true   # Cookie 场景；具体放行域名完全由谓词决定
```

**启动期校验（非法配置 fail-fast，拒绝带病上线）**：

- `allowed-origins` / `allowed-origin-patterns` 含 `*` 且 `allow-credentials=true` → 启动失败
  （规范禁止该组合，等价于向任意站点开放携带凭证的跨域）
- `mode=pattern` 但未配置 `allowed-origin-patterns` → 启动失败
- `mode=dynamic` 但容器中没有 `CorsOriginPredicate` Bean → 启动失败
- `mode=open` + `allow-credentials=true` → 启动失败（Cookie 场景请改用 `dynamic`）

### 7. 接口限流（@RateLimit）

标注在方法或类上，按固定窗口计数限流，超出限制返回 **429**（`TOO_MANY_REQUESTS`）：

```java
import xyz.migoo.framework.web.core.annotation.RateLimit;
import xyz.migoo.framework.web.core.annotation.RateLimitType;

// 按 IP 限流（默认维度）: 60 秒内最多 100 次
@RateLimit(limit = 100, window = 60)
@GetMapping("/api/list")
public Result<List<String>> list() { ... }

// 按登录用户限流: 同一用户 60 秒内最多 20 次（未登录归入 anonymous）
@RateLimit(type = RateLimitType.USER, limit = 20, window = 60)
@GetMapping("/api/my-orders")
public Result<List<OrderVO>> myOrders() { ... }

// 按自定义 key（SpEL）限流: 登录接口防爆破 —— 同一用户名 60 秒内最多 5 次
@RateLimit(type = RateLimitType.KEY, key = "#username", limit = 5, window = 60)
@PostMapping("/auth/login")
public Result<LoginVO> login(String username, String password) { ... }
```

| 属性 | 说明 | 默认 |
|------|------|------|
| `type` | 限流维度: `IP` / `USER` / `KEY` | `IP` |
| `key` | SpEL 表达式，`KEY` 维度必填（支持 `#username`、`#p0`/`#a0` 下标占位符） | - |
| `limit` | 窗口内允许通过的最大次数（必填） | - |
| `window` | 统计窗口时长（秒） | `60` |
| `message` | 超限提示（留空使用 i18n 消息 `common.too.many.requests`） | - |

- 统计键格式为 `类名#方法名:维度值`，不同方法、不同维度互不影响；
- 总开关 `migoo.web.rate-limit.enabled`（默认开启）；
- `USER` 维度的用户编号由 security 组件在认证通过后写入请求属性，OAuth2 模式下请改用 `KEY` 维度。

#### 计数存储（StateStore SPI）

限流计数经 `StateStore` 接口存取，按「应用自定义 > Redis > 内存」优先级装配：

| 实现 | 装配条件 | 适用场景 |
|------|----------|----------|
| 应用自定义 `StateStore` Bean | `@ConditionalOnMissingBean` 优先 | 任意存储 |
| `RedisStateStore` | 检测到 `RedisConnectionFactory`（引入 redis 组件）自动替换 | 多实例共享计数 |
| `InMemoryStateStore` | 默认 | 单机开箱即用 |

`StateStore` 同时服务于限流计数、幂等占位与 security 组件的登录失败计数/锁定状态。

### 8. 幂等防重复提交（@Idempotent）

标注在方法或类上，防重窗口内的相同请求直接拒绝，返回业务码 **900**（`REPEATED_REQUESTS`）：

```java
import xyz.migoo.framework.web.core.annotation.Idempotent;

// 默认维度: 同一用户 60 秒内相同请求体只受理一次（防双击/重复提交）
@Idempotent
@PostMapping("/api/orders")
public Result<Long> createOrder(@RequestBody OrderCreateReqBody req) { ... }

// 按业务单号幂等（SpEL），窗口 5 分钟
@Idempotent(key = "#orderId", expire = 300)
@PostMapping("/api/pay")
public Result<?> pay(String orderId, BigDecimal amount) { ... }

// 自定义重复提示
@Idempotent(message = "订单正在处理中，请勿重复提交")
public Result<?> submit(@RequestBody ReqBody req) { ... }
```

| 属性 | 说明 | 默认 |
|------|------|------|
| `key` | 幂等键 SpEL（支持 `#参数名`、`#p0`/`#a0`），留空用默认维度 | - |
| `expire` | 防重窗口（秒），首次成功后窗口内重复请求被拒 | `60` |
| `message` | 重复请求提示（留空使用 i18n 消息 `common.repeat.request`） | - |

- 幂等键格式 `migoo:idempotent:类名#方法名:维度值`；默认维度 = `登录用户 + 可摘要参数的 SHA-256 摘要`——只摘要 `@RequestBody` 参数与基本类型/字符串/枚举/时间等简单参数，自动跳过 HttpServletRequest、MultipartFile 等容器对象；全部不可摘要时退化为「用户 + 方法」级，建议配 `key` 精确化；
- 失败语义与 MQ 幂等拦截器一致：**成功保留占位**（窗口内拒绝重复）、**失败释放占位**（允许重试）；执行中按 30 秒短占位过期兜底，进程崩溃后窗口到期自动恢复；
- 占位与防重窗口均经 `StateStore`（同限流存储）：单机内存实现，检测到 Redis 自动多实例共享（**多实例部署必须引 redis 组件**，否则各实例各自占位）；
- 总开关 `migoo.web.idempotent.enabled`（默认开启）；
- 本注解语义是**拒绝重复**，不是**重放响应**——重复请求抛 900，不会回放首次的返回值；需要「结果回放」型幂等请自行缓存返回值。

---

## 自动注册的组件

| 组件 | 说明 | 条件 |
|------|------|------|
| `GlobalExceptionHandler` | 全局异常处理 | Servlet Web 环境 |
| `ResponseBodyStorageAdvice` | Result 存入 RequestAttribute | Servlet Web 环境 |
| `ResponseBodyI18nAdvice` | 响应体 i18n 消息解析 | Servlet Web 环境 |
| `CorsFilter` | CORS 过滤（四档模式，见 §6；默认 STRICT 空来源 = 已注册但不放行跨域） | `migoo.web.cors.enabled=true` |
| `CacheRequestBodyFilter` | 请求体缓存 | `migoo.web.cache-body.enabled=true` |
| `RateLimitAspect` | `@RateLimit` 注解限流切面 | `migoo.web.rate-limit.enabled=true`（默认开启） |
| `IdempotentAspect` | `@Idempotent` 注解幂等切面（防重复提交） | `migoo.web.idempotent.enabled=true`（默认开启） |
| `StateStore` | 计数/状态存储（默认内存实现） | 无自定义 `StateStore` Bean 时 |
| `RateLimiter` | 固定窗口限流器 | 无自定义 `RateLimiter` Bean 时 |
| `RedisStateStore` | Redis 计数存储（多实例共享，替换内存实现） | 检测到 `RedisConnectionFactory` 时 |
| `TraceIdFilter` | TraceId 生成与注入 | Servlet Web 环境 |
| `I18NLocaleResolver` | 语言解析（Accept-Language） | Servlet Web 环境 |
| 虚拟线程执行器 | Tomcat 使用虚拟线程（Java 21+） | Tomcat 在 classpath |

## 配置项

```yaml
# Web 模块完整配置
migoo:
  web:
    cors:                                 # CORS 跨域（四档模式，见 §6）
      enabled: true
      mode: strict                        # strict | open | pattern | dynamic
      allowed-origins: []                 # strict 白名单，默认空 = 不放行跨域
      allowed-origin-patterns: []         # pattern 模式来源模式
      allowed-methods: ["*"]
      allowed-headers: ["*"]
      allow-credentials: false            # 默认关闭；开启时禁止 * 来源
      max-age: 1800
    cache-body:                           # 请求体缓存
      enabled: true
      max-size: 10485760                  # 10MB
    rate-limit:                           # @RateLimit 注解限流
      enabled: true
    idempotent:                           # @Idempotent 注解幂等（防重复提交）
      enabled: true

# Spring MVC 配置（配合 404 异常处理）
spring:
  mvc:
    throw-exception-if-no-handler-found: true
    static-path-pattern: /static/**
```

## 架构说明

模块采用单一入口 + `@Import` 组装架构：

```
MiGooWebAutoConfiguration (入口)
├── CorsConfiguration          → CorsFilter
├── FilterConfiguration        → TraceIdFilter + CacheRequestBodyFilter
├── ExceptionHandlingConfiguration → GlobalExceptionHandler
├── ResponseBodyConfiguration  → ResponseBodyStorageAdvice + ResponseBodyI18nAdvice
├── I18nConfiguration          → I18NLocaleResolver + I18NMessage
├── RateLimitConfiguration     → StateStore(内存) + RateLimiter + RateLimitAspect
└── VirtualThreadConfiguration → TomcatProtocolHandlerCustomizer

MiGooWebRedisStateStoreAutoConfiguration（@AutoConfigureAfter 主入口）
└── RedisStateStore            → 检测到 Redis 时替换内存 StateStore（多实例共享）
```

所有配置类通过 `@ConditionalOnWebApplication(type = SERVLET)` 保护，非 Web 环境不会激活。
