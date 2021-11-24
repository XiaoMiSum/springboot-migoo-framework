package xyz.migoo.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @author xiaomi
 */
@Getter
@AllArgsConstructor
public enum CommonStatusEnum {

    ENABLE(1, "开启"),
    DISABLE(0, "关闭");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}
