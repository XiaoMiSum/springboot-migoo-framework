package xyz.migoo.framework.infra.enums;

import xyz.migoo.framework.common.exception.ErrorCode;

import static xyz.migoo.framework.common.core.KeyValue.of;

public class SysErrorCodeConstants {

    // ========== AUTH 模块 100200000 ==========

    public static final ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = ErrorCode.of(100200000,
            of("zh-CN", "登录失败，用户名或密码错误"), of("zh-TW", "登录失败，用户名或密码错误"),
            of("en-US", "Login failed, login name or password error"));
    public static final ErrorCode AUTH_LOGIN_USER_DISABLED = ErrorCode.of(100200001,
            of("zh-CN", "登录失败，用户已停用"), of("zh-TW", "登录失败，用户已停用"),
            of("en-US", "Login failed, User disabled"));
    public static final ErrorCode AUTH_LOGIN_FAIL_UNKNOWN = ErrorCode.of(100200002,
            of("zh-CN", "登录失败"), of("zh-TW", "登录失败"),
            of("en-US", "Login failed"));
    public static final ErrorCode AUTH_LOGIN_CAPTCHA_CODE_ERROR = ErrorCode.of(100200003,
            of("zh-CN", "验证码错误"), of("zh-TW", "验证码错误"),
            of("en-US", "Captcha error"));

    // ========== TOKEN 模块 100200100 ==========

    public static final ErrorCode AUTH_TOKEN_EXPIRED = ErrorCode.of(100200100, "Token 已经过期");
    public static final ErrorCode TOKEN_PARSE_FAIL = ErrorCode.of(100200101, "Token 解析失败");

    // ========== 菜单模块 100200200 ==========

    public static final ErrorCode MENU_NAME_DUPLICATE = ErrorCode.of(100200200, "已经存在该名字的菜单");
    public static final ErrorCode MENU_PARENT_NOT_EXISTS = ErrorCode.of(100200201, "父菜单不存在");
    public static final ErrorCode MENU_PARENT_ERROR = ErrorCode.of(100200202, "不能设置自己为父菜单");
    public static final ErrorCode MENU_NOT_EXISTS = ErrorCode.of(100200203, "菜单不存在");
    public static final ErrorCode MENU_EXISTS_CHILDREN = ErrorCode.of(100200204, "存在子菜单，无法删除");
    public static final ErrorCode MENU_PARENT_NOT_DIR_OR_MENU = ErrorCode.of(100200205, "父菜单的类型必须是目录或者菜单");

    // ========== 角色模块 100200300 ==========

    public static final ErrorCode ROLE_NOT_EXISTS = ErrorCode.of(100200300, "角色不存在");
    public static final ErrorCode ROLE_NAME_DUPLICATE = ErrorCode.of(100200301, "已经存在名为【{}】的角色");
    public static final ErrorCode ROLE_CODE_DUPLICATE = ErrorCode.of(100200302, "已经存在编码为【{}】的角色");
    public static final ErrorCode ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE = ErrorCode.of(100200304, "不能操作类型为系统内置的角色");


    // ========== 用户模块 100200400 ==========

    public static final ErrorCode USER_IS_EXISTS = ErrorCode.of(100200400, "用户账号已经存在");
    public static final ErrorCode USER_MOBILE_EXISTS = ErrorCode.of(100200401, "手机号已经存在");
    public static final ErrorCode USER_EMAIL_EXISTS = ErrorCode.of(100200402, "邮箱已经存在");
    public static final ErrorCode USER_NOT_EXISTS = ErrorCode.of(100200403, "用户不存在");
    public static final ErrorCode USER_PASSWORD_FAILED = ErrorCode.of(100200404, "用户密码校验失败");
    public static final ErrorCode USER_PASSWORD_OLD_NEW = ErrorCode.of(100200405, "操作失败，新密码与原密码一致");
    public static final ErrorCode USER_ORIGINAL_PASSWORD_UNCONFORMITY = ErrorCode.of(100200406, "操作失败，原密码错误");

    // ========== 部门模块 100200400 ==========

    public static final ErrorCode DEPT_NAME_DUPLICATE = ErrorCode.of(100200401, "已经存在该名字的部门");
    public static final ErrorCode DEPT_PARENT_NOT_EXITS = ErrorCode.of(100200402, "父级部门不存在");
    public static final ErrorCode DEPT_NOT_FOUND = ErrorCode.of(100200403, "当前部门不存在");
    public static final ErrorCode DEPT_EXITS_CHILDREN = ErrorCode.of(100200404, "存在子部门，无法删除");
    public static final ErrorCode DEPT_PARENT_ERROR = ErrorCode.of(100200405, "不能设置自己为父部门");
    public static final ErrorCode DEPT_EXISTS_USER = ErrorCode.of(100200406, "部门中存在员工，无法删除");
    public static final ErrorCode DEPT_NOT_ENABLE = ErrorCode.of(100200407, "部门不处于开启状态，不允许选择");
    public static final ErrorCode DEPT_PARENT_IS_CHILD = ErrorCode.of(100200408, "不能设置自己的子部门为父部门");

    // ========== 岗位模块 100200500 ==========

    public static final ErrorCode POST_NOT_FOUND = ErrorCode.of(1002005001, "岗位不存在");
    public static final ErrorCode POST_NOT_ENABLE = ErrorCode.of(1002005002, "岗位已禁用");
    public static final ErrorCode POST_NAME_DUPLICATE = ErrorCode.of(1002005001, "已经存在该名字的岗位");
    public static final ErrorCode POST_CODE_DUPLICATE = ErrorCode.of(1002005001, "已经存在该标识的岗位");

    static {
        ErrorCode.put(SysErrorCodeConstants.class);
    }
}
