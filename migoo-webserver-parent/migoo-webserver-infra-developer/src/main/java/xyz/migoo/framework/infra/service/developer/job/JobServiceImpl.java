package xyz.migoo.framework.infra.service.developer.job;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import xyz.migoo.framework.common.exception.util.ServiceExceptionUtil;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.infra.controller.developer.job.vo.JobCreateReqVO;
import xyz.migoo.framework.infra.controller.developer.job.vo.JobPageReqVO;
import xyz.migoo.framework.infra.controller.developer.job.vo.JobUpdateReqVO;
import xyz.migoo.framework.infra.convert.developer.job.JobConvert;
import xyz.migoo.framework.infra.dal.dataobject.developer.job.JobDO;
import xyz.migoo.framework.infra.dal.mapper.developer.job.JobMapper;
import xyz.migoo.framework.infra.enums.JobStatusEnum;
import xyz.migoo.framework.quartz.core.scheduler.SchedulerManager;
import xyz.migoo.framework.quartz.core.util.CronUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static xyz.migoo.framework.common.util.collection.CollectionUtils.containsAny;
import static xyz.migoo.framework.infra.enums.DeveloperErrorCodeConstants.*;

@Service
@Validated
public class JobServiceImpl implements JobService {

    @Resource
    private JobMapper jobMapper;

    @Resource
    private SchedulerManager schedulerManager;

    private static void fillJobMonitorTimeoutEmpty(JobDO job) {
        if (job.getMonitorTimeout() == null) {
            job.setMonitorTimeout(0);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createJob(JobCreateReqVO createReqVO) throws SchedulerException {
        validateCronExpression(createReqVO.getCronExpression());
        // 校验唯一性
        if (jobMapper.selectByHandlerName(createReqVO.getHandlerName()) != null) {
            throw ServiceExceptionUtil.get(JOB_HANDLER_EXISTS);
        }
        // 插入
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.INIT.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);

        // 添加 Job 到 Quartz 中
        schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(), job.getCronExpression(),
                job.getRetryCount(), job.getRetryInterval());
        // 更新
        JobDO updateObj = new JobDO().setId(job.getId()).setStatus(JobStatusEnum.NORMAL.getStatus());
        jobMapper.updateById(updateObj);

        // 返回
        return job.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(JobUpdateReqVO updateReqVO) throws SchedulerException {
        validateCronExpression(updateReqVO.getCronExpression());
        // 校验存在
        JobDO job = validateJobExists(updateReqVO.getId());
        JobDO updateObj = JobConvert.INSTANCE.convert(updateReqVO);
        fillJobMonitorTimeoutEmpty(updateObj);
        jobMapper.updateById(updateObj);

        if (!StrUtil.equals(job.getHandlerName(), updateObj.getHandlerName())) {
            // 新handlerName 与 原handlerName 不一致， 删除Quartz原handlerName任务 再添加新handlerName任务
            schedulerManager.deleteJob(job.getHandlerName());
            schedulerManager.addJob(updateObj.getId(), updateObj.getHandlerName(), updateObj.getHandlerParam(), updateObj.getCronExpression(),
                    updateObj.getRetryCount(), updateObj.getRetryInterval());
        } else {
            // 更新 Job 到 Quartz 中
            schedulerManager.updateJob(updateObj.getHandlerName(), updateReqVO.getHandlerParam(), updateReqVO.getCronExpression(),
                    updateReqVO.getRetryCount(), updateReqVO.getRetryInterval());
        }
        if (job.getStatus().equals(JobStatusEnum.STOP.getStatus())) {
            // 原任务状态是停止的，需要将任务停止
            schedulerManager.pauseJob(updateObj.getHandlerName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJobStatus(Long id, Integer status) throws SchedulerException {
        // 校验 status
        if (!containsAny(status, JobStatusEnum.NORMAL.getStatus(), JobStatusEnum.STOP.getStatus())) {
            throw ServiceExceptionUtil.get(JOB_CHANGE_STATUS_INVALID);
        }
        // 校验存在
        JobDO job = validateJobExists(id);
        // 校验是否已经为当前状态
        if (job.getStatus().equals(status)) {
            throw ServiceExceptionUtil.get(JOB_CHANGE_STATUS_EQUALS);
        }
        // 更新 Job 状态
        JobDO updateObj = new JobDO().setId(id).setStatus(status);
        jobMapper.updateById(updateObj);
        // 更新状态 Job 到 Quartz 中
        if (JobStatusEnum.NORMAL.getStatus().equals(status)) { // 开启
            schedulerManager.resumeJob(job.getHandlerName());
        } else { // 暂停
            schedulerManager.pauseJob(job.getHandlerName());
        }
    }

    @Override
    public void triggerJob(Long id) throws SchedulerException {
        // 校验存在
        JobDO job = validateJobExists(id);
        if (schedulerManager.isExists(job.getHandlerName())) {
            // 如果 Quartz 存在此 Job，则触发 Quartz 中的 Job
            schedulerManager.triggerJob(job.getId(), job.getHandlerName(), job.getHandlerParam());
        } else {
            // 如果 Quartz 不存在此 Job，则添加到 Quartz 中，出发执行后再从 Quartz中删除
            schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(), job.getCronExpression(),
                    job.getRetryCount(), job.getRetryInterval());
            schedulerManager.triggerJob(job.getId(), job.getHandlerName(), job.getHandlerParam());
            schedulerManager.deleteJob(job.getHandlerName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Long id) throws SchedulerException {
        // 校验存在
        JobDO job = validateJobExists(id);
        // 更新
        jobMapper.deleteById(id);

        // 删除 Job 到 Quartz 中
        schedulerManager.deleteJob(job.getHandlerName());
    }

    private JobDO validateJobExists(Long id) {
        JobDO job = jobMapper.selectById(id);
        if (job == null) {
            throw ServiceExceptionUtil.get(JOB_NOT_EXISTS);
        }
        return job;
    }

    private void validateCronExpression(String cronExpression) {
        if (!CronUtils.isValid(cronExpression)) {
            throw ServiceExceptionUtil.get(JOB_CRON_EXPRESSION_VALID);
        }
    }

    @Override
    public JobDO getJob(Long id) {
        return jobMapper.selectById(id);
    }

    @Override
    public List<JobDO> getJobList(Collection<Long> ids) {
        return jobMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<JobDO> getJobPage(JobPageReqVO pageReqVO) {
        return jobMapper.selectPage(pageReqVO);
    }


    @PostConstruct
    @Override
    public void initialize() {
        for (JobDO job : jobMapper.selectList()) {
            try {
                // 添加 Job 到 Quartz 中
                schedulerManager.addJob(job.getId(), job.getHandlerName(), job.getHandlerParam(), job.getCronExpression(),
                        job.getRetryCount(), job.getRetryInterval());
                if (Objects.equals(job.getStatus(), JobStatusEnum.STOP.getStatus())) {
                    // 任务为停止状态，则关闭任务
                    schedulerManager.pauseJob(job.getHandlerName());
                } else {
                    // 任务为运行中\初始化状态，则更新任务的状态为运行中
                    JobDO updateObj = new JobDO().setId(job.getId()).setStatus(JobStatusEnum.NORMAL.getStatus());
                    jobMapper.updateById(updateObj);
                }
            } catch (Exception e) {
                // 发生异常，更新任务状态为停止
                JobDO updateObj = new JobDO().setId(job.getId()).setStatus(JobStatusEnum.STOP.getStatus());
                jobMapper.updateById(updateObj);
            }
        }
    }
}