package xyz.migoo.framework.infra.controller.login.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import xyz.migoo.framework.common.util.json.JsonUtils;

@Data
public class AuthLoginReqVO {

    @NotBlank(message = "{infra.login.username.empty}")
    private String username;

    @NotBlank(message = "{infra.login.password.empty}")
    private String password;

    private String code;

    private String uuid;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }
}
