package xyz.migoo.framework.mybatis.core;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class LambdaUpdateWrapperXTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), TestDO.class);
    }

    @Test
    void setAppendsColumn() {
        LambdaUpdateWrapperX<TestDO> wrapper = new LambdaUpdateWrapperX<>();
        wrapper.set(TestDO::getName, "x");
        assertThat(wrapper.getSqlSet()).contains("name");
    }

    @Test
    void setSqlAppendsRawSql() {
        LambdaUpdateWrapperX<TestDO> wrapper = new LambdaUpdateWrapperX<>();
        wrapper.setSql("name = 'x'");
        assertThat(wrapper.getSqlSet()).contains("name = 'x'");
    }

    @Test
    void likeInIfPresentSkipNullAndBlank() {
        assertThat(new LambdaUpdateWrapperX<TestDO>().likeIfPresent(TestDO::getName, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaUpdateWrapperX<TestDO>().likeIfPresent(TestDO::getName, "  ").getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaUpdateWrapperX<TestDO>().inIfPresent(TestDO::getAge, Collections.emptyList()).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaUpdateWrapperX<TestDO>().inIfPresent(TestDO::getAge, (Object[]) new Integer[0]).getCustomSqlSegment()).isEmpty();
    }

    @Test
    void likeInIfPresentAppendWhenPresent() {
        LambdaUpdateWrapperX<TestDO> wrapper = new LambdaUpdateWrapperX<TestDO>()
                .likeIfPresent(TestDO::getName, "yun")
                .inIfPresent(TestDO::getAge, Arrays.asList(1, 2));
        assertThat(wrapper.getCustomSqlSegment()).contains("LIKE").contains("IN");
        assertThat(wrapper.getParamNameValuePairs()).containsValue("%yun%");
    }

    @Test
    void notInIfPresent() {
        assertThat(new LambdaUpdateWrapperX<TestDO>().notInIfPresent(TestDO::getAge, Arrays.asList(1, 2))
                .getCustomSqlSegment()).contains("NOT IN");
    }

    @Test
    void comparisonIfPresent() {
        assertThat(new LambdaUpdateWrapperX<TestDO>().eqIfPresent(TestDO::getName, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaUpdateWrapperX<TestDO>().neIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaUpdateWrapperX<TestDO>().gtIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaUpdateWrapperX<TestDO>().geIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaUpdateWrapperX<TestDO>().ltIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();
        assertThat(new LambdaUpdateWrapperX<TestDO>().leIfPresent(TestDO::getAge, null).getCustomSqlSegment()).isEmpty();

        assertThat(new LambdaUpdateWrapperX<TestDO>().eqIfPresent(TestDO::getName, "yun").getCustomSqlSegment()).contains("=");
        assertThat(new LambdaUpdateWrapperX<TestDO>().geIfPresent(TestDO::getAge, 18).getCustomSqlSegment()).contains(">=");
        assertThat(new LambdaUpdateWrapperX<TestDO>().leIfPresent(TestDO::getAge, 65).getCustomSqlSegment()).contains("<=");
    }

    @Test
    void betweenIfPresent() {
        assertThat(new LambdaUpdateWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, 18, 65).getCustomSqlSegment()).contains("BETWEEN");
        assertThat(new LambdaUpdateWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, 18, null).getCustomSqlSegment()).contains(">=");
        assertThat(new LambdaUpdateWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, null, 65).getCustomSqlSegment()).contains("<=");
        assertThat(new LambdaUpdateWrapperX<TestDO>().betweenIfPresent(TestDO::getAge, null, null).getCustomSqlSegment()).isEmpty();
    }
}
