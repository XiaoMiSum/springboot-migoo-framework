package xyz.migoo.framework.infra.controller.cvs;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.enums.CommonStatusEnum;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.pojo.Tree;
import xyz.migoo.framework.infra.controller.cvs.vo.*;
import xyz.migoo.framework.infra.convert.cvs.CVSProviderConvert;
import xyz.migoo.framework.infra.dal.dataobject.cvs.CVSProviderDO;
import xyz.migoo.framework.infra.service.cvs.CVSProviderService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/developer/cvs/provider")
public class CVSProviderController {

    @Resource
    private CVSProviderService service;

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:cvs:provider:query')")
    public Result<PageResult<CVSProviderPageRespVO>> getPage(CVSProviderPageQueryReqVO req) {
        return Result.getSuccessful(CVSProviderConvert.INSTANCE.convert(service.getPage(req)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:cvs:provider:query')")
    public Result<CVSProviderPageRespVO> getPage(@PathVariable("id") Long id) {
        return Result.getSuccessful(CVSProviderConvert.INSTANCE.convert(service.get(id)));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermission('developer:cvs:provider:add')")
    public Result<?> add(@RequestBody CVSProviderAddReqVO req) {
        service.add(CVSProviderConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('developer:cvs:provider:update')")
    public Result<?> update(@RequestBody CVSProviderUpdateReqVO req) {
        service.update(CVSProviderConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:cvs:provider:remove')")
    public Result<?> remove(@PathVariable("id") Long id) {
        service.remove(id);
        return Result.getSuccessful();
    }

    @GetMapping("/tree")
    public Result<List<Tree>> getTree(CVSProviderQueryReqVO req) {
        // 获得用户分页列表
        req.setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<Tree> results = Lists.newArrayList();
        service.getList(req).stream().collect(Collectors.groupingBy(CVSProviderDO::getCode))
                .forEach((key, value) -> {
                    Tree root = Tree.rootNode(key, key);
                    value.forEach(item -> root.addChildren(item.getAccount(), item.getAccount(), item.getStatus()));
                    results.add(root);
                });
        return Result.getSuccessful(results);
    }
}
