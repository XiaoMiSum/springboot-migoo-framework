---
layout: default
---

# MiGoo Spring Boot Framework

基于 Spring Boot 4.1.0 的企业级快速开发框架，提供安全认证、数据访问、缓存、消息队列等开箱即用的能力。

## 组件文档

| 组件                    | 说明                             |
|-------------------------|----------------------------------|
| [Common](common.md)     | 公共工具类、异常处理、分页、校验 |
| [Web](web.md)           | Web MVC 配置、全局异常、统一响应 |
| [Security](security.md) | 认证授权、JWT、OAuth2、TOTP 2FA  |
| [MyBatis](mybatis.md)   | MyBatis-Plus 增强、分页、数据源  |
| [Redis](redis.md)       | Redis 配置、工具类               |
| [MQ](mq.md)             | Redis 消息队列（Stream/Pub-Sub） |

## 快速开始

### 引入 BOM

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

### 按需引入组件

```xml
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
```

## 技术栈

| 组件            | 版本   |
|-----------------|--------|
| Java            | 21+    |
| Spring Boot     | 4.1.0  |
| Spring Security | 7.1.0  |
| MyBatis-Plus    | 3.5.16 |
| Redisson        | 4.2.0  |

## 许可证

[MIT License](https://github.com/xiaomisum/springboot-migoo-framework/blob/master/LICENSE)
