package xyz.migoo.framework.infra.dal.mapper.file;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.infra.convert.file.FileContentConvert;
import xyz.migoo.framework.infra.dal.dataobject.file.FileContentDO;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.oss.core.client.FileContentDTO;
import xyz.migoo.framework.oss.core.client.db.IFileContentMapper;

import java.util.List;

@Mapper
public interface FileContentMapper extends BaseMapperX<FileContentDO>, IFileContentMapper {

    default void deleteByConfigIdAndPath(Long configId, String path) {
        this.delete(new LambdaQueryWrapper<FileContentDO>()
                .eq(FileContentDO::getConfigId, configId)
                .eq(FileContentDO::getPath, path));
    }

    default List<FileContentDO> _selectListByConfigIdAndPath(Long configId, String path) {
        return selectList(new LambdaQueryWrapper<FileContentDO>()
                .eq(FileContentDO::getConfigId, configId)
                .eq(FileContentDO::getPath, path));
    }


    default List<FileContentDTO> selectListByConfigIdAndPath(Long configId, String path) {
        return FileContentConvert.INSTANCE.convert(_selectListByConfigIdAndPath(configId, path));
    }

    default void insert(byte[] content, String path, String type, Long configId) {
        insert(new FileContentDO().setContent(content).setPath(path).setConfigId(configId));
    }

}
