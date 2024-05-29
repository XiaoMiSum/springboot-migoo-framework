package xyz.migoo.framework.infra.controller.sys.permission.role.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleUpdateReqVO extends RoleBaseVO {

    @NotNull(message = "角色编号不能为空")
    private Long id;

    @NotNull(message = "更新状态不能为空")
    @Min(value = 1, message = "更新状态错误")
    @Max(value = 2, message = "更新状态错误")
    private Integer status;
}
