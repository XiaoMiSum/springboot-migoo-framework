package xyz.migoo.framework.infra.controller.sys.permission.role.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleBaseVO {

    @NotBlank(message = "{infra.role.name.empty}")
    @Size(max = 30, message = "{infra.role.name.size.max}")
    private String name;

    @NotBlank(message = "{infra.role.code.empty}")
    @Size(max = 100, message = "{infra.role.code.size.max}")
    private String code;

    @NotNull(message = "{common.sort.null}")
    private Integer sort;

    private String remark;

}