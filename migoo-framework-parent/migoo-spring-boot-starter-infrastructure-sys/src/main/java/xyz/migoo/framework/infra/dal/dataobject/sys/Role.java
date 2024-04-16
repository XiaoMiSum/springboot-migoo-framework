package xyz.migoo.framework.infra.dal.dataobject.sys;

import xyz.migoo.framework.infra.dal.dataobject.IdEnhanceDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName(value = "sys_role", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class Role extends IdEnhanceDO {

    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色标识
     */
    private String code;
    /**
     * 角色排序
     */
    private Integer sort;
    /**
     * 角色状态
     */
    private Integer status;
    /**
     * 角色类型
     */
    private Integer type;
    /**
     * 备注
     */
    private String remark;

}
