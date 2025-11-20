package xyz.migoo.framework.infra.dal.dataobject.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

@TableName(value = "sys_user_role", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRole extends BaseDO<Long, UserRole> {
    /**
     * 用户 ID
     */
    private Long userId;
    /**
     * 角色 ID
     */
    private Long roleId;

}
