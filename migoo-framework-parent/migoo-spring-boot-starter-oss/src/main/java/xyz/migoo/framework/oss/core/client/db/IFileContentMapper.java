package xyz.migoo.framework.oss.core.client.db;

import xyz.migoo.framework.oss.core.client.FileContentDTO;

import java.util.List;

public interface IFileContentMapper {

    void insert(byte[] content, String path, String type, Long configId);

    void deleteByConfigIdAndPath(Long configId, String path);

    List<FileContentDTO> selectListByConfigIdAndPath(Long configId, String path);
}
