package xyz.migoo.framework.infra.dal.dataobject.sys;

import xyz.migoo.framework.infra.dal.dataobject.IdEnhanceDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName(value = "sys_menu", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class Menu extends IdEnhanceDO {

    /**
     * 菜单名称
     */
    private String name;
    /**
     * 权限标识
     * <p>
     * 一般格式为：${系统}:${模块}:${操作}
     * 例如说：system:admin:add，即 system 服务的添加管理员。
     * <p>
     * 当我们把该 SysMenuDO 赋予给角色后，意味着该角色有该资源：
     * - 对于后端，配合 @PreAuthorize 注解，配置 API 接口需要该权限，从而对 API 接口进行权限控制。
     * - 对于前端，配合前端标签，配置按钮是否展示，避免用户没有该权限时，结果可以看到该操作。
     */
    private String permission;
    /**
     * 菜单类型
     */
    @TableField("menu_type")
    private Integer type;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 父菜单ID
     */
    private Long parentId;
    /**
     * 路由地址
     */
    private String path;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 组件名称
     */
    private String componentName;
    /**
     * 组件路径
     */
    private String component;
    /**
     * 状态
     */
    private Integer status;
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
