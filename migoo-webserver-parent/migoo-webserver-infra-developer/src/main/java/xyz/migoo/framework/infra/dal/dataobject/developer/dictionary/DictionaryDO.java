package xyz.migoo.framework.infra.dal.dataobject.developer.dictionary;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "infra_dictionary", autoResultMap = true)
public class DictionaryDO extends BaseDO<Long> {

    private String code;

    private String name;

    private String source;

    private Integer status;
}
