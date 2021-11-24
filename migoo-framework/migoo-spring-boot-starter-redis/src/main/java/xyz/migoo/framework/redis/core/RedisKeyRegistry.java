package xyz.migoo.framework.redis.core;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RedisKeyDefine} 注册表
 *
 * @author xiaomi
 * Created on 2021/11/21 16:14
 */
public class RedisKeyRegistry {

    /**
     * Redis RedisKeyDefine 数组
     */
    private static final List<RedisKeyDefine> defines = new ArrayList<>();

    public static void add(RedisKeyDefine define) {
        defines.add(define);
    }

    public static List<RedisKeyDefine> list() {
        return defines;
    }

    public static int size() {
        return defines.size();
    }
}
