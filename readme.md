# MiGoo Spring Boot Framework

基于 Spring Boot 4.1.0 的企业级快速开发框架，提供安全认证、数据访问、缓存、消息队列等开箱即用的能力。

[![Java](https://img.shields.io/badge/Java-21+-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 特性

- **安全认证** - JWT / OAuth2 / TOTP 二次验证，开箱即用
- **数据访问** - MyBatis-Plus 增强，分页、加密字段、自动填充
- **缓存工具** - RedisKit 类型安全操作，四种部署模式支持
- **消息队列** - Redis Stream / Pub/Sub，幂等消费、自动重试
- **Web 基础** - 全局异常处理、统一响应、TraceId、i18n
- **公共工具** - 错误码、分页、校验注解、JSON/集合/日期工具类

## 快速开始

### 1. 引入 BOM

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>xyz.migoo.springboot</groupId>
            <artifactId>migoo-framework-dependencies</artifactId>
            <version>1.3.17</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 按需引入组件

```xml

<dependencies>
    <!-- 公共组件 -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-common</artifactId>
    </dependency>

    <!-- Web 组件 -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Security 组件 -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-security</artifactId>
    </dependency>

    <!-- MyBatis 组件 -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-mybatis</artifactId>
    </dependency>

    <!-- Redis 组件 -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-redis</artifactId>
    </dependency>

    <!-- MQ 组件 -->
    <dependency>
        <groupId>xyz.migoo.springboot</groupId>
        <artifactId>migoo-spring-boot-starter-mq</artifactId>
    </dependency>
</dependencies>
```

### 3. 开始使用

```java
// 定义错误码
public interface UserErrorCode {
    ErrorCode USER_NOT_FOUND = ErrorCode.of(1001000000, "用户不存在");
}

// Controller 返回统一响应
@GetMapping("/user")
public Result<UserVO> getUser(@AuthUser AuthUserDetails user) {
    return Result.ok(userMapper.selectById(user.getId()));
}

// Redis 缓存操作
redisKit.

set(UserRedisKey.TOKEN.getKeyDefine(),token,userId);
String cached = redisKit.get(UserRedisKey.TOKEN.getKeyDefine(), userId);
```

## 组件文档

| 组件                                 | 说明                             | 文档                     |
|--------------------------------------|----------------------------------|--------------------------|
| `migoo-spring-boot-starter-common`   | 公共工具类、异常处理、分页、校验 | [查看](docs/common.md)   |
| `migoo-spring-boot-starter-web`      | Web MVC 配置、全局异常、统一响应 | [查看](docs/web.md)      |
| `migoo-spring-boot-starter-security` | 认证授权、JWT、OAuth2、TOTP 2FA  | [查看](docs/security.md) |
| `migoo-spring-boot-starter-mybatis`  | MyBatis-Plus 增强、分页、数据源  | [查看](docs/mybatis.md)  |
| `migoo-spring-boot-starter-redis`    | Redis 配置、工具类               | [查看](docs/redis.md)    |
| `migoo-spring-boot-starter-mq`       | Redis 消息队列（Stream/Pub-Sub） | [查看](docs/mq.md)       |

## 项目结构

```
migoo-framework-parent/
├── migoo-framework-dependencies      # BOM 统一版本管理
├── migoo-spring-boot-starter-common  # 公共组件
├── migoo-spring-boot-starter-web     # Web 组件
├── migoo-spring-boot-starter-security# 安全组件
├── migoo-spring-boot-starter-mybatis # MyBatis 组件
├── migoo-spring-boot-starter-redis   # Redis 组件
└── migoo-spring-boot-starter-mq      # MQ 组件
```

## 许可证

[MIT License](LICENSE)
