package xyz.migoo.framework.mybatis.core.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础实体对象
 */
@Data
public class BaseDO implements Serializable {

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 创建者id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long creatorId;
    /**
     * 创建者姓名
     */
    @TableField(fill = FieldFill.INSERT)
    private String creatorName;
    /**
     * 更新者id
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updaterId;
    /**
     * 更新者姓名
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updaterName;
    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
    /**
     * 是否启用
     */
    @TableField(fill = FieldFill.INSERT)
    private Boolean enabled;

}