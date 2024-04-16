package xyz.migoo.framework.infra.controller.sys.permission.menu.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public abstract class MenuBaseVO {

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String name;

    @Size(max = 100)
    private String permission;

    @NotNull(message = "菜单类型不能为空")
    private Integer type;

    @NotNull(message = "显示顺序不能为空")
    private Integer sort;

    @NotNull(message = "父菜单 ID 不能为空")
    private Long parentId;

    @Size(max = 200, message = "路由地址不能超过200个字符")
    private String path;

    private String icon;

    @Size(max = 50, message = "组件名称不能超过50个字符")
    private String componentName;

    @Size(max = 200, message = "组件路径不能超过255个字符")
    private String component;

    /**
     * 是否可见
     */
    private Boolean visible;
    /**
     * 总是显示
     */
    private Boolean alwaysShow;
    /**
     * 是否缓存
     */
    private Boolean keepAlive;
}
