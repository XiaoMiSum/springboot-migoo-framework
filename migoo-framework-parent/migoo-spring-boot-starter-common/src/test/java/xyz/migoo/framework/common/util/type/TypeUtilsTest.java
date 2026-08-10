package xyz.migoo.framework.common.util.type;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TypeUtils} 单元测试
 */
class TypeUtilsTest {

    // ========== getTypeArgument ==========

    @Test
    void getTypeArgument_single() {
        assertThat(TypeUtils.getTypeArgument(StringChild.class)).isEqualTo(String.class);
    }

    @Test
    void getTypeArgument_nonParameterized() {
        // 未参数化的类，沿父链回溯到 Object -> null
        assertThat(TypeUtils.getTypeArgument(Base.class)).isNull();
    }

    @Test
    void getTypeArgument_byIndex() {
        Type arg0 = TypeUtils.getTypeArgument(IntStrPair.class, 0);
        assertThat(arg0).isEqualTo(Integer.class);

        Type arg1 = TypeUtils.getTypeArgument(IntStrPair.class, 1);
        assertThat(arg1).isEqualTo(String.class);

        // 无 index 重载等价于 index 0
        assertThat(TypeUtils.getTypeArgument(IntStrPair.class)).isEqualTo(Integer.class);
    }

    @Test
    void getTypeArgument_indexOutOfBounds() {
        // index 2 超出 Pair 的类型参数个数，回溯父类链（Pair 未参数化 -> Object）-> null
        assertThat(TypeUtils.getTypeArgument(IntStrPair.class, 2)).isNull();
    }

    // ========== hasTypeArgument ==========

    @Test
    void hasTypeArgument_true() {
        assertThat(TypeUtils.hasTypeArgument(StringChild.class)).isTrue();
    }

    @Test
    void hasTypeArgument_false() {
        assertThat(TypeUtils.hasTypeArgument(String.class)).isFalse();
    }

    // ========== 测试用泛型类 ==========

    abstract static class Base<T> {
    }

    static class StringChild extends Base<String> {
    }

    abstract static class Pair<K, V> {
    }

    static class IntStrPair extends Pair<Integer, String> {
    }

}
