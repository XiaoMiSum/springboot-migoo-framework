package xyz.migoo.framework.oss.core.client.db;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import xyz.migoo.framework.oss.core.client.AbstractFileClient;
import xyz.migoo.framework.oss.core.client.FileContentDTO;

import java.util.Comparator;
import java.util.List;

/**
 * 基于 DB 存储的文件客户端的配置类
 *
 * @author xiaomi
 */
public class DBFileClient extends AbstractFileClient<DBFileClientConfig> {

    private IFileContentMapper fileContentMapper;

    public DBFileClient(Long id, DBFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        fileContentMapper = SpringUtil.getBean(IFileContentMapper.class);
    }

    @Override
    public String upload(byte[] content, String path, String type) {

        fileContentMapper.insert(content, path, type, getId());
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) {
        fileContentMapper.deleteByConfigIdAndPath(getId(), path);
    }

    @Override
    public byte[] getContent(String path) {
        List<FileContentDTO> list = fileContentMapper.selectListByConfigIdAndPath(getId(), path);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        // 排序后，拿 id 最大的，即最后上传的
        list.sort(Comparator.comparing(FileContentDTO::getId));
        return CollUtil.getLast(list).getContent();
    }

}
