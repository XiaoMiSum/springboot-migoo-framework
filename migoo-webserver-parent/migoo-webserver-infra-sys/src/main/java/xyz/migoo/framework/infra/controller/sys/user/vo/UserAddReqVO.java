package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAddReqVO extends UserBaseVO {

    @NotBlank(message = "{infra.user.password.empty}")
    private String password;
}
