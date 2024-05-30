package xyz.migoo.framework.infra.enums;

import xyz.migoo.framework.common.exception.ErrorCode;

public interface DictionaryErrorCodeConstants {


    // ========== 字典模块 1006010000 ==========
    ErrorCode DICTIONARY_KEY_NOT_EXISTS = new ErrorCode(1006010000, "字典不存在");
    ErrorCode DICTIONARY_KEY_EXISTS = new ErrorCode(1006010001, "字典已存在");

    // ========== 模块 99999999 ==========
    ErrorCode ERROR = new ErrorCode(99999999, "异常操作");
}
