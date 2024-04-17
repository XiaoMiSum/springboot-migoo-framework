package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
public class PermissionAssignUserRoleReqVO {

    @NotNull(message = "用户编号不能为空")
    private Long userId;

    private final Set<Long> roleIds = Collections.emptySet();

}