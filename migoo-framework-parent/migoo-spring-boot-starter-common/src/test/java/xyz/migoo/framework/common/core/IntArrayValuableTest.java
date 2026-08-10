package xyz.migoo.framework.common.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link IntArrayValuable} 的单元测试
 *
 * @author xiaomi
 */
class IntArrayValuableTest {

    @Test
    void toCollection_shouldBoxIntArray() {
        // 匿名实现返回 int[]，toCollection() 应将其装箱为 List<Integer>
        IntArrayValuable valuable = () -> new int[]{1, 2};
        assertThat(valuable.array()).containsExactly(1, 2);
        assertThat((List<Integer>) valuable.toCollection()).containsExactly(1, 2);
    }
}
