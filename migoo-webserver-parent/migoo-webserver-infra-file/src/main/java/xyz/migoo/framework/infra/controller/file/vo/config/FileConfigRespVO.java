package xyz.migoo.framework.infra.controller.file.vo.config;

import lombok.Data;
import xyz.migoo.framework.oss.core.client.FileClientConfig;

import java.util.Date;

@Data
public class FileConfigRespVO {

    private Long id;

    private String name;

    private Integer storage;

    private Boolean master;

    private FileClientConfig config;

    private String remark;

    private Date createTime;

}
