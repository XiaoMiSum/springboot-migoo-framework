package xyz.migoo.framework.infra.controller.file;

import cn.hutool.core.util.URLUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.migoo.framework.common.util.servlet.ServletUtils;
import xyz.migoo.framework.infra.service.file.FileService;

@RestController
@RequestMapping("/d/f")
@Validated
@Slf4j
public class DownloadController {

    @Resource
    private FileService fileService;


    @GetMapping("/{configId}/**")
    public void getFileContent(HttpServletRequest request, HttpServletResponse response,
                               @PathVariable("configId") Long configId) throws Exception {
        // 获取请求的路径
        // 解码，解决中文路径的问题 https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/807/
        String path = URLUtil.decode(request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1));

        // 读取内容
        byte[] content = fileService.getFileContent(configId, path);
        if (content == null) {
            log.warn("[getFileContent][configId({}) path({}) 文件不存在]", configId, path);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        ServletUtils.writeAttachment(response, path, content);
    }
}
