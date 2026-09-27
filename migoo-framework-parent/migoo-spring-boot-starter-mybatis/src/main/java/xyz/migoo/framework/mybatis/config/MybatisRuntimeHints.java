package xyz.migoo.framework.mybatis.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import xyz.migoo.framework.mybatis.core.handler.DefaultFieldHandler;
import xyz.migoo.framework.mybatis.core.handler.EncryptTypeHandler;
import xyz.migoo.framework.mybatis.core.handler.IntegerListTypeHandler;
import xyz.migoo.framework.mybatis.core.handler.LongListTypeHandler;
import xyz.migoo.framework.mybatis.core.handler.StringListTypeHandler;
import xyz.migoo.framework.mybatis.core.handler.UTCLocalDateTimeHandler;
import xyz.migoo.framework.mybatis.core.handler.UUIDTypeHandler;

import java.util.List;

/**
 * MyBatis 组件 GraalVM native image / AOT 运行时线索
 *
 * <p>MyBatis 的 TypeHandler 由 {@code TypeHandlerRegistry} 通过反射 newInstance 创建，
 * native 下需登记公开构造器；此处仅覆盖框架自带的处理器，应用自定义 TypeHandler 由应用自行登记
 * （或使用 {@code @Reflective} 注解交由 AOT 自动收集）。</p>
 *
 * <p>由 {@link MybatisAutoConfiguration} 通过 {@code @ImportRuntimeHints} 引入。</p>
 *
 * @author xiaomi
 */
public class MybatisRuntimeHints implements RuntimeHintsRegistrar {

    /**
     * 框架内置 TypeHandler / 填充器清单（MyBatis 反射实例化）
     */
    static final List<Class<?>> REFLECTIVE_INSTANTIATED = List.of(
            EncryptTypeHandler.class,
            UTCLocalDateTimeHandler.class,
            UUIDTypeHandler.class,
            LongListTypeHandler.class,
            StringListTypeHandler.class,
            IntegerListTypeHandler.class,
            DefaultFieldHandler.class);

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        REFLECTIVE_INSTANTIATED.forEach(type ->
                hints.reflection().registerType(type, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
    }
}
