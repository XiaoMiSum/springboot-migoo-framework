package xyz.migoo.framework.jackson.databind;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link LocalDateTimeSerializer} 单元测试
 *
 * <p>序列化格式：{@code yyyy-MM-dd HH:mm:ss}。</p>
 */
class LocalDateTimeSerializerTest {

    /** 测试载体 POJO：仅包含一个 LocalDateTime 字段 */
    static class DateTimeDto {
        public LocalDateTime time;
    }

    private final JsonMapper objectMapper = JsonMapper.builder()
            .addModule(new SimpleModule().addSerializer(LocalDateTime.class, new LocalDateTimeSerializer()))
            .build();

    @Test
    void serializeFormatsDateTimeWithPattern() throws Exception {
        DateTimeDto dto = new DateTimeDto();
        dto.time = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        assertThat(objectMapper.writeValueAsString(dto)).isEqualTo("{\"time\":\"2024-01-02 03:04:05\"}");
    }

    @Test
    void serializeMidnightPadsZero() throws Exception {
        DateTimeDto dto = new DateTimeDto();
        dto.time = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        assertThat(objectMapper.writeValueAsString(dto)).isEqualTo("{\"time\":\"2024-01-01 00:00:00\"}");
    }

    @Test
    void serializeEndOfYearSecondBoundary() throws Exception {
        DateTimeDto dto = new DateTimeDto();
        dto.time = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        assertThat(objectMapper.writeValueAsString(dto)).isEqualTo("{\"time\":\"2024-12-31 23:59:59\"}");
    }

    @Test
    void serializeNullSkipsOutput() throws Exception {
        LocalDateTimeSerializer serializer = new LocalDateTimeSerializer();
        JsonGenerator generator = mock(JsonGenerator.class);
        // null 值不写入任何内容
        serializer.serialize(null, generator, mock(SerializationContext.class));
        verify(generator, never()).writeString(org.mockito.ArgumentMatchers.anyString());
    }
}
