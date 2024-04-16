package xyz.migoo.framework.infra.dal.dataobject.sys;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.migoo.framework.infra.dal.dataobject.IdEnhanceDO;

@TableName(value = "sys_dept", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class Dept extends IdEnhanceDO {
    /**
     * 部门名称
     */
    private String name;
    /**
     * 父部门id
     */
    private Long parentId;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 负责人id
     */
    private Long leaderUserId;
    /**
     * 部门邮箱
     */
    private String email;
    /**
     * 状态 1 正常 0 停用
     */
    private Integer status;
}
