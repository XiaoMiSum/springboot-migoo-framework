package xyz.migoo.framework.infra.controller.file;

import cn.hutool.core.io.IoUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.infra.controller.file.vo.file.*;
import xyz.migoo.framework.infra.convert.file.FileContentConvert;
import xyz.migoo.framework.infra.dal.dataobject.file.FileDO;
import xyz.migoo.framework.infra.service.file.FileService;

@RestController
@RequestMapping("/developer/file")
@Validated
@Slf4j
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping
    public Result<String> uploadFile(FileUploadReqVO uploadReqVO) throws Exception {
        MultipartFile file = uploadReqVO.getFile();
        String path = uploadReqVO.getPath();
        return Result.getSuccessful(fileService.createFile(file.getOriginalFilename(), path,
                IoUtil.readBytes(file.getInputStream()), uploadReqVO.getSource()));
    }

    @PostMapping("/create")
    public Result<Long> createFile(@Valid @RequestBody FileCreateReqVO createReqVO) {
        return Result.getSuccessful(fileService.createFile(createReqVO));
    }

    @GetMapping("/presigned-url")
    public Result<FilePresignedUrlRespVO> getFilePresignedUrl(@RequestParam("path") String path) throws Exception {
        return Result.getSuccessful(fileService.getFilePresignedUrl(path));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('developer:file:remove')")
    public Result<Boolean> deleteFile(@PathVariable("id") Long id) throws Exception {
        fileService.deleteFile(id);
        return Result.getSuccessful(true);
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermission('developer:file:query')")
    public Result<PageResult<FileRespVO>> getFilePage(@Valid FilePageReqVO pageVO) {
        PageResult<FileDO> pageResult = fileService.getFilePage(pageVO);
        return Result.getSuccessful(FileContentConvert.INSTANCE.convert(pageResult));
    }

}
