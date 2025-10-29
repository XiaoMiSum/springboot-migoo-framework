package xyz.migoo.framework.infra.controller.sys.dept.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import xyz.migoo.framework.common.validation.Email;

@Data
public abstract class DeptBaseVO {

    @NotBlank(message = "{infra.dept.name.empty}")
    @Size(max = 30, message = "{infra.dept.name.size.max}")
    private String name;

    private Long parentId;

    @NotNull(message = "{common.sort.null}")
    private Integer sort;

    private Long leaderUserId;

    @Email(message = "{infra.user.email.invalid}")
    @Size(max = 50, message = "{infra.user.email.size.max")
    private String email;

}