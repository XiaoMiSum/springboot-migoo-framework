package xyz.migoo.framework.infra.controller.sys.permission.menu.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuUpdateReqVO extends MenuBaseVO {

    @NotNull(message = "菜单编号不能为空")
    private Integer id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
