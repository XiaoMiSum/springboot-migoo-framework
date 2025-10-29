package xyz.migoo.framework.infra.controller.sys.permission.menu.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public abstract class MenuBaseVO {

    @NotBlank(message = "{infra.menu.name.empty}")
    @Size(max = 50, message = "{infra.menu.name.size.max}")
    private String name;

    @Size(max = 100, message = "{infra.menu.permission.size.max}")
    private String permission;

    @NotNull(message = "{infra.menu.type.empty}")
    private Integer type;

    @NotNull(message = "{common.sort.null}")
    private Integer sort;

    private Long parentId;

    @Size(max = 200, message = "{infra.menu.path.size.max}")
    private String path;

    private String icon;

    @Size(max = 50, message = "{infra.menu.component.name.size.max}")
    private String componentName;

    @Size(max = 200, message = "{infra.menu.component.size.max}")
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
