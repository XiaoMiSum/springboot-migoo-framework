package xyz.migoo.framework.infra.controller.file.vo.file;

import lombok.Data;

import java.util.Date;

@Data
public class FileRespVO {

    private Long id;

    private Long configId;

    private String path;

    private String name;

    private String url;

    private String type;

    private Integer size;

    private Date createTime;

}
