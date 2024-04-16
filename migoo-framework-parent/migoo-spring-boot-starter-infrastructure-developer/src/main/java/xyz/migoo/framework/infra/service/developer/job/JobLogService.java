package xyz.migoo.franework.infra.service.developer.job;

import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.quartz.core.service.JobLogFrameworkService;
import xyz.migoo.franework.infra.controller.developer.job.vo.JobLogPageReqVO;
import xyz.migoo.franework.infra.dal.dataobject.developer.job.JobLogDO;

import java.util.Collection;
import java.util.List;

public interface JobLogService extends JobLogFrameworkService {

    /**
     * 获得定时任务
     *
     * @param id 编号
     * @return 定时任务
     */
    JobLogDO getJobLog(Long id);

    /**
     * 获得定时任务列表
     *
     * @param ids 编号
     * @return 定时任务列表
     */
    List<JobLogDO> getJobLogList(Collection<Long> ids);

    /**
     * 获得定时任务分页
     *
     * @param pageReqVO 分页查询
     * @return 定时任务分页
     */
    PageResult<JobLogDO> getJobLogPage(JobLogPageReqVO pageReqVO);

    void clearLog();

}
