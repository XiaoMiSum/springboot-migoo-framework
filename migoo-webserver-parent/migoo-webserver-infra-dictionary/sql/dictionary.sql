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
-- Records of sys_menu
-- ----------------------------
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
