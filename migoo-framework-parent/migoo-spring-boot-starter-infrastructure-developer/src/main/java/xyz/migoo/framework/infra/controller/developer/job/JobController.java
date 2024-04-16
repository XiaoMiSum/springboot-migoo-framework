package xyz.migoo.franework.infra.controller.developer.job;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.quartz.SchedulerException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.quartz.core.util.CronUtils;
import xyz.migoo.franework.infra.controller.developer.job.vo.JobCreateReqVO;
import xyz.migoo.franework.infra.controller.developer.job.vo.JobPageReqVO;
import xyz.migoo.franework.infra.controller.developer.job.vo.JobRespVO;
import xyz.migoo.franework.infra.controller.developer.job.vo.JobUpdateReqVO;
import xyz.migoo.franework.infra.convert.developer.job.JobConvert;
import xyz.migoo.franework.infra.dal.dataobject.developer.job.JobDO;
import xyz.migoo.franework.infra.service.developer.job.JobService;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/developer/job")
public class JobController {

    @Resource
    private JobService service;

    @PostMapping
    @PreAuthorize("@ss.hasPermission('developer:job:add')")
    public Result<Long> createJob(@Valid @RequestBody JobCreateReqVO createReqVO)
            throws SchedulerException {
        return Result.getSuccessful(service.createJob(createReqVO));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('developer:job:update')")
    public Result<Boolean> updateJob(@Valid @RequestBody JobUpdateReqVO updateReqVO)
            throws SchedulerException {
        service.updateJob(updateReqVO);
        return Result.getSuccessful(true);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:job:update')")
    public Result<Boolean> updateJobStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status)
            throws SchedulerException {
        service.updateJobStatus(id, status);
        return Result.getSuccessful(true);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:job:remove')")
    public Result<Boolean> deleteJob(@PathVariable("id") Long id) throws SchedulerException {
        service.deleteJob(id);
        return Result.getSuccessful(true);
    }

    @PutMapping("/trigger")
    @PreAuthorize("@ss.hasPermission('developer:job:trigger')")
    public Result<Boolean> triggerJob(@RequestParam("id") Long id) throws SchedulerException {
        service.triggerJob(id);
        return Result.getSuccessful(true);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:job:query')")
    public Result<JobRespVO> getJob(@PathVariable("id") Long id) {
        JobDO job = service.getJob(id);
        return Result.getSuccessful(JobConvert.INSTANCE.convert(job));
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:job:query')")
    public Result<PageResult<JobRespVO>> getJobPage(@Valid JobPageReqVO pageVO) {
        PageResult<JobDO> pageResult = service.getJobPage(pageVO);
        return Result.getSuccessful(JobConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/get_next_times")
    @PreAuthorize("@ss.hasPermission('developer:job:query')")
    public Result<List<Date>> getJobNextTimes(@RequestParam("id") Long id,
                                              @RequestParam(value = "count", required = false, defaultValue = "5") Integer count) {
        JobDO job = service.getJob(id);
        if (Objects.isNull(job)) {
            return Result.getSuccessful(Collections.emptyList());
        }
        return Result.getSuccessful(CronUtils.getNextTimes(job.getCronExpression(), count));
    }
}
