package xyz.migoo.framework.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Key Value 的键值对
 *
 * @author xiaomi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyValue<K, V> {

    private K key;
    private V value;

    public static <K, V> KeyValue<K, V> of(K k, V v) {
        return new KeyValue<>(k, v);
    }
}
