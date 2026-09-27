package xyz.migoo.framework.redis.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * {@link RedisAutoConfiguration} 冒烟测试：验证默认模板可装配、应用自定义 Bean 可覆盖。
 *
 * <p>不连接真实 Redis：仅需 {@link RedisConnectionFactory} 类型存在即可完成装配验证。</p>
 */
class RedisAutoConfigurationSmokeTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class))
            .withBean(RedisConnectionFactory.class, () -> mock(RedisConnectionFactory.class));

    @Test
    void defaultRedisTemplateIsRegistered() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(RedisTemplate.class);
            RedisTemplate<String, Object> template = context.getBean(RedisTemplate.class);
            // 默认模板为 @Primary，按类型注入时唯一命中
            assertThat(context.getBean(RedisTemplate.class)).isSameAs(template);
        });
    }

    @Test
    void userRedisTemplateOverridesDefault() {
        RedisTemplate<String, Object> custom = new RedisTemplate<>();
        // RedisTemplate 是 InitializingBean，需先挂连接工厂才能通过 afterPropertiesSet 校验
        custom.setConnectionFactory(mock(RedisConnectionFactory.class));
        runner.withBean("redisTemplate", RedisTemplate.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    // 应用按名称自定义 redisTemplate 后，默认模板整体回退（@ConditionalOnMissingBean(name)）
                    assertThat(context.getBeansOfType(RedisTemplate.class)).hasSize(1);
                    assertThat(context.getBean("redisTemplate")).isSameAs(custom);
                });
    }
}
