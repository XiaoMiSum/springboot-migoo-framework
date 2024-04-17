package xyz.migoo.framework.oss.core.enums;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.migoo.framework.oss.core.client.FileClient;
import xyz.migoo.framework.oss.core.client.FileClientConfig;
import xyz.migoo.framework.oss.core.client.db.DBFileClient;
import xyz.migoo.framework.oss.core.client.db.DBFileClientConfig;
import xyz.migoo.framework.oss.core.client.local.LocalFileClient;
import xyz.migoo.framework.oss.core.client.local.LocalFileClientConfig;
import xyz.migoo.framework.oss.core.client.s3.S3FileClient;
import xyz.migoo.framework.oss.core.client.s3.S3FileClientConfig;

/**
 * 文件存储器枚举
 *
 * @author xiaomi
 */
@AllArgsConstructor
@Getter
public enum FileStorageEnum {

    DB(1, DBFileClientConfig.class, DBFileClient.class),

    LOCAL(10, LocalFileClientConfig.class, LocalFileClient.class),

    S3(20, S3FileClientConfig.class, S3FileClient.class),
    ;

    /**
     * 存储器
     */
    private final Integer storage;

    /**
     * 配置类
     */
    private final Class<? extends FileClientConfig> configClass;
    /**
     * 客户端类
     */
    private final Class<? extends FileClient> clientClass;

    public static FileStorageEnum getByStorage(Integer storage) {
        return ArrayUtil.firstMatch(o -> o.getStorage().equals(storage), values());
    }

}
