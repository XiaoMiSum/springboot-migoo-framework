package xyz.migoo.framework.infra.controller.file;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.migoo.framework.common.pojo.PageResult;
import xyz.migoo.framework.common.pojo.Result;
import xyz.migoo.framework.common.util.servlet.ServletUtils;
import xyz.migoo.framework.infra.controller.file.vo.file.*;
import xyz.migoo.framework.infra.convert.file.FileContentConvert;
import xyz.migoo.framework.infra.dal.dataobject.file.FileDO;
import xyz.migoo.framework.infra.service.file.FileService;

@RestController
@RequestMapping("/infra/file")
@Validated
@Slf4j
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    public Result<String> uploadFile(FileUploadReqVO uploadReqVO) throws Exception {
        MultipartFile file = uploadReqVO.getFile();
        String path = uploadReqVO.getPath();
        return Result.getSuccessful(fileService.createFile(file.getOriginalFilename(), path,
                IoUtil.readBytes(file.getInputStream()), uploadReqVO.getSource()));
    }

    @GetMapping("/presigned-url")
    public Result<FilePresignedUrlRespVO> getFilePresignedUrl(@RequestParam("path") String path) throws Exception {
        return Result.getSuccessful(fileService.getFilePresignedUrl(path));
    }

    @PostMapping("/create")
    public Result<Long> createFile(@Valid @RequestBody FileCreateReqVO createReqVO) {
        return Result.getSuccessful(fileService.createFile(createReqVO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('infra:file:remove')")
    public Result<Boolean> deleteFile(@PathVariable("id") Long id) throws Exception {
        fileService.deleteFile(id);
        return Result.getSuccessful(true);
    }

    @GetMapping("/{configId}/**")
    @PermitAll
    public void getFileContent(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable("configId") Long configId) throws Exception {
        // 获取请求的路径
        String path = StrUtil.subAfter(request.getRequestURI(), "/get/", false);
        if (StrUtil.isEmpty(path)) {
            throw new IllegalArgumentException("结尾的 path 路径必须传递");
        }
        // 解码，解决中文路径的问题 https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/807/
        path = URLUtil.decode(path);

        // 读取内容
        byte[] content = fileService.getFileContent(configId, path);
        if (content == null) {
            log.warn("[getFileContent][configId({}) path({}) 文件不存在]", configId, path);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        ServletUtils.writeAttachment(response, path, content);
    }

    @GetMapping
    @PreAuthorize("@ss.hasPermission('infra:file:query')")
    public Result<PageResult<FileRespVO>> getFilePage(@Valid FilePageReqVO pageVO) {
        PageResult<FileDO> pageResult = fileService.getFilePage(pageVO);
        return Result.getSuccessful(FileContentConvert.INSTANCE.convert(pageResult));
    }

}
