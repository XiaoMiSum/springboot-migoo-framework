package xyz.migoo.framework.infra.controller.cvs;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.cvs.core.client.dto.Option;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSMachinePageQueryReqVO;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSMachinePageRespVO;
import xyz.migoo.framework.infra.controller.cvs.vo.CVSMachineUpdateReqVO;
import xyz.migoo.framework.infra.convert.cvs.CVSMachineConvert;
import xyz.migoo.framework.infra.service.cvs.CVSMachineService;

@RestController
@RequestMapping("/developer/cvs/machine")
public class CVSMachineController {

    @Resource
    private CVSMachineService service;

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:cvs:provider:query')")
    public Result<PageResult<CVSMachinePageRespVO>> getPage(CVSMachinePageQueryReqVO req) {
        return Result.getSuccessful(CVSMachineConvert.INSTANCE.convert(service.getPage(req)));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermission('developer:cvs:provider:sync')")
    public Result<?> sync() {
        service.sync();
        return Result.getSuccessful();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('developer:cvs:provider:update')")
    public Result<?> update(@RequestBody CVSMachineUpdateReqVO req) {
        service.update(CVSMachineConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:cvs:provider:remove')")
    public Result<?> remove(@PathVariable("id") Long id) {
        service.remove(id);
        return Result.getSuccessful();
    }

    @PostMapping("/{id}/{option}")
    @PreAuthorize("@ss.hasPermission('developer:cvs:provider:remove')")
    public Result<?> insOption(@PathVariable("id") Long id, @PathVariable("option") Option option) {
        return service.option(id, option);
    }
}
