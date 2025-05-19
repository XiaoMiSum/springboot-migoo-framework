package xyz.migoo.framework.infra.dal.dataobject.file;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;

/**
 * 文件表
 * 每次文件上传，都会记录一条记录到该表中
 *
 * @author xiaomi
 */
@TableName("infra_file")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FileDO extends BaseDO<Long> {

    /**
     * 配置编号
     * <p>
     * 关联 {@link FileConfigDO#getId()}
     */
    private Long configId;
    /**
     * 原文件名
     */
    private String name;
    /**
     * 路径，即文件名
     */
    private String path;
    /**
     * 访问地址
     */
    private String url;
    /**
     * 文件的 MIME 类型，例如 "application/octet-stream"
     */
    private String type;
    /**
     * 文件大小
     */
    private Integer size;
    /**
     * 文件大小
     */
    private String source;

}
