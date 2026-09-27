package xyz.migoo.framework.mybatis.config;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.context.annotation.ImportRuntimeHints;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MybatisRuntimeHints} 单元测试：验证框架内置 TypeHandler 的反射实例化线索与自动配置接线
 */
class MybatisRuntimeHintsTest {

    private final RuntimeHints hints = new RuntimeHints();

    private RuntimeHints registered() {
        new MybatisRuntimeHints().registerHints(hints, getClass().getClassLoader());
        return hints;
    }

    @Test
    void allFrameworkTypeHandlersRegisteredForInstantiation() {
        registered();
        // MyBatis 通过反射 newInstance 创建 TypeHandler，必须登记公开构造器
        assertThat(MybatisRuntimeHints.REFLECTIVE_INSTANTIATED).isNotEmpty();
        MybatisRuntimeHints.REFLECTIVE_INSTANTIATED.forEach(type -> {
            assertThat(hints.reflection().getTypeHint(type)).isNotNull();
            assertThat(hints.reflection().getTypeHint(type).getMemberCategories())
                    .contains(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
        });
    }

    @Test
    void autoConfigurationImportsRuntimeHints() {
        ImportRuntimeHints annotation = MybatisAutoConfiguration.class.getAnnotation(ImportRuntimeHints.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains(MybatisRuntimeHints.class);
    }
}
