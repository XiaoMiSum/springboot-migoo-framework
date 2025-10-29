package xyz.migoo.framework.infra.enums;

import xyz.migoo.framework.common.exception.ErrorCode;

public class SysErrorCodeConstants {

    // ========== AUTH 模块 100200000 ==========

    public static final ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = ErrorCode.of(100200000, "infra.login.bad.credentials");
    public static final ErrorCode AUTH_LOGIN_USER_DISABLED = ErrorCode.of(100200001, "infra.login.user.disabled");
    public static final ErrorCode AUTH_LOGIN_FAIL_UNKNOWN = ErrorCode.of(100200002, "infra.login.failed");

    // ========== TOKEN 模块 100200100 ==========

    public static final ErrorCode AUTH_TOKEN_EXPIRED = ErrorCode.of(100200100, "infra.token.expired");
    public static final ErrorCode TOKEN_PARSE_FAIL = ErrorCode.of(100200101, "infra.token.bad");

    // ========== 菜单模块 100200200 ==========

    public static final ErrorCode MENU_NAME_DUPLICATE = ErrorCode.of(100200200, "infra.menu.exists");
    public static final ErrorCode MENU_PARENT_NOT_EXISTS = ErrorCode.of(100200201, "infra.menu.parent.not.exists");
    public static final ErrorCode MENU_PARENT_ERROR = ErrorCode.of(100200202, "infra.menu.parent.error");
    public static final ErrorCode MENU_NOT_EXISTS = ErrorCode.of(100200203, "infra.menu.not.exists");
    public static final ErrorCode MENU_EXISTS_CHILDREN = ErrorCode.of(100200204, "infra.menu.has.children");
    public static final ErrorCode MENU_PARENT_NOT_DIR_OR_MENU = ErrorCode.of(100200205, "infra.menu.parent.type.error");

    // ========== 角色模块 100200300 ==========

    public static final ErrorCode ROLE_NOT_EXISTS = ErrorCode.of(100200300, "infra.role.not.exists");
    public static final ErrorCode ROLE_NAME_DUPLICATE = ErrorCode.of(100200301, "infra.role.name.exists");
    public static final ErrorCode ROLE_CODE_DUPLICATE = ErrorCode.of(100200302, "infra.role.code.exists");
    public static final ErrorCode ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE = ErrorCode.of(100200304, "infra.role.type.error");


    // ========== 用户模块 100200400 ==========

    public static final ErrorCode USER_IS_EXISTS = ErrorCode.of(100200400, "infra.user.username.exists");
    public static final ErrorCode USER_PHONE_EXISTS = ErrorCode.of(100200401, "infra.user.phone.exists");
    public static final ErrorCode USER_EMAIL_EXISTS = ErrorCode.of(100200402, "infra.user.email.exists");
    public static final ErrorCode USER_NOT_EXISTS = ErrorCode.of(100200403, "infra.user.not.exists");
    public static final ErrorCode USER_PASSWORD_FAILED = ErrorCode.of(100200404, "infra.user.password.failed");
    public static final ErrorCode USER_PASSWORD_OLD_NEW = ErrorCode.of(100200405, "infra.user.password.new.old.same");
    public static final ErrorCode USER_ORIGINAL_PASSWORD_UNCONFORMITY = ErrorCode.of(100200406, "infra.user.password.old.error");

    // ========== 部门模块 100200400 ==========

    public static final ErrorCode DEPT_NAME_DUPLICATE = ErrorCode.of(100200401, "infra.dept.name.exists");
    public static final ErrorCode DEPT_PARENT_NOT_EXITS = ErrorCode.of(100200402, "infra.dept.parent.not.exists");
    public static final ErrorCode DEPT_NOT_FOUND = ErrorCode.of(100200403, "infra.dept.not.exists");
    public static final ErrorCode DEPT_EXITS_CHILDREN = ErrorCode.of(100200404, "infra.dept.has.children");
    public static final ErrorCode DEPT_PARENT_ERROR = ErrorCode.of(100200405, "infra.dept.parent.error");
    public static final ErrorCode DEPT_EXISTS_USER = ErrorCode.of(100200406, "infra.dept.has.employees");
    public static final ErrorCode DEPT_NOT_ENABLE = ErrorCode.of(100200407, "infra.dept.disabled");
    public static final ErrorCode DEPT_PARENT_IS_CHILD = ErrorCode.of(100200408, "infra.dept.parent.error");

    // ========== 岗位模块 100200500 ==========

    public static final ErrorCode POST_NOT_FOUND = ErrorCode.of(1002005001, "infra.post.not.exists");
    public static final ErrorCode POST_NOT_ENABLE = ErrorCode.of(1002005002, "infra.post.disabled");
    public static final ErrorCode POST_NAME_DUPLICATE = ErrorCode.of(1002005001, "infra.post.name.exists");
    public static final ErrorCode POST_CODE_DUPLICATE = ErrorCode.of(1002005001, "infra.post.code.exists");

}