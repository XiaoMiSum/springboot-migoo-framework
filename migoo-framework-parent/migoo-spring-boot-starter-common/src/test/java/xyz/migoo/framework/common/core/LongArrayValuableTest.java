package xyz.migoo.framework.common.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LongArrayValuable} 的单元测试
 *
 * @author xiaomi
 */
class LongArrayValuableTest {

    @Test
    void toCollection_shouldBoxLongArray() {
        // 匿名实现返回 long[]，toCollection() 应将其装箱为 List<Long>
        LongArrayValuable valuable = () -> new long[]{1L, 2L};
        assertThat(valuable.array()).containsExactly(1L, 2L);
        assertThat((List<Long>) valuable.toCollection()).containsExactly(1L, 2L);
    }
}
