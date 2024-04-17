package xyz.migoo.framework.infra.dal.dataobject.file;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import xyz.migoo.framework.mybatis.core.dataobject.BaseDO;
import xyz.migoo.framework.oss.core.client.db.DBFileClient;

/**
 * 文件内容表
 * <p>
 * 专门用于存储 {@link DBFileClient} 的文件内容
 *
 * @author 芋道源码
 */
@TableName("infra_file_content")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileContentDO extends BaseDO<Long> {
    
    /**
     * 配置编号
     * <p>
     * 关联 {@link FileConfigDO#getId()}
     */
    private Long configId;
    /**
     * 路径，即文件名
     */
    private String path;
    /**
     * 文件内容
     */
    private byte[] content;

    private String source;

}
