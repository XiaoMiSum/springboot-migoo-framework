package xyz.migoo.framework.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link NumberConstants} 的单元测试
 *
 * @author xiaomi
 */
class NumberConstantsTest {

    @Test
    void constants_shouldHaveExpectedIntegerValues() {
        // 校验全部数字常量
        assertThat(NumberConstants.N_0).isEqualTo(0);
        assertThat(NumberConstants.N_1).isEqualTo(1);
        assertThat(NumberConstants.N_2).isEqualTo(2);
        assertThat(NumberConstants.N_3).isEqualTo(3);
        assertThat(NumberConstants.N_4).isEqualTo(4);
        assertThat(NumberConstants.N_5).isEqualTo(5);
        assertThat(NumberConstants.N_6).isEqualTo(6);
        assertThat(NumberConstants.N_7).isEqualTo(7);
        assertThat(NumberConstants.N_10).isEqualTo(10);
        assertThat(NumberConstants.N_11).isEqualTo(11);
        assertThat(NumberConstants.N_30).isEqualTo(30);
        assertThat(NumberConstants.N_200).isEqualTo(200);
    }
}
