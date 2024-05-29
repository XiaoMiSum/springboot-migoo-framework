/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80100 (8.1.0)
 Source Host           : 127.0.0.1:3306
 Source Schema         : ones

 Target Server Type    : MySQL
 Target Server Version : 80100 (8.1.0)
 File Encoding         : 65001

 Date: 21/04/2024 13:04:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for infra_dictionary
-- ----------------------------
DROP TABLE IF EXISTS `infra_dictionary`;
CREATE TABLE `infra_dictionary`
(
    `id`          int         NOT NULL AUTO_INCREMENT,
    `code`        varchar(64) NOT NULL COMMENT '字典键值',
    `name`        varchar(32) NOT NULL COMMENT '字典名称',
    `source`      varchar(64) NULL DEFAULT NULL COMMENT '来源系统',
    `status`      tinyint     NULL DEFAULT 1,
    `deleted`     bit(1)      NULL DEFAULT b'0',
    `creator`     varchar(64) NULL DEFAULT NULL,
    `create_time` datetime    NULL DEFAULT NULL,
    `updater`     varchar(64) NULL DEFAULT NULL,
    `update_time` datetime    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `inx` (`code` ASC, `name` ASC, `source` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 12 COMMENT = '字典信息表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of infra_dictionary
-- ----------------------------
INSERT INTO `infra_dictionary`
VALUES (1, 'common_status', '状态', 'ones', 1, b'0', '超级管理员', '2024-05-23 11:04:26', '超级管理员',
        '2024-05-23 14:47:43');
INSERT INTO `infra_dictionary`
VALUES (2, 'system_role_type', '角色类型', 'ones', 1, b'0', '超级管理员', '2024-05-23 14:55:32', '超级管理员',
        '2024-05-23 14:55:32');
INSERT INTO `infra_dictionary`
VALUES (3, 'system_user_gender', '性别', 'ones', 1, b'0', '超级管理员', '2024-05-23 15:29:04', '超级管理员',
        '2024-05-23 15:29:04');
INSERT INTO `infra_dictionary`
VALUES (4, 'system_menu_type', '菜单类型', 'ones', 1, b'0', '超级管理员', '2024-05-23 15:37:52', '超级管理员',
        '2024-05-23 15:37:52');
INSERT INTO `infra_dictionary`
VALUES (5, 'infra_job_status', '定时任务状态', 'ones', 1, b'0', '超级管理员', '2024-05-23 15:45:29', '超级管理员',
        '2024-05-23 15:45:29');
INSERT INTO `infra_dictionary`
VALUES (6, 'infra_job_log_status', '定时任务调度状态', 'ones', 1, b'0', '超级管理员', '2024-05-23 15:45:59',
        '超级管理员', '2024-05-23 15:49:54');
INSERT INTO `infra_dictionary`
VALUES (7, 'infra_api_error_log_process_status', '异常日志处理状态', 'ones', 1, b'0', '超级管理员',
        '2024-05-23 15:46:22', '超级管理员', '2024-05-23 15:46:22');
INSERT INTO `infra_dictionary`
VALUES (8, 'infra_file_storage', '文件存储类型', 'ones', 1, b'0', '超级管理员', '2024-05-23 15:50:50', '超级管理员',
        '2024-05-23 15:50:50');
INSERT INTO `infra_dictionary`
VALUES (9, 'infra_boolean_string', '布尔类型字符', 'ones', 1, b'0', '超级管理员', '2024-05-23 15:51:11', '超级管理员',
        '2024-05-23 15:51:11');
INSERT INTO `infra_dictionary`
VALUES (10, 'infra_sms_send_status', '短信发送状态', 'ones', 1, b'0', '超级管理员', '2024-05-23 16:21:22', '超级管理员',
        '2024-05-23 16:21:22');
INSERT INTO `infra_dictionary`
VALUES (11, 'infra_sms_receive_status', '短信接收状态', 'ones', 1, b'0', '超级管理员', '2024-05-23 16:21:46',
        '超级管理员', '2024-05-23 16:21:46');

-- ----------------------------
-- Table structure for infra_dictionary_value
-- ----------------------------
DROP TABLE IF EXISTS `infra_dictionary_value`;
CREATE TABLE `infra_dictionary_value`
(
    `id`          int         NOT NULL AUTO_INCREMENT,
    `dict_code`   varchar(64) NOT NULL COMMENT '所属字典',
    `label`       varchar(10) NOT NULL COMMENT '页面展示名称',
    `value`       varchar(64) NOT NULL COMMENT '字典数据值',
    `status`      tinyint     NULL DEFAULT 1,
    `color_type`  varchar(20) NULL DEFAULT NULL,
    `sort`        int         NULL DEFAULT 0,
    `deleted`     bit(1)      NULL DEFAULT b'0',
    `creator`     varchar(64) NULL DEFAULT NULL,
    `create_time` datetime    NULL DEFAULT NULL,
    `updater`     varchar(64) NULL DEFAULT NULL,
    `update_time` datetime    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `inx_dict_key` (`dict_code` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 31 COMMENT = '字典数据表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of infra_dictionary_value
-- ----------------------------
INSERT INTO `infra_dictionary_value`
VALUES (1, 'common_status', '启用', '1', 1, 'success', 0, b'0', '超级管理员', '2024-05-23 11:30:28', '超级管理员',
        '2024-05-23 14:47:51');
INSERT INTO `infra_dictionary_value`
VALUES (2, 'common_status', '禁用', '0', 1, 'danger', 1, b'0', '超级管理员', '2024-05-23 11:38:25', '超级管理员',
        '2024-05-23 11:38:25');
INSERT INTO `infra_dictionary_value`
VALUES (3, 'system_role_type', '内置角色', '1', 1, 'danger', 0, b'0', '超级管理员', '2024-05-23 14:56:15', '超级管理员',
        '2024-05-23 14:56:15');
INSERT INTO `infra_dictionary_value`
VALUES (4, 'system_role_type', '自定义角色', '2', 1, 'primary', 1, b'0', '超级管理员', '2024-05-23 14:56:37',
        '超级管理员', '2024-05-23 14:56:44');
INSERT INTO `infra_dictionary_value`
VALUES (5, 'system_user_gender', '男', '1', 1, 'default', 0, b'0', '超级管理员', '2024-05-23 15:29:43', '超级管理员',
        '2024-05-23 15:29:43');
INSERT INTO `infra_dictionary_value`
VALUES (6, 'system_user_gender', '女', '0', 1, 'default', 1, b'0', '超级管理员', '2024-05-23 15:30:00', '超级管理员',
        '2024-05-23 15:30:00');
INSERT INTO `infra_dictionary_value`
VALUES (7, 'system_menu_type', '目录', '1', 1, 'danger', 0, b'0', '超级管理员', '2024-05-23 15:39:51', '超级管理员',
        '2024-05-23 15:40:28');
INSERT INTO `infra_dictionary_value`
VALUES (8, 'system_menu_type', '菜单', '2', 1, 'success', 1, b'0', '超级管理员', '2024-05-23 15:40:02', '超级管理员',
        '2024-05-23 15:40:25');
INSERT INTO `infra_dictionary_value`
VALUES (9, 'system_menu_type', '按钮', '3', 1, 'info', 2, b'0', '超级管理员', '2024-05-23 15:40:19', '超级管理员',
        '2024-05-23 15:40:19');
INSERT INTO `infra_dictionary_value`
VALUES (10, 'infra_job_status', '初始化', '0', 1, 'info', 0, b'0', '超级管理员', '2024-05-23 15:47:31', '超级管理员',
        '2024-05-23 15:47:31');
INSERT INTO `infra_dictionary_value`
VALUES (11, 'infra_job_status', '运行中', '1', 1, 'success', 1, b'0', '超级管理员', '2024-05-23 15:47:45', '超级管理员',
        '2024-05-23 15:47:45');
INSERT INTO `infra_dictionary_value`
VALUES (12, 'infra_job_status', '已暂停', '2', 1, 'danger', 2, b'0', '超级管理员', '2024-05-23 15:47:59', '超级管理员',
        '2024-05-23 15:47:59');
INSERT INTO `infra_dictionary_value`
VALUES (13, 'infra_job_log_status', '调度中', '0', 1, 'info', 0, b'0', '超级管理员', '2024-05-23 15:48:33',
        '超级管理员', '2024-05-23 15:48:33');
INSERT INTO `infra_dictionary_value`
VALUES (14, 'infra_job_log_status', '调度成功', '1', 1, 'success', 1, b'0', '超级管理员', '2024-05-23 15:48:41',
        '超级管理员', '2024-05-23 15:48:41');
INSERT INTO `infra_dictionary_value`
VALUES (15, 'infra_job_log_status', '调度失败', '2', 1, 'danger', 2, b'0', '超级管理员', '2024-05-23 15:48:51',
        '超级管理员', '2024-05-23 15:48:51');
INSERT INTO `infra_dictionary_value`
VALUES (16, 'infra_api_error_log_process_status', '未处理', '0', 1, 'danger', 0, b'0', '超级管理员',
        '2024-05-23 15:49:18', '超级管理员', '2024-05-23 15:49:18');
INSERT INTO `infra_dictionary_value`
VALUES (17, 'infra_api_error_log_process_status', '已处理', '1', 1, 'success', 1, b'0', '超级管理员',
        '2024-05-23 15:49:30', '超级管理员', '2024-05-23 15:49:30');
INSERT INTO `infra_dictionary_value`
VALUES (18, 'infra_api_error_log_process_status', '已忽略', '2', 1, 'info', 2, b'0', '超级管理员',
        '2024-05-23 15:49:43', '超级管理员', '2024-05-23 15:49:43');
INSERT INTO `infra_dictionary_value`
VALUES (19, 'infra_file_storage', '数据库', '1', 1, 'default', 0, b'0', '超级管理员', '2024-05-23 15:52:23',
        '超级管理员', '2024-05-23 15:52:23');
INSERT INTO `infra_dictionary_value`
VALUES (20, 'infra_file_storage', '本地存储', '10', 1, 'warning', 10, b'0', '超级管理员', '2024-05-23 15:52:39',
        '超级管理员', '2024-05-23 15:52:39');
INSERT INTO `infra_dictionary_value`
VALUES (21, 'infra_file_storage', 'S3协议', '20', 1, 'danger', 20, b'0', '超级管理员', '2024-05-23 15:52:56',
        '超级管理员', '2024-05-23 15:52:56');
INSERT INTO `infra_dictionary_value`
VALUES (22, 'infra_boolean_string', '是', 'true', 1, 'success', 0, b'0', '超级管理员', '2024-05-23 15:53:39',
        '超级管理员', '2024-05-23 15:53:39');
INSERT INTO `infra_dictionary_value`
VALUES (23, 'infra_boolean_string', '否', 'false', 1, 'info', 1, b'0', '超级管理员', '2024-05-23 15:53:54',
        '超级管理员', '2024-05-23 15:53:54');
INSERT INTO `infra_dictionary_value`
VALUES (24, 'infra_sms_send_status', '初始化', '0', 1, 'info', 0, b'0', '超级管理员', '2024-05-23 16:22:19',
        '超级管理员', '2024-05-23 16:22:19');
INSERT INTO `infra_dictionary_value`
VALUES (25, 'infra_sms_send_status', '发送成功', '10', 1, 'success', 10, b'0', '超级管理员', '2024-05-23 16:22:40',
        '超级管理员', '2024-05-23 16:22:40');
INSERT INTO `infra_dictionary_value`
VALUES (26, 'infra_sms_send_status', '发送失败', '20', 1, 'danger', 20, b'0', '超级管理员', '2024-05-23 16:22:52',
        '超级管理员', '2024-05-23 16:22:52');
INSERT INTO `infra_dictionary_value`
VALUES (27, 'infra_sms_send_status', '仅日志', '30', 1, 'warning', 30, b'0', '超级管理员', '2024-05-23 16:23:35',
        '超级管理员', '2024-05-23 16:23:35');
INSERT INTO `infra_dictionary_value`
VALUES (28, 'infra_sms_receive_status', '初始化', '0', 1, 'info', 0, b'0', '超级管理员', '2024-05-23 16:24:21',
        '超级管理员', '2024-05-23 16:24:21');
INSERT INTO `infra_dictionary_value`
VALUES (29, 'infra_sms_receive_status', '接收成功', '10', 1, 'success', 10, b'0', '超级管理员', '2024-05-23 16:24:36',
        '超级管理员', '2024-05-23 16:24:36');
INSERT INTO `infra_dictionary_value`
VALUES (30, 'infra_sms_receive_status', '接收失败', '20', 1, 'danger', 20, b'0', '超级管理员', '2024-05-23 16:24:46',
        '超级管理员', '2024-05-23 16:24:46');

-- ----------------------------
-- Table structure for infra_error_log
-- ----------------------------
DROP TABLE IF EXISTS `infra_error_log`;
CREATE TABLE `infra_error_log`
(
    `id`                           int          NOT NULL AUTO_INCREMENT,
    `application_name`             varchar(255) NULL DEFAULT NULL COMMENT '应用名称',
    `request_method`               varchar(255) NULL DEFAULT NULL COMMENT '请求方法',
    `request_url`                  text         NULL COMMENT '请求地址',
    `request_params`               text         NULL COMMENT '请求参数',
    `user_ip`                      varchar(255) NULL DEFAULT NULL COMMENT '来源ip',
    `exception_time`               datetime     NULL DEFAULT NULL COMMENT '异常时间',
    `exception_name`               varchar(255) NULL DEFAULT NULL COMMENT '异常名称',
    `exception_class_name`         varchar(255) NULL DEFAULT NULL COMMENT '异常类',
    `exception_file_name`          varchar(255) NULL DEFAULT NULL COMMENT '异常文件',
    `exception_method_name`        varchar(255) NULL DEFAULT NULL COMMENT '异常方法',
    `exception_line_number`        int          NULL DEFAULT NULL COMMENT '异常行',
    `exception_stack_trace`        text         NULL COMMENT '堆栈信息',
    `exception_root_cause_message` text         NULL,
    `exception_message`            text         NULL,
    `status`                       tinyint      NULL DEFAULT 0,
    `deleted`                      tinyint      NULL DEFAULT 0,
    `creator`                      varchar(64)  NULL DEFAULT NULL,
    `create_time`                  datetime     NULL DEFAULT NULL,
    `updater`                      varchar(64)  NULL DEFAULT NULL,
    `update_time`                  datetime     NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1 COMMENT = '接口异常表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of infra_error_log
-- ----------------------------

-- ----------------------------
-- Table structure for infra_job
-- ----------------------------
DROP TABLE IF EXISTS `infra_job`;
CREATE TABLE `infra_job`
(
    `id`              bigint      NOT NULL AUTO_INCREMENT COMMENT '任务编号',
    `name`            varchar(32) NOT NULL COMMENT '任务名称',
    `status`          tinyint     NOT NULL COMMENT '任务状态',
    `handler_name`    varchar(64) NOT NULL COMMENT '处理器的名字',
    `handler_param`   text        NULL COMMENT '处理器的参数',
    `cron_expression` varchar(32) NOT NULL COMMENT 'CRON 表达式',
    `retry_count`     int         NOT NULL DEFAULT 0 COMMENT '重试次数',
    `retry_interval`  int         NOT NULL DEFAULT 0 COMMENT '重试间隔',
    `monitor_timeout` int         NOT NULL DEFAULT 0 COMMENT '监控超时时间',
    `creator`         varchar(64) NULL     DEFAULT '' COMMENT '创建者',
    `create_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         varchar(64) NULL     DEFAULT '' COMMENT '更新者',
    `update_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1 COMMENT = '定时任务表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of infra_job
-- ----------------------------

-- ----------------------------
-- Table structure for infra_job_log
-- ----------------------------
DROP TABLE IF EXISTS `infra_job_log`;
CREATE TABLE `infra_job_log`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '日志编号',
    `job_id`        bigint       NOT NULL COMMENT '任务编号',
    `handler_name`  varchar(64)  NOT NULL COMMENT '处理器的名字',
    `handler_param` varchar(255) NULL     DEFAULT NULL COMMENT '处理器的参数',
    `execute_index` tinyint      NOT NULL DEFAULT 1 COMMENT '第几次执行',
    `begin_time`    datetime     NOT NULL COMMENT '开始执行时间',
    `end_time`      datetime     NULL     DEFAULT NULL COMMENT '结束执行时间',
    `duration`      int          NULL     DEFAULT NULL COMMENT '执行时长',
    `status`        tinyint      NOT NULL COMMENT '任务状态',
    `result`        longtext     NULL COMMENT '结果数据',
    `deleted`       bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `creator`       varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1 COMMENT = '定时任务日志表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of infra_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for infra_sms_channel
-- ----------------------------
DROP TABLE IF EXISTS `infra_sms_channel`;
CREATE TABLE `infra_sms_channel`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `signature`    varchar(12)  NOT NULL COMMENT '短信签名',
    `code`         varchar(63)  NOT NULL COMMENT '三方编码',
    `status`       tinyint      NULL     DEFAULT 1 COMMENT '开启状态',
    `api_key`      varchar(128) NOT NULL COMMENT '短信 API 的账号',
    `api_secret`   varchar(128) NULL     DEFAULT NULL COMMENT '短信 API 的秘钥',
    `callback_url` varchar(255) NULL     DEFAULT NULL COMMENT '短信发送回调 URL',
    `remark`       varchar(255) NULL     DEFAULT '' COMMENT '备注',
    `creator`      varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2 COMMENT = '短信三方'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of infra_sms_channel
-- ----------------------------
INSERT INTO `infra_sms_channel`
VALUES (1, 'BARK', 'BARK', 1, 'BARK', 'BARK', 'http://BARK.top', NULL, '超级管理员', '2023-09-23 15:28:36',
        '超级管理员', '2023-09-23 15:30:34', b'0');

-- ----------------------------
-- Table structure for infra_sms_log
-- ----------------------------
DROP TABLE IF EXISTS `infra_sms_log`;
CREATE TABLE `infra_sms_log`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `channel_id`       bigint       NOT NULL COMMENT '短信三方编号',
    `channel_code`     varchar(63)  NOT NULL COMMENT '短信三方编码',
    `template_id`      bigint       NOT NULL COMMENT '模板编号',
    `template_code`    varchar(63)  NOT NULL COMMENT '模板编码',
    `template_content` varchar(255) NOT NULL COMMENT '短信内容',
    `template_params`  varchar(255) NOT NULL COMMENT '短信参数',
    `api_template_id`  varchar(63)  NOT NULL COMMENT '短信 API 的模板编号',
    `mobile`           varchar(64)  NOT NULL COMMENT '手机号',
    `user_id`          bigint       NULL     DEFAULT NULL COMMENT '用户编号',
    `user_type`        tinyint      NULL     DEFAULT NULL COMMENT '用户类型',
    `send_status`      tinyint      NOT NULL DEFAULT 0 COMMENT '发送状态',
    `send_time`        datetime     NULL     DEFAULT NULL COMMENT '发送时间',
    `send_code`        int          NULL     DEFAULT NULL COMMENT '发送结果的编码',
    `send_msg`         varchar(255) NULL     DEFAULT NULL COMMENT '发送结果的提示',
    `api_send_code`    varchar(63)  NULL     DEFAULT NULL COMMENT '短信 API 发送结果的编码',
    `api_send_msg`     varchar(255) NULL     DEFAULT NULL COMMENT '短信 API 发送失败的提示',
    `api_request_id`   varchar(255) NULL     DEFAULT NULL COMMENT '短信 API 发送返回的唯一请求 ID',
    `api_serial_no`    varchar(255) NULL     DEFAULT NULL COMMENT '短信 API 发送返回的序号',
    `receive_status`   tinyint      NOT NULL DEFAULT 0 COMMENT '接收状态',
    `receive_time`     datetime     NULL     DEFAULT NULL COMMENT '接收时间',
    `api_receive_code` varchar(63)  NULL     DEFAULT NULL COMMENT 'API 接收结果的编码',
    `api_receive_msg`  varchar(255) NULL     DEFAULT NULL COMMENT 'API 接收结果的说明',
    `creator`          varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`          varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1 COMMENT = '短信日志'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of infra_sms_log
-- ----------------------------

-- ----------------------------
-- Table structure for infra_sms_template
-- ----------------------------
DROP TABLE IF EXISTS `infra_sms_template`;
CREATE TABLE `infra_sms_template`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `status`          tinyint      NULL     DEFAULT 1 COMMENT '开启状态',
    `code`            varchar(63)  NOT NULL COMMENT '模板编码',
    `name`            varchar(63)  NOT NULL COMMENT '模板名称',
    `content`         varchar(255) NOT NULL COMMENT '模板内容',
    `params`          varchar(255) NOT NULL COMMENT '参数数组',
    `api_template_id` varchar(63)  NOT NULL COMMENT '短信 API 的模板编号',
    `channel_id`      bigint       NOT NULL COMMENT '短信三方编号',
    `channel_code`    varchar(63)  NOT NULL COMMENT '短信三方编码',
    `remark`          varchar(255) NULL     DEFAULT '' COMMENT '备注',
    `creator`         varchar(64)  NULL     DEFAULT '' COMMENT '创建者',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         varchar(64)  NULL     DEFAULT '' COMMENT '更新者',
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1 COMMENT = '短信模板'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of infra_sms_template
-- ----------------------------

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu`
VALUES (2, '研发工具', 'developer', 1, 9999, 0, '/developer', 'fa:connectdevelop', NULL, NULL, 1, 1, 0, 1, 0, '', NULL,
        '超级管理员', '2023-09-27 22:51:30');
INSERT INTO `sys_menu`
VALUES (107, '任务管理', '', 1, 0, 2, 'job', 'fa-solid:tasks', '', '', 1, 1, 1, 1, 0, '超级管理员',
        '2023-10-01 14:50:25', '超级管理员', '2024-04-14 12:58:36');
INSERT INTO `sys_menu`
VALUES (108, '异常日志', 'developer:error-log:query', 2, 2, 2, 'error-log', 'fa-solid:bug', 'Bug',
        'developer/errorlog/index', 1, 1, 1, 1, 0, '超级管理员', '2022-07-10 13:54:45', '超级管理员',
        '2024-04-14 12:57:39');
INSERT INTO `sys_menu`
VALUES (110, '短信管理', '', 1, 5, 2, 'message', 'ep:message', NULL, NULL, 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-06 17:17:16', '超级管理员', '2024-04-14 12:57:34');
INSERT INTO `sys_menu`
VALUES (130, '定时任务', 'developer:job:query', 2, 0, 107, 'list', 'fa-solid:tasks', 'JobIndex', 'developer/job/index',
        1, 1, 1, 1, 0, '超级管理员', '2023-03-17 17:10:47', '超级管理员', '2024-04-14 13:00:20');
INSERT INTO `sys_menu`
VALUES (131, '调度日志', '', 2, 1, 107, 'log', 'ep:document', 'JobLog', 'developer/job/logger/index', 1, 1, 1, 1, 0,
        '超级管理员', '2023-03-18 13:50:00', '超级管理员', '2024-04-14 13:00:17');
INSERT INTO `sys_menu`
VALUES (132, '新增', 'developer:job:add', 3, 0, 130, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-03-17 17:12:23', '超级管理员', '2024-04-14 13:01:05');
INSERT INTO `sys_menu`
VALUES (133, '修改', 'developer:job:update', 3, 1, 130, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-03-17 17:12:39', '超级管理员', '2024-04-14 13:01:07');
INSERT INTO `sys_menu`
VALUES (134, '删除', 'developer:job:remove', 3, 2, 130, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-03-17 17:12:58', '超级管理员', '2024-04-14 13:01:11');
INSERT INTO `sys_menu`
VALUES (135, '执行', 'developer:job:trigger', 3, 3, 130, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-03-17 17:13:23', '超级管理员', '2024-04-14 13:01:15');
INSERT INTO `sys_menu`
VALUES (136, '修改', 'developer:error-log:update', 3, 0, 108, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2022-07-10 13:55:37', '超级管理员', '2024-04-14 13:00:07');
INSERT INTO `sys_menu`
VALUES (137, '删除', 'developer:error-log:remove', 3, 1, 108, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2022-07-10 13:55:57', '超级管理员', '2024-04-14 13:00:05');
INSERT INTO `sys_menu`
VALUES (145, '短信渠道', 'developer:sms:channel:query', 2, 0, 110, 'channel', 'fa:at', 'SmsChannel',
        'developer/sms/channel/index', 1, 1, 1, 1, 0, '超级管理员', '2023-06-06 17:21:14', '超级管理员',
        '2024-04-14 13:02:42');
INSERT INTO `sys_menu`
VALUES (146, '短信模板', 'developer:sms:template:query', 2, 1, 110, 'template', 'fa-solid:align-justify', 'SmsTemplate',
        'developer/sms/template/index', 1, 1, 1, 1, 0, '超级管理员', '2023-06-06 17:30:09', '超级管理员',
        '2024-04-14 13:02:57');
INSERT INTO `sys_menu`
VALUES (147, '短信日志', 'developer:sms:log:query', 2, 2, 110, 'logger', 'ep:document', 'SmsLog',
        'developer/sms/log/index', 1, 1, 1, 1, 0, '超级管理员', '2023-06-09 16:39:31', '超级管理员',
        '2024-04-14 13:03:06');
INSERT INTO `sys_menu`
VALUES (148, '新增', 'developer:sms:channel:add', 3, 0, 145, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-06 17:21:43', '超级管理员', '2024-04-14 13:04:38');
INSERT INTO `sys_menu`
VALUES (149, '修改', 'developer:sms:channel:update', 3, 1, 145, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-06 17:22:44', '超级管理员', '2024-04-14 13:04:45');
INSERT INTO `sys_menu`
VALUES (150, '删除', 'developer:sms:channel:remove', 3, 2, 145, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-06 17:23:02', '超级管理员', '2024-04-14 13:04:50');
INSERT INTO `sys_menu`
VALUES (151, '新增', 'developer:sms:template:add', 3, 0, 146, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-06 17:30:23', '超级管理员', '2024-04-14 13:04:53');
INSERT INTO `sys_menu`
VALUES (152, '修改', 'developer:sms:template:update', 3, 1, 146, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-06 17:30:40', '超级管理员', '2024-04-14 13:04:55');
INSERT INTO `sys_menu`
VALUES (153, '删除', 'developer:sms:template:remove', 3, 2, 146, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-06 17:30:57', '超级管理员', '2024-04-14 13:04:58');
INSERT INTO `sys_menu`
VALUES (154, '测试', 'developer:sms:template:send:sms', 3, 3, 146, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-06 17:49:03', '超级管理员', '2024-04-14 13:05:01');
INSERT INTO `sys_menu`
VALUES (162, '字典管理', '', 1, 6, 2, 'dictionary', 'ep:folder-opened', '', '', 1, 1, 1, 1, 0, '超级管理员',
        '2024-04-22 14:33:21', '超级管理员', '2024-05-23 10:03:04');
INSERT INTO `sys_menu`
VALUES (163, '字典列表', 'developer:dictionary:query', 2, 0, 162, 's', 'ep:files', 'DictionaryList',
        'developer/dictionary/index', 1, 1, 1, 1, 0, '超级管理员', '2024-04-22 14:41:51', '超级管理员',
        '2024-05-23 10:29:46');
INSERT INTO `sys_menu`
VALUES (164, '字典数据', 'developer:dictionary:value:query', 2, 1, 162, 'values', 'fa:crosshairs', 'DictionaryValue',
        'developer/dictionary/value/index', 1, 1, 1, 1, 0, '超级管理员', '2024-04-22 14:43:40', '超级管理员',
        '2024-05-23 11:22:38');
INSERT INTO `sys_menu`
VALUES (165, '新增', 'developer:dictionary:add', 3, 0, 163, '', '', '', '', 1, 1, 1, 1, 0, '超级管理员',
        '2024-04-22 14:44:01', '超级管理员', '2024-04-22 14:46:12');
INSERT INTO `sys_menu`
VALUES (166, '修改', 'developer:dictionary:update', 3, 0, 163, '', '', '', '', 1, 1, 1, 1, 0, '超级管理员',
        '2024-04-22 14:44:21', '超级管理员', '2024-04-22 14:46:15');
INSERT INTO `sys_menu`
VALUES (167, '删除', 'developer:dictionary:remove', 3, 1, 163, '', '', '', '', 1, 1, 1, 1, 0, '超级管理员',
        '2024-04-22 14:44:32', '超级管理员', '2024-04-22 14:46:18');
INSERT INTO `sys_menu`
VALUES (168, '新增', 'developer:dictionary:value:add', 3, 0, 164, '', '', '', '', 1, 1, 1, 1, 0, '超级管理员',
        '2024-04-22 14:44:01', '超级管理员', '2024-04-22 14:46:12');
INSERT INTO `sys_menu`
VALUES (169, '修改', 'developer:dictionary:value:update', 3, 0, 164, '', '', '', '', 1, 1, 1, 1, 0, '超级管理员',
        '2024-04-22 14:44:21', '超级管理员', '2024-04-22 14:46:15');
INSERT INTO `sys_menu`
VALUES (170, '删除', 'developer:dictionary:value:remove', 3, 1, 164, '', '', '', '', 1, 1, 1, 1, 0, '超级管理员',
        '2024-04-22 14:44:32', '超级管理员', '2024-04-22 14:46:18');

SET FOREIGN_KEY_CHECKS = 1;
