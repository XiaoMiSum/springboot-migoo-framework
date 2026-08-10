package xyz.migoo.framework.mybatis.core.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.Test;
import xyz.migoo.framework.common.pojo.PageParam;
import xyz.migoo.framework.common.pojo.SortField;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisUtilsTest {

    @Test
    void buildPageWithoutSorting() {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(2);
        pageParam.setPageSize(20);

        Page<Object> page = MyBatisUtils.buildPage(pageParam);

        assertThat(page.getCurrent()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(20);
        assertThat(page.orders()).isEmpty();
    }

    @Test
    void buildPageWithSorting() {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(1);
        pageParam.setPageSize(10);
        List<SortField> sortingFields = Arrays.asList(
                new SortField("name", SortField.ORDER_ASC),
                new SortField("age", SortField.ORDER_DESC));

        Page<Object> page = MyBatisUtils.buildPage(pageParam, sortingFields);

        assertThat(page.orders()).hasSize(2);
        List<OrderItem> orders = page.orders();
        assertThat(orders.get(0).getColumn()).isEqualTo("name");
        assertThat(orders.get(0).isAsc()).isTrue();
        assertThat(orders.get(1).getColumn()).isEqualTo("age");
        assertThat(orders.get(1).isAsc()).isFalse();
    }

    @Test
    void buildPageIgnoresEmptySorting() {
        Page<Object> page = MyBatisUtils.buildPage(new PageParam(), List.of());
        assertThat(page.orders()).isEmpty();
    }

    @Test
    void addInterceptorInsertsAtIndex() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor inner = new PaginationInnerInterceptor(DbType.MYSQL);

        MyBatisUtils.addInterceptor(interceptor, inner, 0);

        assertThat(interceptor.getInterceptors()).containsExactly(inner);
    }

    @Test
    void getTableNameStripsBackticks() {
        assertThat(MyBatisUtils.getTableName(new Table("t_user"))).isEqualTo("t_user");
        assertThat(MyBatisUtils.getTableName(new Table("`t_user`"))).isEqualTo("t_user");
    }

    @Test
    void buildColumnWithoutAlias() {
        Column column = MyBatisUtils.buildColumn("t_user", null, "id");
        assertThat(column.toString()).isEqualTo("t_user.id");
    }

    @Test
    void buildColumnWithAlias() {
        Column column = MyBatisUtils.buildColumn("t_user", new Alias("u"), "id");
        assertThat(column.toString()).isEqualTo("u.id");
    }
}
