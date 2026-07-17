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
List<UserVO> voList = CollectionUtils.convertList(doList, BeanUtils::toBean);
Map<Long, UserDO> map = CollectionUtils.convertMap(list, UserDO::getId);
List<UserDO> filtered = CollectionUtils.filterList(list, u -> u.getStatus() == 1);

// ========== 日期 ==========
LocalDateTime today = LocalDateTimeUtils.getToday();
boolean between = LocalDateTimeUtils.isBetween(time, start, end);
long days = LocalDateTimeUtils.between(start, end);

// ========== Bean 拷贝 ==========
UserVO vo = BeanUtils.toBean(userDO, UserVO.class);
PageResult<UserVO> voPage = BeanUtils.toBean(doPage, UserVO.class);

// ========== 加解密 ==========
String encrypted = EncryptTypeHandler.encrypt("敏感数据");

// ========== RSA ==========
String sign = RSA.sign(content, privateKey);
boolean ok = RSA.verify(content, sign, publicKey);
```

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
