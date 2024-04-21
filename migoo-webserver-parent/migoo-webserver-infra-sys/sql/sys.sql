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
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '部门名称',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父部门id',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `leader_user_id` bigint NULL DEFAULT NULL COMMENT '负责人',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '部门状态（1正常 0停用）',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, 'migoo.club', 0, 0, 1, 'migoo@migoo.xyz', 1, 0, '系统', '2022-04-29 18:04:35', '系统', '2022-04-29 18:04:35');
INSERT INTO `sys_dept` VALUES (2, '董事会', 1, 0, 1, NULL, 1, 0, '系统', '2022-05-01 16:56:08', '系统', '2022-05-01 16:56:43');
INSERT INTO `sys_dept` VALUES (3, '总经办', 1, 10, 1, NULL, 1, 0, '系统', '2022-05-01 16:56:35', '系统', '2022-05-01 16:56:44');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '权限标识',
  `menu_type` tinyint NOT NULL COMMENT '菜单类型',
  `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父菜单ID',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '路由地址',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `component_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '菜单状态',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否可见',
  `always_show` tinyint NOT NULL DEFAULT 0 COMMENT '总是显示：1:是，0:否',
  `keep_alive` tinyint NOT NULL DEFAULT 1 COMMENT '是否缓存',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1070 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 'system:manager', 1, 9998, 0, '/system', 'ep:setting', NULL, '', 1, 1, 0, 1, 0, '', '2022-04-29 18:34:52', '超级管理员', '2023-09-27 22:48:18');
INSERT INTO `sys_menu` VALUES (3, '工作台', 'dashboard', 2, 0, 0, '', 'ep:alarm-clock', 'Dashboard', 'manager/dashboard/index', 1, 1, 1, 1, 0, '', NULL, '超级管理员', '2023-09-27 22:37:53');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 'system:user:query', 2, 0, 1, 'user', 'ep:user', 'SystemUser', 'manager/system/user/index', 1, 1, 1, 1, 0, '', '2022-04-29 18:45:02', '超级管理员', '2023-09-27 22:48:39');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 'system:role:query', 2, 1, 1, 'role', 'fa-solid:user-plus', 'UserRole', 'manager/system/role/index', 1, 1, 1, 1, 0, '', '2022-04-29 18:45:02', '超级管理员', '2023-09-27 22:49:31');
INSERT INTO `sys_menu` VALUES (102, '部门管理', 'system:dept:query', 2, 2, 1, 'dept', 'ep:basketball', 'SystemDept', 'manager/system/dept/index', 1, 1, 1, 1, 0, '', '2022-04-29 18:45:02', '超级管理员', '2023-09-27 22:50:05');
INSERT INTO `sys_menu` VALUES (103, '岗位管理', 'system:post:query', 2, 3, 1, 'post', 'ep:postcard', 'SystemPost', 'manager/system/post/index', 1, 1, 1, 1, 0, '', '2022-04-29 18:45:02', '超级管理员', '2023-09-27 22:50:32');
INSERT INTO `sys_menu` VALUES (104, '菜单管理', 'system:menu:query', 2, 4, 1, 'menu', 'ep:menu', 'SystemMenu', 'manager/system/menu/index', 1, 1, 1, 1, 0, '', '2022-04-29 18:45:02', '超级管理员', '2023-09-27 22:50:53');
INSERT INTO `sys_menu` VALUES (106, '主题设置', 'layout:setting', 3, 0, 3, '', '', NULL, '', 1, 1, 0, 1, 0, '超级管理员', '2023-07-23 18:56:46', '超级管理员', '2024-04-14 12:58:34');
INSERT INTO `sys_menu` VALUES (111, '新增', 'system:user:add', 3, 0, 100, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 13:55:34', '奥丁1', '2024-04-14 12:57:29');
INSERT INTO `sys_menu` VALUES (112, '修改', 'system:user:update', 3, 1, 100, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 13:55:59', '超级管理员', '2024-04-14 12:57:28');
INSERT INTO `sys_menu` VALUES (113, '删除', 'system:user:remove', 3, 3, 100, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 13:56:20', '奥丁1', '2024-04-14 12:57:27');
INSERT INTO `sys_menu` VALUES (114, '重置密码', 'system:user:reset-password', 3, 4, 100, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 14:00:56', '奥丁1', '2024-04-14 12:57:26');
INSERT INTO `sys_menu` VALUES (115, '分配角色', 'system:permission:assign-user-role', 3, 4, 100, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:05:28', '奥丁1', '2024-04-14 12:57:24');
INSERT INTO `sys_menu` VALUES (116, '新增', 'system:role:add', 3, 0, 101, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:00:30', '奥丁1', '2024-04-14 12:57:23');
INSERT INTO `sys_menu` VALUES (117, '修改', 'system:role:update', 3, 1, 101, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:01:01', '超级管理员', '2024-04-14 12:57:22');
INSERT INTO `sys_menu` VALUES (118, '删除', 'system:role:remove', 3, 2, 101, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:01:24', '奥丁1', '2024-04-14 12:57:21');
INSERT INTO `sys_menu` VALUES (119, '分配菜单', 'system:permission:assign-role-menu', 3, 3, 101, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:06:41', '奥丁1', '2024-04-14 12:57:20');
INSERT INTO `sys_menu` VALUES (120, '新增', 'system:dept:add', 3, 0, 102, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:07:56', '奥丁1', '2024-04-14 12:57:18');
INSERT INTO `sys_menu` VALUES (121, '修改', 'system:dept:update', 3, 1, 102, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:08:12', '超级管理员', '2024-04-14 12:57:17');
INSERT INTO `sys_menu` VALUES (122, '删除', 'system:dept:remove', 3, 3, 102, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:08:24', '奥丁1', '2024-04-14 12:57:16');
INSERT INTO `sys_menu` VALUES (123, '新增', 'system:post:add', 3, 0, 103, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:15:22', '奥丁1', '2024-04-14 12:57:15');
INSERT INTO `sys_menu` VALUES (124, '修改', 'system:post:update', 3, 1, 103, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:18:11', '超级管理员', '2024-04-14 12:57:14');
INSERT INTO `sys_menu` VALUES (125, '删除', 'system:post:remove', 3, 2, 103, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:18:28', '奥丁1', '2024-04-14 12:57:01');
INSERT INTO `sys_menu` VALUES (126, '新增', 'system:menu:add', 3, 0, 104, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:19:11', '奥丁1', '2024-04-14 12:56:59');
INSERT INTO `sys_menu` VALUES (127, '修改', 'system:menu:update', 3, 1, 104, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:19:42', '超级管理员', '2024-04-14 12:56:57');
INSERT INTO `sys_menu` VALUES (128, '删除', 'system:menu:remove', 3, 2, 104, '', '', NULL, '', 1, 1, 0, 1, 0, '奥丁1', '2022-05-01 16:19:56', '奥丁1', '2024-04-14 12:56:56');

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位名称',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位编码',
  `sort` int NOT NULL COMMENT '排序',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1正常；0停用',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '岗位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, '董事长', 'chairman', 0, 1, '董事长', 0, '系统', '2022-04-29 20:43:45', '系统', '2022-04-29 20:44:34');
INSERT INTO `sys_post` VALUES (2, '研发工程师', 'developer', 1, 1, '研发工程师', 0, '系统', '2022-04-29 20:43:45', '系统', '2022-04-29 20:44:34');
INSERT INTO `sys_post` VALUES (3, '运营', 'operations', 2, 1, '运营', 0, '系统', '2022-04-29 20:43:45', '系统', '2022-04-29 20:44:34');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `sort` int NOT NULL COMMENT '排序',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1正常；0停用',
  `type` int NOT NULL COMMENT '角色类型',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'super_admin', 0, 1, 1, '超级管理员', 0, '系统', '2022-04-29 20:43:45', '系统', '2022-04-29 20:44:34');
INSERT INTO `sys_role` VALUES (2, '开发者', 'developer', 1, 1, 2, '开发者', 0, '奥丁1', '2022-05-14 13:14:56', '奥丁1', '2022-05-14 13:14:56');
INSERT INTO `sys_role` VALUES (3, '管理员', 'admin', 2, 1, 2, '普通管理员', 0, '超级管理员', '2022-07-16 21:54:07', '超级管理员', '2022-07-16 21:54:07');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色id',
  `menu_id` bigint NOT NULL COMMENT '菜单id',
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  `creator` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `updater` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `phone` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录名',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态0禁用、1启用',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别 1男 2女',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门编号',
  `post_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位编号',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱地址',
  `secret_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '验证器安全码',
  `bind_authenticator` tinyint NOT NULL DEFAULT 0 COMMENT '身份验证器绑定状态：0 -未绑定 1- 已绑定',
  `required_verify_authenticator` tinyint NOT NULL DEFAULT 1 COMMENT '是否验证身份验证器',
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'superadmin', '$2a$10$UxsSdlstbdutt3vG51ltiOkyxCAHnC8Q9p3Ds3W3o62KJ9qHLXBSa', NULL, '超级管理员', '', 1, 1, 1, NULL, NULL, '', 1, 0, 0, '1', '2022-04-29 16:06:56', '超级管理员', '2022-11-23 12:29:42');
INSERT INTO `sys_user` VALUES (2, 'developer', '$2a$10$UxsSdlstbdutt3vG51ltiOkyxCAHnC8Q9p3Ds3W3o62KJ9qHLXBSa', NULL, '开发者', NULL, 1, 0, 2, '[]', 'mail@cc.com', '', 0, 1, 0, '奥丁1', '2022-05-01 17:53:00', '奥丁1', '2022-05-31 20:14:42');
INSERT INTO `sys_user` VALUES (3, 'admin', '$2a$10$UxsSdlstbdutt3vG51ltiOkyxCAHnC8Q9p3Ds3W3o62KJ9qHLXBSa', NULL, '管理员', NULL, 1, NULL, 1, '[]', NULL, '', 0, 1, 0, '超级管理员', '2022-07-16 21:52:15', '管理员', '2022-07-16 22:44:59');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户id',
  `role_id` bigint NOT NULL COMMENT '角色id',
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, 0, '系统', '2022-04-29 20:54:56', '系统', '2022-04-29 20:55:01');
INSERT INTO `sys_user_role` VALUES (2, 2, 2, 0, '系统', '2022-05-14 13:21:55', '系统', '2022-05-14 13:22:01');
INSERT INTO `sys_user_role` VALUES (3, 3, 3, 0, '超级管理员', '2022-07-16 22:32:22', '超级管理员', '2022-07-16 22:32:22');

SET FOREIGN_KEY_CHECKS = 1;
