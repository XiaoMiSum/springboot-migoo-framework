package xyz.migoo.framework.common.util.collection;

import com.google.common.collect.ArrayListMultimap;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.common.pojo.KeyValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MapUtils} 单元测试
 */
class MapUtilsTest {

    // ========== getList ==========

    @Test
    void getList() {
        ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
        multimap.put("a", 1);
        multimap.put("a", 2);
        multimap.put("b", 3);

        // 无值的 key 跳过；存在的 key 按输入 key 顺序收集所有值
        List<Integer> result = MapUtils.getList(multimap, List.of("a", "missing", "b"));
        assertThat(result).containsExactly(1, 2, 3);

        // 全部为无值 key -> 空列表
        assertThat(MapUtils.getList(multimap, List.of("nope"))).isEmpty();
    }

    // ========== findAndThen ==========

    @Test
    void findAndThen() {
        List<String> received = new ArrayList<>();

        // null / empty map -> no-op
        MapUtils.<String, String>findAndThen(null, "a", received::add);
        MapUtils.<String, String>findAndThen(Collections.emptyMap(), "a", received::add);
        assertThat(received).isEmpty();

        // key 不存在 -> no-op
        MapUtils.findAndThen(Map.of("a", "1"), "b", received::add);
        assertThat(received).isEmpty();

        // key 存在 -> consumer 被调用
        MapUtils.findAndThen(Map.of("a", "1"), "a", received::add);
        assertThat(received).containsExactly("1");
    }

    // ========== convertMap ==========

    @Test
    void convertMap() {
        Map<String, Integer> result = MapUtils.convertMap(List.of(
                KeyValue.of("a", 1),
                KeyValue.of("b", 2),
                KeyValue.of("c", 3)));
        // LinkedHashMap，保持插入顺序
        assertThat(result).isInstanceOf(LinkedHashMap.class);
        assertThat(new ArrayList<>(result.keySet())).containsExactly("a", "b", "c");
        assertThat(result).containsEntry("a", 1).containsEntry("b", 2).containsEntry("c", 3);
    }

}
