package xyz.migoo.framework.infra.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色标识枚举
 */
@Getter
@AllArgsConstructor
public enum RoleCodeEnum {

    /**
     * 超级管理员
     */
    SUPER_ADMIN("super_admin");

    /**
     * 角色编码
     */
    private final String key;

}
