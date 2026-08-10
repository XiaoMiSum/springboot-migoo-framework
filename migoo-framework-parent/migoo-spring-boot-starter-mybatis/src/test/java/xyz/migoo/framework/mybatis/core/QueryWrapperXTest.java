package xyz.migoo.framework.mybatis.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class QueryWrapperXTest {

    @Test
    void selectSFunctionConvertsToUnderlineColumns() {
        assertThat(new QueryWrapperX<TestDO>().select(TestDO::getName, TestDO::getRoleId).getSqlSelect())
                .contains("name").contains("role_id");
    }

    @Test
    void limitAppendsLimitSql() {
        assertThat(new QueryWrapperX<TestDO>().limit(3).getTargetSql()).containsIgnoringCase("limit 3");
    }

    @Test
    void likeIfPresentSkipsNullAndBlank() {
        assertThat(new QueryWrapperX<TestDO>().likeIfPresent(TestDO::getName, null).getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().likeIfPresent(TestDO::getName, "  ").getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().likeIfPresent("name", "  ").getCustomSqlSegment()).isEmpty();
    }

    @Test
    void likeIfPresentAppendsWhenHasText() {
        assertThat(new QueryWrapperX<TestDO>().likeIfPresent(TestDO::getName, "yun").getCustomSqlSegment())
                .contains("LIKE");
        assertThat(new QueryWrapperX<TestDO>().likeIfPresent("name", "yun").getCustomSqlSegment())
                .contains("LIKE");
    }

    @Test
    void inNotInIfPresent() {
        assertThat(new QueryWrapperX<TestDO>().inIfPresent(TestDO::getAge, Collections.emptyList()).getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().inIfPresent("age", Collections.emptyList()).getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().inIfPresent(TestDO::getAge, Arrays.asList(1, 2)).getCustomSqlSegment()).contains("IN");
        assertThat(new QueryWrapperX<TestDO>().inIfPresent("age", Arrays.asList(1, 2)).getCustomSqlSegment()).contains("IN");
        assertThat(new QueryWrapperX<TestDO>().notInIfPresent(TestDO::getAge, Arrays.asList(1, 2)).getCustomSqlSegment()).contains("NOT IN");
    }

    @Test
    void comparisonIfPresent() {
        assertThat(new QueryWrapperX<TestDO>().eqIfPresent(TestDO::getName, null).getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().eqIfPresent("name", null).getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().neIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().gtIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().geIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().ltIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().leIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();

        assertThat(new QueryWrapperX<TestDO>().eqIfPresent(TestDO::getName, "yun").getCustomSqlSegment()).contains("=");
        assertThat(new QueryWrapperX<TestDO>().gtIfPresent(TestDO::getAge, 18).getCustomSqlSegment()).contains(">");
        assertThat(new QueryWrapperX<TestDO>().leIfPresent(TestDO::getAge, 65).getCustomSqlSegment()).contains("<=");
    }

    @Test
    void betweenIfPresent() {
        assertThat(new QueryWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, 18, 65).getCustomSqlSegment()).contains("BETWEEN");
        assertThat(new QueryWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, 18, null).getCustomSqlSegment()).contains(">=");
        assertThat(new QueryWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, null, 65).getCustomSqlSegment()).contains("<=");
        assertThat(new QueryWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, null, null).getCustomSqlSegment()).isEmpty();
        assertThat(new QueryWrapperX<TestDO>().betweenIfPresent("age", 18, 65).getCustomSqlSegment()).contains("BETWEEN");
    }

    @Test
    void fluentChainingKeepsXType() {
        QueryWrapperX<TestDO> wrapper = new QueryWrapperX<TestDO>()
                .eqIfPresent("name", "yun")
                .likeIfPresent("name", "yun")
                .inIfPresent("age", Arrays.asList(1, 2))
                .orderByDesc("age")
                .last("LIMIT 1");
        assertThat(wrapper).isInstanceOf(QueryWrapperX.class);
        assertThat(wrapper.getCustomSqlSegment()).contains("LIKE").contains("IN");
    }
}
