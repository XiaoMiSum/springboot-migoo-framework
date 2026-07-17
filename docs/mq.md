# migoo-spring-boot-starter-mq

MQ 组件，基于 Redis 实现消息队列，支持 Stream（可靠投递）和 Pub/Sub（广播）两种模式。

## 快速开始

| 步骤 | Stream（可靠） | Pub/Sub（广播） |
|------|----------------|-----------------|
| 1. 引入依赖 | 添加 `migoo-spring-boot-starter-mq` | 同左 |
| 2. 定义消息 | 继承 `AbstractStreamMessage` | 继承 `AbstractChannelMessage` |
| 3. 发送消息 | `redisMQTemplate.send(message)` | 同左 |
| 4. 消费消息 | 继承 `AbstractStreamMessageListener` | 继承 `AbstractChannelMessageListener` |

```java
// 定义消息
@Data
public class OrderMessage extends AbstractStreamMessage {
    private Long orderId;
}

// 发送
@Resource
private RedisMQTemplate redisMQTemplate;
redisMQTemplate.send(orderMessage);

// 消费
@Component
public class OrderListener extends AbstractStreamMessageListener<OrderMessage> {
    @Override
    protected void onMessage(OrderMessage message) {
        log.info("收到订单: {}", message.getOrderId());
    }
}
```

## 依赖

```xml
<dependency>
    <groupId>xyz.migoo.springboot</groupId>
    <artifactId>migoo-spring-boot-starter-mq</artifactId>
</dependency>
```

## 应用层对接

### Stream 模式（推荐）

支持 ACK、重试、死信队列、幂等消费，适用于可靠业务消息。

#### 1. 定义消息

```java
@Data
public class OrderCreatedMessage extends AbstractStreamMessage {

    private Long orderId;
    private Integer status;

    // 覆写 getChannel() 自定义 Stream key（默认为类名）
    @Override
    public String getChannel() {
        return "order:created";
    }
}
```

#### 2. 发送消息

```java
@Resource
private RedisMQTemplate redisMQTemplate;

OrderCreatedMessage message = new OrderCreatedMessage();
message.setOrderId(123456L);
message.setStatus(1);
RecordId recordId = redisMQTemplate.send(message);
```

#### 3. 消费消息

```java
@Component
public class OrderCreatedListener extends AbstractStreamMessageListener<OrderCreatedMessage> {

    @Override
    protected void onMessage(OrderCreatedMessage message) {
        log.info("收到订单创建消息: {}", message.getOrderId());
        // 处理业务逻辑
    }
}
```

#### 4. 重试与死信

框架自动处理重试：

- 消费失败自动重试，最多 `migoo.mq.max-retry` 次（默认 3）
- 超过重试次数自动进入死信队列 `<channel>:dead_letter`
- `MessageAlreadyConsumedException`（幂等跳过）不计入重试

### Pub/Sub 模式

广播模式，所有消费者均收到消息，无 ACK、无重试。

#### 1. 定义消息

```java
@Data
public class NotificationMessage extends AbstractChannelMessage {

    private String title;
    private String content;

    @Override
    public String getChannel() {
        return "notification";
    }
}
```

#### 2. 发送 & 消费

```java
// 发送
NotificationMessage message = new NotificationMessage();
message.setTitle("系统通知");
message.setContent("您的订单已发货");
redisMQTemplate.send(message);

// 消费
@Component
public class NotificationListener extends AbstractChannelMessageListener<NotificationMessage> {

    @Override
    protected void onMessage(NotificationMessage message) {
        log.info("收到通知: {}", message.getTitle());
    }
}
```

### 幂等消费

框架默认开启幂等消费拦截（`migoo.mq.idempotent.enabled=true`），基于 `messageId` + Redis SETNX 去重，无需额外配置。

### 自定义拦截器

实现 `RedisMessageInterceptor` 接口，可拦截消息的发送和消费生命周期：

```java
@Component
public class MessageAuditInterceptor implements RedisMessageInterceptor {

    @Override
    public void consumeMessageBefore(String channel, Object message) {
        log.info("消费前审计: channel={}, messageId={}", channel,
                ((AbstractMessage) message).getMessageId());
    }

    @Override
    public void sendMessageAfter(String channel, Object message) {
        // 发送后回调
    }
}
```

| 拦截点 | 说明 |
|--------|------|
| `sendMessageBefore` | 发送前 |
| `sendMessageAfter` | 发送后 |
| `sendMessageError` | 发送异常 |
| `consumeMessageBefore` | 消费前 |
| `consumeMessageAfter` | 消费后 |
| `consumeMessageError` | 消费异常 |

---

## 配置项

```yaml
migoo:
  mq:
    group: def_group                    # 消费组名称（Stream）
    max-retry: 3                        # 最大重试次数（Stream）
    dead-letter-enabled: true           # 死信队列开关（Stream）
    idempotent:
      enabled: true                     # 幂等拦截开关（默认开启）
      expire-time: 24h                  # 幂等 Key 过期时间
```

## 自动注册的组件

| 组件 | 条件 |
|------|------|
| `RedisMQTemplate` | 始终注册 |
| `IdempotentMessageInterceptor` | `idempotent.enabled=true`（默认） |
| `StreamMessageListenerContainer` | 存在 `AbstractStreamMessageListener` Bean 时 |
| `RedisMessageListenerContainer` | 存在 `AbstractChannelMessageListener` Bean 时 |
