package xyz.migoo.franework.infra.convert.developer.job;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.franework.infra.controller.developer.job.vo.JobCreateReqVO;
import xyz.migoo.franework.infra.controller.developer.job.vo.JobRespVO;
import xyz.migoo.franework.infra.controller.developer.job.vo.JobUpdateReqVO;
import xyz.migoo.franework.infra.dal.dataobject.developer.job.JobDO;

import java.util.List;

@Mapper
public interface JobConvert {

    JobConvert INSTANCE = Mappers.getMapper(JobConvert.class);

    JobDO convert(JobCreateReqVO bean);

    JobDO convert(JobUpdateReqVO bean);

    JobRespVO convert(JobDO bean);

    List<JobRespVO> convertList(List<JobDO> list);

    PageResult<JobRespVO> convertPage(PageResult<JobDO> page);

}