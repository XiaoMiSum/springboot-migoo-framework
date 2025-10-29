package xyz.migoo.framework.infra.controller.sys.permission.role.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleUpdateReqVO extends RoleBaseVO {

    @NotNull(message = "{infra.role.id.empty}")
    private Long id;

    @NotNull(message = "{infra.role.status.empty}")
    @Min(value = 1, message = "{infra.role.status.invalid}")
    @Max(value = 2, message = "{infra.role.status.invalid}")
    private Integer status;
}
