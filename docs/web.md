---
layout: default
---

# migoo-spring-boot-starter-web

Web 组件，提供全局异常处理、统一响应封装、TraceId 注入、i18n 国际化、CORS 过滤等。

## 快速开始

| 步骤 | 说明 |
|------|------|
| 1. 引入依赖 | 添加 `migoo-spring-boot-starter-web` |
| 2. 零配置生效 | 全局异常处理、TraceId、CORS、请求体缓存自动生效 |
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
        apiErrorLogMapper.insert(BeanUtils.toBean(apiErrorLog, ApiErrorLogDO.class));
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

`CacheRequestBodyFilter` 自动将请求体包装为 `CachedBodyHttpServletRequest`，支持重复读取（仅 JSON，默认最大 10MB）。

可通过配置调整缓存行为：

```yaml
migoo:
  web:
    cache-body:
      enabled: true       # 是否开启，默认 true
      max-size: 10485760  # 最大缓存大小（字节），默认 10MB
```

### 6. CORS 跨域配置

默认允许所有来源。可通过配置自定义：

```yaml
migoo:
  web:
    cors:
      enabled: true                      # 是否开启，默认 true
      allowed-origins:                   # 允许的来源，默认 ["*"]
        - https://example.com
        - https://admin.example.com
      allowed-methods: ["GET", "POST"]   # 允许的 HTTP 方法，默认 ["*"]
      allowed-headers: ["*"]             # 允许的请求头，默认 ["*"]
      allow-credentials: true            # 是否允许携带凭证，默认 true
      max-age: 1800                      # 预检请求缓存时间（秒），默认 1800
```

---

## 自动注册的组件

| 组件 | 说明 | 条件 |
|------|------|------|
| `GlobalExceptionHandler` | 全局异常处理 | Servlet Web 环境 |
| `ResponseBodyStorageAdvice` | Result 存入 RequestAttribute | Servlet Web 环境 |
| `ResponseBodyI18nAdvice` | 响应体 i18n 消息解析 | Servlet Web 环境 |
| `CorsFilter` | CORS 过滤 | `migoo.web.cors.enabled=true` |
| `CacheRequestBodyFilter` | 请求体缓存 | `migoo.web.cache-body.enabled=true` |
| `TraceIdFilter` | TraceId 生成与注入 | Servlet Web 环境 |
| `I18NLocaleResolver` | 语言解析（Accept-Language） | Servlet Web 环境 |
| 虚拟线程执行器 | Tomcat 使用虚拟线程（Java 21+） | Tomcat 在 classpath |

## 配置项

```yaml
# Web 模块完整配置
migoo:
  web:
    cors:                                 # CORS 跨域
      enabled: true
      allowed-origins: ["*"]
      allowed-methods: ["*"]
      allowed-headers: ["*"]
      allow-credentials: true
      max-age: 1800
    cache-body:                           # 请求体缓存
      enabled: true
      max-size: 10485760                  # 10MB

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
└── VirtualThreadConfiguration → TomcatProtocolHandlerCustomizer
```

所有配置类通过 `@ConditionalOnWebApplication(type = SERVLET)` 保护，非 Web 环境不会激活。
