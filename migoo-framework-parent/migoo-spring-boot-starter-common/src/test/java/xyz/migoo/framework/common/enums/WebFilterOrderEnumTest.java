package xyz.migoo.framework.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link WebFilterOrderEnum} 的单元测试
 *
 * @author xiaomi
 */
class WebFilterOrderEnumTest {

    @Test
    void filterOrders_shouldHaveExpectedValues() {
        // 各过滤器顺序常量
        assertThat(WebFilterOrderEnum.CORS_FILTER).isEqualTo(Integer.MIN_VALUE);
        assertThat(WebFilterOrderEnum.TRACE_FILTER).isEqualTo(Integer.MIN_VALUE + 1);
        assertThat(WebFilterOrderEnum.REQUEST_BODY_CACHE_FILTER).isEqualTo(Integer.MIN_VALUE + 500);
        assertThat(WebFilterOrderEnum.API_ACCESS_LOG_FILTER).isEqualTo(-104);
        assertThat(WebFilterOrderEnum.DEMO_FILTER).isEqualTo(Integer.MAX_VALUE);
    }
}
