
-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (155, '文件管理', '', 1, 1, 2, 'file', 'ep:folder-opened', '', '', 1, 1, 1, 1, 0, '超级管理员', '2024-04-22 14:33:21', '超级管理员', '2024-04-22 14:45:31');
INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (156, '文件列表', 'developer:file:query', 2, 0, 155, 'f', 'ep:files', 'FileList', 'developer/file/index', 1, 1, 1, 1, 0, '超级管理员', '2024-04-22 14:41:51', '超级管理员', '2024-04-22 14:45:42');
INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (157, 'OSS配置', 'developer:file:config:query', 2, 1, 155, 'config', 'fa:crosshairs', 'FileConfig', 'developer/file/config/index', 1, 1, 1, 1, 0, '超级管理员', '2024-04-22 14:43:40', '超级管理员', '2024-04-22 14:45:48');
INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (158, '删除', 'developer:file:remove', 3, 0, 156, '', '', '', '', 1, 1, 1, 1, 0, '超级管理员', '2024-04-22 14:44:01', '超级管理员', '2024-04-22 14:46:12');
INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (159, '新增', 'developer:file:config:add', 3, 0, 157, '', '', '', '', 1, 1, 1, 1, 0, '超级管理员', '2024-04-22 14:44:21', '超级管理员', '2024-04-22 14:46:15');
INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (160, '修改', 'developer:file:config:update', 3, 1, 157, '', '', '', '', 1, 1, 1, 1, 0, '超级管理员', '2024-04-22 14:44:32', '超级管理员', '2024-04-22 14:46:18');
INSERT INTO `sys_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component_name`, `component`, `status`, `visible`, `always_show`, `keep_alive`, `deleted`, `creator`, `create_time`, `updater`, `update_time`) VALUES (161, '删除', 'developer:file:config:remove', 3, 2, 157, '', '', '', '', 1, 1, 1, 1, 0, '超级管理员', '2024-04-22 14:44:45', '超级管理员', '2024-04-22 14:46:19');


-- ----------------------------
-- Table structure for infra_file
-- ----------------------------
DROP TABLE IF EXISTS `infra_file`;
CREATE TABLE `infra_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件编号',
  `config_id` bigint NULL DEFAULT NULL COMMENT '配置编号',
  `name` varchar(256) NULL DEFAULT NULL COMMENT '文件名',
  `path` varchar(512) NOT NULL COMMENT '文件路径',
  `url` varchar(1024) NOT NULL COMMENT '文件 URL',
  `type` varchar(128) NULL DEFAULT NULL COMMENT '文件类型',
  `source` varchar(128) NULL DEFAULT NULL COMMENT '文件来源',
  `size` int NOT NULL COMMENT '文件大小',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1  COMMENT = '文件表';

-- ----------------------------
-- Records of infra_file
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for infra_file_config
-- ----------------------------
DROP TABLE IF EXISTS `infra_file_config`;
CREATE TABLE `infra_file_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(63) NOT NULL COMMENT '配置名',
  `storage` tinyint NOT NULL COMMENT '存储器',
  `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
  `master` bit(1) NOT NULL COMMENT '是否为主配置',
  `config` varchar(4096) NOT NULL COMMENT '存储配置',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT =1  COMMENT = '文件配置表';

-- ----------------------------
-- Table structure for infra_file_content
-- ----------------------------
DROP TABLE IF EXISTS `infra_file_content`;
CREATE TABLE `infra_file_content`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `config_id` bigint NOT NULL COMMENT '配置编号',
  `path` varchar(512) NOT NULL COMMENT '文件路径',
  `content` mediumblob NOT NULL COMMENT '文件内容',
  `creator` varchar(64) NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1  COMMENT = '文件表';

-- ----------------------------
-- Records of infra_file_content
-- ----------------------------
BEGIN;
COMMIT;