package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPasswordReqVO {

    @NotNull(message = "用户编号不能为空")
    private Long id;

    @NotBlank(message = "登录密码不能为空")
    private String password;
}
