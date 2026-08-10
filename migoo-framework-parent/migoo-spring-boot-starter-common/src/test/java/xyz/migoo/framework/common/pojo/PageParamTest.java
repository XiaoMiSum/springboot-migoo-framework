package xyz.migoo.framework.common.pojo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PageParam} 的单元测试
 *
 * @author xiaomi
 */
class PageParamTest {

    @Test
    void defaultValues_shouldBePageNoOneAndPageSizeTen() {
        // 默认页码为 1，每页条数为 10
        PageParam pageParam = new PageParam();
        assertThat(pageParam.getPageNo()).isEqualTo(1);
        assertThat(pageParam.getPageSize()).isEqualTo(10);
    }

    @Test
    void settersAndGetters_shouldRoundTripValues() {
        // 设置后通过 getter 取回
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(5);
        pageParam.setPageSize(20);
        assertThat(pageParam.getPageNo()).isEqualTo(5);
        assertThat(pageParam.getPageSize()).isEqualTo(20);
    }
}
