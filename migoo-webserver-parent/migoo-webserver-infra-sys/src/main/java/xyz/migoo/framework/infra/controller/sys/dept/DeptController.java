package xyz.migoo.framework.infra.controller.sys.dept;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.sys.dept.vo.*;
import xyz.migoo.framework.infra.convert.sys.DeptConvert;
import xyz.migoo.framework.infra.dal.dataobject.sys.Dept;
import xyz.migoo.framework.infra.service.sys.dept.DeptService;

import java.util.Comparator;
import java.util.List;

import static xyz.migoo.framework.common.enums.CommonStatus.enabled;

@RestController
@RequestMapping("/dept")
public class DeptController {
    @Resource
    private DeptService deptService;

    @GetMapping
    @PreAuthorize("@ss.hasPermission('system:dept:query')")
    public Result<List<DeptRespVO>> getDeptPage(DeptQueryReqVO req) {
        // 获得用户分页列表
        List<Dept> list = deptService.getList(req);
        list.sort(Comparator.comparing(Dept::getSort));
        return Result.getSuccessful(DeptConvert.INSTANCE.convert(list));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermission('system:dept:add')")
    public Result<?> addDept(@Valid @RequestBody DeptAddReqVO addReq) {
        deptService.verify(null, addReq.getParentId(), addReq.getName());
        deptService.add(DeptConvert.INSTANCE.convert(addReq));
        return Result.getSuccessful();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('system:dept:update')")
    public Result<?> updateDept(@Valid @RequestBody DeptUpdateReqVO updateReqVO) {
        deptService.verify(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());
        deptService.update(DeptConvert.INSTANCE.convert(updateReqVO));
        return Result.getSuccessful();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:dept:update')")
    public Result<?> getDept(@PathVariable("id") Long id) {
        return Result.getSuccessful(DeptConvert.INSTANCE.convert(deptService.get(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('system:dept:remove')")
    public Result<?> removeDept(@PathVariable("id") Long id) {
        deptService.remove(id);
        return Result.getSuccessful();
    }

    @GetMapping("/simple")
    public Result<List<DeptSimpleRespVO>> getDeptSimple(DeptQueryReqVO req) {
        // 获得用户分页列表
        req.setStatus(enabled.status());
        List<Dept> list = deptService.getList(req);
        list.sort(Comparator.comparing(Dept::getSort));
        return Result.getSuccessful(DeptConvert.INSTANCE.convert0(list));
    }
}
