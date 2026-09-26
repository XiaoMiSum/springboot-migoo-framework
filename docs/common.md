---
layout: default
---

# migoo-spring-boot-starter-common

公共组件，提供统一响应体、分页模型、异常体系、校验注解、工具类等。

## 快速开始

| 步骤 | 说明 |
|------|------|
| 1. 引入依赖 | 添加 `migoo-spring-boot-starter-common` |
| 2. 定义错误码 | 创建 `ErrorCode` 常量（10 位数字） |
| 3. 抛出异常 | `ServiceExceptionUtil.get(ErrorCode)` |
| 4. 返回响应 | `Result.ok(data)` / `Result.error(ErrorCode)` |

```java
// 错误码
public interface UserErrorCode {
    ErrorCode USER_NOT_FOUND = ErrorCode.of(1001000000, "用户不存在");
}

// 抛异常
throw ServiceExceptionUtil.get(UserErrorCode.USER_NOT_FOUND);

// 统一响应
return Result.ok(userVO);

// 分页
PageResult<UserVO> page = userMapper.selectPage(reqParam);
return Result.ok(page);
```

## 依赖

```xml
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-common</artifactId>
</dependency>
```

## 应用层对接

### 1. 定义业务错误码

```java
public interface UserErrorCode {
    ErrorCode USER_NOT_FOUND = ErrorCode.of(1001000000, "用户不存在");
    ErrorCode USER_STATUS_DISABLED = ErrorCode.of(1001000001, "用户已被禁用");
}
```

> 错误码约定：10 位数字，分四段 `类型(1) / 系统(3) / 模块(3) / 错误编号(3)`，业务码从 `1,000,000,000` 起。

### 2. 抛出业务异常

```java
// 基础用法
throw ServiceExceptionUtil.get(UserErrorCode.USER_NOT_FOUND);

// 带参数（{0} 占位符替换）
throw ServiceExceptionUtil.get(UserErrorCode.USER_NOT_FOUND, userId);
```

全局异常处理器（web 组件）会自动捕获 `ServiceException`，返回对应错误码的 `Result` 响应。

### 3. Controller 返回统一响应

```java
// 成功（无数据）
return Result.ok();

// 成功（带数据）
return Result.ok(userVO);

// 分页查询
return Result.ok(userMapper.selectPage(reqParam));
```

### 4. 分页请求/响应

```java
// 请求 DTO 继承 PageParam
public class UserPageReqParam extends PageParam {
    private String name;
    private Integer status;
}

// Mapper 继承 BaseMapperX，返回 PageResult
PageResult<UserVO> result = userMapper.selectPage(reqParam);
// result.getList()  -> 数据列表
// result.getTotal() -> 总条数
```

### 5. 参数校验注解

```java
public class UserCreateReqBody {
    @Mobile                          // 手机号（11位，1开头）
    private String mobile;

    @Email                           // 邮箱
    private String email;

    @Password                        // 密码（8-32位，含字母+数字+特殊字符）
    private String password;

    @InEnum(UserStatusEnum.class)    // 枚举值校验
    private Integer status;
}
```

配合 `@Valid` 在 Controller 自动校验。

### 6. 工具类速查

```java
// ========== JSON ==========
String json = JsonUtils.toJsonString(object);
UserVO user = JsonUtils.parseObject(json, UserVO.class);
List<UserVO> list = JsonUtils.parseArray(json, UserVO.class);

// ========== 集合 ==========
List<String> names = CollectionUtils.convertList(doList, UserDO::getName);
Map<Long, UserDO> map = CollectionUtils.convertMap(list, UserDO::getId);
List<UserDO> filtered = CollectionUtils.filterList(list, u -> u.getStatus() == 1);

// ========== 日期 ==========
LocalDateTime today = LocalDateTimeUtils.getToday();
boolean between = LocalDateTimeUtils.isBetween(time, start, end);
long days = LocalDateTimeUtils.between(start, end);

// ========== Bean 拷贝 ==========
// 框架不内置 Bean 拷贝工具，使用 Spring 标准 BeanUtils（org.springframework.beans.BeanUtils），
// 或编译期方案 MapStruct
UserVO vo = new UserVO();
BeanUtils.copyProperties(userDO, vo);

// PageResult 逐层转换
PageResult<UserVO> voPage = new PageResult<>(
        CollectionUtils.convertList(doPage.getList(), item -> {
            UserVO itemVo = new UserVO();
            BeanUtils.copyProperties(item, itemVo);
            return itemVo;
        }),
        doPage.getTotal());

// ========== 加解密 ==========
String encrypted = EncryptTypeHandler.encrypt("敏感数据");

// ========== RSA ==========
String sign = RSA.sign(content, privateKey);
boolean ok = RSA.verify(content, sign, publicKey);
```

---

## 分布式 ID

`xyz.migoo.framework.common.id.IdGenerator` SPI（**零 Spring 依赖**，直接 new，或注册为 Bean 按类型注入）：

| 实现 | 形态 | 特点 | 适用 |
|---|---|---|---|
| `UuidV7IdGenerator` | UUIDv7（RFC 9562，36 字符） | 零协调、零外部依赖；**字典序即时间序**；同毫秒 12 位计数严格单调、计数溢出自旋到下一毫秒；时钟回拨沿用上次时间戳不倒退 | VARCHAR 主键、对外编号、请求 ID |
| `SnowflakeIdGenerator` | 19 位数字串 / `long` | `[1bit 符号][41bit 时间戳][5bit 机房][5bit 机器][12bit 序列]`，纪元 2024-01-01（约 69 年）；同毫秒序列递增、溢出自旋；**时钟回拨直接抛错**（拒绝发号而非发出重复 ID） | BIGINT 主键、订单号等纯数字场景 |

```java
// UUIDv7：零配置
IdGenerator idGenerator = new UuidV7IdGenerator();
String id = idGenerator.nextId();          // 例如 01901f2c-8b3e-7a41-9c6d-2f5b8e0a17d3

// 雪花：多机部署须为每台机器分配唯一 workId（0 ~ 31）
SnowflakeIdGenerator snowflake = new SnowflakeIdGenerator(0, 5);
long id = snowflake.nextLong();            // BIGINT 主键
String idStr = snowflake.nextId();         // 纯数字字符串形态
```

```java
// 注册为 Bean 后按 IdGenerator 类型注入
@Bean
public IdGenerator idGenerator() {
    return new UuidV7IdGenerator();
}
```

- **排序口径**：UUIDv7 按**字符串**排序 = 时间序；雪花按 **long** 排序 = 时间序。不要把雪花转成字符串排序——位数会随时间增长（18 → 19 位），字典序会错位；
- **雪花 workId 分配**：静态配置（每台机器唯一）或由部署环境注入；框架不内置协调（避免反向引入 redis/zk 依赖）；
- **边界**：雪花时钟回拨抛 `IllegalStateException`，调用方等待时钟追平后重试；系统时钟早于纪元 2024-01-01 或 41 位时间位耗尽时拒绝发号；
- 与既有主键不冲突：`BaseUuidDO` 的主键策略（uuid-creator 时间有序 UUID）保持不变，本节是给业务编号/自定义主键场景的可选能力。

---

## 敏感数据脱敏（@Sensitive）

`xyz.migoo.framework.common.sensitive` 包，Jackson 注解内省驱动——`JsonUtils` 与 Spring MVC 响应序列化**自动生效**，无需注册额外模块：

| 策略 | 输出示例 | 说明 |
|---|---|---|
| `MOBILE` | `138****5678` | 手机号：前 3 后 4 |
| `ID_CARD` | `3301**********1234` | 身份证：前 4 后 4 |
| `EMAIL` | `a***@example.com` | 邮箱：本地部分留首字符，域名保留 |
| `BANK_CARD` | `6222********7890` | 卡号：前 4 后 4 |
| `PASSWORD` | `******` | 定长掩码（不泄露原长度） |

```java
public class UserVO {
    @Sensitive(type = SensitiveType.MOBILE)
    private String mobile;        // -> "138****5678"

    @Sensitive(type = SensitiveType.PASSWORD)
    private String password;      // -> "******"

    private String nickname;      // 未标注，原样输出
}
```

- 只作用于**序列化**（输出方向），反序列化不还原——入参无需脱敏；
- 数值字段（如 Long 卡号）同样适用，脱敏后以**字符串**形态输出；
- null 原样输出 JSON null；长度不足以保留两侧时整体掩码；非邮箱格式（无 `@`）整体掩码；
- 非 JSON 场景独立调用：`SensitiveDataUtil.mask(value, SensitiveType.MOBILE)`。

---

## 核心 API 一览

### Result

| 方法 | 说明 |
|------|------|
| `Result.ok()` | 成功（无数据） |
| `Result.ok(data)` | 成功（带数据） |
| `Result.error(ErrorCode)` | 错误响应 |

### ErrorCode

```java
ErrorCode.of(code, msg)  // 工厂方法
```

### PageParam / PageResult

| 字段 | 说明 |
|------|------|
| `PageParam.pageNo` | 页码，默认 1 |
| `PageParam.pageSize` | 每页条数，默认 10，最大 100 |
| `PageResult.list` | 数据列表 |
| `PageResult.total` | 总条数 |

### GlobalErrorCodeConstants

| 常量 | 码 | 说明 |
|------|----|------|
| `SUCCESS` | 200 | 成功 |
| `BAD_REQUEST` | 400 | 请求参数错误 |
| `UNAUTHORIZED` | 401 | 未认证 |
| `FORBIDDEN` | 403 | 权限不足 |
| `NOT_FOUND` | 404 | 资源不存在 |
| `INTERNAL_SERVER_ERROR` | 500 | 系统内部错误 |

### IdGenerator（分布式 ID）

| 方法 | 说明 |
|------|------|
| `IdGenerator.nextId()` | 生成下一个 ID（字符串形态） |
| `SnowflakeIdGenerator.nextLong()` | 雪花 `long` 形态（BIGINT 主键） |
| `UuidV7IdGenerator.nextUuid()` | UUIDv7 `UUID` 形态 |

### @Sensitive / SensitiveDataUtil（脱敏）

| 方法 | 说明 |
|------|------|
| `@Sensitive(type = ...)` | 字段脱敏，Jackson 序列化自动生效 |
| `SensitiveDataUtil.mask(value, type)` | 独立脱敏调用（非 JSON 场景） |
