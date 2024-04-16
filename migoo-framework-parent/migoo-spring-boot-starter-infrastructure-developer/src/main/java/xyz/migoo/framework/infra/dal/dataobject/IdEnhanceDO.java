package xyz.migoo.framework.infra.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

@Getter
@Setter
public abstract class IdEnhanceDO extends BaseDO {

    @TableId
    private Long id;

}
