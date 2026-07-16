package xyz.migoo.framework.common.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 泛型类型解析工具类
 *
 * @author migoo
 * @since 1.3.16
 */
public final class TypeUtils {

    private TypeUtils() {
    }

    /**
     * 获取泛型参数类型
     *
     * @param clazz 类
     * @param <T>   泛型类型
     * @return 泛型参数的 Class 对象
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getTypeArgument(Class<?> clazz) {
        return (Class<T>) getTypeArgument(clazz, 0);
    }

    /**
     * 获取指定位置的泛型参数类型
     *
     * @param clazz 类
     * @param index 泛型参数位置（从0开始）
     * @return 泛型参数的 Type
     */
    public static Type getTypeArgument(Class<?> clazz, int index) {
        Type superClass = clazz.getGenericSuperclass();
        if (superClass instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (index < typeArguments.length) {
                return typeArguments[index];
            }
        }
        // 尝试从父类获取
        Class<?> parent = clazz.getSuperclass();
        if (parent != null && parent != Object.class) {
            return getTypeArgument(parent, index);
        }
        return null;
    }

    /**
     * 检查类是否有泛型参数
     */
    public static boolean hasTypeArgument(Class<?> clazz) {
        try {
            Type superClass = clazz.getGenericSuperclass();
            return superClass instanceof ParameterizedType;
        } catch (Exception e) {
            return false;
        }
    }
}
