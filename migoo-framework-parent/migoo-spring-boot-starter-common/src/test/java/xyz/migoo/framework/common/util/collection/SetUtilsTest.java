package xyz.migoo.framework.common.util.collection;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SetUtils} 单元测试
 */
class SetUtilsTest {

    @Test
    void asSet_withElements() {
        Set<Integer> result = SetUtils.asSet(1, 2, 3);
        assertThat(result).hasSize(3).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void asSet_empty() {
        Set<Integer> result = SetUtils.asSet();
        assertThat(result).isEmpty();
    }

    @Test
    void asSet_deduplicates() {
        Set<Integer> result = SetUtils.asSet(1, 2, 2, 3, 1);
        assertThat(result).hasSize(3).containsExactlyInAnyOrder(1, 2, 3);
    }

}
