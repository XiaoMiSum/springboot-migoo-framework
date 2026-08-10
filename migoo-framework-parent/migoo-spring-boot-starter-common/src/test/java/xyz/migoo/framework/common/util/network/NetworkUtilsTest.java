package xyz.migoo.framework.common.util.network;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * {@link NetworkUtils} 单元测试
 *
 * @author migoo
 */
class NetworkUtilsTest {

    // ========== isUnknown ==========

    @Test
    void isUnknown_null_shouldReturnTrue() {
        assertThat(NetworkUtils.isUnknown(null)).isTrue();
    }

    @Test
    void isUnknown_empty_shouldReturnTrue() {
        assertThat(NetworkUtils.isUnknown("")).isTrue();
    }

    @Test
    void isUnknown_lowerCase_shouldReturnTrue() {
        assertThat(NetworkUtils.isUnknown("unknown")).isTrue();
    }

    @Test
    void isUnknown_upperCase_shouldReturnTrue() {
        assertThat(NetworkUtils.isUnknown("UNKNOWN")).isTrue();
    }

    @Test
    void isUnknown_realIp_shouldReturnFalse() {
        assertThat(NetworkUtils.isUnknown("192.168.1.1")).isFalse();
    }

    // ========== 本机信息 ==========

    @Test
    void getLocalHost_shouldReturnNonEmptyStringWithoutException() {
        // 本机网络环境不确定，只断言不抛异常且结果非空
        String localHost = assertDoesNotThrow(NetworkUtils::getLocalHost);
        assertThat(localHost).isNotEmpty();
    }

    @Test
    void getHostname_shouldNotThrowOrReturnNull() {
        // 部分环境可能返回空串，只断言不抛异常且结果不为 null
        String hostname = assertDoesNotThrow(NetworkUtils::getHostname);
        assertThat(hostname).isNotNull();
    }

    @Test
    void getIpAddress_shouldNotThrowOrReturnNull() {
        String ipAddress = assertDoesNotThrow(NetworkUtils::getIpAddress);
        assertThat(ipAddress).isNotNull();
    }

    // ========== getClientIp ==========

    @Test
    void getClientIp_firstNonUnknownProxyIp_shouldReturnIt() {
        assertThat(NetworkUtils.getClientIp("1.2.3.4", "5.6.7.8, 9.9.9.9")).isEqualTo("5.6.7.8");
    }

    @Test
    void getClientIp_shouldSkipUnknownProxyIps() {
        assertThat(NetworkUtils.getClientIp("1.2.3.4", "unknown, 9.9.9.9")).isEqualTo("9.9.9.9");
    }

    @Test
    void getClientIp_nullOtherProxyIps_shouldReturnIp() {
        assertThat(NetworkUtils.getClientIp("1.2.3.4", null)).isEqualTo("1.2.3.4");
    }

    @Test
    void getClientIp_unknownIp_shouldReturnIpDirectly() {
        // ip 本身是 unknown 时直接返回，不解析代理列表
        assertThat(NetworkUtils.getClientIp("unknown", "5.6.7.8")).isEqualTo("unknown");
    }

    // ========== getMultistageReverseProxyIp ==========

    @Test
    void getMultistageReverseProxyIp_lastNonUnknownProxyIp_shouldReturnIt() {
        assertThat(NetworkUtils.getMultistageReverseProxyIp("1.2.3.4", "5.6.7.8, 9.9.9.9")).isEqualTo("9.9.9.9");
    }

    @Test
    void getMultistageReverseProxyIp_shouldSkipTrailingUnknown() {
        assertThat(NetworkUtils.getMultistageReverseProxyIp("1.2.3.4", "5.6.7.8, unknown")).isEqualTo("5.6.7.8");
    }

    @Test
    void getMultistageReverseProxyIp_nullOtherProxyIps_shouldReturnIp() {
        assertThat(NetworkUtils.getMultistageReverseProxyIp("1.2.3.4", null)).isEqualTo("1.2.3.4");
    }

    // ========== isInternalIp ==========

    @Test
    void isInternalIp_loopback_shouldReturnTrue() {
        assertThat(NetworkUtils.isInternalIp("127.0.0.1")).isTrue();
    }

    @Test
    void isInternalIp_10Range_shouldReturnTrue() {
        assertThat(NetworkUtils.isInternalIp("10.0.0.1")).isTrue();
    }

    @Test
    void isInternalIp_192168Range_shouldReturnTrue() {
        assertThat(NetworkUtils.isInternalIp("192.168.1.1")).isTrue();
    }

    @Test
    void isInternalIp_17216Range_shouldReturnTrue() {
        assertThat(NetworkUtils.isInternalIp("172.16.0.1")).isTrue();
    }

    @Test
    void isInternalIp_publicIp_shouldReturnFalse() {
        assertThat(NetworkUtils.isInternalIp("8.8.8.8")).isFalse();
    }

    @Test
    void isInternalIp_invalidIp_shouldReturnFalse() {
        // 无法解析的主机名会抛 UnknownHostException，被捕获后返回 false
        assertThat(NetworkUtils.isInternalIp("not-an-ip")).isFalse();
    }

    @Test
    void isInternalIp_null_shouldReturnFalse() {
        assertThat(NetworkUtils.isInternalIp(null)).isFalse();
    }

    @Test
    void isInternalIp_empty_shouldReturnFalse() {
        assertThat(NetworkUtils.isInternalIp("")).isFalse();
    }
}
