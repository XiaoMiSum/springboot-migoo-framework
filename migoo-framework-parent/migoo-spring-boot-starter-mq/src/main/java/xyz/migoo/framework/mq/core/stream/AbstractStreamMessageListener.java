package xyz.migoo.framework.mq.core.stream;

import xyz.migoo.framework.common.util.type.TypeUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.stream.StreamListener;
import xyz.migoo.framework.common.util.JsonUtils;
import xyz.migoo.framework.common.observability.MqMessageConsumeFailedEvent;
import xyz.migoo.framework.common.observability.MqMessageDeadLetteredEvent;
import xyz.migoo.framework.mq.config.MQProperties;
import xyz.migoo.framework.mq.core.RedisMQTemplate;
import xyz.migoo.framework.mq.core.interceptor.IdempotentMessageInterceptor;
import xyz.migoo.framework.mq.core.interceptor.RedisMessageInterceptorUtils;
import xyz.migoo.framework.mq.core.message.AbstractMessage;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Redis Stream 消息监听器抽象基类
 * <p>
 * 支持消费者组、ACK确认、异常处理和死信队列
 *
 * @param <T> 消息类型
 */
@Slf4j
public abstract class AbstractStreamMessageListener<T extends AbstractStreamMessage> implements StreamListener<String, ObjectRecord<String, String>> {

    /**
     * 死信队列 Key 后缀
     */
    private static final String DEAD_LETTER_SUFFIX = ":dead_letter";

    /**
     * 单轮认领的 PEL 消息条数上限
     */
    private static final long RECLAIM_BATCH_SIZE = 100L;

    /**
     * 认领者消费者名兜底值（容器尚未注册 consumerName 时使用）
     */
    private static final String DEFAULT_RECLAIM_CONSUMER = "migoo-reclaimer";

    /**
     * 消息类型
     */
    private final Class<T> messageType;

    /**
     * Redis Stream Key
     */
    @Getter
    private final String streamKey;

    /**
     * 消费者分组名称
     */
    @Getter
    private final String group;

    /**
     * 最大重试次数
     */
    @Getter
    private final int maxRetry;

    /**
     * 是否启用死信队列
     */
    @Getter
    private final boolean deadLetterEnabled;

    /**
     * 消费成功后是否删除消息
     */
    @Getter
    private final boolean deleteAfterAck;

    /**
     * RedisMQTemplate
     */
    @Setter
    private RedisMQTemplate redisMQTemplate;

    /**
     * RedisTemplate
     */
    @Setter
    private RedisTemplate<String, ?> redisTemplate;

    /**
     * 事件发布器（可观测性信号，可空：未注入时不发布）
     */
    @Setter
    private ApplicationEventPublisher eventPublisher;

    /**
     * 消费者名（由 MQAutoConfiguration 容器注册时注入，
     * 认领任务将 PEL 消息认领到该消费者名下）
     */
    @Getter
    @Setter
    private String consumerName;

    protected AbstractStreamMessageListener(MQProperties properties) {
        this.messageType = getMessageClass();
        // 直接使用类名作为 Stream Key，无需反射创建临时对象
        this.streamKey = messageType.getSimpleName();
        this.group = properties.getGroup();
        this.maxRetry = properties.getMaxRetry();
        this.deadLetterEnabled = properties.getDeadLetterEnabled();
        this.deleteAfterAck = properties.getDeleteAfterAck();
    }

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        // 误配置守卫：RedisTemplate 缺失必须立刻失败（否则成功/失败路径的 Redis 操作都会被静默吞掉）
        Objects.requireNonNull(redisTemplate, "RedisTemplate 未注入，请检查 MQAutoConfiguration 配置");

        // 消费消息
        T messageObj = JsonUtils.parseObject(message.getValue(), messageType);
        Objects.requireNonNull(messageObj, "解析消息失败，消息内容为空");

        // 获取重试次数
        int retryCount = getRetryCount(messageObj);

        try {
            consumeMessageBefore(messageObj);
            this.onMessage(messageObj);

            // ack 消息消费完成
            redisTemplate.opsForStream().acknowledge(group, message);
            log.debug("[onMessage][消费Stream消息成功] stream={}, messageId={}, recordId={}",
                    streamKey, messageObj.getMessageId(), message.getId());
            // 根据配置决定是否删除消息
            if (deleteAfterAck) {
                redisTemplate.opsForStream().delete(message);
            }
        } catch (IdempotentMessageInterceptor.MessageAlreadyConsumedException e) {
            // 消息已被消费，直接 ACK 并跳过
            redisTemplate.opsForStream().acknowledge(group, message);
            if (deleteAfterAck) {
                redisTemplate.opsForStream().delete(message);
            }
            log.info("[onMessage][消息已被其他消费者处理，跳过] stream={}, messageId={}, status={}",
                    streamKey, messageObj.getMessageId(), e.getStatus());
        } catch (Exception e) {
            log.error("[onMessage][消费Stream消息失败] stream={}, messageId={}, recordId={}, retryCount={}/{}",
                    streamKey, messageObj.getMessageId(), message.getId(), retryCount, maxRetry, e);
            consumeMessageError(messageObj, e);

            // 处理消费失败
            handleConsumeError(message, messageObj, e, retryCount);
        } finally {
            consumeMessageAfter(messageObj);
        }
    }

    /**
     * 处理消息
     *
     * @param message 消息
     */
    public abstract void onMessage(T message);

    /**
     * 获取消息的重试次数
     *
     * @param message 消息
     * @return 重试次数
     */
    private int getRetryCount(T message) {
        String retryHeader = message.getHeader("retry-count");
        if (retryHeader == null) {
            return 0;
        }
        try {
            return Integer.parseInt(retryHeader);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 处理消费错误
     *
     * @param message    Redis 消息记录
     * @param messageObj 消息对象
     * @param e          异常
     * @param retryCount 当前重试次数
     */
    private void handleConsumeError(ObjectRecord<String, String> message, T messageObj, Exception e, int retryCount) {
        boolean willRetry = retryCount < maxRetry;
        publishEvent(new MqMessageConsumeFailedEvent(streamKey, willRetry));

        if (willRetry) {
            // 退避重试：不 ACK、不删除、不立即重投，消息留在 PEL；
            // 由 StreamReclaimTask 在闲置超过 migoo.mq.reclaim.backoff 后认领并重投
            //（重投时 retry-count+1），避免失败消息立即重投造成失败风暴
            log.info("[handleConsumeError][消费失败，消息留在 PEL 等待退避重投] stream={}, messageId={}, recordId={}, retryCount={}/{}",
                    streamKey, messageObj.getMessageId(), message.getId(), retryCount, maxRetry);
            return;
        }

        // 重试次数耗尽（终态）：ACK + 死信 + 删除原消息
        redisTemplate.opsForStream().acknowledge(group, message);
        if (deadLetterEnabled) {
            sendToDeadLetterQueue(messageObj, e);
        }
        redisTemplate.opsForStream().delete(message);
    }

    /**
     * 认领本监听器消费组 PEL 中闲置超过退避时长的消息，按重试次数重投或转死信
     * <p>
     * 两类滞留场景统一由此收口：
     * <ul>
     *     <li><b>失败退避</b>：{@link #onMessage} 失败时消息留在 PEL，闲置达到 backoff 后在此重投；</li>
     *     <li><b>孤儿认领</b>：消费者崩溃/重启后未 ACK 的消息，闲置达到 backoff 后重投
     *         （重投次数同样受 max-retry 约束，不会无限循环）。</li>
     * </ul>
     * 重投顺序为「先重投、后 ACK」：中途故障宁可重复投递（幂等拦截器按 messageId 去重），
     * 不可丢失。由 {@code StreamReclaimTask} 定时调用；任何异常只记日志并返回 0，
     * 自愈任务不得影响业务。
     *
     * @param backoff 退避时长（PEL 闲置阈值，须大于业务最长处理时长，否则处理中的消息可能被重复认领）
     * @return 本轮认领处理成功的消息数
     */
    public int reclaimPending(Duration backoff) {
        if (redisTemplate == null) {
            return 0;
        }
        try {
            StreamOperations<String, Object, Object> operations = redisTemplate.opsForStream();
            // 第一步：XPENDING 带 IDLE 过滤，只看闲置超过退避时长的 PEL 条目
            PendingMessages pending = operations.pending(streamKey, group, Range.unbounded(), RECLAIM_BATCH_SIZE, backoff);
            if (pending.isEmpty()) {
                return 0;
            }
            RecordId[] ids = pending.stream().map(PendingMessage::getId).toArray(RecordId[]::new);
            // 第二步：XCLAIM 认领到本消费者名下（带相同 min-idle 二次防护并发认领）
            String claimer = consumerName != null ? consumerName : DEFAULT_RECLAIM_CONSUMER;
            List<MapRecord<String, Object, Object>> claimed = operations.claim(streamKey, group, claimer, backoff, ids);
            int handled = 0;
            for (MapRecord<String, Object, Object> record : claimed) {
                try {
                    handleClaimed(operations, record);
                    handled++;
                } catch (Exception ex) {
                    log.error("[reclaimPending][处理认领消息失败] stream={}, group={}, recordId={}",
                            streamKey, group, record.getId(), ex);
                }
            }
            if (handled > 0) {
                log.info("[reclaimPending][认领并处理 PEL 消息] stream={}, group={}, count={}", streamKey, group, handled);
            }
            return handled;
        } catch (Exception ex) {
            if (isGroupNotCreated(ex)) {
                // 分组尚未创建（容器未启动）：非异常场景，静默等待下一轮
                log.debug("[reclaimPending][消费者组尚未创建，跳过] stream={}, group={}", streamKey, group);
                return 0;
            }
            log.warn("[reclaimPending][查询/认领 PEL 消息失败] stream={}, group={}, error={}",
                    streamKey, group, ex.getMessage());
            return 0;
        }
    }

    /**
     * 处理单条认领消息：按 retry-count 决定重投或死信，终态后 ACK
     *
     * @param operations Stream 操作
     * @param record     已认领到本消费者的 PEL 消息
     */
    private void handleClaimed(StreamOperations<String, Object, Object> operations,
                               MapRecord<String, Object, Object> record) {
        T messageObj = parseMessage(record);
        if (messageObj == null) {
            // 毒消息：无法解析则 ACK 丢弃，避免每轮认领都重复处理；删除与否跟随 delete-after-ack
            log.error("[handleClaimed][消息无法解析，ACK 并丢弃] stream={}, recordId={}", streamKey, record.getId());
            finishClaimed(operations, record);
            return;
        }

        int retryCount = getRetryCount(messageObj);
        if (retryCount >= maxRetry) {
            // 孤儿消息已耗尽重试次数（正常失败路径在耗尽时已转死信，此处兜底崩溃场景）
            if (deadLetterEnabled) {
                sendToDeadLetterQueue(messageObj,
                        new IllegalStateException(String.format("消费失败次数已达上限(%d)，认领时转入死信", maxRetry)));
            }
            finishClaimed(operations, record);
            return;
        }

        messageObj.addHeader("retry-count", String.valueOf(retryCount + 1));
        // 先重投再 ACK：中途故障宁可重复投递（幂等拦截器兜底）也不丢失
        operations.add(StreamRecords.newRecord()
                .ofObject(JsonUtils.toJsonString(messageObj))
                .withStreamKey(streamKey));
        log.debug("[handleClaimed][消息重投] stream={}, messageId={}, retryCount={}",
                streamKey, messageObj.getMessageId(), retryCount + 1);
        finishClaimed(operations, record);
    }

    /**
     * 认领消息的终态处理：ACK（可选删除原消息）
     */
    private void finishClaimed(StreamOperations<String, Object, Object> operations,
                               MapRecord<String, Object, Object> record) {
        operations.acknowledge(group, record);
        if (deleteAfterAck) {
            operations.delete(record);
        }
    }

    /**
     * 从认领记录中提取 JSON 消息体（单字段记录，取第一个值）
     *
     * @param record 认领记录
     * @return JSON 消息体；无有效值返回 {@code null}（按毒消息处理）
     */
    private String extractJson(MapRecord<String, Object, Object> record) {
        Object value = record.getValue().values().stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return text;
        }
        if (value instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * 解析认领记录为消息对象
     *
     * @param record 认领记录
     * @return 消息对象；解析失败返回 {@code null}
     */
    private T parseMessage(MapRecord<String, Object, Object> record) {
        String json = extractJson(record);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return JsonUtils.parseObject(json, messageType);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 判断是否为「消费者组尚未创建」（Redis NOGROUP），逐层查找 cause
     *
     * @param ex 异常
     * @return true-组未创建
     */
    private static boolean isGroupNotCreated(Throwable ex) {
        Throwable current = ex;
        int depth = 0;
        while (current != null && depth++ < 10) {
            String message = current.getMessage();
            if (message != null && (message.contains("NOGROUP") || message.contains("no such key"))) {
                return true;
            }
            current = current.getCause() == current ? null : current.getCause();
        }
        return false;
    }

    /**
     * 发送消息到死信队列
     *
     * @param messageObj 消息对象
     * @param e          异常
     */
    private void sendToDeadLetterQueue(T messageObj, Exception e) {
        String deadLetterKey = streamKey + DEAD_LETTER_SUFFIX;
        try {
            // 添加错误信息到 headers
            messageObj.addHeader("error-message", e.getMessage());
            messageObj.addHeader("error-time", String.valueOf(System.currentTimeMillis()));

            redisTemplate.opsForStream().add(StreamRecords.newRecord().ofObject(JsonUtils.toJsonString(messageObj)).withStreamKey(deadLetterKey));
            publishEvent(new MqMessageDeadLetteredEvent(streamKey, e.getClass().getSimpleName()));
            log.warn("[sendToDeadLetterQueue][消息已发送到死信队列] stream={}, messageId={}, deadLetterKey={}",
                    streamKey, messageObj.getMessageId(), deadLetterKey);
        } catch (Exception ex) {
            log.error("[sendToDeadLetterQueue][发送到死信队列失败] stream={}, messageId={}, deadLetterKey={}",
                    streamKey, messageObj.getMessageId(), deadLetterKey, ex);
        }
    }

    /**
     * 发布可观测性信号事件（观测不得影响业务：发布异常只记日志）
     *
     * @param event common 中的信号事件 record
     */
    private void publishEvent(Object event) {
        if (eventPublisher == null) {
            return;
        }
        try {
            eventPublisher.publishEvent(event);
        } catch (Exception ex) {
            log.warn("[publishEvent][发布可观测性信号事件失败] event({})", event, ex);
        }
    }

    /**
     * 通过解析类上的泛型，获得消息类型
     *
     * @return 消息类型
     */
    @SuppressWarnings("unchecked")
    private Class<T> getMessageClass() {
        Type type = TypeUtils.getTypeArgument(getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
        }
        return (Class<T>) type;
    }

    private void consumeMessageBefore(AbstractMessage message) {
        Objects.requireNonNull(redisMQTemplate, "RedisMQTemplate 未注入，请检查 MQAutoConfiguration 配置");
        RedisMessageInterceptorUtils.consumeMessageBefore(redisMQTemplate.getInterceptors(), message);
    }

    private void consumeMessageAfter(AbstractMessage message) {
        Objects.requireNonNull(redisMQTemplate, "RedisMQTemplate 未注入，请检查 MQAutoConfiguration 配置");
        RedisMessageInterceptorUtils.consumeMessageAfter(redisMQTemplate.getInterceptors(), message);
    }

    private void consumeMessageError(AbstractMessage message, Throwable throwable) {
        Objects.requireNonNull(redisMQTemplate, "RedisMQTemplate 未注入，请检查 MQAutoConfiguration 配置");
        RedisMessageInterceptorUtils.consumeMessageError(redisMQTemplate.getInterceptors(), message, throwable);
    }
}
