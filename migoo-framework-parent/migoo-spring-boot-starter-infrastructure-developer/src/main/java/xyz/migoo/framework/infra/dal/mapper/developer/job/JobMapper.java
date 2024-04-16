package xyz.migoo.franework.infra.dal.mapper.developer.job;

import org.apache.ibatis.annotations.Mapper;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.mybatis.core.BaseMapperX;
import xyz.migoo.framework.mybatis.core.LambdaQueryWrapperX;
import xyz.migoo.franework.infra.controller.developer.job.vo.JobPageReqVO;
import xyz.migoo.franework.infra.dal.dataobject.developer.job.JobDO;

@Mapper
public interface JobMapper extends BaseMapperX<JobDO> {

    default JobDO selectByHandlerName(String handlerName) {
        return selectOne(new LambdaQueryWrapperX<JobDO>().eq(JobDO::getHandlerName, handlerName));
    }

    default PageResult<JobDO> selectPage(JobPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<JobDO>()
                .likeIfPresent(JobDO::getName, reqVO.getName())
                .eqIfPresent(JobDO::getStatus, reqVO.getStatus())
                .likeIfPresent(JobDO::getHandlerName, reqVO.getHandlerName())
        );
    }
}