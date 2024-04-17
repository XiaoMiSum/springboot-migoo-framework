package xyz.migoo.framework.infra.convert.file;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.file.vo.file.FileRespVO;
import xyz.migoo.framework.infra.dal.dataobject.file.FileContentDO;
import xyz.migoo.framework.infra.dal.dataobject.file.FileDO;
import xyz.migoo.framework.oss.core.client.FileContentDTO;

import java.util.List;

@Mapper
public interface FileContentConvert {

    FileContentConvert INSTANCE = Mappers.getMapper(FileContentConvert.class);

    List<FileContentDTO> convert(List<FileContentDO> beans);

    FileContentDTO convert(FileContentDO bean);

    PageResult<FileRespVO> convert(PageResult<FileDO> beans);

    FileRespVO convert(FileDO bean);

}
