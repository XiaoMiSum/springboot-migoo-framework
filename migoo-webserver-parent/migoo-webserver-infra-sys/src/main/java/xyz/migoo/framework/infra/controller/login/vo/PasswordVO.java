package xyz.migoo.framework.infra.controller.login.vo;

import lombok.Data;

@Data
public class PasswordVO {

    private String oldPassword;

    private String newPassword;

    private Long id;

}
