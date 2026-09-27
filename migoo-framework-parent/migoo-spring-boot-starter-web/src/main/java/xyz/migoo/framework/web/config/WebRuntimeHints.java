package xyz.migoo.framework.web.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import xyz.migoo.framework.common.exception.ErrorCode;
import xyz.migoo.framework.common.pojo.PageParam;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;

/**
 * Web 组件 GraalVM native image / AOT 运行时线索
 *
 * <p>native image 下反射与资源访问默认不可用，Spring AOT 处理时收集本线索，
 * 使统一响应体在 native 运行时仍可被 Jackson 正常序列化/反序列化、i18n 资源束可加载。</p>
 *
 * <p>由 {@link MiGooWebAutoConfiguration} 通过 {@code @ImportRuntimeHints} 引入，
 * 应用无需任何额外配置即可生效（Boot 对配置类自动配置的 AOT 处理会级联收集）。</p>
 *
 * @author xiaomi
 */
public class WebRuntimeHints implements RuntimeHintsRegistrar {

    /**
     * Jackson 成员访问约定：可调用构造器（反序列化 new 实例）+ 公开方法（getter/setter/record accessor）
     * + 字段读写（直接字段访问绑定）
     */
    public static final MemberCategory[] JACKSON_MEMBERS = {
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.ACCESS_DECLARED_FIELDS
    };

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // 统一响应体：应用所有响应均经 Jackson 序列化 Result / PageResult / 分页入参
        hints.reflection().registerType(Result.class, JACKSON_MEMBERS);
        hints.reflection().registerType(PageResult.class, JACKSON_MEMBERS);
        hints.reflection().registerType(PageParam.class, JACKSON_MEMBERS);
        // ErrorCode 为 record，错误码常量若直接出现在响应体中需 record 构造器与 accessor
        hints.reflection().registerType(ErrorCode.class, JACKSON_MEMBERS);

        // i18n 资源束：Boot 默认 basename=messages，框架文档约定 i18n/ 目录放多语言文件
        hints.resources().registerPattern("messages*.properties");
        hints.resources().registerPattern("i18n/*.properties");
    }
}
