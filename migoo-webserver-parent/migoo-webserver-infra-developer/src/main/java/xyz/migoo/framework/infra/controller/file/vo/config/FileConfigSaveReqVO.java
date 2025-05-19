package xyz.migoo.framework.infra.controller.file.vo.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class FileConfigSaveReqVO {

    private Long id;

    @NotNull(message = "配置名不能为空")
    private String name;

    @NotNull(message = "存储器不能为空")
    private Integer storage;

    @NotNull(message = "存储配置不能为空")
    private Map<String, Object> config;

    private String remark;

}
