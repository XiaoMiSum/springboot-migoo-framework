package xyz.migoo.framework.common.util;

import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JsonUtils 单元测试（Jackson 3 / tools.jackson）
 */
class JsonUtilsTest {

    /** 测试用 record */
    public record TestDTO(String name, Integer age) {
    }

    @Test
    void toJsonString_nullReturnsEmptyObject() {
        assertThat(JsonUtils.toJsonString(null)).isEqualTo("{}");
    }

    @Test
    void toJsonString_recordRoundtrip() {
        TestDTO dto = new TestDTO("migoo", 18);
        String json = JsonUtils.toJsonString(dto);
        assertThat(json).contains("migoo").contains("18");
        assertThat(JsonUtils.parseObject(json, TestDTO.class)).isEqualTo(dto);
    }

    @Test
    void parseObject_string() {
        assertThat(JsonUtils.parseObject((String) null, TestDTO.class)).isNull();
        assertThat(JsonUtils.parseObject("", TestDTO.class)).isNull();
        assertThat(JsonUtils.parseObject("{\"name\":\"migoo\",\"age\":18}", TestDTO.class))
                .isEqualTo(new TestDTO("migoo", 18));
    }

    @Test
    void parseObject_invalidJsonThrowsRuntimeException() {
        assertThatThrownBy(() -> JsonUtils.parseObject("{", TestDTO.class))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void parseObject_byteArray() {
        assertThat(JsonUtils.parseObject((byte[]) null, TestDTO.class)).isNull();
        assertThat(JsonUtils.parseObject(new byte[0], TestDTO.class)).isNull();
        byte[] valid = "{\"name\":\"migoo\",\"age\":18}".getBytes(StandardCharsets.UTF_8);
        assertThat(JsonUtils.parseObject(valid, TestDTO.class)).isEqualTo(new TestDTO("migoo", 18));
        assertThatThrownBy(() -> JsonUtils.parseObject("{".getBytes(StandardCharsets.UTF_8), TestDTO.class))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void parseObject_typeReference() {
        assertThat(JsonUtils.parseObject(null, new TypeReference<List<String>>() {})).isNull();
        List<String> list = JsonUtils.parseObject("[\"a\",\"b\"]", new TypeReference<List<String>>() {});
        assertThat(list).containsExactly("a", "b");
    }

    @Test
    void parseArray() {
        assertThat(JsonUtils.parseArray(null, Integer.class)).isEmpty();
        assertThat(JsonUtils.parseArray("", Integer.class)).isEmpty();
        assertThat(JsonUtils.parseArray("[1,2,3]", Integer.class)).containsExactly(1, 2, 3);
        assertThat(JsonUtils.parseArray("[\"a\",\"b\"]", String.class)).containsExactly("a", "b");
    }

    @Test
    void toJSON() {
        assertThat(JsonUtils.toJSON(null)).isNull();
        assertThat(JsonUtils.toJSON("")).isNull();
        JsonNode node = JsonUtils.toJSON("{\"a\":1,\"b\":\"x\"}");
        assertThat(node).isNotNull();
        assertThat(node.get("a").asInt()).isEqualTo(1);
        assertThat(node.get("b").asText()).isEqualTo("x");
    }

    @Test
    void parseObject_withPath() {
        // 嵌套路径提取对象
        TestDTO dto = JsonUtils.parseObject("{\"user\":{\"name\":\"migoo\",\"age\":20}}", "user", TestDTO.class);
        assertThat(dto).isEqualTo(new TestDTO("migoo", 20));
        // 路径指向标量
        assertThat(JsonUtils.parseObject("{\"code\":\"CN\"}", "code", String.class)).isEqualTo("CN");
    }

    @Test
    void convert_class() {
        assertThat(JsonUtils.convert(null, TestDTO.class)).isNull();
        TestDTO dto = new TestDTO("migoo", 18);
        assertThat(JsonUtils.convert(dto, TestDTO.class)).isEqualTo(dto);
        Map<String, Object> map = JsonUtils.convert(dto, Map.class);
        assertThat(map).containsEntry("name", "migoo").containsEntry("age", 18);
    }

    @Test
    void convert_typeReference() {
        assertThat(JsonUtils.convert(null, new TypeReference<List<TestDTO>>() {})).isNull();
        List<TestDTO> source = List.of(new TestDTO("migoo", 18));
        List<TestDTO> result = JsonUtils.convert(source, new TypeReference<List<TestDTO>>() {});
        assertThat(result).containsExactly(new TestDTO("migoo", 18));
    }
}
