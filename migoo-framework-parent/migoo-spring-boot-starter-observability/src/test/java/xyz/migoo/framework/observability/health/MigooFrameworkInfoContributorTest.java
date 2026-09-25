package xyz.migoo.framework.observability.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MigooFrameworkInfoContributor} 单元测试
 *
 * <p>验证 {@code /actuator/info} 输出：版本号已注入、模块按 classpath 探测
 * （本模块测试类路径只有 common 与 observability）。</p>
 */
class MigooFrameworkInfoContributorTest {

    @Test
    @SuppressWarnings("unchecked")
    void contributesVersionAndDetectedModules() {
        Info.Builder builder = new Info.Builder();
        new MigooFrameworkInfoContributor().contribute(builder);
        Info info = builder.build();

        Object detail = info.getDetails().get("migoo");
        assertThat(detail).isInstanceOf(Map.class);
        Map<?, ?> migoo = (Map<?, ?>) detail;

        // 构建时注入的版本（不允许是未过滤的 ${...} 占位符）
        assertThat((String) migoo.get("version")).isNotBlank().doesNotStartWith("${");

        // 测试类路径: common + observability 在，web 不在
        assertThat((List<String>) migoo.get("modules"))
                .contains("common", "observability")
                .doesNotContain("web");
    }

}
