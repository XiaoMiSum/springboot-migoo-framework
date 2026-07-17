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

`CacheRequestBodyFilter` 自动将请求体包装为 `CachedBodyHttpServletRequest`，支持重复读取（仅 JSON，最大 10MB）。

---

## 自动注册的组件

| 组件 | 说明 |
|------|------|
| `GlobalExceptionHandler` | 全局异常处理 |
| `GlobalResponseBodyHandler` | 响应体 i18n 处理 |
| `CorsFilter` | CORS 过滤（允许所有来源） |
| `CacheRequestBodyFilter` | 请求体缓存 |
| `TraceIdFilter` | TraceId 生成与注入 |
| `I18NLocaleResolver` | 语言解析（Accept-Language） |
| 虚拟线程执行器 | Tomcat 使用虚拟线程（Java 21+） |

## 配置项

```yaml
spring:
  mvc:
    throw-exception-if-no-handler-found: true
    static-path-pattern: /static/**
```

> 框架无专属配置项，Tomcat 虚拟线程通过 Spring Boot 3.x 的 `server.threads.virtual=true` 控制（默认开启）。
