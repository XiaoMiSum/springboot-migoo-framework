package xyz.migoo.framework.mybatis.core;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class LambdaQueryWrapperXTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), TestDO.class);
    }

    @Test
    void likeIfPresentSkipsNullAndBlank() {
        assertThat(new LambdaQueryWrapperX<TestDO>().likeIfPresent(TestDO::getName, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaQueryWrapperX<TestDO>().likeIfPresent(TestDO::getName, "  ").getCustomSqlSegment()).isEmpty();
    }

    @Test
    void likeIfPresentAppendsWhenHasText() {
        LambdaQueryWrapperX<TestDO> wrapper = new LambdaQueryWrapperX<TestDO>().likeIfPresent(TestDO::getName, "yun");
        assertThat(wrapper.getCustomSqlSegment()).contains("LIKE");
        assertThat(wrapper.getParamNameValuePairs()).containsValue("%yun%");
    }

    @Test
    void likeLeftAndRightIfPresent() {
        LambdaQueryWrapperX<TestDO> left = new LambdaQueryWrapperX<TestDO>().likeLeftIfPresent(TestDO::getName, "yun");
        assertThat(left.getCustomSqlSegment()).contains("LIKE");
        assertThat(left.getParamNameValuePairs()).containsValue("%yun");

        LambdaQueryWrapperX<TestDO> right = new LambdaQueryWrapperX<TestDO>().likeRightIfPresent(TestDO::getName, "yun");
        assertThat(right.getCustomSqlSegment()).contains("LIKE");
        assertThat(right.getParamNameValuePairs()).containsValue("yun%");
    }

    @Test
    void comparisonIfPresentSkipNull() {
        assertThat(new LambdaQueryWrapperX<TestDO>().eqIfPresent(TestDO::getName, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaQueryWrapperX<TestDO>().neIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaQueryWrapperX<TestDO>().gtIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaQueryWrapperX<TestDO>().geIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaQueryWrapperX<TestDO>().ltIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaQueryWrapperX<TestDO>().leIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
    }

    @Test
    void comparisonIfPresentAppendWhenValuePresent() {
        assertThat(new LambdaQueryWrapperX<TestDO>().eqIfPresent(TestDO::getName, "yun").getCustomSqlSegment()).contains("=");
        assertThat(new LambdaQueryWrapperX<TestDO>().neIfPresent(TestDO::getAge, 18).getCustomSqlSegment()).contains("<>");
        assertThat(new LambdaQueryWrapperX<TestDO>().gtIfPresent(TestDO::getAge, 18).getCustomSqlSegment()).contains(">");
        assertThat(new LambdaQueryWrapperX<TestDO>().geIfPresent(TestDO::getAge, 18).getCustomSqlSegment()).contains(">=");
        assertThat(new LambdaQueryWrapperX<TestDO>().ltIfPresent(TestDO::getAge, 65).getCustomSqlSegment()).contains("<");
        assertThat(new LambdaQueryWrapperX<TestDO>().leIfPresent(TestDO::getAge, 65).getCustomSqlSegment()).contains("<=");
    }

    @Test
    void inNotInIfPresent() {
        assertThat(new LambdaQueryWrapperX<TestDO>().inIfPresent(TestDO::getAge, Collections.emptyList()).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaQueryWrapperX<TestDO>().inIfPresent(TestDO::getAge, (Object[]) new Integer[0]).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaQueryWrapperX<TestDO>().inIfPresent(TestDO::getAge, Arrays.asList(1, 2, 3)).getCustomSqlSegment()).contains("IN");
        assertThat(new LambdaQueryWrapperX<TestDO>().notInIfPresent(TestDO::getAge, Arrays.asList(1, 2)).getCustomSqlSegment()).contains("NOT IN");
    }

    @Test
    void betweenIfPresent() {
        assertThat(new LambdaQueryWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, 18, 65).getCustomSqlSegment()).contains("BETWEEN");
        assertThat(new LambdaQueryWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, 18, null).getCustomSqlSegment()).contains(">=");
        assertThat(new LambdaQueryWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, null, 65).getCustomSqlSegment()).contains("<=");
        assertThat(new LambdaQueryWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, null, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaQueryWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, new Object[]{18, 65}).getCustomSqlSegment()).contains("BETWEEN");
    }

    @Test
    void limitAppendsLimitSql() {
        assertThat(new LambdaQueryWrapperX<TestDO>().limit(5).getTargetSql()).containsIgnoringCase("limit 5");
    }

    @Test
    void selectXAppendsColumns() {
        assertThat(new LambdaQueryWrapperX<TestDO>().selectX(TestDO::getName, TestDO::getAge).getSqlSelect())
                .contains("name").contains("age");
    }

    @Test
    void fluentChainingKeepsXType() {
        LambdaQueryWrapperX<TestDO> wrapper = new LambdaQueryWrapperX<TestDO>()
                .eqIfPresent(TestDO::getName, "yun")
                .likeIfPresent(TestDO::getName, "yun")
                .inIfPresent(TestDO::getAge, Arrays.asList(1, 2))
                .orderByDesc(TestDO::getAge)
                .last("LIMIT 1");
        assertThat(wrapper).isInstanceOf(LambdaQueryWrapperX.class);
        assertThat(wrapper.getCustomSqlSegment()).contains("LIKE").contains("IN");
    }
}
