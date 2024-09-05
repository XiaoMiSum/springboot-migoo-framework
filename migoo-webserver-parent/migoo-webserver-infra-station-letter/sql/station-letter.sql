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
-- Table structure for infra_station_letter
-- ----------------------------
DROP TABLE IF EXISTS `infra_station_letter`;
CREATE TABLE `infra_station_letter`
(
    `id`                           bigint          NOT NULL AUTO_INCREMENT,
    `code`             varchar(32) NOT NULL COMMENT '应用名称',
    `title`               varchar(32) NULL DEFAULT NULL COMMENT '消息标题',
    `content`                  text         NULL COMMENT '消息内容',
    `to_user_id`                           bigint          NOT NULL  COMMENT '接收用户id',
    `to_user_type`                           bigint          NOT NULL  COMMENT '接收用户类型',
    `from_user_id`                           bigint          NOT NULL  COMMENT '来源用户id',
    `from_user_type`                           bigint          NOT NULL  COMMENT '来源用户类型',
    `unread`                       tinyint      NULL DEFAULT 1 COMMENT '1 未读 0 已读',
    `deleted`                      tinyint      NULL DEFAULT 0,
    `creator`                      varchar(64)  NULL DEFAULT NULL,
    `create_time`                  datetime     NULL DEFAULT NULL,
    `updater`                      varchar(64)  NULL DEFAULT NULL,
    `update_time`                  datetime     NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1 COMMENT = '站内信'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of infra_error_log
-- ----------------------------


SET FOREIGN_KEY_CHECKS = 1;
