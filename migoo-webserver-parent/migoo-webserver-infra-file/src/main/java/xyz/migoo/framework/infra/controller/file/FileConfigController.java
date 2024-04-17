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
@RequestMapping("/infra/file/config")
@Validated
public class FileConfigController {

    @Resource
    private FileConfigService fileConfigService;

    @PostMapping
    @PreAuthorize("@ss.hasPermission('infra:file:config:create')")
    public Result<Long> createFileConfig(@Valid @RequestBody FileConfigSaveReqVO createReqVO) {
        return Result.getSuccessful(fileConfigService.createFileConfig(createReqVO));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermission('infra:file:config:update')")
    public Result<Boolean> updateFileConfig(@Valid @RequestBody FileConfigSaveReqVO updateReqVO) {
        fileConfigService.updateFileConfig(updateReqVO);
        return Result.getSuccessful(true);
    }

    @PutMapping("/master")
    @PreAuthorize("@ss.hasPermission('infra:file:config:update')")
    public Result<Boolean> updateFileConfigMaster(@RequestParam("id") Long id) {
        fileConfigService.updateFileConfigMaster(id);
        return Result.getSuccessful(true);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('infra:file:config:delete')")
    public Result<Boolean> deleteFileConfig(@PathVariable("id") Long id) {
        fileConfigService.deleteFileConfig(id);
        return Result.getSuccessful(true);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('infra:file:config:query')")
    public Result<FileConfigRespVO> getFileConfig(@PathVariable("id") Long id) {
        FileConfigDO config = fileConfigService.getFileConfig(id);
        return Result.getSuccessful(BeanUtil.toBean(config, FileConfigRespVO.class));
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermission('infra:file:config:query')")
    public Result<PageResult<FileConfigRespVO>> getFileConfigPage(@Valid FileConfigPageReqVO pageVO) {
        PageResult<FileConfigDO> pageResult = fileConfigService.getFileConfigPage(pageVO);
        return Result.getSuccessful(FileConfigConvert.INSTANCE.convert(pageResult));
    }

    @GetMapping("/test")
    @PreAuthorize("@ss.hasPermission('infra:file:config:query')")
    public Result<String> testFileConfig(@RequestParam("id") Long id) throws Exception {
        String url = fileConfigService.testFileConfig(id);
        return Result.getSuccessful(url);
    }
}
