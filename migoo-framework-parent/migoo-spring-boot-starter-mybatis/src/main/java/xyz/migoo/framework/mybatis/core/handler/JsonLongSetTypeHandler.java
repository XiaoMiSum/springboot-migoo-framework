package xyz.migoo.framework.mybatis.core.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import xyz.migoo.framework.common.util.json.JsonUtils;

import java.util.Set;

/**
 * @author xiaomi
 * Created on 2021/11/23 20:28
 */
public class JsonLongSetTypeHandler extends AbstractJsonTypeHandler<Object> {

    private static final TypeReference<Set<Long>> TYPE_REFERENCE = new TypeReference<>() {
    };

    public JsonLongSetTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public Object parse(String json) {
        return JsonUtils.parseObject(json, TYPE_REFERENCE);
    }

    @Override
    public String toJson(Object obj) {
        return JsonUtils.toJsonString(obj);
    }
}
