---
layout: default
---

# MiGoo Spring Boot Framework

基于 Spring Boot 4.1.0 的企业级快速开发框架，提供安全认证、数据访问、缓存、消息队列等开箱即用的能力。

## 组件文档

| 组件                      | 说明                             |
|---------------------------|----------------------------------|
| [Common](common.md)       | 公共工具类、异常处理、分页、校验 |
| [Web](web.md)             | Web MVC 配置、全局异常、统一响应 |
| [Security](security.md)   | 认证授权、JWT、OAuth2、TOTP 2FA  |
| [WebSocket](websocket.md) | WebSocket 连接管理、Token 认证   |
| [MyBatis](mybatis.md)     | MyBatis-Plus 增强、分页、数据源  |
| [Redis](redis.md)         | Redis 配置、工具类               |
| [MQ](mq.md)               | Redis 消息队列（Stream/Pub-Sub） |
| [Observability](observability.md) | 可观测性：Metrics、Tracing、日志关联 |
| [Springdoc](springdoc.md) | OpenAPI 接口文档：springdoc + Swagger UI |

## 快速开始

### 引入 BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>xyz.migoo.springboot</groupId>
            <artifactId>migoo-framework-dependencies</artifactId>
            <version>1.4.0</version>
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

<!-- WebSocket 组件 -->
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-websocket</artifactId>
</dependency>

<!-- MQ 组件 -->
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-mq</artifactId>
</dependency>

<!-- 可观测性组件 -->
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-observability</artifactId>
</dependency>

<!-- 接口文档组件 -->
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-springdoc</artifactId>
</dependency>
```

> ⚠️ 若应用**未继承** `spring-boot-starter-parent`，请自行开启编译参数 `-parameters`
> （`maven.compiler.parameters=true`），否则 Spring MVC 的 `@RequestParam` 省略 `name`、
> `@AuthenticationPrincipal` 等参数名解析会在运行期报错。

## 技术栈

| 组件              | 版本   |
|-------------------|--------|
| Java              | 21+    |
| Spring Boot       | 4.1.0  |
| Spring Security   | 7.1.0  |
| MyBatis-Plus      | 3.5.17 |
| mybatis-plus-join | 1.5.9  |
| Jackson           | 3.1.0  |
| springdoc         | 3.1.1  |

> 1.4.0 起 BOM 收敛：移除了长期未用/无法解析的死条目（spring-ai-bom、fastjson2、redisson、
> dynamic-datasource、jdom2、jsoup、ip2region、caffeine、wechatpay、alipay 等），
> 需要这些组件的应用请自行在业务侧声明版本，降低供应链与升级维护成本。

### GraalVM Native / AOT

框架内置运行时线索（`RuntimeHintsRegistrar`，经各自动配置 `@ImportRuntimeHints` 自动收集）：

- **web**：统一响应体 `Result`/`PageResult`/`PageParam`/`ErrorCode` 的 Jackson 反射绑定 + i18n 资源束（`messages*.properties`、`i18n/*.properties`）
- **security**：`AuthUserDetails`、`LoginResult` 的反射绑定
- **mybatis**：框架内置 TypeHandler 的反射实例化（`INVOKE_PUBLIC_CONSTRUCTORS`）

应用侧自定义类型（自己的响应 VO、TypeHandler）需自行登记线索（`@RegisterReflectionForBinding`
或 `RuntimeHintsRegistrar` + `@ImportRuntimeHints`）；mybatis 的业务实体反射与 springdoc、
JWT 等三方库的 native 支持以各自上游能力为准，完整 native 构建请以实测为准。

## 许可证

[MIT License](https://github.com/xiaomisum/springboot-migoo-framework/blob/master/LICENSE)
