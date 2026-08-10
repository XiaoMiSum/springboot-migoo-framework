package xyz.migoo.framework.mq.core.message;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AbstractMessage} 单元测试
 */
class AbstractMessageTest {

    /** 测试用消息：带 content 业务字段 */
    static class DemoMessage extends AbstractMessage {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /** 测试用消息：另一个子类，验证 channel 随类名变化 */
    static class AnotherMessage extends AbstractMessage {
    }

    @Test
    void constructorAutoGeneratesMessageId() {
        AbstractMessage message = new DemoMessage();
        // 构造时自动生成 UUID 作为消息唯一标识
        assertThat(message.getMessageId()).isNotBlank();
        assertThat(message.getMessageId()).matches("[0-9a-fA-F-]{36}");
        assertThat(UUID.fromString(message.getMessageId())).isNotNull();
    }

    @Test
    void eachMessageHasUniqueMessageId() {
        AbstractMessage first = new DemoMessage();
        AbstractMessage second = new DemoMessage();
        // 每条消息的 messageId 互不相同
        assertThat(first.getMessageId()).isNotEqualTo(second.getMessageId());
    }

    @Test
    void constructorInitializesEmptyHeaders() {
        AbstractMessage message = new DemoMessage();
        // headers 默认初始化为空 Map，而不是 null
        assertThat(message.getHeaders()).isNotNull().isEmpty();
    }

    @Test
    void addHeaderAndGetHeaderRoundtrip() {
        AbstractMessage message = new DemoMessage();
        message.addHeader("key1", "value1");
        message.addHeader("key2", "value2");
        assertThat(message.getHeader("key1")).isEqualTo("value1");
        assertThat(message.getHeader("key2")).isEqualTo("value2");
        assertThat(message.getHeaders()).containsEntry("key1", "value1").containsEntry("key2", "value2");
    }

    @Test
    void getHeaderReturnsNullForMissingKey() {
        AbstractMessage message = new DemoMessage();
        assertThat(message.getHeader("missing")).isNull();
    }

    @Test
    void addHeaderOverwritesExistingValue() {
        AbstractMessage message = new DemoMessage();
        message.addHeader("key", "v1");
        message.addHeader("key", "v2");
        // 相同 key 覆盖旧值
        assertThat(message.getHeader("key")).isEqualTo("v2");
        assertThat(message.getHeaders()).hasSize(1);
    }

    @Test
    void getChannelReturnsClassSimpleName() {
        // channel 生成规则：直接使用类名
        assertThat(new DemoMessage().getChannel()).isEqualTo("DemoMessage");
        assertThat(new AnotherMessage().getChannel()).isEqualTo("AnotherMessage");
    }

    @Test
    void setMessageIdAllowsManualOverride() {
        AbstractMessage message = new DemoMessage();
        message.setMessageId("manual-id");
        assertThat(message.getMessageId()).isEqualTo("manual-id");
    }
}
