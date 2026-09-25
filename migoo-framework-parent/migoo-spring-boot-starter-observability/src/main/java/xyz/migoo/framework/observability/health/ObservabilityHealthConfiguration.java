package xyz.migoo.framework.observability.health;

import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 健康与信息输出配置
 *
 * <p>向 {@code /actuator/info} 输出框架版本与已装配组件；
 * {@code info} 端点已由环境后处理器默认纳入 actuator 暴露清单（见
 * {@code ObservabilityEnvironmentPostProcessor}）。</p>
 *
 * <p>Actuator 被排除出 classpath 时整个配置不装配。</p>
 *
 * @author xiaomi
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(InfoContributor.class)
public class ObservabilityHealthConfiguration {

    /**
     * 框架信息输出 Bean
     *
     * @return 框架 InfoContributor
     */
    @Bean
    @ConditionalOnMissingBean(MigooFrameworkInfoContributor.class)
    public MigooFrameworkInfoContributor migooFrameworkInfoContributor() {
        return new MigooFrameworkInfoContributor();
    }

}
