package xyz.migoo.framework.common.sensitive;

import org.junit.jupiter.api.Test;
import xyz.migoo.framework.common.util.JsonUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Sensitive} + {@link SensitiveSerializer} 端到端序列化测试
 *
 * <p>经 {@link JsonUtils}（纯 Jackson {@code JsonMapper}）验证元注解挂载、
 * 上下文化读取策略、数值字段转掩码字符串、null 与未标注字段行为。</p>
 */
class SensitiveSerializerTest {

    static class UserVO {

        @Sensitive(type = SensitiveType.MOBILE)
        public String mobile = "13812345678";

        @Sensitive(type = SensitiveType.EMAIL)
        public String email = "alice@example.com";

        @Sensitive(type = SensitiveType.PASSWORD)
        public String password = "secret";

        @Sensitive(type = SensitiveType.BANK_CARD)
        public Long bankCard = 6222021234567890L;

        public String nickname = "小米";
    }

    static class NullableVO {

        @Sensitive(type = SensitiveType.MOBILE)
        public String mobile;
    }

    @Test
    void masksAnnotatedFieldsAndKeepsPlainOnes() {
        String json = JsonUtils.toJsonString(new UserVO());

        assertThat(json).contains("\"138****5678\"");
        assertThat(json).contains("\"a***@example.com\"");
        assertThat(json).contains("\"******\"");
        // 数值字段脱敏后以字符串形态输出
        assertThat(json).contains("\"6222********7890\"");
        // 未标注字段原样输出；原值不再出现
        assertThat(json).contains("\"小米\"");
        assertThat(json).doesNotContain("13812345678")
                .doesNotContain("alice@example.com")
                .doesNotContain("secret");
    }

    @Test
    void nullAnnotatedFieldSerializesAsJsonNull() {
        String json = JsonUtils.toJsonString(new NullableVO());

        assertThat(json).contains("\"mobile\":null");
    }
}
