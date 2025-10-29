package xyz.migoo.framework.infra.controller.sys.permission.menu.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuUpdateReqVO extends MenuBaseVO {

    @NotNull(message = "{infra.menu.id.empty}")
    private Integer id;

    @NotNull(message = "{infra.menu.status.empty}")
    private Integer status;
}
