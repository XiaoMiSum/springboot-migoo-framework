package xyz.migoo.framework.infra.dal.dataobject.sys;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

import java.util.List;
import java.util.Map;

@TableName(value = "sys_page_configurer", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ConfigurerDO extends BaseDO<Long> {

    private String name;

    private String label;

    private String value;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> options;
}
