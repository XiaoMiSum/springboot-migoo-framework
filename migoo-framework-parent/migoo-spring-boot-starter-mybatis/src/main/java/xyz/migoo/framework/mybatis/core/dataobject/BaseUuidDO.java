package xyz.migoo.framework.mybatis.core.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 基础实体对象 - 使用 UUID 作为主键
 * <p>
 * 使用说明：
 * - 适用于需要全局唯一 ID 的场景
 * - 数据库字段类型建议为 CHAR(36) 或 BINARY(16)
 * - UUID TypeHandler 已在 MybatisAutoConfiguration 中全局注册
 *
 * @author xiaomi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("unchecked")
public abstract class BaseUuidDO<SELF extends BaseUuidDO<SELF>> extends BaseDO {

    /**
     * 主键
     */
    @TableId()
    private UUID id;

    public SELF setId(UUID id) {
        this.id = id;
        return (SELF) this;
    }
}
