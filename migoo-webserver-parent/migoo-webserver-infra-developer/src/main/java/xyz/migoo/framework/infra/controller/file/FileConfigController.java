package xyz.migoo.framework.infra.controller.file;

import cn.hutool.core.bean.BeanUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.file.vo.config.FileConfigPageReqVO;
import xyz.migoo.framework.infra.controller.file.vo.config.FileConfigRespVO;
import xyz.migoo.framework.infra.controller.file.vo.config.FileConfigSaveReqVO;
import xyz.migoo.framework.infra.convert.file.FileConfigConvert;
import xyz.migoo.framework.infra.dal.dataobject.file.FileConfigDO;
import xyz.migoo.framework.infra.service.file.FileConfigService;

@RestController
@RequestMapping("/developer/file/config")
@Validated
public class FileConfigController {

    @Resource
    private FileConfigService fileConfigService;

    @PostMapping
    @PreAuthorize("@ss.hasPermission('developer:file:config:add')")
    public Result<Long> createFileConfig(@Valid @RequestBody FileConfigSaveReqVO createReqVO) {
        return Result.ok(fileConfigService.createFileConfig(createReqVO));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('developer:file:config:update')")
    public Result<Boolean> updateFileConfig(@Valid @RequestBody FileConfigSaveReqVO updateReqVO) {
        fileConfigService.updateFileConfig(updateReqVO);
        return Result.ok(true);
    }

    @PutMapping("/master")
    @PreAuthorize("@ss.hasPermission('developer:file:config:update')")
    public Result<Boolean> updateFileConfigMaster(@RequestParam("id") Long id) {
        fileConfigService.updateFileConfigMaster(id);
        return Result.ok(true);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:file:config:remove')")
    public Result<Boolean> deleteFileConfig(@PathVariable("id") Long id) {
        fileConfigService.deleteFileConfig(id);
        return Result.ok(true);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:file:config:query')")
    public Result<FileConfigRespVO> getFileConfig(@PathVariable("id") Long id) {
        FileConfigDO config = fileConfigService.getFileConfig(id);
        return Result.ok(BeanUtil.toBean(config, FileConfigRespVO.class));
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:file:config:query')")
    public Result<PageResult<FileConfigRespVO>> getFileConfigPage(@Valid FileConfigPageReqVO pageVO) {
        PageResult<FileConfigDO> pageResult = fileConfigService.getFileConfigPage(pageVO);
        return Result.ok(FileConfigConvert.INSTANCE.convert(pageResult));
    }

    @GetMapping("/test")
    @PreAuthorize("@ss.hasPermission('developer:file:config:query')")
    public Result<String> testFileConfig(@RequestParam("id") Long id) throws Exception {
        String url = fileConfigService.testFileConfig(id);
        return Result.ok(url);
    }
}
