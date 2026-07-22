package xyz.migoo.framework.common.core;

import java.util.Collection;
import java.util.stream.IntStream;

/**
 * 可生成 Int 数组的接口
 *
 * @author xiaomi
 */
public interface IntArrayValuable extends ArrayValuable {

    /**
     * @return int 数组
     */
    int[] array();

    @Override
    default Collection<?> toCollection() {
        return IntStream.of(array()).boxed().toList();
    }

}
