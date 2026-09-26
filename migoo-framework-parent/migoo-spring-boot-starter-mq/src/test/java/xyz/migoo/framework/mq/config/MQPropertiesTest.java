package xyz.migoo.framework.mq.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MQProperties} 单元测试
 */
class MQPropertiesTest {

    @Test
    void defaultValues() {
        MQProperties properties = new MQProperties();
        assertThat(properties.getGroup()).isEqualTo("def_group");
        assertThat(properties.getMaxRetry()).isEqualTo(3);
        assertThat(properties.getDeadLetterEnabled()).isTrue();
        assertThat(properties.getDeleteAfterAck()).isFalse();
        // 幂等子配置默认为启用，过期时间 24 小时
        assertThat(properties.getIdempotent()).isNotNull();
        assertThat(properties.getIdempotent().getEnabled()).isTrue();
        assertThat(properties.getIdempotent().getExpireTime()).isEqualTo(Duration.ofHours(24));
    }

    @Test
    void settersAndGettersRoundtrip() {
        MQProperties properties = new MQProperties();
        properties.setGroup("group-a");
        properties.setMaxRetry(7);
        properties.setDeadLetterEnabled(false);
        properties.setDeleteAfterAck(true);
        assertThat(properties.getGroup()).isEqualTo("group-a");
        assertThat(properties.getMaxRetry()).isEqualTo(7);
        assertThat(properties.getDeadLetterEnabled()).isFalse();
        assertThat(properties.getDeleteAfterAck()).isTrue();
    }

    @Test
    void idempotentSubConfigIsSharedInstance() {
        MQProperties properties = new MQProperties();
        MQProperties.Idempotent idempotent = properties.getIdempotent();
        idempotent.setEnabled(false);
        idempotent.setExpireTime(Duration.ofMinutes(30));
        // 修改子配置对象后，属性同步生效
        assertThat(properties.getIdempotent().getEnabled()).isFalse();
        assertThat(properties.getIdempotent().getExpireTime()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void idempotentSubConfigCanBeReplaced() {
        MQProperties properties = new MQProperties();
        MQProperties.Idempotent custom = new MQProperties.Idempotent();
        custom.setEnabled(false);
        properties.setIdempotent(custom);
        assertThat(properties.getIdempotent()).isSameAs(custom);
        assertThat(properties.getIdempotent().getEnabled()).isFalse();
    }

    @Test
    void idempotentConfigCanBeReusedIndependently() {
        MQProperties.Idempotent idempotent = new MQProperties.Idempotent();
        assertThat(idempotent.getEnabled()).isTrue();
        assertThat(idempotent.getExpireTime()).isEqualTo(Duration.ofHours(24));
        idempotent.setExpireTime(Duration.ofSeconds(60));
        assertThat(idempotent.getExpireTime()).isEqualTo(Duration.ofSeconds(60));
    }

    @Test
    void healthDefaults() {
        MQProperties properties = new MQProperties();
        assertThat(properties.getHealth()).isNotNull();
        // 健康检查默认开启，积压阈值 1000 条
        assertThat(properties.getHealth().getEnabled()).isTrue();
        assertThat(properties.getHealth().getBacklogThreshold()).isEqualTo(1000L);
    }

    @Test
    void healthSubConfigRoundtrip() {
        MQProperties properties = new MQProperties();
        properties.getHealth().setEnabled(false);
        properties.getHealth().setBacklogThreshold(50L);
        assertThat(properties.getHealth().getEnabled()).isFalse();
        assertThat(properties.getHealth().getBacklogThreshold()).isEqualTo(50L);
    }

    @Test
    void reclaimDefaults() {
        MQProperties properties = new MQProperties();
        assertThat(properties.getReclaim()).isNotNull();
        // 认领与退避重投默认开启：退避 5s、轮询 5s
        assertThat(properties.getReclaim().getEnabled()).isTrue();
        assertThat(properties.getReclaim().getBackoff()).isEqualTo(Duration.ofSeconds(5));
        assertThat(properties.getReclaim().getInterval()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void reclaimSubConfigRoundtrip() {
        MQProperties properties = new MQProperties();
        properties.getReclaim().setEnabled(false);
        properties.getReclaim().setBackoff(Duration.ofSeconds(30));
        properties.getReclaim().setInterval(Duration.ofSeconds(10));
        assertThat(properties.getReclaim().getEnabled()).isFalse();
        assertThat(properties.getReclaim().getBackoff()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.getReclaim().getInterval()).isEqualTo(Duration.ofSeconds(10));
    }
}
