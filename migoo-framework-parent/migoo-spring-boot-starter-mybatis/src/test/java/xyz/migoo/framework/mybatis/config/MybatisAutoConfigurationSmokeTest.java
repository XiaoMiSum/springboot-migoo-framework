package xyz.migoo.framework.mybatis.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MybatisAutoConfiguration} 冒烟测试：验证默认插件/填充器可装配、应用自定义 Bean 可覆盖。
 *
 * <p>不连接真实数据库：本配置仅注册与连接无关的装配型 Bean。</p>
 */
class MybatisAutoConfigurationSmokeTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MybatisAutoConfiguration.class));

    @Test
    void defaultBeansAreRegistered() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(MybatisPlusInterceptor.class);
            assertThat(context).hasSingleBean(MetaObjectHandler.class);
            assertThat(context).hasSingleBean(ConfigurationCustomizer.class);
        });
    }

    @Test
    void userBeansOverrideDefaults() {
        MybatisPlusInterceptor customInterceptor = new MybatisPlusInterceptor();
        MetaObjectHandler customHandler = new MetaObjectHandler() {
            @Override
            public void insertFill(org.apache.ibatis.reflection.MetaObject metaObject) {
            }

            @Override
            public void updateFill(org.apache.ibatis.reflection.MetaObject metaObject) {
            }
        };
        runner.withBean("customInterceptor", MybatisPlusInterceptor.class, () -> customInterceptor)
                .withBean("customMetaObjectHandler", MetaObjectHandler.class, () -> customHandler)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    // 应用自定义 Bean 优先，框架默认值按 @ConditionalOnMissingBean 退让
                    assertThat(context.getBeansOfType(MybatisPlusInterceptor.class)).hasSize(1);
                    assertThat(context.getBean(MybatisPlusInterceptor.class)).isSameAs(customInterceptor);
                    assertThat(context.getBeansOfType(MetaObjectHandler.class)).hasSize(1);
                    assertThat(context.getBean(MetaObjectHandler.class)).isSameAs(customHandler);
                });
    }
}
