package xyz.migoo.framework.infra.dal.dataobject.sys;

import xyz.migoo.framework.infra.dal.dataobject.IdEnhanceDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName(value = "sys_user_role", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRole extends IdEnhanceDO {
    /**
     * 用户 ID
     */
    private Long userId;
    /**
     * 角色 ID
     */
    private Long roleId;

}
