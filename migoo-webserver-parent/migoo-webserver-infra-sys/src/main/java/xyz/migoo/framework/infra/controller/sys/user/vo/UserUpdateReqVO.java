package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserUpdateReqVO extends UserBaseVO {

    @NotNull(message = "{infra.user.id.empty}")
    private Long id;

    @NotNull(message = "{infra.user.status.empty}")
    private Integer status;
}
