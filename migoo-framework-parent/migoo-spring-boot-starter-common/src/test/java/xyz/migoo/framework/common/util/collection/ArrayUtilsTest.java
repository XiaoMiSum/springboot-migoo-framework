package xyz.migoo.framework.common.util.collection;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ArrayUtils} 单元测试
 */
class ArrayUtilsTest {

    // ========== append ==========

    @Test
    void append_nullObject_returnsNewElementsAsIs() {
        Consumer<String> c1 = s -> {
        };
        Consumer<String> c2 = s -> {
        };
        Consumer<String>[] newElements = new Consumer[]{c1, c2};
        // 第一个参数为 null 时，直接返回 newElements 数组本身
        Consumer<String>[] result = ArrayUtils.append(null, newElements);
        assertThat(result).isSameAs(newElements);
        assertThat(result).hasSize(2).containsExactly(c1, c2);
    }

    @Test
    void append_withObject_returnsCombinedArray() {
        Consumer<String> first = s -> {
        };
        Consumer<String> a = s -> {
        };
        Consumer<String> b = s -> {
        };
        Consumer<String>[] result = ArrayUtils.append(first, a, b);
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(first, a, b);
    }

    // ========== toArray ==========

    @Test
    void toArray_withMapper_mapsAndConverts() {
        // convertList 映射后转数组，["ab","cde"] -> String::length -> [2, 3]
        Integer[] result = ArrayUtils.toArray(List.of("ab", "cde"), String::length);
        assertThat(result).containsExactly(2, 3);
        assertThat(result).isInstanceOf(Integer[].class);
    }

    @Test
    void toArray_emptyOrNull_returnsEmptyObjectArray() {
        Object[] empty = ArrayUtils.toArray(Collections.emptyList());
        assertThat(empty).isEmpty();
        assertThat(empty).isInstanceOf(Object[].class);

        Object[] nullResult = ArrayUtils.toArray(null);
        assertThat(nullResult).isEmpty();
        assertThat(nullResult).isInstanceOf(Object[].class);
    }

    @Test
    void toArray_nonEmpty_runtimeComponentTypeOfFirstElement() {
        // 非空集合：运行时数组类型以第一个元素类型为准
        String[] result = ArrayUtils.toArray(List.of("a", "b"));
        assertThat(result).isInstanceOf(String[].class);
        assertThat(result).containsExactly("a", "b");
    }

    // ========== get ==========

    @Test
    void get_elementAtIndex_returnsElement() {
        assertThat(ArrayUtils.get(new String[]{"a", "b", "c"}, 1)).isEqualTo("b");
    }

    @Test
    void get_nullOrOutOfBounds_returnsNull() {
        assertThat(ArrayUtils.<String>get(null, 0)).isNull();
        assertThat(ArrayUtils.get(new String[]{"a"}, 5)).isNull();
        assertThat(ArrayUtils.get(new String[]{"a", "b"}, 2)).isNull();
    }

}
