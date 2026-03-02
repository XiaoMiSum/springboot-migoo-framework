package xyz.migoo.framework.common.util.json;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.SerializationFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON 工具类
 *
 * @author xiaomi
 */
public class JsonUtils {

    private static ObjectMapper objectMapper = JsonMapper.builder()
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .build();

    // 静态初始化块已不再需要，因为已在构建器中配置
    static {
        // Jackson 3.x 中 JavaTimeModule 已经内置，无需手动注册
    }

    /**
     * 初始化 objectMapper 属性
     * <p>
     * 通过这样的方式，使用 Spring 创建的 ObjectMapper Bean
     *
     * @param objectMapper ObjectMapper 对象
     */
    public static void init(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper;
    }

    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, clazz);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated(since = "1.2.0", forRemoval = true)
    public static JsonNode readTree(String text) {
        try {
            return objectMapper.readTree(text);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode toJSON(String text) {
        try {
            return objectMapper.readTree(text);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, String path, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
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
