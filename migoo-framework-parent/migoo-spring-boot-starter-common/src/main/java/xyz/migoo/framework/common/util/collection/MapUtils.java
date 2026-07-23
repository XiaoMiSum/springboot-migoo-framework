package xyz.migoo.framework.common.util.collection;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import xyz.migoo.framework.common.pojo.KeyValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Map 工具类
 *
 * @author xiaomi
 */
public class MapUtils {

    /**
     * 从哈希表表中，获得 keys 对应的所有 value 数组
     *
     * @param multimap 哈希表
     * @param keys     keys
     * @return value 数组
     */
    public static <K, V> List<V> getList(Multimap<K, V> multimap, Collection<K> keys) {
        List<V> result = new ArrayList<>();
        keys.forEach(k -> {
            Collection<V> values = multimap.get(k);
            if (values.isEmpty()) {
                return;
            }
            result.addAll(values);
        });
        return result;
    }

    public static <K, V> void findAndThen(Map<K, V> map, K key, Consumer<V> consumer) {
        if (map == null || map.isEmpty()) {
            return;
        }
        V value = map.get(key);
        if (value == null) {
            return;
        }
        consumer.accept(value);
    }

    public static <K, V> Map<K, V> convertMap(List<KeyValue<K, V>> keyValues) {
        Map<K, V> map = Maps.newLinkedHashMapWithExpectedSize(keyValues.size());
        keyValues.forEach(keyValue -> map.put(keyValue.getKey(), keyValue.getValue()));
        return map;
    }

}
