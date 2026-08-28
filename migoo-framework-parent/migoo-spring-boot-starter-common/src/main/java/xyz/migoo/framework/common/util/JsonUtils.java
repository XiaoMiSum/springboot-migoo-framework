package xyz.migoo.framework.common.util;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON 工具类
 *
 * @author xiaomi
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();


    public static String toJsonString(Object object) throws JacksonException {
        if (object == null) {
            return "{}";
        }
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T parseObject(String text, Class<T> clazz) throws JacksonException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return objectMapper.readValue(text, clazz);
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) throws JacksonException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return objectMapper.readValue(bytes, clazz);
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) throws JacksonException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return objectMapper.readValue(text, typeReference);
    }

    public static <T> T parseObject(InputStream in, TypeReference<T> typeReference) {
        if (in == null) {
            return null;
        }
        return objectMapper.readValue(in, typeReference);
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) throws JacksonException {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(text, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static JsonNode toJSON(String text) throws JacksonException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return objectMapper.readTree(text);
    }

    public static <T> T parseObject(String text, String path, Class<T> clazz) throws JacksonException {
        if (text == null || text.isEmpty()) {
            return null;
        }
        JsonNode pathNode = objectMapper.readTree(text).path(path);
        return objectMapper.readValue(pathNode.toString(), clazz);
    }

    public static <T> T convert(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        return objectMapper.convertValue(object, clazz);
    }

    public static <T> T convert(Object object, TypeReference<T> typeReference) {
        if (object == null) {
            return null;
        }
        return objectMapper.convertValue(object, typeReference);
    }

    public static <T> T toObject(JsonNode node, Class<T> clazz) throws JacksonException {
        if (node == null) {
            return null;
        }
        return objectMapper.treeToValue(node, clazz);
    }

    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    public static JsonNode valueToTree(Object value) {
        return objectMapper.valueToTree(value);
    }

}
