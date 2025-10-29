package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPasswordReqVO {

    @NotNull(message = "{infra.user.id.empty}")
    private Long id;

    @NotBlank(message = "{infra.user.password.empty}")
    private String password;
}
