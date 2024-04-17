package xyz.migoo.framework.oss.core.client;

import lombok.Data;

@Data
public class FileContentDTO {

    private Long id;

    private byte[] content;

    private String type;

    private String path;

    private Integer configId;

    private String source;
}
