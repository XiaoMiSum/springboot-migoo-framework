# MiGoo Spring Boot Framework

基于 Spring Boot 4.1.0 的企业级快速开发框架，提供安全认证、数据访问、缓存、消息队列等开箱即用的能力。

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 21+ |
| Spring Boot | 4.1.0 |
| Spring Security | 7.1.0 |
| MyBatis-Plus | 3.5.16 |
| Redisson | 4.2.0 |

## 快速开始

### 1. 引入 BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>xyz.migoo.springboot</groupId>
            <artifactId>migoo-framework-dependencies</artifactId>
            <version>1.3.16</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 按需引入组件

```xml
<dependencies>
    <!-- 公共组件（基础工具类、异常处理、分页等） -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-common</artifactId>
    </dependency>

    <!-- Web 组件（全局异常处理、统一响应、i18n） -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Security 组件（JWT 认证、OAuth2、TOTP 2FA） -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-security</artifactId>
    </dependency>

    <!-- MyBatis 组件（分页、动态数据源、加密） -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-mybatis</artifactId>
    </dependency>

    <!-- Redis 组件（RedisKit 工具类） -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-redis</artifactId>
    </dependency>

    <!-- MQ 组件（Redis 消息队列） -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-mq</artifactId>
    </dependency>
</dependencies>
```

## 组件说明

| 组件 | 说明 | 文档 |
|------|------|------|
| [migoo-spring-boot-starter-common](#common) | 公共工具类、异常处理、分页、校验 | [查看](#common) |
| [migoo-spring-boot-starter-web](#web) | Web MVC 配置、全局异常、统一响应 | [查看](#web) |
| [migoo-spring-boot-starter-security](#security) | 认证授权、JWT、OAuth2、TOTP 2FA | [查看](#security) |
| [migoo-spring-boot-starter-mybatis](#mybatis) | MyBatis-Plus 增强、分页、数据源 | [查看](#mybatis) |
| [migoo-spring-boot-starter-redis](#redis) | Redis 配置、工具类 | [查看](#redis) |
| [migoo-spring-boot-starter-mq](#mq) | Redis 消息队列（Stream/Pub-Sub） | [查看](#mq) |

---

## <a name="common"></a>migoo-spring-boot-starter-common

公共组件，提供基础工具类、异常处理、分页模型、校验注解等。

### 核心功能

#### 统一响应体

```java
// 成功响应
Result<Void> result = Result.ok();

// 带数据响应
Result<UserVO> result = Result.success(userVO);

// 错误响应
Result<Void> result = Result.error(ErrorCode.error(1001, "用户不存在"));
```

#### 分页查询

```java
// 请求参数
public class UserPageReqParam extends PageParam {
    private String name;
    private Integer status;
}

// 分页查询
PageResult<UserVO> result = userMapper.selectPage(reqParam);
```

#### 自定义异常

```java
// 定义错误码
public interface UserErrorCode {
    ErrorCode USER_NOT_FOUND = new ErrorCode(1001, "用户不存在");
    ErrorCode USER_STATUS_DISABLED = new ErrorCode(1002, "用户已被禁用");
}

// 抛出异常
throw ServiceExceptionUtil.get(UserErrorCode.USER_NOT_FOUND);
```

#### 校验注解

```java
public class UserCreateReqBody {
    @Mobile
    private String mobile;

    @Email
    private String email;

    @Password
    private String password;

    @InEnum(UserStatusEnum.class)
    private Integer status;
}
```

#### 工具类

```java
// JSON 工具
String json = JsonUtils.toJsonString(object);
UserVO user = JsonUtils.parseObject(json, UserVO.class);

// 日期工具
LocalDateTime dateTime = LocalDateTimeUtils.parse("2024-01-01 12:00:00");
String formatted = LocalDateTimeUtils.format(dateTime);

// 集合工具
List<UserVO> filtered = CollectionUtils.filter(users, user -> user.getStatus() == 1);
```

---

## <a name="web"></a>migoo-spring-boot-starter-web

Web 组件，提供全局异常处理、统一响应封装、TraceId 注入、i18n 支持。

### 配置项

```yaml
migoo:
  web:
    # 全局异常处理
    exception-handler:
      enabled: true
    # 统一响应体
    response-body-handler:
      enabled: true
    # TraceId
    trace-id:
      enabled: true
      header-name: X-Trace-Id
```

### 核心功能

#### 全局异常处理

自动捕获 Controller 层异常，返回统一错误响应：

```java
// 业务异常 → 400 + 错误码
throw ServiceExceptionUtil.get(UserErrorCode.USER_NOT_FOUND);

// 参数校验异常 → 400 + 校验错误信息
// Spring @Valid 自动处理

// 权限不足 → 403
throw new AccessDeniedException("权限不足");

// 未认证 → 401
throw new AuthenticationServiceException("未登录");
```

#### TraceId 注入

每个请求自动生成 TraceId，注入到 MDC 和响应头：

```java
// 日志中自动包含 traceId
log.info("用户登录: {}", username); // 日志输出包含 [traceId]

// 响应头自动返回
// X-Trace-Id: 1234567890abcdef
```

#### i18n 国际化

```java
// 注入 I18NMessage
@Resource
private I18NMessage i18n;

// 获取国际化消息
String message = i18n.getMessage(UserErrorCode.USER_NOT_FOUND.msg());
```

#### API 错误日志

```java
@ApiErrorLog
@GetMapping("/api/user/{id}")
public UserVO getUser(@PathVariable Long id) {
    return userService.getUser(id);
}
// 接口报错时自动记录错误日志
```

---

## <a name="security"></a>migoo-spring-boot-starter-security

安全组件，支持 JWT 认证和 OAuth2 Resource Server 两种模式，内置 TOTP 二次验证。

### 配置项

```yaml
migoo:
  security:
    # 安全模式: jwt 或 oauth2
    mode: jwt
    # 密码加密密钥
    password-secret: your-password-secret
    # 登出 URL
    logout-url: /auth/logout
    # 免认证 URL
    permit-all-urls:
      - /auth/login
      - /auth/register
      - /public/**

    authorization:
      # Token 请求头
      header-name: Authorization
      # Access Token 过期时间（默认 30 分钟）
      access-token-expires: PT30M
      # Refresh Token 过期时间（默认 7 天）
      refresh-token-expires: P7D
      # JWT 签名密钥
      secret-key: your-jwt-secret-key
      # Refresh Token 请求头
      refresh-header-name: X-Refresh-Token

    # OAuth2 配置（mode: oauth2 时生效）
    oauth2:
      issuer-uri: https://auth.example.com
      # 或直接指定 JWK Set URI
      # jwk-set-uri: https://auth.example.com/.well-known/jwks.json
```

### 快速开始

#### 1. 实现 UserDetailsBridge

```java
@Component
public class UserDetailsBridgeImpl implements UserDetailsBridge {

    @Resource
    private UserMapper userMapper;

    @Override
    public UserDetails loadByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public UserDetails loadByUserId(String userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void clean(String token) {
        // Token 黑名单（可选）
        redisTemplate.opsForValue().set("token:blacklist:" + token, "1", 30, TimeUnit.MINUTES);
    }
}
```

#### 2. 自定义认证服务（可选）

如果不使用默认的 `DefaultJwtAuthService`，可以自行实现 `AuthUserDetailsService`：

```java
@Component
public class AuthUserServiceImpl implements AuthUserDetailsService<UserDetails> {

    @Override
    public LoginResult<UserDetails> authenticate(String username, String password) {
        // 自定义认证逻辑
    }

    @Override
    public UserDetails verifyToken(String accessToken) {
        // 自定义 Token 验证逻辑
    }

    @Override
    public LoginResult<UserDetails> refreshToken(String refreshToken) {
        // 自定义刷新逻辑
    }
}
```

### 注解使用

#### @AuthUser - 获取当前用户

```java
@GetMapping("/profile")
public UserVO getProfile(@AuthUser UserDetails user) {
    return userMapper.selectById(user.getId());
}

// 可选参数（未登录返回 null）
@GetMapping("/public/info")
public UserInfo getInfo(@AuthUser(required = false) UserDetails user) {
    if (user == null) {
        return publicInfo;
    }
    return userService.getInfo(user.getId());
}
```

#### @RequiresTotp - TOTP 二次验证

```java
@PostMapping("/auth/bind-totp")
@RequiresTotp
public TotpBinding bindTotp(@AuthUser UserDetails user) {
    return totpService.generateBinding(user.getUsername(), "MyApp");
}

@PostMapping("/auth/verify-totp")
@RequiresTotp
public void verifyTotp(@RequestParam String code) {
    totpService.verify(code);
}
```

### TOTP 2FA 流程

```java
// 1. 生成绑定信息
TotpBinding binding = totpService.generateBinding("user@example.com", "MyApp");
// binding.getTotpSecret() → Base32 编码的密钥（存入数据库）
// binding.getOtpAuthUri()  → otpauth:// URI（用于生成 QR 码）

// 2. 用户扫描 QR 码后，验证验证码
totpService.verify("123456");

// 3. 登录时验证（需设置 twoFactorEnabled=true）
if (user.isTwoFactorEnabled() && user.isTwoFactorBound()) {
    // 要求用户提供 TOTP 验证码
}
```

---

## <a name="mybatis"></a>migoo-spring-boot-starter-mybatis

MyBatis-Plus 增强组件，提供扩展 Wrapper、分页支持、加密类型处理器等。

### 配置项

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db
    username: root
    password: root

  # 动态数据源
  datasource:
    dynamic:
      primary: master
      strict: false
      datasource:
        master:
          url: jdbc:mysql://master:3306/db
          username: root
          password: root
        slave:
          url: jdbc:mysql://slave:3306/db
          username: root
          password: root
```

### 核心功能

#### 基础实体类

```java
// UUID 主键
@TableName("t_user")
public class UserDO extends BaseDO {
    private String name;
    private Integer status;
}

// 自增主键
@TableName("t_user")
public class UserDO extends BaseAutoIncDO {
    private String name;
    private Integer status;
}
```

#### 扩展 Wrapper

```java
// LambdaQueryWrapperX - 支持排序、分页
LambdaQueryWrapperX<UserDO> wrapper = new LambdaQueryWrapperX<UserDO>()
    .eqIfPresent(UserDO::getStatus, status)
    .likeIfPresent(UserDO::getName, name)
    .orderByDesc(UserDO::getId);
List<UserDO> users = userMapper.selectList(wrapper);

// LambdaUpdateWrapperX - 条件更新
LambdaUpdateWrapperX<UserDO> updateWrapper = new LambdaUpdateWrapperX<UserDO>()
    .set(UserDO::getStatus, 0)
    .eq(UserDO::getId, userId);
userMapper.update(null, updateWrapper);
```

#### 分页查询

```java
// Mapper 继承 BaseMapperX
public interface UserMapper extends BaseMapperX<UserDO> {
    default PageResult<UserDO> selectPage(UserPageReqParam reqParam) {
        return selectPage(reqParam, new LambdaQueryWrapperX<UserDO>()
            .eqIfPresent(UserDO::getStatus, reqParam.getStatus()));
    }
}

// 使用
PageResult<UserVO> result = userMapper.selectPage(reqParam);
```

#### 加密类型处理器

```java
@TableName("t_user")
public class UserDO extends BaseDO {
    // 自动加密/解密
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String mobile; // 存储时加密，读取时解密

    @TableField(typeHandler = JsonLongSetTypeHandler.class)
    private Set<Long> roleIds; // JSON 存储，自动转换
}
```

---

## <a name="redis"></a>migoo-spring-boot-starter-redis

Redis 组件，提供 RedisKit 工具类和标准化 Key 定义。

### 配置项

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: ''
      database: 0
```

### 核心功能

#### RedisKit 使用

```java
@Resource
private RedisKit redisKit;

// String 操作
redisKit.set("key", "value", 30, TimeUnit.MINUTES);
String value = redisKit.get("key");

// Hash 操作
redisKit.hSet("hash", "field", "value");
Object hashValue = redisKit.hGet("hash", "field");

// List 操作
redisKit.lPush("list", "value");
List<Object> values = redisKit.lRange("list", 0, -1);

// Set 操作
redisKit.sAdd("set", "value");
Set<Object> members = redisKit.sMembers("set");

// ZSet 操作
redisKit.zAdd("zset", "value", 1.0);
Set<Object> range = redisKit.zRange("zset", 0, -1);
```

#### 标准化 Key 定义

```java
public enum RedisKeyDefine {
    // 用户相关
    USER_TOKEN("user:token:%s", "用户 Token", RedisKeyTTLEnum.TOKEN_EXPIRE),

    // 验证码
    SMS_CODE("sms:code:%s", "短信验证码", RedisKeyTTLEnum.FIVE_MINUTES),

    // 分布式锁
    DISTRIBUTIVE_LOCK("lock:%s", "分布式锁", RedisKeyTTLEnum.TEN_SECONDS);

    private final String pattern;
    private final String description;
    private final RedisKeyTTLEnum ttl;
}
```

---

## <a name="mq"></a>migoo-spring-boot-starter-mq

MQ 组件，基于 Redis 实现消息队列，支持 Stream 和 Pub/Sub 两种模式。

### 配置项

```yaml
migoo:
  mq:
    # Redis Stream 配置
    stream:
      enabled: true
      consumer-group: my-group
      consumer-name: my-consumer
    # Redis Pub/Sub 配置
    pubsub:
      enabled: true
```

### 核心功能

#### Stream 消息模式

```java
// 定义消息
@Data
@StreamMessage(channel = "order")
public class OrderMessage extends AbstractStreamMessage {
    private Long orderId;
    private Integer status;
}

// 发送消息
@Resource
private RedisMQTemplate redisMQTemplate;

OrderMessage message = new OrderMessage();
message.setOrderId(123456L);
message.setStatus(1);
redisMQTemplate.send(message);

// 消费消息
@Component
public class OrderMessageListener extends AbstractStreamMessageListener<OrderMessage> {

    @Override
    protected void onMessage(OrderMessage message) {
        log.info("收到订单消息: {}", message.getOrderId());
        // 处理业务逻辑
    }
}
```

#### Pub/Sub 消息模式

```java
// 定义消息
@Data
@ChannelMessage(channel = "notification")
public class NotificationMessage extends AbstractChannelMessage {
    private String title;
    private String content;
}

// 发送消息
NotificationMessage message = new NotificationMessage();
message.setTitle("系统通知");
message.setContent("您的订单已发货");
redisMQTemplate.send(message);

// 消费消息
@Component
public class NotificationListener extends AbstractChannelMessageListener<NotificationMessage> {

    @Override
    protected void onMessage(NotificationMessage message) {
        log.info("收到通知: {}", message.getTitle());
        // 处理业务逻辑
    }
}
```

#### 幂等消费

```java
@Component
public class OrderMessageListener extends AbstractStreamMessageListener<OrderMessage> {

    @Override
    @IdempotentMessage // 开启幂等消费
    protected void onMessage(OrderMessage message) {
        // 同一消息只会被处理一次
    }
}
```

---

## 许可证

[MIT License](LICENSE)
