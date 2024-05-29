
-- ----------------------------
-- Table structure for infra_dictionary
-- ----------------------------
INSERT INTO `infra_dictionary` VALUES (12, 'infra_cvs_machine_status', '云服务器状态', 'ones', 1, b'0', '超级管理员', '2024-05-27 16:55:05', '超级管理员', '2024-05-27 16:55:05');
INSERT INTO `infra_dictionary` VALUES (13, 'infra_cvs_provider', '云服务提供商', 'ones', 1, b'0', '超级管理员', '2024-05-27 16:55:30', '超级管理员', '2024-05-27 17:23:01');

-- ----------------------------
-- Table structure for infra_dictionary_value
-- ----------------------------
INSERT INTO `infra_dictionary_value` VALUES (31, 'infra_cvs_machine_status', '准备中', 'Creating', 1, 'info', 0, b'0', '超级管理员', '2024-05-27 16:59:15', '超级管理员', '2024-05-27 16:59:15');
INSERT INTO `infra_dictionary_value` VALUES (32, 'infra_cvs_machine_status', '准备中', 'Pending', 1, 'info', 1, b'0', '超级管理员', '2024-05-27 16:59:30', '超级管理员', '2024-05-27 16:59:30');
INSERT INTO `infra_dictionary_value` VALUES (33, 'infra_cvs_machine_status', '运行中', 'Running', 1, 'success', 2, b'0', '超级管理员', '2024-05-27 16:59:51', '超级管理员', '2024-05-27 16:59:51');
INSERT INTO `infra_dictionary_value` VALUES (34, 'infra_cvs_machine_status', '启动中', 'Starting', 1, 'default', 3, b'0', '超级管理员', '2024-05-27 17:00:14', '超级管理员', '2024-05-27 17:00:26');
INSERT INTO `infra_dictionary_value` VALUES (35, 'infra_cvs_machine_status', '删除中', 'Deleting', 1, 'danger', 4, b'0', '超级管理员', '2024-05-27 17:00:41', '超级管理员', '2024-05-27 17:00:41');
INSERT INTO `infra_dictionary_value` VALUES (36, 'infra_cvs_machine_status', '重启中', 'Rebooting', 1, 'warning', 5, b'0', '超级管理员', '2024-05-27 17:01:02', '超级管理员', '2024-05-27 17:01:02');
INSERT INTO `infra_dictionary_value` VALUES (37, 'infra_cvs_machine_status', '停止中', 'Stopping', 1, 'warning', 6, b'0', '超级管理员', '2024-05-27 17:01:18', '超级管理员', '2024-05-27 17:01:18');
INSERT INTO `infra_dictionary_value` VALUES (38, 'infra_cvs_machine_status', '已停止', 'Stopped', 1, 'warning', 7, b'0', '超级管理员', '2024-05-27 17:01:33', '超级管理员', '2024-05-27 17:01:33');
INSERT INTO `infra_dictionary_value` VALUES (39, 'infra_cvs_machine_status', '已释放', 'Released', 1, 'danger', 8, b'0', '超级管理员', '2024-05-27 17:01:47', '超级管理员', '2024-05-27 17:01:47');
INSERT INTO `infra_dictionary_value` VALUES (40, 'infra_cvs_provider', '阿里云', 'ALI_CLOUD', 1, 'default', 0, b'0', '超级管理员', '2024-05-27 17:02:16', '超级管理员', '2024-05-27 17:03:51');
INSERT INTO `infra_dictionary_value` VALUES (41, 'infra_cvs_provider', '腾讯云', 'TENCENT', 1, 'default', 1, b'0', '超级管理员', '2024-05-27 17:02:36', '超级管理员', '2024-05-27 17:03:52');


-- ----------------------------
-- Table structure for infra_sms_template
-- ----------------------------
INSERT INTO `infra_sms_template`
VALUES (1, 1, 'CVS_MACHINE_EXPIRATION_REMINDER', '云服务过期推送',
        '账号: {account}, 有 {total} 台服务器即将过期, 续费总额 {amount} 元!', '[\"account\",\"total\",\"amount\"]',
        'CVS_MACHINE_EXPIRATION_REMINDER', 1, 'BARK', NULL, '超级管理员', '2023-09-23 15:33:03', '超级管理员',
        '2023-09-23 15:42:13', b'0');

-- ----------------------------
-- Table structure for infra_job
-- ----------------------------
INSERT INTO `infra_job`
VALUES (1, '云服务器续费提醒', 2, 'CVSMachineAlarmJobHandler',
        '{"devices": ["kpiywgwtugsiywqj"], "days": 7, "template": "CVS_MACHINE_EXPIRATION_REMINDER"}',
        '0 0 12 * * ?', 0, 0, 0, '超级管理员', '2023-03-18 16:51:08', '超级管理员', '2023-03-19 15:13:44', b'0');
INSERT INTO `infra_job`
VALUES (2, '云服务器同步', 2, 'CVSMachineSyncJobHandler', '', '0 0 0 * * ?', 0, 0, 0, '超级管理员',
        '2023-03-18 16:51:08', '超级管理员', '2023-03-19 15:13:44', b'0');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
INSERT INTO `sys_menu`
VALUES (109, '云服务器', '', 1, 4, 2, 'cvs', 'fa:cloud', NULL, NULL, 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-04 21:32:29', '超级管理员', '2024-04-14 12:57:36');
INSERT INTO `sys_menu`
VALUES (138, '实例列表', 'developer:cvs:machine:query', 2, 0, 109, 'server', 'fa:medium', 'CVSMachine',
        'manager/developer/cvs/machine/index', 1, 1, 1, 1, 0, '超级管理员', '2023-06-04 22:15:16',
        '超级管理员', '2024-04-14 13:00:04');
INSERT INTO `sys_menu`
VALUES (139, '云服务商', 'developer:cvs:provider:query', 2, 1, 109, 'provider', 'fa-solid:address-card',
        'CVSProvider', 'manager/developer/cvs/provider/index', 1, 1, 1, 1, 0, '超级管理员',
        '2023-06-04 22:18:38', '超级管理员', '2024-04-14 13:00:02');
INSERT INTO `sys_menu`
VALUES (140, '修改', 'developer:cvs:machine:update', 3, 0, 138, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-04 22:17:05', '超级管理员', '2024-04-14 13:01:56');
INSERT INTO `sys_menu`
VALUES (141, '删除', 'developer:cvs:machine:remove', 3, 1, 138, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-04 22:17:19', '超级管理员', '2024-04-14 13:01:58');
INSERT INTO `sys_menu`
VALUES (142, '新增', 'developer:cvs:provider:add', 3, 0, 139, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-04 22:19:19', '超级管理员', '2024-04-14 13:01:59');
INSERT INTO `sys_menu`
VALUES (143, '修改', 'developer:cvs:provider:update', 3, 1, 139, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-04 22:19:32', '超级管理员', '2024-04-14 13:02:03');
INSERT INTO `sys_menu`
VALUES (144, '删除', 'developer:cvs:provider:remove', 3, 2, 139, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员',
        '2023-06-04 22:19:43', '超级管理员', '2024-04-14 13:02:06');

-- ----------------------------
-- Table structure for infra_cvs_machine
-- ----------------------------
DROP TABLE IF EXISTS `infra_cvs_machine`;
CREATE TABLE `infra_cvs_machine`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account` varchar(64) NOT NULL COMMENT '云服务账号',
  `hostname` varchar(64) NULL DEFAULT NULL COMMENT '主机名称',
  `instance_id` varchar(64) NOT NULL COMMENT '实例编号',
  `operate_system` varchar(64) NOT NULL COMMENT '操作系统',
  `private_ip_address` varchar(64) NULL DEFAULT NULL COMMENT '内网IP地址',
  `public_ip_address` varchar(64) NOT NULL COMMENT '外网IP地址',
  `status` varchar(32) NOT NULL COMMENT '运行状态',
  `expired_time` varchar(64) NOT NULL COMMENT '过期时间',
  `machine_type` varchar(32) NOT NULL COMMENT '主机类型',
  `price` decimal(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '续费价格',
  `deleted` tinyint NULL DEFAULT 0,
  `creator` varchar(64) NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `updater` varchar(64) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '云服务器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of infra_cvs_machine
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_cvs_provider
-- ----------------------------
DROP TABLE IF EXISTS `infra_cvs_provider`;
CREATE TABLE `infra_cvs_provider`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL COMMENT '云服务商编码',
  `account` varchar(64) NOT NULL COMMENT '云服务账号',
  `access_key_id` varchar(255) NOT NULL COMMENT '访问凭证id',
  `access_key_secret` varchar(255) NOT NULL COMMENT '访问凭证安全码',
  `region` varchar(64) NOT NULL COMMENT '区域编号',
  `status` tinyint NULL DEFAULT 1,
  `deleted` tinyint NULL DEFAULT 0,
  `creator` varchar(64) NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `updater` varchar(64) NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '云服务账号表' ROW_FORMAT = Dynamic;


-- ----------------------------
-- Records of infra_cvs_provider
-- ----------------------------
BEGIN;
COMMIT;