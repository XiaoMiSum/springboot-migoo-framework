package xyz.migoo.framework.jackson.databind;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BigDecimalSerializer} 单元测试
 *
 * <p>序列化规则：输出为字符串，固定保留两位小数，多余位数按 {@link java.math.RoundingMode#HALF_DOWN} 舍入。</p>
 */
class BigDecimalSerializerTest {

    /** 测试载体 POJO：仅包含一个 BigDecimal 字段 */
    static class MoneyDto {
        public BigDecimal amount;
    }

    private final JsonMapper objectMapper = JsonMapper.builder()
            .addModule(new SimpleModule().addSerializer(BigDecimal.class, new BigDecimalSerializer()))
            .build();

    @Test
    void serializeKeepsTwoDecimalPlaces() throws Exception {
        MoneyDto dto = new MoneyDto();
        dto.amount = new BigDecimal("12.3");
        // 保留两位小数并输出为 JSON 字符串
        assertThat(objectMapper.writeValueAsString(dto)).isEqualTo("{\"amount\":\"12.30\"}");
    }

    @Test
    void serializeRoundsHalfDown() throws Exception {
        MoneyDto dto = new MoneyDto();
        dto.amount = new BigDecimal("1.005");
        // HALF_DOWN：5 恰好处于中间，向下舍入
        assertThat(objectMapper.writeValueAsString(dto)).isEqualTo("{\"amount\":\"1.00\"}");
    }

    @Test
    void serializeRoundsUpOverHalf() throws Exception {
        MoneyDto dto = new MoneyDto();
        dto.amount = new BigDecimal("1.006");
        assertThat(objectMapper.writeValueAsString(dto)).isEqualTo("{\"amount\":\"1.01\"}");
    }

    @Test
    void serializeIntegerAppendsDecimalZeros() throws Exception {
        MoneyDto dto = new MoneyDto();
        dto.amount = BigDecimal.valueOf(3);
        assertThat(objectMapper.writeValueAsString(dto)).isEqualTo("{\"amount\":\"3.00\"}");
    }

    @Test
    void serializeLargeValueAvoidsScientificNotation() throws Exception {
        MoneyDto dto = new MoneyDto();
        dto.amount = new BigDecimal("12345678901234567890.123");
        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).isEqualTo("{\"amount\":\"12345678901234567890.12\"}");
        assertThat(json).doesNotContain("E");
    }

    @Test
    void serializeNullWritesJsonNull() throws Exception {
        MoneyDto dto = new MoneyDto();
        // Jackson 对 null 值不调用自定义 serializer，直接输出 null
        assertThat(objectMapper.writeValueAsString(dto)).isEqualTo("{\"amount\":null}");
    }
}
