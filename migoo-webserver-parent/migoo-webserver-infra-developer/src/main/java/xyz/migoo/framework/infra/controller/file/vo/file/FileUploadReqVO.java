package xyz.migoo.framework.infra.controller.file.vo.file;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class FileUploadReqVO {

    @NotNull(message = "文件附件不能为空")
    private MultipartFile file;

    private String path;

    private String source;

}
