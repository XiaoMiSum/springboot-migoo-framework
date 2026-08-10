package xyz.migoo.framework.common.util.object;

import org.junit.jupiter.api.Test;
import xyz.migoo.framework.common.pojo.PageParam;
import xyz.migoo.framework.common.pojo.SortField;
import xyz.migoo.framework.common.pojo.SortablePageParam;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link PageUtils} 单元测试
 */
class PageUtilsTest {

    /**
     * 可序列化的函数式接口：PageUtils 通过 SerializedLambda#writeReplace 提取字段名，
     * 因此传入的 Lambda 必须实现 {@link Serializable}
     */
    @FunctionalInterface
    interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
    }

    /** 测试用方法引用，需强制转为可序列化函数 */
    private static SerializableFunction<UserVO, String> idFunc() {
        return UserVO::getId;
    }

    private static SerializableFunction<UserVO, Boolean> activeFunc() {
        return UserVO::isActive;
    }

    // ========== getStart ==========

    @Test
    void getStart() {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(1);
        pageParam.setPageSize(10);
        assertThat(PageUtils.getStart(pageParam)).isEqualTo(0);

        pageParam.setPageNo(3);
        pageParam.setPageSize(20);
        assertThat(PageUtils.getStart(pageParam)).isEqualTo(40);
    }

    // ========== buildSortingField ==========

    @Test
    void buildSortingField_defaultOrder() {
        SortField sortField = PageUtils.buildSortingField(idFunc());
        assertThat(sortField.getField()).isEqualTo("id");
        assertThat(sortField.getOrder()).isEqualTo(SortField.ORDER_DESC);
    }

    @Test
    void buildSortingField_withOrder() {
        // is 前缀被剥离 -> active
        SortField sortField = PageUtils.buildSortingField(activeFunc(), SortField.ORDER_ASC);
        assertThat(sortField.getField()).isEqualTo("active");
        assertThat(sortField.getOrder()).isEqualTo(SortField.ORDER_ASC);
    }

    @Test
    void buildSortingField_invalidOrder() {
        assertThatThrownBy(() -> PageUtils.buildSortingField(UserVO::getId, "invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ========== buildDefaultSortingField ==========

    @Test
    void buildDefaultSortingField_emptySortingFields() {
        SortablePageParam sortablePageParam = new SortablePageParam();
        assertThat(sortablePageParam.getSortingFields()).isNull();

        PageUtils.buildDefaultSortingField(sortablePageParam, idFunc());
        assertThat(sortablePageParam.getSortingFields()).hasSize(1);
        assertThat(sortablePageParam.getSortingFields().get(0).getField()).isEqualTo("id");
        assertThat(sortablePageParam.getSortingFields().get(0).getOrder()).isEqualTo(SortField.ORDER_DESC);
    }

    @Test
    void buildDefaultSortingField_nonEmptySortingFields() {
        SortablePageParam sortablePageParam = new SortablePageParam();
        sortablePageParam.setSortingFields(List.of(new SortField("name", SortField.ORDER_ASC)));

        // 非空时保持不变
        PageUtils.buildDefaultSortingField(sortablePageParam, UserVO::getId);
        assertThat(sortablePageParam.getSortingFields()).hasSize(1);
        assertThat(sortablePageParam.getSortingFields().get(0).getField()).isEqualTo("name");
        assertThat(sortablePageParam.getSortingFields().get(0).getOrder()).isEqualTo(SortField.ORDER_ASC);
    }

    @Test
    void buildDefaultSortingField_nullParam() {
        assertThatCode(() -> PageUtils.buildDefaultSortingField(null, UserVO::getId))
                .doesNotThrowAnyException();
    }

    /**
     * 测试用的 VO，方法引用 UserVO::getId / UserVO::isActive
     * 依赖 SerializedLambda#writeReplace 提取字段名
     */
    static class UserVO {

        public String getId() {
            return "1";
        }

        public boolean isActive() {
            return true;
        }
    }

}
