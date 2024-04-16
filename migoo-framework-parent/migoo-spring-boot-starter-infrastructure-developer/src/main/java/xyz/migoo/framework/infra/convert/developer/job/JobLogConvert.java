package xyz.migoo.franework.infra.convert.developer.job;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.franework.infra.controller.developer.job.vo.JobLogRespVO;
import xyz.migoo.franework.infra.dal.dataobject.developer.job.JobLogDO;

import java.util.List;

@Mapper
public interface JobLogConvert {

    JobLogConvert INSTANCE = Mappers.getMapper(JobLogConvert.class);

    JobLogRespVO convert(JobLogDO bean);

    List<JobLogRespVO> convertList(List<JobLogDO> list);

    PageResult<JobLogRespVO> convertPage(PageResult<JobLogDO> page);

}
