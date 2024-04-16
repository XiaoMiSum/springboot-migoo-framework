package xyz.migoo.framework.infra.controller.sys.dept.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import xyz.migoo.framework.common.validation.Email;

@Data
public abstract class DeptBaseVO {

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 30, message = "部门名称长度不能超过30个字符")
    private String name;

    private Long parentId;

    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    private Long leaderUserId;

    @Email
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

}