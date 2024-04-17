
-- ----------------------------
-- Records of sys_menu
-- ----------------------------
BEGIN;
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (2, '研发工具', 'developer', 1, 9999, 0, '/developer', 'fa:connectdevelop', NULL, NULL, 1, 1, 0, 1, 0, '', NULL, '超级管理员', '2023-09-27 22:51:30');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (107, '任务管理', '', 1, 0, 2, 'job', 'fa-solid:tasks', '', '', 1, 1, 1, 1, 0, '超级管理员', '2023-10-01 14:50:25', '超级管理员', '2024-04-14 12:58:36');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (108, '异常日志', 'developer:error-log:query', 2, 2, 2, 'error-log', 'fa-solid:bug', 'Bug', 'manager/developer/errorlog/index', 1, 1, 1, 1, 0, '超级管理员', '2022-07-10 13:54:45', '超级管理员', '2024-04-14 12:57:39');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (110, '短信管理', '', 1, 5, 2, 'message', 'ep:message', NULL, NULL, 1, 1, 0, 1, 0, '超级管理员', '2023-06-06 17:17:16', '超级管理员', '2024-04-14 12:57:34');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (130, '定时任务', 'developer:job:query', 2, 0, 107, 'list', 'fa-solid:tasks', 'JobIndex', 'manager/developer/job/index', 1, 1, 1, 1, 0, '超级管理员', '2023-03-17 17:10:47', '超级管理员', '2024-04-14 13:00:20');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (131, '调度日志', '', 2, 1, 107, 'log', 'ep:document', 'JobLog', 'manager/developer/job/logger/index', 1, 1, 1, 1, 0, '超级管理员', '2023-03-18 13:50:00', '超级管理员', '2024-04-14 13:00:17');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (132, '新增', 'developer:job:add', 3, 0, 130, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-03-17 17:12:23', '超级管理员', '2024-04-14 13:01:05');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (133, '修改', 'developer:job:update', 3, 1, 130, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-03-17 17:12:39', '超级管理员', '2024-04-14 13:01:07');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (134, '删除', 'developer:job:remove', 3, 2, 130, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-03-17 17:12:58', '超级管理员', '2024-04-14 13:01:11');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (135, '执行', 'developer:job:trigger', 3, 3, 130, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-03-17 17:13:23', '超级管理员', '2024-04-14 13:01:15');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (136, '修改', 'developer:error-log:update', 3, 0, 108, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2022-07-10 13:55:37', '超级管理员', '2024-04-14 13:00:07');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (137, '删除', 'developer:error-log:remove', 3, 1, 108, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2022-07-10 13:55:57', '超级管理员', '2024-04-14 13:00:05');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (145, '短信渠道', 'developer:sms:channel:query', 2, 0, 110, 'channel', 'fa:at', 'SmsChannel', 'manager/developer/sms/channel/index', 1, 1, 1, 1, 0, '超级管理员', '2023-06-06 17:21:14', '超级管理员', '2024-04-14 13:02:42');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (146, '短信模板', 'developer:sms:template:query', 2, 1, 110, 'template', 'fa-solid:align-justify', 'SmsTemplate', 'manager/developer/sms/template/index', 1, 1, 1, 1, 0, '超级管理员', '2023-06-06 17:30:09', '超级管理员', '2024-04-14 13:02:57');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (147, '短信日志', 'developer:sms:log:query', 2, 2, 110, 'logger', 'ep:document', 'SmsLog', 'manager/developer/sms/log/index', 1, 1, 1, 1, 0, '超级管理员', '2023-06-09 16:39:31', '超级管理员', '2024-04-14 13:03:06');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (148, '新增', 'developer:sms:channel:add', 3, 0, 145, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-06-06 17:21:43', '超级管理员', '2024-04-14 13:04:38');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (149, '修改', 'developer:sms:channel:update', 3, 1, 145, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-06-06 17:22:44', '超级管理员', '2024-04-14 13:04:45');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (150, '删除', 'developer:sms:channel:remove', 3, 2, 145, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-06-06 17:23:02', '超级管理员', '2024-04-14 13:04:50');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (151, '新增', 'developer:sms:template:add', 3, 0, 146, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-06-06 17:30:23', '超级管理员', '2024-04-14 13:04:53');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (152, '修改', 'developer:sms:template:update', 3, 1, 146, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-06-06 17:30:40', '超级管理员', '2024-04-14 13:04:55');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (153, '删除', 'developer:sms:template:remove', 3, 2, 146, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-06-06 17:30:57', '超级管理员', '2024-04-14 13:04:58');
      INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (154, '测试', 'developer:sms:template:send:sms', 3, 3, 146, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-06-06 17:49:03', '超级管理员', '2024-04-14 13:05:01');

COMMIT;


-- ----------------------------
-- Table structure for infra_error_log
-- ----------------------------
DROP TABLE IF EXISTS `infra_error_log`;
CREATE TABLE `infra_error_log`
(
    `id`                           int NOT NULL AUTO_INCREMENT,
    `application_name`             varchar(255) DEFAULT NULL COMMENT '应用名称',
    `request_method`               varchar(255) DEFAULT NULL COMMENT '请求方法',
    `request_url`                  text COMMENT '请求地址',
    `request_params`               text COMMENT '请求参数',
    `user_ip`                      varchar(255) DEFAULT NULL COMMENT '来源ip',
    `exception_time`               datetime     DEFAULT NULL COMMENT '异常时间',
    `exception_name`               varchar(255) DEFAULT NULL COMMENT '异常名称',
    `exception_class_name`         varchar(255) DEFAULT NULL COMMENT '异常类',
    `exception_file_name`          varchar(255) DEFAULT NULL COMMENT '异常文件',
    `exception_method_name`        varchar(255) DEFAULT NULL COMMENT '异常方法',
    `exception_line_number`        int          DEFAULT NULL COMMENT '异常行',
    `exception_stack_trace`        text COMMENT '堆栈信息',
    `exception_root_cause_message` text,
    `exception_message`            text,
    `status`                       tinyint      DEFAULT '0',
    `deleted`                      tinyint      DEFAULT '0',
    `creator`                      varchar(64)  DEFAULT NULL,
    `create_time`                  datetime     DEFAULT NULL,
    `updater`                      varchar(64)  DEFAULT NULL,
    `update_time`                  datetime     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1 COMMENT ='接口异常表';

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
    `handler_param`   text                 DEFAULT NULL COMMENT '处理器的参数',
    `cron_expression` varchar(32) NOT NULL COMMENT 'CRON 表达式',
    `retry_count`     int         NOT NULL DEFAULT '0' COMMENT '重试次数',
    `retry_interval`  int         NOT NULL DEFAULT '0' COMMENT '重试间隔',
    `monitor_timeout` int         NOT NULL DEFAULT '0' COMMENT '监控超时时间',
    `creator`         varchar(64)          DEFAULT '' COMMENT '创建者',
    `create_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         varchar(64)          DEFAULT '' COMMENT '更新者',
    `update_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1 COMMENT ='定时任务表';


-- ----------------------------
-- Records of infra_job
-- ----------------------------
BEGIN;
INSERT INTO `infra_job` (`id`, `name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`,
                         `retry_interval`, `monitor_timeout`, `creator`, `create_time`, `updater`, `update_time`,
                         `deleted`)
VALUES (1, '测试定时任务', 2, 'TestJobHandler', '', '0 0 12 * * ?', 0, 0, 0, '超级管理员', '2023-03-18 16:51:08',
        '超级管理员', '2023-03-19 15:13:44', b'0');
COMMIT;


-- ----------------------------
-- Table structure for infra_job_log
-- ----------------------------
DROP TABLE IF EXISTS `infra_job_log`;
CREATE TABLE `infra_job_log`
(
    `id`            bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '日志编号',
    `job_id`        bigint                                                       NOT NULL COMMENT '任务编号',
    `handler_name`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '处理器的名字',
    `handler_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '处理器的参数',
    `execute_index` tinyint                                                      NOT NULL DEFAULT '1' COMMENT '第几次执行',
    `begin_time`    datetime                                                     NOT NULL COMMENT '开始执行时间',
    `end_time`      datetime                                                              DEFAULT NULL COMMENT '结束执行时间',
    `duration`      int                                                                   DEFAULT NULL COMMENT '执行时长',
    `status`        tinyint                                                      NOT NULL COMMENT '任务状态',
    `result`        longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '结果数据',
    `deleted`       bit(1)                                                       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `creator`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT '' COMMENT '创建者',
    `create_time`   datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT '' COMMENT '更新者',
    `update_time`   datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1 COMMENT ='定时任务日志表';


-- ----------------------------
-- Table structure for infra_sms_channel
-- ----------------------------
DROP TABLE IF EXISTS `infra_sms_channel`;
CREATE TABLE `infra_sms_channel`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `signature`    varchar(12)  NOT NULL COMMENT '短信签名',
    `code`         varchar(63)  NOT NULL COMMENT '三方编码',
    `status`       tinyint      NULL     DEFAULT '1' COMMENT '开启状态',
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
  AUTO_INCREMENT = 1 COMMENT = '短信三方';

-- ----------------------------
-- Records of infra_sms_channel
-- ----------------------------
BEGIN;
INSERT INTO `infra_sms_channel` (`id`, `signature`, `code`, `status`, `api_key`, `api_secret`, `callback_url`, `remark`,
                                 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (1, 'BARK', 'BARK', 1, 'BARK', 'BARK', 'http://BARK.top', NULL, '超级管理员', '2023-09-23 15:28:36',
        '超级管理员', '2023-09-23 15:30:34', b'0');
COMMIT;

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
  AUTO_INCREMENT = 1 COMMENT = '短信日志';

-- ----------------------------
-- Records of infra_sms_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_sms_template
-- ----------------------------
DROP TABLE IF EXISTS `infra_sms_template`;
CREATE TABLE `infra_sms_template`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `status`          tinyint      NULL     DEFAULT '1' COMMENT '开启状态',
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
  AUTO_INCREMENT = 1 COMMENT = '短信模板';

-- ----------------------------
-- Records of infra_sms_template
-- ----------------------------
BEGIN;
INSERT INTO `infra_sms_template` (`id`, `status`, `code`, `name`, `content`, `params`, `api_template_id`, `channel_id`,
                                  `channel_code`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                                  `deleted`)
VALUES (1, 1, 'TEST_SMS_TEMPLATE', '测试短信推送',
        '账号: {account}, 有 {total} 台服务器即将过期, 续费总额 {amount} 元!', '[\"account\",\"total\",\"amount\"]',
        'TEST_SMS_TEMPLATE', 1, 'BARK', NULL, '超级管理员', '2023-09-23 15:33:03', '超级管理员',
        '2023-09-23 15:42:13', b'0');
COMMIT;