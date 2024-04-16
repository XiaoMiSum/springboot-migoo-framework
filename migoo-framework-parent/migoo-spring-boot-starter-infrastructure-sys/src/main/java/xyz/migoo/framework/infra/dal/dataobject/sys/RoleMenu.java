package xyz.migoo.framework.infra.dal.dataobject.sys;

import xyz.migoo.framework.infra.dal.dataobject.IdEnhanceDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName(value = "sys_role_menu", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleMenu extends IdEnhanceDO {

    /**
     * 角色ID
     */
    private Long roleId;
    /**
     * 菜单ID
     */
    private Long menuId;

}
