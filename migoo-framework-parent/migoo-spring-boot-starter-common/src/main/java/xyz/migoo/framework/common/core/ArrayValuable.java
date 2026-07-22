package xyz.migoo.framework.common.core;

import java.util.Collection;

/**
 * 可生成 Collection 的枚举接口
 *
 * @author xiaomi
 */
public interface ArrayValuable {

    /**
     * @return 枚举值集合
     */
    Collection<?> toCollection();

}
