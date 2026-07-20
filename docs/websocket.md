---
layout: default
---

# migoo-spring-boot-starter-websocket

WebSocket 组件，提供 WebSocket 连接管理、Token 认证、会话管理等功能。支持单机和分布式两种模式。

## 快速开始

| 步骤 | 说明 |
|------|------|
| 1. 引入依赖 | 添加 `migoo-spring-boot-starter-websocket` 和 `migoo-spring-boot-starter-security` |
| 2. 配置属性 | 配置 WebSocket 端点路径和 token 参数 |
| 3. 实现 Handler | 继承 `MiGooWebSocketHandler` 处理业务逻辑 |
| 4. 客户端连接 | 使用 WebSocket 客户端连接并传递 token |

```java
// 1. 实现业务 Handler
@Component
public class ChatWebSocketHandler extends MiGooWebSocketHandler {

    public ChatWebSocketHandler(WebSocketSessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String userId = getUserId(session);
        AuthUserDetails<?, ?> userDetails = getUserDetails(session);
        
        // 处理消息并回复
        sendMessage(session, "收到消息: " + message.getPayload());
    }
}

// 2. 客户端连接（JavaScript）
const socket = new WebSocket('ws://localhost:8080/ws?token=your-jwt-token');
```

## 依赖

```xml
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-websocket</artifactId>
</dependency>

<!-- security 模块（可选，用于 token 验证） -->
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-security</artifactId>
</dependency>

<!-- redis 模块（可选，用于分布式 WebSocket） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 配置项

### 单机模式（默认）

```yaml
migoo:
  websocket:
    enabled: true                      # 是否启用，默认 true
    distributed: false                 # 是否启用分布式模式，默认 false
    endpoint: /ws                      # WebSocket 端点路径，默认 /ws
    allowed-origins: "*"               # 允许的来源，默认 *（多个用逗号分隔）
    token-header: Authorization        # Token Header 名称，默认 Authorization
    token-prefix: "Bearer "            # Token 前缀，默认 "Bearer "
    max-session-timeout: 1800000       # 最大会话超时时间（毫秒），默认 30 分钟
```

### 分布式模式

```yaml
migoo:
  websocket:
    enabled: true
    distributed: true                  # 启用分布式模式
    endpoint: /ws
    allowed-origins: "*"
    token-header: Authorization
    token-prefix: "Bearer "

spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your-password
```

## 架构说明

### 类结构

```
WebSocketSessionManager (接口)
└── AbstractWebSocketSessionManager (抽象基类)
    ├── LocalWebSocketSessionManager    → 单机模式
    └── DistributedWebSocketSessionManager → 分布式模式（Redis Pub/Sub）
```

### 自动配置

```
MiGooWebSocketAutoConfiguration
├── WebSocketSessionManager → 条件化加载
│   ├── LocalWebSocketSessionManager      (distributed=false)
│   └── DistributedWebSocketSessionManager (distributed=true + Redis)
├── MiGooWebSocketHandler          → 消息处理
├── WebSocketConfigurer            → 端点配置
└── WebSocketSecurityConfiguration → Token 认证（可选）
```

### 分布式模式工作原理

1. **本地会话**：每个节点维护自己的 WebSocket 连接
2. **跨节点通信**：通过 Redis Pub/Sub 广播消息
3. **消息推送流程**：
   - 发送消息给用户 A
   - 先检查本地是否有用户 A 的会话
   - 如果有，直接发送
   - 如果没有，通过 Redis Pub/Sub 广播，其他节点收到后检查本地会话并发送

## 核心组件

### WebSocketSessionManager

会话管理器接口，提供统一的 API：

```java
@Resource
private WebSocketSessionManager sessionManager;

// 发送消息给指定用户
sessionManager.sendToUser(userId, "Hello!");

// 广播消息给所有在线用户
sessionManager.broadcast("系统通知");

// 获取在线用户数
int count = sessionManager.getOnlineUserCount();

// 获取用户的所有会话
Collection<WebSocketSession> sessions = sessionManager.getUserSessions(userId);
```

### WebSocketAuthInterceptor

Token 认证拦截器，在 WebSocket 握手前验证 token：

- 从 `Authorization` Header 或 `token` 查询参数获取 token
- 复用 `AuthUserDetailsFetcher.verifyToken()` 验证 token
- 将 `AuthUserDetails`、`USER_ID`、`USER_NAME` 存入 WebSocket session

```java
// 从 session 获取用户信息
AuthUserDetails<?, ?> userDetails = (AuthUserDetails<?, ?>) 
    session.getAttributes().get("USER_DETAILS");
String userId = (String) session.getAttributes().get("USER_ID");
```

### MiGooWebSocketHandler

消息处理器基类，提供便捷方法：

```java
public class MyHandler extends MiGooWebSocketHandler {

    public MyHandler(WebSocketSessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 获取当前用户信息
        AuthUserDetails<?, ?> user = getUserDetails(session);
        String userId = getUserId(session);
        
        // 发送消息给当前用户
        sendMessage(session, "Echo: " + message.getPayload());
        
        // 发送消息给其他用户
        sendMessageToUser(otherUserId, "新消息");
        
        // 广播
        broadcast("系统通知");
    }
}
```

## 应用层对接

### 1. 实现业务 Handler

继承 `MiGooWebSocketHandler` 并重写 `handleTextMessage` 方法：

```java
@Component
@Slf4j
public class ChatHandler extends MiGooWebSocketHandler {

    public ChatHandler(WebSocketSessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String userId = getUserId(session);
        String payload = message.getPayload();
        
        log.info("用户 {} 发送消息: {}", userId, payload);
        
        // 处理业务逻辑...
        sendMessage(session, "处理完成");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        super.afterConnectionEstablished(session);
        
        String userId = getUserId(session);
        broadcast("用户 " + userId + " 已上线");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        super.afterConnectionClosed(session, status);
        
        String userId = getUserId(session);
        broadcast("用户 " + userId + " 已离线");
    }
}
```

### 2. 客户端实现

```javascript
class WebSocketClient {
    constructor(url, token) {
        this.url = url;
        this.token = token;
        this.socket = null;
    }

    connect() {
        this.socket = new WebSocket(`${this.url}?token=${this.token}`);
        
        this.socket.onopen = () => {
            console.log('连接成功');
        };
        
        this.socket.onmessage = (event) => {
            console.log('收到消息:', event.data);
        };
        
        this.socket.onclose = (event) => {
            console.log('连接关闭:', event.code, event.reason);
        };
        
        this.socket.onerror = (error) => {
            console.error('连接错误:', error);
        };
    }

    send(message) {
        if (this.socket && this.socket.readyState === WebSocket.OPEN) {
            this.socket.send(message);
        }
    }

    disconnect() {
        if (this.socket) {
            this.socket.close();
        }
    }
}

// 使用
const client = new WebSocketClient('ws://localhost:8080/ws', 'your-jwt-token');
client.connect();
```

## 自动注册的组件

| 组件 | 说明 | 条件 |
|------|------|------|
| 组件 | 说明 | 条件 |
|------|------|------|
| `WebSocketSessionManager` | 会话管理器接口 | - |
| `AbstractWebSocketSessionManager` | 抽象基类，封装公共逻辑 | - |
| `LocalWebSocketSessionManager` | 单机会话管理器 | `distributed=false`（默认） |
| `DistributedWebSocketSessionManager` | 分布式会话管理器 | `distributed=true` + Redis 在 classpath |
| `MiGooWebSocketHandler` | 消息处理器 | `migoo.websocket.enabled=true` |
| `WebSocketAuthInterceptor` | Token 认证拦截器 | security 模块存在 |
| `WebSocketConfigurer` | WebSocket 配置 | `migoo.websocket.enabled=true` |
