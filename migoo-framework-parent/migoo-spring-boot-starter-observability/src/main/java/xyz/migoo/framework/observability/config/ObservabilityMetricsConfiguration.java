package xyz.migoo.framework.observability.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 指标通用配置
 *
 * <p>把 {@code migoo.observability.metrics.common-tags} 应用到所有指标
 * （与 Boot 官方 {@code management.metrics.tags.*} 叠加生效），
 * 通用 tag 通常承载 application / env / region 等全局维度。</p>
 *
 * <p>提供 {@link MeterRegistryCustomizer} Bean 并以 {@code @ConditionalOnMissingBean} 保护，
 * 应用可用同类型 Bean 覆盖本实现。</p>
 *
 * @author xiaomi
 */
@Configuration(proxyBeanMethods = false)
public class ObservabilityMetricsConfiguration {

    /**
     * 通用 tag 定制器
     *
     * @param properties 可观测性配置（读取 {@code metrics.common-tags}）
     * @return 应用通用 tag 的 {@link MeterRegistryCustomizer}
     */
    @Bean
    @ConditionalOnMissingBean(MeterRegistryCustomizer.class)
    public MeterRegistryCustomizer<MeterRegistry> migooCommonTagsCustomizer(MigooObservabilityProperties properties) {
        Map<String, String> commonTags = properties.getMetrics().getCommonTags();
        return registry -> commonTags.forEach((key, value) -> registry.config().commonTags(key, value));
    }

}
