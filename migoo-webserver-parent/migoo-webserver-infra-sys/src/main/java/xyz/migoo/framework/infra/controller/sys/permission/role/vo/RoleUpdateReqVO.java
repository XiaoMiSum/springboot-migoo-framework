package xyz.migoo.framework.infra.controller.sys.permission.role.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleUpdateReqVO extends RoleBaseVO {

    @NotNull(message = "角色编号不能为空")
    private Long id;
}
