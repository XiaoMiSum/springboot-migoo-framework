package xyz.migoo.framework.infra.controller.sys.permission.role.vo;

import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
public class PermissionAssignRoleMenuReqVO {

    private Long roleId;

    private final Set<Long> menuIds = Collections.emptySet();

}