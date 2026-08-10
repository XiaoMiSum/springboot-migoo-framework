package xyz.migoo.framework.web.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MigooWebProperties} 单元测试
 *
 * <p>验证 CORS / CacheBody 子配置的默认值以及 setter 绑定。</p>
 */
class MigooWebPropertiesTest {

    @Test
    void corsDefaultsAreApplied() {
        MigooWebProperties properties = new MigooWebProperties();
        MigooWebProperties.Cors cors = properties.getCors();

        assertThat(cors).isNotNull();
        assertThat(cors.isEnabled()).isTrue();
        assertThat(cors.getAllowedOrigins()).containsExactly("*");
        assertThat(cors.getAllowedMethods()).containsExactly("*");
        assertThat(cors.getAllowedHeaders()).containsExactly("*");
        assertThat(cors.isAllowCredentials()).isTrue();
        assertThat(cors.getMaxAge()).isEqualTo(1800L);
    }

    @Test
    void cacheBodyDefaultsAreApplied() {
        MigooWebProperties properties = new MigooWebProperties();
        MigooWebProperties.CacheBody cacheBody = properties.getCacheBody();

        assertThat(cacheBody).isNotNull();
        assertThat(cacheBody.isEnabled()).isTrue();
        // 默认 10MB
        assertThat(cacheBody.getMaxSize()).isEqualTo(10 * 1024 * 1024);
    }

    @Test
    void subConfigsAreIndependentInstancesPerProperties() {
        MigooWebProperties first = new MigooWebProperties();
        MigooWebProperties second = new MigooWebProperties();
        assertThat(first.getCors()).isNotSameAs(second.getCors());
        assertThat(first.getCacheBody()).isNotSameAs(second.getCacheBody());
    }

    @Test
    void corsSettersBindValues() {
        MigooWebProperties.Cors cors = new MigooWebProperties.Cors();
        cors.setEnabled(false);
        cors.setAllowedOrigins(List.of("https://a.com", "https://b.com"));
        cors.setAllowedMethods(List.of("GET", "POST"));
        cors.setAllowedHeaders(List.of("Authorization"));
        cors.setAllowCredentials(false);
        cors.setMaxAge(60L);

        assertThat(cors.isEnabled()).isFalse();
        assertThat(cors.getAllowedOrigins()).containsExactly("https://a.com", "https://b.com");
        assertThat(cors.getAllowedMethods()).containsExactly("GET", "POST");
        assertThat(cors.getAllowedHeaders()).containsExactly("Authorization");
        assertThat(cors.isAllowCredentials()).isFalse();
        assertThat(cors.getMaxAge()).isEqualTo(60L);
    }

    @Test
    void cacheBodySettersBindValues() {
        MigooWebProperties.CacheBody cacheBody = new MigooWebProperties.CacheBody();
        cacheBody.setEnabled(false);
        cacheBody.setMaxSize(2048);

        assertThat(cacheBody.isEnabled()).isFalse();
        assertThat(cacheBody.getMaxSize()).isEqualTo(2048);
    }

    @Test
    void outerSettersOverrideSubConfigs() {
        MigooWebProperties properties = new MigooWebProperties();
        MigooWebProperties.Cors customCors = new MigooWebProperties.Cors();
        customCors.setEnabled(false);
        MigooWebProperties.CacheBody customCacheBody = new MigooWebProperties.CacheBody();
        customCacheBody.setMaxSize(1);

        properties.setCors(customCors);
        properties.setCacheBody(customCacheBody);

        assertThat(properties.getCors()).isSameAs(customCors);
        assertThat(properties.getCacheBody()).isSameAs(customCacheBody);
    }
}
