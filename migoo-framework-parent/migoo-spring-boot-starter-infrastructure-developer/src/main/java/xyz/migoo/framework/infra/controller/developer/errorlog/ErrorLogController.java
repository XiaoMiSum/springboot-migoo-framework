package xyz.migoo.framework.infra.controller.developer.errorlog;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.developer.errorlog.vo.ApiErrorLogPageRespVO;
import xyz.migoo.framework.infra.controller.developer.errorlog.vo.ApiErrorLogQueryReqVO;
import xyz.migoo.framework.infra.controller.developer.errorlog.vo.ApiErrorLogUpdateVO;
import xyz.migoo.framework.infra.convert.developer.errorlog.ErrorLogConvert;
import xyz.migoo.framework.infra.service.developer.errorlog.ErrorLogService;

@RestController
@RequestMapping("/developer/error-log")
public class ErrorLogController {

    @Resource
    private ErrorLogService service;

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:error-log:query')")
    public Result<PageResult<ApiErrorLogPageRespVO>> getPage(ApiErrorLogQueryReqVO req) {
        return Result.getSuccessful(ErrorLogConvert.INSTANCE.convert(service.getPage(req)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:error-log:update')")
    public Result<?> get(@PathVariable("id") Long id) {
        return Result.getSuccessful(ErrorLogConvert.INSTANCE.convert(service.get(id)));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('developer:error-log:update')")
    public Result<?> update(@RequestBody ApiErrorLogUpdateVO req) {
        service.update(ErrorLogConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:error-log:remove')")
    public Result<?> remove(@PathVariable("id") Long id) {
        service.remove(id);
        return Result.getSuccessful();
    }
}
