package xyz.migoo.framework.common.util.collection;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CollectionUtils} 单元测试
 */
class CollectionUtilsTest {

    // ========== containsAny(Object, Object...) ==========

    @Test
    void containsAny_varargs() {
        assertThat(CollectionUtils.containsAny("a", "a", "b")).isTrue();
        assertThat(CollectionUtils.containsAny("c", "a", "b")).isFalse();
        assertThat(CollectionUtils.containsAny(null, "a")).isFalse();
        assertThat(CollectionUtils.containsAny("a")).isFalse();
    }

    // ========== isAnyEmpty ==========

    @Test
    void isAnyEmpty_varargs() {
        assertThat(CollectionUtils.isAnyEmpty()).isFalse();
        assertThat(CollectionUtils.isAnyEmpty(List.of(1), Collections.emptyList())).isTrue();
        assertThat(CollectionUtils.isAnyEmpty(null, List.of(1))).isTrue();
        assertThat(CollectionUtils.isAnyEmpty(List.of(1), List.of(2))).isFalse();
    }

    // ========== anyMatch ==========

    @Test
    void anyMatch() {
        assertThat(CollectionUtils.anyMatch(List.of(1, 2, 3), i -> i > 2)).isTrue();
        assertThat(CollectionUtils.anyMatch(List.of(1, 2), i -> i > 2)).isFalse();
    }

    // ========== filterList ==========

    @Test
    void filterList() {
        assertThat(CollectionUtils.filterList(null, i -> true)).isEmpty();
        assertThat(CollectionUtils.filterList(Collections.emptyList(), i -> true)).isEmpty();
        List<Integer> result = CollectionUtils.filterList(List.of(1, 2, 3, 4), i -> i % 2 == 0);
        assertThat(result).isInstanceOf(ArrayList.class).containsExactly(2, 4);
    }

    // ========== distinct ==========

    @Test
    void distinct_byKey() {
        assertThat(CollectionUtils.distinct(null, Function.identity())).isEmpty();
        // HashMap 迭代顺序不固定，使用 inAnyOrder 断言
        assertThat(CollectionUtils.distinct(List.of("a", "b", "a", "c"), Function.identity()))
                .containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    void distinct_withCover() {
        // cover 选择后值：key 相同（'a'）时保留第二个元素 "a2"
        assertThat(CollectionUtils.distinct(List.of("a1", "b1", "a2"), s -> s.charAt(0), (t1, t2) -> t2))
                .containsExactlyInAnyOrder("a2", "b1");
    }

    // ========== convertList ==========

    @Test
    void convertList_fromArray() {
        assertThat(CollectionUtils.convertList((String[]) null, s -> s.toUpperCase())).isEmpty();
        assertThat(CollectionUtils.convertList(new String[0], s -> s.toUpperCase())).isEmpty();
        assertThat(CollectionUtils.convertList(new String[]{"a", "b"}, String::toUpperCase))
                .containsExactly("A", "B");
        // 映射结果为 null 的元素被过滤
        assertThat(CollectionUtils.convertList(new String[]{"a", null, "b"}, s -> s))
                .containsExactly("a", "b");
    }

    @Test
    void convertList_fromCollection() {
        assertThat(CollectionUtils.convertList((Collection<Integer>) null, i -> i * 2)).isEmpty();
        assertThat(CollectionUtils.convertList(Collections.<Integer>emptyList(), i -> i * 2)).isEmpty();
        assertThat(CollectionUtils.convertList(List.of(1, 2, 3), i -> i * 2)).containsExactly(2, 4, 6);
        // 映射结果为 null 的元素被过滤
        assertThat(CollectionUtils.convertList(List.of(1, 2, 3), i -> i == 2 ? null : i))
                .containsExactly(1, 3);
    }

    @Test
    void convertList_withFilter() {
        assertThat(CollectionUtils.<Integer, Integer>convertList(null, i -> i, i -> i > 0)).isEmpty();
        assertThat(CollectionUtils.convertList(List.of(1, 2, 3, 4), i -> i, i -> i % 2 == 0))
                .containsExactly(2, 4);
    }

    // ========== convertListByFlatMap ==========

    @Test
    void convertListByFlatMap() {
        assertThat(CollectionUtils.<List<Integer>, Integer>convertListByFlatMap(null, c -> c.stream())).isEmpty();
        assertThat(CollectionUtils.convertListByFlatMap(List.of(List.of(1, 2), List.of(3)), Collection::stream))
                .containsExactly(1, 2, 3);
        // 展开后的 null 元素被过滤
        assertThat(CollectionUtils.convertListByFlatMap(List.of(1, 2), i -> Stream.of(i, null)))
                .containsExactly(1, 2);
    }

    @Test
    void convertListByFlatMap_withMapper() {
        assertThat(CollectionUtils.<Integer, Integer, Integer>convertListByFlatMap(null, i -> i, i -> Stream.of(i))).isEmpty();
        assertThat(CollectionUtils.convertListByFlatMap(List.of(1, 2), i -> i * 10, i -> Stream.of(i, i + 1)))
                .containsExactly(10, 11, 20, 21);
    }

    // ========== mergeValuesFromMap ==========

    @Test
    void mergeValuesFromMap() {
        Map<String, List<Integer>> map = new HashMap<>();
        map.put("a", List.of(1, 2));
        map.put("b", List.of(3));
        // map 迭代顺序不固定，使用 inAnyOrder 断言
        assertThat(CollectionUtils.mergeValuesFromMap(map)).containsExactlyInAnyOrder(1, 2, 3);
    }

    // ========== convertSet ==========

    @Test
    void convertSet() {
        assertThat(CollectionUtils.convertSet(null, i -> i)).isEmpty();
        assertThat(CollectionUtils.convertSet(Collections.emptyList(), i -> i)).isEmpty();
        // Set 顺序不固定，使用 inAnyOrder 断言
        assertThat(CollectionUtils.convertSet(List.of(1, 2, 3, 2), i -> i * 10))
                .containsExactlyInAnyOrder(10, 20, 30);
        // 映射结果为 null 的元素被过滤
        assertThat(CollectionUtils.convertSet(List.of(1, 2), i -> i == 1 ? null : i))
                .containsExactly(2);
    }

    @Test
    void convertSet_withFilter() {
        assertThat(CollectionUtils.<Integer, Integer>convertSet(null, i -> i, i -> i > 0)).isEmpty();
        assertThat(CollectionUtils.convertSet(List.of(1, 2, 3, 4), i -> i, i -> i % 2 == 0))
                .containsExactlyInAnyOrder(2, 4);
    }

    // ========== convertSetByFlatMap ==========

    @Test
    void convertSetByFlatMap() {
        assertThat(CollectionUtils.<List<Integer>, Integer>convertSetByFlatMap(null, Collection::stream)).isEmpty();
        assertThat(CollectionUtils.convertSetByFlatMap(List.of(List.of(1, 2), List.of(2, 3)), Collection::stream))
                .containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void convertSetByFlatMap_withMapper() {
        assertThat(CollectionUtils.<Integer, Integer, Integer>convertSetByFlatMap(null, i -> i, i -> Stream.of(i))).isEmpty();
        assertThat(CollectionUtils.convertSetByFlatMap(List.of(1, 2), i -> i * 10, i -> Stream.of(i, i + 1)))
                .containsExactlyInAnyOrder(10, 11, 20, 21);
    }

    // ========== convertMapByFilter ==========

    @Test
    void convertMapByFilter() {
        assertThat(CollectionUtils.convertMapByFilter(null, s -> true, s -> s)).isEmpty();
        Map<String, String> result = CollectionUtils.convertMapByFilter(
                List.of("a", "b", "c"), s -> !"b".equals(s), s -> s);
        assertThat(result).isInstanceOf(HashMap.class);
        assertThat(result).containsOnlyKeys("a", "c");
        assertThat(result).containsEntry("a", "a").containsEntry("c", "c");
    }

    // ========== convertMap 六种重载 ==========

    @Test
    void convertMap_default() {
        assertThat(CollectionUtils.convertMap(null, s -> s)).isEmpty();
        assertThat(CollectionUtils.convertMap(List.of("a", "b"), Function.identity()))
                .containsEntry("a", "a").containsEntry("b", "b");
        // 重复 key 时默认取第一个值，不抛异常
        Map<String, String> duplicate = CollectionUtils.convertMap(List.of("a", "a"), Function.identity());
        assertThat(duplicate).hasSize(1).containsEntry("a", "a");
    }

    @Test
    void convertMap_withSupplier() {
        // null/empty 时返回 supplier.get()
        Map<String, String> empty = CollectionUtils.convertMap(null, s -> s, () -> new LinkedHashMap<>());
        assertThat(empty).isInstanceOf(LinkedHashMap.class).isEmpty();

        Map<String, String> result = CollectionUtils.convertMap(List.of("a"), s -> s, () -> new LinkedHashMap<>());
        assertThat(result).isInstanceOf(LinkedHashMap.class).containsEntry("a", "a");
    }

    @Test
    void convertMap_withValueFunc() {
        Map<String, Integer> result = CollectionUtils.convertMap(List.of(1, 2), i -> "k" + i, i -> i * 10);
        assertThat(result).containsEntry("k1", 10).containsEntry("k2", 20);
    }

    @Test
    void convertMap_withMergeFunction() {
        // 重复 key 'a' 时 merge 生效："a" + "a" = "aa"
        Map<Character, String> result = CollectionUtils.convertMap(
                List.of("a", "a", "b"), s -> s.charAt(0), s -> s, (v1, v2) -> v1 + v2);
        assertThat(result).containsEntry('a', "aa").containsEntry('b', "b");
    }

    @Test
    void convertMap_withValueFuncAndSupplier() {
        Map<String, String> result = CollectionUtils.convertMap(
                List.of("a", "b"), s -> s, s -> s, () -> new LinkedHashMap<>());
        assertThat(result).isInstanceOf(LinkedHashMap.class);
        assertThat(result).containsEntry("a", "a").containsEntry("b", "b");
    }

    @Test
    void convertMap_full() {
        // 5 参：mergeFunction + supplier 同时生效
        Map<String, String> result = CollectionUtils.convertMap(
                List.of("a", "a"), s -> s, s -> s, (v1, v2) -> v1 + "!" + v2, () -> new TreeMap<>());
        assertThat(result).isInstanceOf(TreeMap.class);
        assertThat(result).hasSize(1).containsEntry("a", "a!a");
    }

    // ========== convertMultiMap / convertMultiMap2 ==========

    @Test
    void convertMultiMap() {
        assertThat(CollectionUtils.<String, Character>convertMultiMap(null, s -> s.charAt(0))).isEmpty();
        Map<Character, List<String>> result = CollectionUtils.convertMultiMap(
                List.of("a1", "b1", "a2"), s -> s.charAt(0));
        assertThat(result).containsKeys('a', 'b');
        // 分组内保持输入顺序
        assertThat(result.get('a')).containsExactly("a1", "a2");
        assertThat(result.get('b')).containsExactly("b1");
    }

    @Test
    void convertMultiMap_withValueFunc() {
        Map<Character, List<String>> result = CollectionUtils.convertMultiMap(
                List.of("a1", "b1", "a2"), s -> s.charAt(0), s -> s.substring(1));
        assertThat(result.get('a')).containsExactly("1", "2");
        assertThat(result.get('b')).containsExactly("1");
    }

    @Test
    void convertMultiMap2() {
        assertThat(CollectionUtils.<String, Character, String>convertMultiMap2(null, s -> s.charAt(0), s -> s)).isEmpty();
        Map<Character, Set<String>> result = CollectionUtils.convertMultiMap2(
                List.of("a1", "a2", "a3"), s -> s.charAt(0), s -> s.substring(1));
        assertThat(result.get('a')).containsExactlyInAnyOrder("1", "2", "3");
    }

    // ========== convertImmutableMap ==========

    @Test
    void convertImmutableMap() {
        Map<String, String> empty = CollectionUtils.convertImmutableMap(Collections.emptyList(), s -> s);
        assertThat(empty).isEmpty();
        assertThat(empty).isSameAs(Collections.emptyMap());

        Map<String, String> result = CollectionUtils.convertImmutableMap(List.of("a", "b"), s -> s);
        assertThat(result).isInstanceOf(ImmutableMap.class);
        assertThat(result).containsEntry("a", "a").containsEntry("b", "b");
    }

    // ========== diffList ==========

    @Test
    void diffList() {
        Item old1 = new Item(1, "old-name-1");
        Item old3 = new Item(3, "old-name-3");
        Item new1 = new Item(1, "new-name-1");
        Item new2 = new Item(2, "new-name-2");

        List<List<Item>> result = CollectionUtils.diffList(
                List.of(old1, old3), List.of(new1, new2),
                (oldItem, newItem) -> oldItem.getId().equals(newItem.getId()));

        assertThat(result).hasSize(3);
        // 0: 仅在 newList 中 -> 新增
        List<Item> createList = result.get(0);
        assertThat(createList).extracting(Item::getId).containsExactly(2);
        // 1: 两边都有 -> 修改（使用 newList 中的新实例）
        List<Item> updateList = result.get(1);
        assertThat(updateList).extracting(Item::getId).containsExactly(1);
        assertThat(updateList.get(0).getName()).isEqualTo("new-name-1");
        // 2: 仅在 oldList 中 -> 删除
        List<Item> deleteList = result.get(2);
        assertThat(deleteList).extracting(Item::getId).containsExactly(3);
        assertThat(deleteList.get(0).getName()).isEqualTo("old-name-3");
    }

    // ========== isEmpty ==========

    @Test
    void isEmpty_collection() {
        assertThat(CollectionUtils.isEmpty((Collection<?>) null)).isTrue();
        assertThat(CollectionUtils.isEmpty(Collections.emptyList())).isTrue();
        assertThat(CollectionUtils.isEmpty(List.of(1))).isFalse();
    }

    @Test
    void isEmpty_map() {
        assertThat(CollectionUtils.isEmpty((Map<?, ?>) null)).isTrue();
        assertThat(CollectionUtils.isEmpty(Collections.emptyMap())).isTrue();
        assertThat(CollectionUtils.isEmpty(Map.of("a", 1))).isFalse();
    }

    // ========== containsAny(Collection, Collection) ==========

    @Test
    void containsAny_collections() {
        assertThat(CollectionUtils.containsAny(null, List.of(1))).isFalse();
        assertThat(CollectionUtils.containsAny(Collections.emptyList(), List.of(1))).isFalse();
        assertThat(CollectionUtils.containsAny(List.of(1), (Collection<?>) null)).isFalse();
        assertThat(CollectionUtils.containsAny(List.of(1), Collections.emptyList())).isFalse();
        assertThat(CollectionUtils.containsAny(List.of(1, 2, 3), List.of(4, 2))).isTrue();
        assertThat(CollectionUtils.containsAny(List.of(1, 2), List.of(3, 4))).isFalse();
    }

    // ========== getFirst ==========

    @Test
    void getFirst() {
        assertThat(CollectionUtils.<String>getFirst(null)).isNull();
        assertThat(CollectionUtils.getFirst(Collections.<String>emptyList())).isNull();
        assertThat(CollectionUtils.getFirst(List.of("a", "b"))).isEqualTo("a");
    }

    // ========== findFirst ==========

    @Test
    void findFirst() {
        assertThat(CollectionUtils.<String>findFirst(null, s -> true)).isNull();
        assertThat(CollectionUtils.findFirst(Collections.<String>emptyList(), s -> true)).isNull();
        assertThat(CollectionUtils.findFirst(List.of("a", "bb", "c"), s -> s.length() > 1)).isEqualTo("bb");
        assertThat(CollectionUtils.findFirst(List.of("a", "b"), s -> s.length() > 3)).isNull();
    }

    @Test
    void findFirst_withFunc() {
        assertThat(CollectionUtils.<String, String>findFirst(null, s -> true, s -> s.toUpperCase())).isNull();
        assertThat(CollectionUtils.<String, String>findFirst(List.of("aa", "b", "ccc"), s -> s.length() > 2, s -> s.toUpperCase()))
                .isEqualTo("CCC");
        assertThat(CollectionUtils.<String, String>findFirst(List.of("a", "b"), s -> s.length() > 5, s -> s.toUpperCase()))
                .isNull();
    }

    // ========== getMaxValue / getMinValue / getSumValue ==========

    @Test
    void getMaxValue() {
        // 空集合在 assert 之前就返回 null，安全
        assertThat(CollectionUtils.getMaxValue(null, String::length)).isNull();
        assertThat(CollectionUtils.getMaxValue(Collections.emptyList(), String::length)).isNull();
        assertThat(CollectionUtils.getMaxValue(List.of("a", "bb", "ccc"), String::length)).isEqualTo(3);
    }

    @Test
    void getMinValue() {
        assertThat(CollectionUtils.<Integer, Integer>getMinValue(null, i -> i)).isNull();
        assertThat(CollectionUtils.<Integer, Integer>getMinValue(Collections.<Integer>emptyList(), i -> i)).isNull();
        assertThat(CollectionUtils.<Integer, Integer>getMinValue(List.of(3, 1, 2), i -> i)).isEqualTo(1);
    }

    @Test
    void getSumValue_default() {
        // 3 参重载：defaultValue 为 null
        assertThat(CollectionUtils.<Integer, Integer>getSumValue(null, i -> i, Integer::sum)).isNull();
        assertThat(CollectionUtils.getSumValue(Collections.<Integer>emptyList(), i -> i, Integer::sum)).isNull();
        assertThat(CollectionUtils.getSumValue(List.of(1, 2, 3), i -> i, Integer::sum)).isEqualTo(6);
    }

    @Test
    void getSumValue_withDefault() {
        // 4 参重载：空集合返回 defaultValue
        assertThat(CollectionUtils.<Integer, Integer>getSumValue(null, i -> i, Integer::sum, 0)).isEqualTo(0);
        assertThat(CollectionUtils.getSumValue(Collections.<Integer>emptyList(), i -> i, Integer::sum, 0)).isEqualTo(0);
        assertThat(CollectionUtils.getSumValue(List.of(1, 2, 3), i -> i, Integer::sum, 0)).isEqualTo(6);
        // null 值被过滤后求和
        assertThat(CollectionUtils.getSumValue(Arrays.asList(1, null, 2), i -> i, Integer::sum, 0)).isEqualTo(3);
        // 全部为 null 时 reduce 为空 -> 返回 defaultValue
        assertThat(CollectionUtils.getSumValue(Arrays.<Integer>asList(null, null), i -> i, Integer::sum, 0)).isEqualTo(0);
    }

    // ========== addIfNotNull ==========

    @Test
    void addIfNotNull() {
        List<String> list = new ArrayList<>();
        CollectionUtils.addIfNotNull(list, null);
        assertThat(list).isEmpty();
        CollectionUtils.addIfNotNull(list, "a");
        CollectionUtils.addIfNotNull(list, null);
        CollectionUtils.addIfNotNull(list, "b");
        assertThat(list).containsExactly("a", "b");
    }

    // ========== singleton ==========

    @Test
    void singleton() {
        assertThat(CollectionUtils.singleton(null)).isEmpty();
        Collection<String> singleton = CollectionUtils.singleton("a");
        assertThat(singleton).hasSize(1).containsExactly("a");
    }

    // ========== newArrayList ==========

    @Test
    void newArrayList() {
        assertThat(CollectionUtils.newArrayList(List.of(List.of(1, 2), List.of(3, 4))))
                .containsExactly(1, 2, 3, 4);
    }

    /**
     * diffList 测试用的简单实体，以 id 作为标识
     */
    static class Item {
        private final Integer id;
        private final String name;

        Item(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        Integer getId() {
            return id;
        }

        String getName() {
            return name;
        }
    }

}
