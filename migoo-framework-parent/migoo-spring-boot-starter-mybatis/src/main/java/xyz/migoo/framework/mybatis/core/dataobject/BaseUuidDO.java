package xyz.migoo.framework.mybatis.core.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基础实体对象 - 使用 UUID 作为主键
 * <p>
 * 使用说明：
 * - 适用于需要全局唯一 ID 的场景
 * - PostgreSQL 数据库会自动生成 UUID
 * - 其他数据库使用 ASSIGN_UUID 策略
 *
 * @author xiaomi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("unchecked")
public abstract class BaseUuidDO<T, SELF extends BaseUuidDO<T, SELF>> extends BaseDO {

    /**
     * 重写 id 字段，指定使用 UUID 策略
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private T id;

    public SELF setId(T id) {
        this.id = id;
        return (SELF) this;
    }
}
