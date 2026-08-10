package xyz.migoo.framework.websocket.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link WebSocketProperties} 单元测试
 */
class WebSocketPropertiesTest {

    @Test
    void defaultValuesMatchDocumentation() {
        WebSocketProperties properties = new WebSocketProperties();
        // 默认值：启用、单机模式、单端点 /ws、允许所有来源、Authorization + Bearer 前缀、超时 30 分钟
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.isDistributed()).isFalse();
        assertThat(properties.getEndpoint()).isEqualTo("/ws");
        assertThat(properties.getAllowedOrigins()).isEqualTo("*");
        assertThat(properties.getTokenHeader()).isEqualTo("Authorization");
        assertThat(properties.getTokenPrefix()).isEqualTo("Bearer ");
        assertThat(properties.getMaxSessionTimeout()).isEqualTo(1800000L);
    }

    @Test
    void getAllEndpointsReturnsDefaultEndpointWhenEndpointsEmpty() {
        WebSocketProperties properties = new WebSocketProperties();
        // endpoints 默认为空列表 -> 回退到单端点 endpoint
        assertThat(properties.getEndpoints()).isEmpty();
        assertThat(properties.getAllEndpoints()).containsExactly("/ws");
    }

    @Test
    void getAllEndpointsPrefersConfiguredEndpointsList() {
        WebSocketProperties properties = new WebSocketProperties();
        properties.setEndpoint("/ws");
        properties.setEndpoints(Arrays.asList("/ws/chat", "/ws/notify"));
        // endpoints 非空时优先返回列表，忽略单端点 endpoint
        assertThat(properties.getAllEndpoints()).containsExactly("/ws/chat", "/ws/notify");
    }

    @Test
    void getAllEndpointsReturnsDefaultEndpointWhenEndpointsNull() {
        WebSocketProperties properties = new WebSocketProperties();
        properties.setEndpoints(null);
        // endpoints 为 null 时同样回退到单端点
        assertThat(properties.getAllEndpoints()).containsExactly("/ws");
    }

    @Test
    void setterBindingUpdatesFields() {
        WebSocketProperties properties = new WebSocketProperties();
        properties.setEnabled(false);
        properties.setDistributed(true);
        properties.setEndpoint("/ws/custom");
        properties.setAllowedOrigins("https://example.com,https://foo.com");
        properties.setTokenHeader("X-Token");
        properties.setTokenPrefix("Token ");
        properties.setMaxSessionTimeout(60000L);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.isDistributed()).isTrue();
        assertThat(properties.getEndpoint()).isEqualTo("/ws/custom");
        assertThat(properties.getAllowedOrigins()).isEqualTo("https://example.com,https://foo.com");
        assertThat(properties.getTokenHeader()).isEqualTo("X-Token");
        assertThat(properties.getTokenPrefix()).isEqualTo("Token ");
        assertThat(properties.getMaxSessionTimeout()).isEqualTo(60000L);
    }

    @Test
    void getAllEndpointsReturnsUnmodifiableListForEndpoints() {
        WebSocketProperties properties = new WebSocketProperties();
        properties.setEndpoints(List.of("/ws/a", "/ws/b"));
        List<String> endpoints = properties.getAllEndpoints();
        // 返回的就是配置的列表本身
        assertThat(endpoints).containsExactly("/ws/a", "/ws/b");
        assertThat(properties.getEndpoints()).containsExactly("/ws/a", "/ws/b");
    }
}
