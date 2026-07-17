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

加密密钥通过环境变量 `mybatis-plus.encryptor.password` 或 JVM 参数 `-Dmybatis-plus.encryptor.password=xxx` 配置。

### 6. JSON 字段存储

```java
@TableName("t_user")
public class UserDO extends BaseUuidDO<UserDO> {
    // Set<Long> 自动序列化为 JSON 存储
    @TableField(typeHandler = JsonLongSetTypeHandler.class)
    private Set<Long> roleIds;
}
```

### 7. 列表字段存储

```java
@TableName("t_user")
public class UserDO extends BaseUuidDO<UserDO> {
    // List<String> 以逗号分隔存储
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> tags;
}
```

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
# 加密密钥配置（可选）
-Dmybatis-plus.encryptor.password=your-encrypt-key
```
