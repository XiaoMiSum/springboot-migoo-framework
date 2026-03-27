package xyz.migoo.framework.mybatis.core.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基础实体对象 - 使用数据库自增 ID
 * <p>
 * 使用说明：
 * - 适用于需要连续编号的场景（如用户、订单）
 * - 数据库字段必须是 SERIAL 或 BIGSERIAL 类型
 * - PostgreSQL/MySQL 等支持自增的数据库会自动生成 ID
 *
 * @author xiaomi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("unchecked")
public abstract class BaseAutoIncDO<T, SELF extends BaseAutoIncDO<T, SELF>> extends BaseDO {

    /**
     * 重写 id 字段，指定使用自增策略
     */
    @TableId(type = IdType.AUTO)
    private T id;

    public SELF setId(T id) {
        this.id = id;
        return (SELF) this;
    }
}
