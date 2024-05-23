package xyz.migoo.framework.infra.dal.dataobject.developer.dictionary;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "infra_dictionary_value", autoResultMap = true)
public class DictionaryValueDO extends BaseDO<Long> {

    private String label;

    private String value;

    private String dictCode;

    private Integer status;

    private String colorType;

    private Integer sort;
}
