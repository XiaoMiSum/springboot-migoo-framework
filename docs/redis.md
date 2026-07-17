---
layout: default
---

# migoo-spring-boot-starter-redis

Redis 组件，提供类型安全的 `RedisKit` 和标准化 Key 定义。

## 快速开始

| 步骤 | 说明 |
|------|------|
| 1. 引入依赖 | 添加 `migoo-spring-boot-starter-redis` |
| 2. 配置连接 | `spring.data.redis.*` 配置 Redis 地址（参考下方四种模式） |
| 3. 定义 Key 模板 | 创建 `RedisKeyDefine` 枚举，指定 key 模板、值类型、过期策略 |
| 4. 注入 RedisKit | `@Resource private RedisKit redisKit;` 直接使用 |

```yaml
# 最小配置
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

```java
// 最简使用
@Resource
private RedisKit redisKit;

redisKit.set(key, value, args);        // 写入
String v = redisKit.get(key, args);    // 读取
long n = redisKit.increment(key, 1L);  // 原子自增
```

## 依赖

```xml
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-redis</artifactId>
</dependency>
```

## 应用层对接

### 1. 配置 Redis 连接

使用 Spring Boot 标准 `spring.data.redis.*` 配置，支持单机、主从、哨兵、集群四种模式：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      connect-timeout: 3s
      lettuce:
        pool:
          enabled: true
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: 2s
```

### 2. 定义 Key 模板

```java
public enum UserRedisKey {
    TOKEN(RedisKeyDefine.<String>builder()
            .keyTemplate("user:token:%s")
            .valueType(new TypeReference<String>() {})
            .timeoutHours(2)
            .build()),

    INFO(RedisKeyDefine.<UserDTO>builder()
            .keyTemplate("user:info:%s")
            .valueType(new TypeReference<UserDTO>() {})
            .timeoutMinutes(30)
            .dynamicTimeout()      // 每次读写重置 TTL
            .build()),

    LOGIN_COUNT(RedisKeyDefine.<Long>builder()
            .keyTemplate("user:login:count:%s")
            .valueType(new TypeReference<Long>() {})
            .timeoutDays(1)
            .fixedTimeout()        // 仅首次设置 TTL
            .build()),

    LOCK_ORDER(RedisKeyDefine.<String>builder()
            .keyTemplate("lock:order:%s")
            .valueType(new TypeReference<String>() {})
            .timeoutSeconds(30)
            .build());

    private final RedisKeyDefine<?> keyDefine;
}
```

### 3. 注入 RedisKit 使用

```java
@Resource
private RedisKit redisKit;

// 设置（自动按 key 定义的过期策略处理）
redisKit.set(UserRedisKey.TOKEN.getKeyDefine(), tokenValue, userId);

// 读取（自动反序列化）
String token = redisKit.get(UserRedisKey.TOKEN.getKeyDefine(), userId);

// 原子自增
long count = redisKit.increment(UserRedisKey.LOGIN_COUNT.getKeyDefine(), userId);

// 分布式锁
String lockValue = UUID.randomUUID().toString();
if (redisKit.tryLock(UserRedisKey.LOCK_ORDER.getKeyDefine(), lockValue, orderId)) {
    try {
        // 业务逻辑
    } finally {
        redisKit.unlock(UserRedisKey.LOCK_ORDER.getKeyDefine(), lockValue, orderId);
    }
}
```

---

## 四种部署模式

### 单机模式（Standalone）

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

### 主从复制模式（Master-Slave）

通过 Lettuce 的 `ReadFrom` 配置实现读写分离：写入主节点，读取从节点。

```yaml
spring:
  data:
    redis:
      host: master-host
      port: 6379
      lettuce:
        read-from: replica-preferred  # 或 master
```

### 哨兵模式（Sentinel）

通过 Redis Sentinel 实现高可用自动故障转移。

```yaml
spring:
  data:
    redis:
      password: your-password
      sentinel:
        master: mymaster
        nodes:
          - sentinel1:26379
          - sentinel2:26379
```

### 集群模式（Cluster）

Redis Cluster 模式，支持拓扑自动刷新。

```yaml
spring:
  data:
    redis:
      password: your-password
      cluster:
        nodes:
          - node1:7000
          - node2:7001
          - node3:7002
      lettuce:
        cluster:
          refresh:
            period: 30s          # 定期刷新周期
            adaptive: true       # 自适应刷新（故障转移时触发）
```

---

## 过期策略

| 策略 | 说明 |
|------|------|
| `PERMANENT` | 永不过期 |
| `FIXED` | 仅首次 `set` 时设置 TTL，后续写入不刷新 |
| `DYNAMIC` | 每次写入都重置 TTL（滑动窗口） |

## 自动注册的组件

| 组件 | 说明 |
|------|------|
| `RedisKit` | Redis 操作工具类 |
| `RedisTemplate<String, Object>` | Spring RedisTemplate（value 使用 String 序列化） |
| `RedisConnectionFactory` | 由 Spring Boot `DataRedisAutoConfiguration` 自动创建 |

## 配置项

所有连接配置由 Spring Boot 管理，参考 [Spring Boot Data Redis Properties](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/data/redis/autoconfigure/DataRedisProperties.html)
