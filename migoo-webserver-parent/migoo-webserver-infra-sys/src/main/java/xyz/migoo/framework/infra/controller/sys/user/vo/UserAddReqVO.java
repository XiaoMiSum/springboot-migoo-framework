package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAddReqVO extends UserBaseVO {

    @NotBlank(message = "登录密码不能为空")
    private String password;
}
