package xyz.migoo.framework.infra.dal.mapper.file;

import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.file.vo.file.FilePageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.file.FileDO;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;

/**
 * 文件操作 Mapper
 *
 * @author xiaomi
 */
@Mapper
public interface FileMapper extends BaseMapperX<FileDO> {

    default PageResult<FileDO> selectPage(FilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileDO>()
                .likeIfPresent(FileDO::getPath, reqVO.getPath())
                .likeIfPresent(FileDO::getType, reqVO.getType())
                .betweenIfPresent(FileDO::getCreatedAt, reqVO.getCreateTime())
                .orderByDesc(FileDO::getId));
    }

}
