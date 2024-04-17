package xyz.migoo.framework.infra.controller.login.vo;

import lombok.Data;
import xyz.migoo.framework.common.util.json.JsonUtils;

@Data
public class AuthLoginRespVO {

    private String token;

    private Boolean requiredBindAuthenticator;

    @Override
    public String toString() {
        return JsonUtils.toJsonString(this);
    }

}