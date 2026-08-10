package xyz.migoo.framework.mybatis.core;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class MPJLambdaWrapperXTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), TestDO.class);
    }

    private MPJLambdaWrapperX<TestDO> newWrapper() {
        MPJLambdaWrapperX<TestDO> wrapper = new MPJLambdaWrapperX<>();
        wrapper.setEntityClass(TestDO.class);
        return wrapper;
    }

    @Test
    void limitAppendsLimitSql() {
        assertThat(newWrapper().limit(10).getTargetSql()).containsIgnoringCase("limit 10");
    }

    @Test
    void selectXAppendsColumns() {
        MPJLambdaWrapperX<TestDO> wrapper = newWrapper().selectX(TestDO::getName, TestDO::getAge);
        assertThat(wrapper.getSqlSelect()).contains("name").contains("age");
    }

    @Test
    void likeIfPresentSkipsNullAndBlank() {
        assertThat(newWrapper().likeIfPresent(TestDO::getName, null).getCustomSqlSegment()).isEmpty();
        assertThat(newWrapper().likeIfPresent(TestDO::getName, "  ").getCustomSqlSegment()).isEmpty();
    }

    @Test
    void likeIfPresentAppendsWhenHasText() {
        MPJLambdaWrapperX<TestDO> wrapper = newWrapper().likeIfPresent(TestDO::getName, "yun");
        assertThat(wrapper.getCustomSqlSegment()).contains("LIKE");
        assertThat(wrapper.getParamNameValuePairs()).containsValue("%yun%");
    }

    @Test
    void inNotInIfPresent() {
        assertThat(newWrapper().inIfPresent(TestDO::getAge, Collections.emptyList()).getCustomSqlSegment()).isEmpty();
        assertThat(newWrapper().inIfPresent(TestDO::getAge, (Object[]) new Integer[0]).getCustomSqlSegment()).isEmpty();
        assertThat(newWrapper().inIfPresent(TestDO::getAge, Arrays.asList(1, 2)).getCustomSqlSegment()).contains("IN");
        assertThat(newWrapper().notInIfPresent(TestDO::getAge, Arrays.asList(1, 2)).getCustomSqlSegment()).contains("NOT IN");
    }

    @Test
    void eqNeIfPresent() {
        assertThat(newWrapper().eqIfPresent(TestDO::getName, null).getCustomSqlSegment()).isEmpty();
        assertThat(newWrapper().neIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();

        MPJLambdaWrapperX<TestDO> wrapper = newWrapper().eqIfPresent(TestDO::getName, "yun");
        assertThat(wrapper.getCustomSqlSegment()).contains("=");
        assertThat(wrapper.getParamNameValuePairs()).containsValue("yun");
    }

    @Test
    void betweenIfPresent() {
        assertThat(newWrapper().betweenIfPresent(TestDO::getAge, 18, 65).getCustomSqlSegment()).contains("BETWEEN");
        assertThat(newWrapper().betweenIfPresent(TestDO::getAge, 18, null).getCustomSqlSegment()).contains(">=");
        assertThat(newWrapper().betweenIfPresent(TestDO::getAge, null, 65).getCustomSqlSegment()).contains("<=");
        assertThat(newWrapper().betweenIfPresent(TestDO::getAge, null, null).getCustomSqlSegment()).isEmpty();
    }

    @Test
    void leftJoinXBuildsJoinSql() {
        MPJLambdaWrapperX<TestDO> wrapper = newWrapper().leftJoinX(TestDO.class, TestDO::getRoleId, TestDO::getId);
        assertThat(wrapper.getFrom()).containsIgnoringCase("left join");
    }

    @Test
    void innerJoinXBuildsJoinSql() {
        MPJLambdaWrapperX<TestDO> wrapper = newWrapper().innerJoinX(TestDO.class, TestDO::getRoleId, "r", TestDO::getId);
        assertThat(wrapper.getFrom()).containsIgnoringCase("inner join");
    }

    @Test
    void fluentChainingKeepsXType() {
        MPJLambdaWrapperX<TestDO> wrapper = newWrapper()
                .eqIfPresent(TestDO::getName, "yun")
                .likeIfPresent(TestDO::getName, "yun")
                .orderByDesc(TestDO::getAge);
        assertThat(wrapper).isInstanceOf(MPJLambdaWrapperX.class);
        assertThat(wrapper.getCustomSqlSegment()).contains("LIKE").contains("=");
    }
}
