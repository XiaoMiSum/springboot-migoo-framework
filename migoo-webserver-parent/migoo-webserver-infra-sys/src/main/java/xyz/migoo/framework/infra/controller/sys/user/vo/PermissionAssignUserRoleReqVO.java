package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
public class PermissionAssignUserRoleReqVO {

    private final Set<Long> roleIds = Collections.emptySet();
    @NotNull(message = "{infra.user.id.empty}")
    private Long userId;

}