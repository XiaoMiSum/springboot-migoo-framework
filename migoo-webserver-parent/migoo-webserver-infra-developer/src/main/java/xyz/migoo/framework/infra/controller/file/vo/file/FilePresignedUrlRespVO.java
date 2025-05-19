package xyz.migoo.framework.infra.controller.file.vo.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FilePresignedUrlRespVO {

    private Long configId;

    private String uploadUrl;

    /**
     * 为什么要返回 url 字段？
     * <p>
     * 前端上传完文件后，需要使用该 URL 进行访问
     */
    private String url;

}
