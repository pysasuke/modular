CREATE DATABASE modular;
USE modular;
-- ----------------------------
-- Table structure for `record`
-- ----------------------------
DROP TABLE IF EXISTS `record`;
CREATE TABLE `record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `describe` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '描述',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `first_state` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '第一道工序状态：0-未完成；1-完成',
  `second_state` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '第二道工序状态：0-未完成；1-完成',
  `third_state` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '第三道工序状态：0-未完成；1-完成',
  `first_time` DATETIME DEFAULT NULL COMMENT '第一道工序完成日期',
  `second_time` DATETIME DEFAULT NULL COMMENT '第二道工序完成日期',
  `third_time` DATETIME DEFAULT NULL COMMENT '第三道工序完成日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
