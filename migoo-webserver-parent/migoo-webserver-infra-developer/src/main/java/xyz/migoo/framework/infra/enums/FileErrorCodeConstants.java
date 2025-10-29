package xyz.migoo.framework.infra.enums;

import xyz.migoo.framework.common.exception.ErrorCode;

/**
 * Infra 错误码枚举类
 * <p>
 * infra 系统，使用 1-001-000-000 段
 */
public class FileErrorCodeConstants {


    // ========= 文件相关 1-001-003-000 =================
    public static final ErrorCode FILE_PATH_EXISTS = ErrorCode.of(1006013000, "infra.file.path.exists");
    public static final ErrorCode FILE_NOT_EXISTS = ErrorCode.of(1006013001, "infra.file.path.not.exists");
    public static final ErrorCode FILE_IS_EMPTY = ErrorCode.of(1006013002, "infra.file.empty");

    // ========== 文件配置 1-001-006-000 ==========
    public static final ErrorCode FILE_CONFIG_NOT_EXISTS = ErrorCode.of(1006014001, "infra.file.config.not.exists");
    public static final ErrorCode FILE_CONFIG_DELETE_FAIL_MASTER = ErrorCode.of(1006014002, "infra.file.config.master");

}
