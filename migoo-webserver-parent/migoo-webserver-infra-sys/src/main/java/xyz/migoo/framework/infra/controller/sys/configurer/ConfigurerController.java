package xyz.migoo.framework.infra.controller.sys.configurer;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.sys.configurer.vo.RequestBodyVO;
import xyz.migoo.framework.infra.convert.sys.ConfigurerConvert;
import xyz.migoo.framework.infra.service.sys.configurer.ConfigurerService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/configurer")
public class ConfigurerController {

    @Resource
    private ConfigurerService service;

    @PutMapping
    @PreAuthorize("@ss.hasPermission('system:configurer:update')")
    public Result<?> save(@RequestBody RequestBodyVO req) {
        service.save(ConfigurerConvert.INSTANCE.convert(req));
        return Result.getSuccessful();
    }

    @GetMapping("/page")
    public Result<?> getAll() {
        List<RequestBodyVO> results = ConfigurerConvert.INSTANCE.convert(service.getList());
        return Result.getSuccessful(results.stream().collect(Collectors.toMap(RequestBodyVO::getName, item -> item)));
    }
}
