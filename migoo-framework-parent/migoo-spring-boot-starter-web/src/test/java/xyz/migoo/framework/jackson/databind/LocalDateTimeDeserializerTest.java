package xyz.migoo.framework.jackson.databind;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link LocalDateTimeDeserializer} 单元测试
 *
 * <p>反序列化格式：{@code yyyy-MM-dd HH:mm:ss}，空字符串返回 {@code null}。</p>
 */
class LocalDateTimeDeserializerTest {

    private final JsonMapper objectMapper = JsonMapper.builder()
            .addModule(new SimpleModule()
                    .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer())
                    .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer()))
            .build();

    @Test
    void deserializeParsesStandardPattern() throws Exception {
        LocalDateTime time = objectMapper.readValue("\"2024-01-02 03:04:05\"", LocalDateTime.class);
        assertThat(time).isEqualTo(LocalDateTime.of(2024, 1, 2, 3, 4, 5));
    }

    @Test
    void deserializeParsesMidnight() throws Exception {
        LocalDateTime time = objectMapper.readValue("\"2024-01-01 00:00:00\"", LocalDateTime.class);
        assertThat(time).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
    }

    @Test
    void deserializeEmptyStringReturnsNull() throws Exception {
        LocalDateTime time = objectMapper.readValue("\"\"", LocalDateTime.class);
        assertThat(time).isNull();
    }

    @Test
    void deserializeBlankStringReturnsNull() throws Exception {
        LocalDateTime time = objectMapper.readValue("\"   \"", LocalDateTime.class);
        assertThat(time).isNull();
    }

    @Test
    void deserializeInvalidFormatThrows() {
        // 格式不符合 yyyy-MM-dd HH:mm:ss 时抛出 DateTimeParseException
        assertThatThrownBy(() -> objectMapper.readValue("\"2024/01/02 03:04:05\"", LocalDateTime.class))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void serializeThenDeserializeRoundTrip() throws Exception {
        String json = objectMapper.writeValueAsString(LocalDateTime.of(2024, 6, 15, 12, 30, 45));
        assertThat(json).isEqualTo("\"2024-06-15 12:30:45\"");
        LocalDateTime time = objectMapper.readValue(json, LocalDateTime.class);
        assertThat(time).isEqualTo(LocalDateTime.of(2024, 6, 15, 12, 30, 45));
    }
}
