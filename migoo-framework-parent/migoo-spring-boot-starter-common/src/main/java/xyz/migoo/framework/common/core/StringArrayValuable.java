package xyz.migoo.framework.common.core;

import java.util.Arrays;
import java.util.Collection;

/**
 * 可生成 String 数组的接口
 *
 * @author xiaomi
 */
public interface StringArrayValuable extends ArrayValuable {

    /**
     * @return String 数组
     */
    String[] array();

    @Override
    default Collection<?> toCollection() {
        return Arrays.asList(array());
    }

}
