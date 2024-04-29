package xyz.migoo.framework.infra.service.developer.job;

import cn.hutool.core.date.DateUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.util.thread.BizThreadPoolUtils;
import xyz.migoo.framework.infra.controller.developer.job.vo.JobLogPageReqVO;
import xyz.migoo.framework.infra.dal.dataobject.developer.job.JobLogDO;
import xyz.migoo.framework.infra.dal.mapper.developer.job.JobLogMapper;
import xyz.migoo.framework.infra.enums.JobLogStatusEnum;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
@Validated
@Slf4j
public class JobLogServiceImpl implements JobLogService {

    @Resource
    private JobLogMapper jobLogMapper;


    @Override
    public Long createJobLog(Long jobId, LocalDateTime beginTime, String jobHandlerName, String jobHandlerParam, Integer executeIndex) {
        JobLogDO log = JobLogDO.builder().jobId(jobId).handlerName(jobHandlerName).handlerParam(jobHandlerParam).executeIndex(executeIndex)
                .beginTime(beginTime).status(JobLogStatusEnum.RUNNING.getStatus()).build();
        jobLogMapper.insert(log);
        return log.getId();
    }

    @Override
    public void updateJobLogResultAsync(Long logId, LocalDateTime endTime, Integer duration, boolean success, String result) {
        BizThreadPoolUtils.submit(() -> {
            try {
                JobLogDO updateObj = JobLogDO.builder().id(logId).endTime(endTime).duration(duration)
                        .status(success ? JobLogStatusEnum.SUCCESS.getStatus() : JobLogStatusEnum.FAILURE.getStatus()).result(result).build();
                jobLogMapper.updateById(updateObj);
            } catch (Exception ex) {
                log.error("[updateJobLogResultAsync][logId({}) endTime({}) duration({}) success({}) result({})]",
                        logId, endTime, duration, success, result);
            }
        });
    }

    @Override
    public JobLogDO getJobLog(Long id) {
        return jobLogMapper.selectById(id);
    }

    @Override
    public List<JobLogDO> getJobLogList(Collection<Long> ids) {
        return jobLogMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<JobLogDO> getJobLogPage(JobLogPageReqVO pageReqVO) {
        return jobLogMapper.selectPage(pageReqVO);
    }

    @Override
    // 每天0点清理7天前的任务执行日志
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearLog() {
        jobLogMapper.removeByDatetime(DateUtil.format(DateUtil.offsetDay(new Date(), -7), "yyyy-MM-dd 00:00:00"));
    }

}
