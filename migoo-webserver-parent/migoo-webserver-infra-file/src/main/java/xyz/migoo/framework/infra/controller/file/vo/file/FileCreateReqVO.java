package xyz.migoo.framework.infra.controller.file.vo.file;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class FileCreateReqVO {

    @NotNull(message = "文件配置编号不能为空")
    private Long configId;

    @NotNull(message = "文件路径不能为空")
    private String path;

    @NotNull(message = "原文件名不能为空")
    private String name;

    @NotNull(message = "文件 URL不能为空")
    private String url;

    private String type;

    private Integer size;

}
