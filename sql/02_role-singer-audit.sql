-- ============================================
-- Music Dreamer 角色 & 审核 扩展表
-- ============================================

-- ----------------------------------------
-- 角色表
-- ----------------------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `role_id`   bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `role_code` varchar(20)  NOT NULL UNIQUE COMMENT '角色编码: USER / SINGER / ADMIN',
  `role_name` varchar(50)  NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------------------
-- 用户角色关联表
-- ----------------------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id`          bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`     bigint NOT NULL,
  `role_id`     bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------------------
-- 歌手认证申请表
-- ----------------------------------------
DROP TABLE IF EXISTS `singer_apply`;
CREATE TABLE `singer_apply` (
  `apply_id`      bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`       bigint NOT NULL COMMENT '申请人用户ID',
  `real_name`     varchar(50)  NOT NULL COMMENT '真实姓名',
  `id_card`       varchar(18)  NOT NULL COMMENT '身份证号',
  `avatar`        varchar(255) DEFAULT NULL COMMENT '歌手头像URL',
  `intro`         text COMMENT '歌手简介',
  `gender`        tinyint DEFAULT '0' COMMENT '性别 0:未知 1:男 2:女 3:组合',
  `country`       varchar(50) DEFAULT NULL COMMENT '国籍/地区',
  `birthday`      date DEFAULT NULL COMMENT '出生日期',
  `status`        tinyint DEFAULT '0' COMMENT '0:待审核 1:通过 2:拒绝',
  `reject_reason` varchar(200) DEFAULT NULL,
  `apply_time`    datetime DEFAULT CURRENT_TIMESTAMP,
  `review_time`   datetime DEFAULT NULL,
  `reviewer_id`   bigint DEFAULT NULL COMMENT '审核人ID',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌手认证申请表';

-- ----------------------------------------
-- 歌曲审核表
-- ----------------------------------------
DROP TABLE IF EXISTS `song_audit`;
CREATE TABLE `song_audit` (
  `audit_id`      bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `song_id`       bigint NOT NULL COMMENT '歌曲ID',
  `status`        tinyint DEFAULT '0' COMMENT '0:待审核 1:通过 2:下架/拒绝',
  `reject_reason` varchar(200) DEFAULT NULL,
  `audit_time`    datetime DEFAULT NULL,
  `auditor_id`    bigint DEFAULT NULL COMMENT '审核人ID',
  UNIQUE KEY `uk_song_id` (`song_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲审核表';

-- ----------------------------------------
-- 种子数据
-- ----------------------------------------
INSERT INTO `role` (`role_code`, `role_name`, `description`) VALUES
  ('USER',   '普通用户', '平台注册用户'),
  ('SINGER', '歌手',     '认证歌手，可上传发布歌曲'),
  ('ADMIN',  '管理员',   '平台管理，审核/封禁/统计');
