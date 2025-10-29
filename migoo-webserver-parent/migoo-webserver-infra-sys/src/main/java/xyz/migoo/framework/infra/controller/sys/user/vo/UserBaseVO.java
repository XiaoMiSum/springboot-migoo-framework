package xyz.migoo.framework.infra.controller.sys.user.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class UserBaseVO {

    @Size(max = 30, message = "{infra.user.nike.name.size.max}")
    private String name;

    @Size(max = 30, message = "{infra.user.username.size.max}")
    @NotEmpty(message = "{infra.user.username.empty}")
    private String username;

    private Long deptId;

    private Long postId;

    private String email;

    private String phone;

    private Integer gender;

    private String avatar;
}
