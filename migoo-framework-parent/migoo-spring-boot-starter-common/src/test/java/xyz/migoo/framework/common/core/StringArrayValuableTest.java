package xyz.migoo.framework.common.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link StringArrayValuable} 的单元测试
 *
 * @author xiaomi
 */
class StringArrayValuableTest {

    @Test
    void toCollection_shouldWrapStringArray() {
        // 匿名实现返回 String[]，toCollection() 应将其包装为 List<String>
        StringArrayValuable valuable = () -> new String[]{"a", "b"};
        assertThat(valuable.array()).containsExactly("a", "b");
        assertThat((List<String>) valuable.toCollection()).containsExactly("a", "b");
    }
}
