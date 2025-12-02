package xyz.migoo.framework.infra.controller.developer.job;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.quartz.SchedulerException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.developer.job.vo.JobCreateReqVO;
import xyz.migoo.framework.infra.controller.developer.job.vo.JobPageReqVO;
import xyz.migoo.framework.infra.controller.developer.job.vo.JobRespVO;
import xyz.migoo.framework.infra.controller.developer.job.vo.JobUpdateReqVO;
import xyz.migoo.framework.infra.convert.developer.job.JobConvert;
import xyz.migoo.framework.infra.dal.dataobject.developer.job.JobDO;
import xyz.migoo.framework.infra.service.developer.job.JobService;
import xyz.migoo.framework.quartz.core.util.CronUtils;

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
        return Result.ok(service.createJob(createReqVO));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('developer:job:update')")
    public Result<Boolean> updateJob(@Valid @RequestBody JobUpdateReqVO updateReqVO)
            throws SchedulerException {
        service.updateJob(updateReqVO);
        return Result.ok(true);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:job:update')")
    public Result<Boolean> updateJobStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status)
            throws SchedulerException {
        service.updateJobStatus(id, status);
        return Result.ok(true);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:job:remove')")
    public Result<Boolean> deleteJob(@PathVariable("id") Long id) throws SchedulerException {
        service.deleteJob(id);
        return Result.ok(true);
    }

    @PutMapping("/trigger")
    @PreAuthorize("@ss.hasPermission('developer:job:trigger')")
    public Result<Boolean> triggerJob(@RequestParam("id") Long id) throws SchedulerException {
        service.triggerJob(id);
        return Result.ok(true);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:job:query')")
    public Result<JobRespVO> getJob(@PathVariable("id") Long id) {
        JobDO job = service.getJob(id);
        return Result.ok(JobConvert.INSTANCE.convert(job));
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:job:query')")
    public Result<PageResult<JobRespVO>> getJobPage(@Valid JobPageReqVO pageVO) {
        PageResult<JobDO> pageResult = service.getJobPage(pageVO);
        return Result.ok(JobConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/get_next_times")
    @PreAuthorize("@ss.hasPermission('developer:job:query')")
    public Result<List<Date>> getJobNextTimes(@RequestParam("id") Long id,
                                              @RequestParam(value = "count", required = false, defaultValue = "5") Integer count) {
        JobDO job = service.getJob(id);
        if (Objects.isNull(job)) {
            return Result.ok(Collections.emptyList());
        }
        return Result.ok(CronUtils.getNextTimes(job.getCronExpression(), count));
    }
}
