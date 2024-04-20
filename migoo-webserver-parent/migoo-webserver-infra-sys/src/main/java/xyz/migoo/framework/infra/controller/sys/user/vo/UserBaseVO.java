package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public abstract class UserBaseVO {

    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    private String name;

    @Size(max = 30, message = "登录名长度不能超过64个字符")
    @NotEmpty(message = "登录名不能为空")
    private String username;

    private Long deptId;

    private Set<Long> postIds;

    private String email;

    private String phone;

    private Integer gender;

    private String avatar;
}
