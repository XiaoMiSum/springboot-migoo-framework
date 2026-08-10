package xyz.migoo.framework.common.pojo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SortablePageParam} 的单元测试
 *
 * @author xiaomi
 */
class SortablePageParamTest {

    @Test
    void inheritedPageFields_shouldUsePageParamDefaults() {
        // 继承自 PageParam，默认页码 1、每页条数 10
        SortablePageParam param = new SortablePageParam();
        assertThat(param.getPageNo()).isEqualTo(1);
        assertThat(param.getPageSize()).isEqualTo(10);
    }

    @Test
    void sortingFields_setterAndGetter_shouldRoundTrip() {
        // sortingFields 设置后通过 getter 取回同一引用
        SortablePageParam param = new SortablePageParam();
        List<SortField> sortingFields = List.of(new SortField("createTime", SortField.ORDER_DESC));
        param.setSortingFields(sortingFields);
        assertThat(param.getSortingFields()).isSameAs(sortingFields);
    }
}
