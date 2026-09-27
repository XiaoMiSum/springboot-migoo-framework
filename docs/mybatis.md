---
layout: default
---

# migoo-spring-boot-starter-mybatis

MyBatis-Plus 增强组件，提供扩展 Wrapper、分页支持、加密类型处理器、自动填充等。

## 快速开始

| 步骤 | 说明 |
|------|------|
| 1. 引入依赖 | 添加 `migoo-spring-boot-starter-mybatis` |
| 2. 配置数据源 | `spring.datasource.*` 配置数据库连接 |
| 3. 定义实体 | 继承 `BaseUuidDO` 或 `BaseAutoIncDO`，自动获得 `createdAt/updatedAt/isDeleted` |
| 4. 定义 Mapper | 继承 `BaseMapperX`，使用 `LambdaQueryWrapperX` 查询 |
| 5. 使用分页 | 请求参数继承 `PageParam`，返回 `PageResult` |

```yaml
# 最小配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db
    username: root
    password: root
```

```java
// 实体定义
@TableName("t_user")
public class UserDO extends BaseUuidDO<UserDO> {
    private String name;
}

// Mapper 定义
@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {
}

// 查询
UserDO user = userMapper.selectOne(UserDO::getName, "张三");
```

## 依赖

```xml
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-mybatis</artifactId>
</dependency>
```

## 应用层对接

### 1. 定义实体类

```java
// UUID 主键（框架自动生成有序 UUID）
@TableName("t_user")
public class UserDO extends BaseUuidDO<UserDO> {
    private String name;
    private Integer status;
}

// 自增主键
@TableName("t_user")
public class UserDO extends BaseAutoIncDO<Long, UserDO> {
    private String name;
    private Integer status;
}
```

`BaseDO` 提供字段：`createdAt`、`updatedAt`、`isDeleted`（逻辑删除），自动填充。

### 2. 定义 Mapper

```java
@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {

    // 自定义分页查询
    default PageResult<UserDO> selectPage(UserPageReqParam reqParam) {
        return selectPage(reqParam, new LambdaQueryWrapperX<UserDO>()
                .likeIfPresent(UserDO::getName, reqParam.getName())
                .eqIfPresent(UserDO::getStatus, reqParam.getStatus()));
    }
}
```

### 3. 使用扩展 Wrapper 查询

```java
// LambdaQueryWrapperX - 条件查询（null 值自动跳过）
LambdaQueryWrapperX<UserDO> wrapper = new LambdaQueryWrapperX<UserDO>()
        .eqIfPresent(UserDO::getStatus, status)       // status 为 null 时跳过
        .likeIfPresent(UserDO::getName, name)          // name 为 null 时跳过
        .betweenIfPresent(UserDO::getCreatedAt, start, end) // 边界为 null 时退化
        .orderByDesc(UserDO::getId);

// 单条查询
UserDO user = userMapper.selectOne(UserDO::getUsername, username);

// 计数
long count = userMapper.selectCount(UserDO::getStatus, 1);

// 批量插入
userMapper.insertBatch(userList);

// 批量更新
userMapper.updateBatch(updateList);
```

### 4. 联表查询（MyBatis-Plus-Join）

```java
@Mapper
public interface OrderMapper extends BaseMapperX<OrderDO> {

    default PageResult<OrderVO> selectPage(OrderPageReqParam reqParam) {
        return selectJoinPage(reqParam, OrderVO.class,
                new MPJLambdaWrapperX<OrderDO>()
                        .leftJoinX(UserDO.class, OrderDO::getUserId, UserDO::getId)
                        .selectAs(UserDO::getName, OrderVO::getUserName)
                        .eqIfPresent(OrderDO::getStatus, reqParam.getStatus()));
    }
}
```

### 5. 加密字段存储

```java
@TableName("t_user")
public class UserDO extends BaseUuidDO<UserDO> {
    // 存储时 AES 加密，读取时自动解密
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String mobile;
}
```

加密密钥按以下优先级读取（**推荐用环境变量，口令不进代码仓库**）：

1. JVM 系统属性：`-Dmybatis-plus.encryptor.password=xxx`
2. 环境变量：`MIGOO_ENCRYPTOR_PASSWORD`（推荐）
3. 遗留环境变量：`mybatis-plus.encryptor.password`（兼容历史部署）

**密文格式（1.4.0 起）**：新写入的密文为 `v1:` 前缀的 AES-256-GCM（PBKDF2-SHA256 12 万轮派生密钥），
自带完整性校验；**无 `v1:` 前缀的历史密文自动走遗留 ECB+MD5 兼容解密**，存量数据无需迁移，
读写一轮后自然升级为新格式。

> 迁移提示：更换密钥前需先把存量密文按旧密钥解密重写，否则旧密文将无法解密。

### 6. JSON 字段存储

```java
@TableName(value = "t_user", autoResultMap = true)
public class UserDO extends BaseUuidDO<UserDO> {
    // Set<Long> 序列化为 JSON 数组存储（MP 内置 JacksonTypeHandler，任意类型均可）
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Long> roleIds;
}
```

### 7. 列表字段存储

```java
@TableName(value = "t_user", autoResultMap = true)
public class UserDO extends BaseUuidDO<UserDO> {
    // List<String> 以逗号分隔存储
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> tags;
}
```

> TypeHandler 在**查询结果映射**时生效需要 `@TableName(autoResultMap = true)`（如上）；
> 插入/更新按字段指定的 TypeHandler 写入，不受此限。

### 8. 自定义排序查询

```java
// 请求参数继承 SortablePageParam
public class UserPageReqParam extends SortablePageParam {
    private String name;
}

// Mapper 中使用
default PageResult<UserDO> selectPage(UserPageReqParam reqParam) {
    return selectPage(reqParam, new LambdaQueryWrapperX<UserDO>()
            .likeIfPresent(UserDO::getName, reqParam.getName())
            .orderByAsc(UserDO::getId));
}
```

---

## 时区设计

框架采用 **DB 统一存 UTC，API 返回 UTC，前端按用户时区显示** 的策略。

```
应用层（UTC+8 16:35）
  ↓ 自动填充 LocalDateTime.now()
  ↓ UTCLocalDateTimeHandler 转换
DB（UTC 08:35）
  ↓ UTCLocalDateTimeHandler 读取
API（UTC 08:35 → "yyyy-MM-dd'T'HH:mm:ss'Z'"）
  ↓ 前端解析 Z 后缀
浏览器（按用户时区显示 16:35）
```

关键组件：

- **`UTCLocalDateTimeHandler`** — 全局 `LocalDateTime` 类型处理器
  - 写入：将系统时区时间转为 UTC Instant 存储
  - 读取：保持 UTC 时间返回，不转回系统时区（由前端自行转换）
- **`DefaultFieldHandler`** — 自动填充 `createdAt` / `updatedAt`
  - 取系统时区的当前时间（`LocalDateTime.now()`），`UTCLocalDateTimeHandler` 负责转 UTC
- **`BaseDO`** — `@JsonFormat(timezone = "UTC")` 确保 API 响应输出带 `Z` 后缀的 ISO 8601 格式

> 注意事项：应用 JVM 时区（`-Duser.timezone=Asia/Shanghai` 或 `TZ` 环境变量）必须与业务期望时区一致，否则 `LocalDateTime.now()` 会取错时间。

## 自动注册的组件

| 组件 | 说明 |
|------|------|
| `@MapperScan` | 扫描 `xyz.migoo.framework.**` 下的 Mapper |
| `PaginationInnerInterceptor` | 分页插件 |
| `UTCLocalDateTimeHandler` | 全局 LocalDateTime UTC 时区处理 |
| `DefaultFieldHandler` | 自动填充 createdAt / updatedAt / isDeleted |

## 配置项

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db
    username: root
    password: root

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
```

```bash
# 加密密钥配置（可选；推荐环境变量方式）
export MIGOO_ENCRYPTOR_PASSWORD='openssl rand -base64 48 生成的强口令'
# 或 JVM 参数方式
-Dmybatis-plus.encryptor.password=your-encrypt-key
```

## 多数据源配置

框架默认提供单数据源，如需多数据源请引入 `dynamic-datasource-spring-boot4-starter`。

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot4-starter</artifactId>
</dependency>
```

版本由 BOM 统一管理，无需单独指定。

### 2. 配置数据源

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      strict: false
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/master_db
          username: root
          password: root
        slave:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://localhost:3306/slave_db
          username: root
          password: root
```

### 3. 使用注解切换数据源

```java
@Service
public class UserService {

    @DS("master")  // 切换到主库
    public void saveMaster(UserDO user) {
        userMapper.insert(user);
    }

    @DS("slave")  // 切换到从库
    public UserDO getFromSlave(Long id) {
        return userMapper.selectById(id);
    }
}
```

### 4. 注意事项

- `@DS` 注解标注在类上可实现类级别数据源切换
- 未标注 `@DS` 的方法使用 `primary` 配置的主数据源
- 多数据源下请确保每个数据源都有对应的 `Mapper` 扫描路径
- 动态数据源事务管理器与 Spring 默认不同，需使用 `@Transactional(rollbackFor = Exception.class)`
