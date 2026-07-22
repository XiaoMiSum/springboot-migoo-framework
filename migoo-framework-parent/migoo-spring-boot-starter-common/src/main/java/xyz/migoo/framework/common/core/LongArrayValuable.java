package xyz.migoo.framework.common.core;

import java.util.Collection;
import java.util.stream.LongStream;

/**
 * 可生成 Long 数组的接口
 *
 * @author xiaomi
 */
public interface LongArrayValuable extends ArrayValuable {

    /**
     * @return long 数组
     */
    long[] array();

    @Override
    default Collection<?> toCollection() {
        return LongStream.of(array()).boxed().toList();
    }

}
