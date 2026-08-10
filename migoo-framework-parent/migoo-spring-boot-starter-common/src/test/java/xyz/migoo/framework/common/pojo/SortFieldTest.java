package xyz.migoo.framework.common.pojo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SortField} 的单元测试
 *
 * @author xiaomi
 */
class SortFieldTest {

    @Test
    void orderConstants_shouldBeAscAndDesc() {
        // 排序方向常量
        assertThat(SortField.ORDER_ASC).isEqualTo("asc");
        assertThat(SortField.ORDER_DESC).isEqualTo("desc");
    }

    @Test
    void noArgsConstructor_shouldLeaveFieldsNull() {
        // 空构造（解决反序列化），field 与 order 均为 null
        SortField sortField = new SortField();
        assertThat(sortField.getField()).isNull();
        assertThat(sortField.getOrder()).isNull();
    }

    @Test
    void allArgsConstructor_shouldSetFields() {
        // 全参构造
        SortField sortField = new SortField("createTime", "desc");
        assertThat(sortField.getField()).isEqualTo("createTime");
        assertThat(sortField.getOrder()).isEqualTo("desc");
    }

    @Test
    void setField_shouldReturnThisAndUpdateField() {
        // 流式 setter 返回当前实例
        SortField sortField = new SortField();
        SortField result = sortField.setField("id");
        assertThat(result).isSameAs(sortField);
        assertThat(sortField.getField()).isEqualTo("id");
    }

    @Test
    void setOrder_shouldReturnThisAndUpdateOrder() {
        // 流式 setter 返回当前实例
        SortField sortField = new SortField();
        SortField result = sortField.setOrder("asc");
        assertThat(result).isSameAs(sortField);
        assertThat(sortField.getOrder()).isEqualTo("asc");
    }
}
