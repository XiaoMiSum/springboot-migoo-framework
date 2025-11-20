package xyz.migoo.framework.infra.dal.dataobject.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.migoo.framework.common.enums.CommonStatus;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

@TableName("sys_post")
@Data
@EqualsAndHashCode(callSuper = true)
public class Post extends BaseDO<Long, Post> {

    /**
     * 岗位名称
     */
    private String name;
    /**
     * 岗位编码
     */
    private String code;
    /**
     * 岗位排序
     */
    private Integer sort;
    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatus}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
