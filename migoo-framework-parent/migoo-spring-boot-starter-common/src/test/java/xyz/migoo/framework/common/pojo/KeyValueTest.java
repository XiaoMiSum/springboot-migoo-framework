package xyz.migoo.framework.common.pojo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link KeyValue} 的单元测试
 *
 * @author xiaomi
 */
class KeyValueTest {

    @Test
    void of_shouldCreateKeyValue() {
        // of(k, v) 工厂方法
        KeyValue<String, Integer> keyValue = KeyValue.of("key", 1);
        assertThat(keyValue.getKey()).isEqualTo("key");
        assertThat(keyValue.getValue()).isEqualTo(1);
    }

    @Test
    void noArgsConstructor_shouldLeaveFieldsNull() {
        // 空构造，key 与 value 均为 null
        KeyValue<String, String> keyValue = new KeyValue<>();
        assertThat(keyValue.getKey()).isNull();
        assertThat(keyValue.getValue()).isNull();
    }

    @Test
    void allArgsConstructor_shouldSetFields() {
        // 全参构造
        KeyValue<String, Integer> keyValue = new KeyValue<>("code", 500);
        assertThat(keyValue.getKey()).isEqualTo("code");
        assertThat(keyValue.getValue()).isEqualTo(500);
    }

    @Test
    void settersAndGetters_shouldRoundTripValues() {
        // 设置后通过 getter 取回
        KeyValue<String, String> keyValue = new KeyValue<>();
        keyValue.setKey("k");
        keyValue.setValue("v");
        assertThat(keyValue.getKey()).isEqualTo("k");
        assertThat(keyValue.getValue()).isEqualTo("v");
    }
}
