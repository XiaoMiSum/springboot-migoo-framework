package xyz.migoo.framework.infra.dal.dataobject.file;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.*;
import xyz.migoo.framework.common.util.json.JsonUtils;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;
import xyz.migoo.framework.oss.core.client.FileClientConfig;
import xyz.migoo.framework.oss.core.enums.FileStorageEnum;

/**
 * 文件配置表
 *
 * @author xiaomi
 */
@TableName(value = "infra_file_config", autoResultMap = true)
@KeySequence("infra_file_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FileConfigDO extends BaseDO<Long> {

    /**
     * 配置名
     */
    private String name;
    /**
     * 存储器
     * <p>
     * 枚举 {@link FileStorageEnum}
     */
    private Integer storage;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否为主配置
     * <p>
     * 由于我们可以配置多个文件配置，默认情况下，使用主配置进行文件的上传
     */
    private Boolean master;

    /**
     * OSS客户端配置
     */
    @TableField(typeHandler = FileClientConfigTypeHandler.class)
    private FileClientConfig config;

    public static class FileClientConfigTypeHandler extends AbstractJsonTypeHandler<Object> {

        public FileClientConfigTypeHandler(Class<?> type) {
            super(type);
        }

        @SneakyThrows
        @Override
        public Object parse(String json) {
            String className = JsonUtils.parseObject(json, "@class", String.class);
            return JSONUtil.toBean(json, Class.forName(className));
        }

        @Override
        public String toJson(Object obj) {
            return JsonUtils.toJsonString(obj);
        }

    }

}
