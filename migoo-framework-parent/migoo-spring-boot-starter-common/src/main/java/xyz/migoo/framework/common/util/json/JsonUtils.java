package xyz.migoo.framework.common.util.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

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


    public static String toJsonString(Object object) {
        try {
            if (object == null) {
                return "{}";
            }
            return objectMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(text, clazz);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode toJSON(String text) {
        try {
            if (text == null || text.isEmpty()) {
                return null;
            }
            return objectMapper.readTree(text);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, String path, Class<T> clazz) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            JsonNode pathNode = objectMapper.readTree(text).path(path);
            return objectMapper.readValue(pathNode.toString(), clazz);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
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

}
