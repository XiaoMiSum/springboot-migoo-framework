package xyz.migoo.framework.infra.convert.file;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.file.vo.config.FileConfigRespVO;
import xyz.migoo.framework.infra.controller.file.vo.config.FileConfigSaveReqVO;
import xyz.migoo.framework.infra.dal.dataobject.file.FileConfigDO;

/**
 * 文件配置 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface FileConfigConvert {

    FileConfigConvert INSTANCE = Mappers.getMapper(FileConfigConvert.class);

    @Mapping(target = "config", ignore = true)
    FileConfigDO convert(FileConfigSaveReqVO bean);

    PageResult<FileConfigRespVO> convert(PageResult<FileConfigDO> beans);

    FileConfigRespVO convert(FileConfigDO bean);

    
}
