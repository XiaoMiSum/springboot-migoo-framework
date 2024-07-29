package xyz.migoo.framework.infra.enums;

import xyz.migoo.framework.common.exception.ErrorCode;

public class DictionaryErrorCodeConstants {


    // ========== 字典模块 1006010000 ==========
    public static final ErrorCode DICTIONARY_KEY_NOT_EXISTS = ErrorCode.of(1006010000, "字典不存在");
    public static final ErrorCode DICTIONARY_KEY_EXISTS = ErrorCode.of(1006010001, "字典已存在");

    static {
        ErrorCode.put(DictionaryErrorCodeConstants.class);
    }
}
