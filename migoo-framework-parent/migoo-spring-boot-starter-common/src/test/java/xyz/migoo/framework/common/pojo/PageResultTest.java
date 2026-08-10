package xyz.migoo.framework.common.pojo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PageResult} 的单元测试
 *
 * @author xiaomi
 */
class PageResultTest {

    @Test
    void noArgsConstructor_shouldLeaveFieldsNull() {
        // 空构造，list 与 total 均为 null
        PageResult<String> pageResult = new PageResult<>();
        assertThat(pageResult.getList()).isNull();
        assertThat(pageResult.getTotal()).isNull();
    }

    @Test
    void listAndTotalConstructor_shouldSetFields() {
        // (list, total) 构造保留传入的 list 引用
        List<String> list = List.of("a", "b");
        PageResult<String> pageResult = new PageResult<>(list, 2L);
        assertThat(pageResult.getList()).isSameAs(list);
        assertThat(pageResult.getTotal()).isEqualTo(2L);
    }

    @Test
    void totalConstructor_shouldCreateEmptyList() {
        // 仅传 total 时，list 为空列表
        PageResult<String> pageResult = new PageResult<>(5L);
        assertThat(pageResult.getList()).isEmpty();
        assertThat(pageResult.getTotal()).isEqualTo(5L);
    }

    @Test
    void empty_shouldCreateEmptyListAndZeroTotal() {
        // empty() 返回空列表且 total 为 0L
        PageResult<String> pageResult = PageResult.empty();
        assertThat(pageResult.getList()).isEmpty();
        assertThat(pageResult.getTotal()).isEqualTo(0L);
    }
}
