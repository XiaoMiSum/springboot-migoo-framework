package xyz.migoo.framework.infra.controller.developer.job;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.developer.job.vo.JobLogPageReqVO;
import xyz.migoo.framework.infra.controller.developer.job.vo.JobLogRespVO;
import xyz.migoo.framework.infra.convert.developer.job.JobLogConvert;
import xyz.migoo.framework.infra.dal.dataobject.developer.job.JobLogDO;
import xyz.migoo.framework.infra.service.developer.job.JobLogService;


@RestController
@RequestMapping("/developer/job/log")
@Validated
public class JobLogController {

    @Resource
    private JobLogService jobLogService;

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:job:query')")
    public Result<JobLogRespVO> getJobLog(@PathVariable("id") Long id) {
        JobLogDO jobLog = jobLogService.getJobLog(id);
        return Result.ok(JobLogConvert.INSTANCE.convert(jobLog));
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:job:query')")
    public Result<PageResult<JobLogRespVO>> getJobLogPage(@Valid JobLogPageReqVO pageVO) {
        PageResult<JobLogDO> pageResult = jobLogService.getJobLogPage(pageVO);
        return Result.ok(JobLogConvert.INSTANCE.convertPage(pageResult));
    }

}
