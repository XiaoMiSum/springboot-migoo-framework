package xyz.migoo.framework.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ResourcePatternHint;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.context.annotation.ImportRuntimeHints;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.pojo.PageParam;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link WebRuntimeHints} 单元测试：验证 native image 运行时线索的登记内容与自动配置接线
 */
class WebRuntimeHintsTest {

    private final RuntimeHints hints = new RuntimeHints();

    private RuntimeHints registered() {
        new WebRuntimeHints().registerHints(hints, getClass().getClassLoader());
        return hints;
    }

    // ==================== 反射线索 ====================

    @Test
    void responseBodyTypesRegisteredForJacksonAccess() {
        registered();
        // 统一响应体与分页类型：构造器 + 公开方法 + 字段读写（Jackson 三种绑定路径都可用）
        for (Class<?> type : List.of(Result.class, PageResult.class, PageParam.class, ErrorCode.class)) {
            assertThat(hints.reflection().getTypeHint(type)).isNotNull();
            assertThat(hints.reflection().getTypeHint(type).getMemberCategories())
                    .contains(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.ACCESS_DECLARED_FIELDS);
        }
    }

    @Test
    void unrelatedTypeIsNotRegistered() {
        registered();
        // 未登记类型不应凭空出现线索（避免误认为全量反射）
        assertThat(hints.reflection().getTypeHint(WebRuntimeHints.class)).isNull();
    }

    // ==================== 资源线索 ====================

    @Test
    void i18nResourcePatternsRegistered() {
        registered();
        List<String> patterns = hints.resources().resourcePatternHints()
                .flatMap(pattern -> pattern.getIncludes().stream())
                .map(ResourcePatternHint::getPattern)
                .toList();
        assertThat(patterns).contains("messages*.properties", "i18n/*.properties");
    }

    // ==================== 自动配置接线 ====================

    @Test
    void autoConfigurationImportsRuntimeHints() {
        // 线索必须挂在自动配置上，AOT 处理时才会被收集
        ImportRuntimeHints annotation = MiGooWebAutoConfiguration.class.getAnnotation(ImportRuntimeHints.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains(WebRuntimeHints.class);
    }
}
