package xyz.migoo.framework.common.util.object;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import xyz.migoo.framework.common.pojo.PageParam;
import xyz.migoo.framework.common.pojo.SortField;
import xyz.migoo.framework.common.pojo.SortablePageParam;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

import static java.util.Collections.singletonList;

/**
 * {@link PageParam} 工具类
 */
public class PageUtils {

    private static final Object[] ORDER_TYPES = new String[]{SortField.ORDER_ASC, SortField.ORDER_DESC};

    public static int getStart(PageParam pageParam) {
        return (pageParam.getPageNo() - 1) * pageParam.getPageSize();
    }

    /**
     * 构建排序字段（默认倒序）
     *
     * @param func 排序字段的 Lambda 表达式
     * @param <T>  排序字段所属的类型
     * @return 排序字段
     */
    public static <T> SortField buildSortingField(Function<T, ?> func) {
        return buildSortingField(func, SortField.ORDER_DESC);
    }

    /**
     * 构建排序字段
     *
     * @param func  排序字段的 Lambda 表达式
     * @param order 排序类型 {@link SortField#ORDER_ASC} {@link SortField#ORDER_DESC}
     * @param <T>   排序字段所属的类型
     * @return 排序字段
     */
    public static <T> SortField buildSortingField(Function<T, ?> func, String order) {
        Assert.isTrue(Arrays.asList(ORDER_TYPES).contains(order),
                String.format("字段的排序类型只能是 %s/%s", ORDER_TYPES));

        String fieldName = getFieldName(func);
        return new SortField(fieldName, order);
    }

    /**
     * 构建默认的排序字段
     * 如果排序字段为空，则设置排序字段；否则忽略
     *
     * @param sortablePageParam 排序分页查询参数
     * @param func              排序字段的 Lambda 表达式
     * @param <T>               排序字段所属的类型
     */
    public static <T> void buildDefaultSortingField(SortablePageParam sortablePageParam, Function<T, ?> func) {
        if (sortablePageParam != null && CollectionUtils.isEmpty(sortablePageParam.getSortingFields())) {
            sortablePageParam.setSortingFields(singletonList(buildSortingField(func)));
        }
    }

    /**
     * 从 Lambda 表达式获取字段名
     */
    @SuppressWarnings("unchecked")
    private static <T> String getFieldName(Function<T, ?> func) {
        try {
            // 获取序列化的 lambda
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            // 获取方法名，格式为 "getXxx" 或 "isXxx"
            String methodName = serializedLambda.getImplMethodName();
            // 移除 get 或 is 前缀
            if (methodName.startsWith("get")) {
                return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            } else if (methodName.startsWith("is")) {
                return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
            }
            return methodName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field name from lambda", e);
        }
    }

}
