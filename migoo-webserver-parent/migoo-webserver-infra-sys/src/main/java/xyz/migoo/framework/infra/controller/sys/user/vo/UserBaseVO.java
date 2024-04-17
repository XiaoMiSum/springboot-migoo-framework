package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public abstract class UserBaseVO {

    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    private String name;

    private Long deptId;

    private Set<Long> postIds;

    private String email;

    private String phone;

    private Integer gender;

    private String avatar;
}
