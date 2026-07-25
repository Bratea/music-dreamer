-- ============================================
-- Music Dreamer 数据库Schema
-- MySQL 8.0+
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------
-- 用户表
-- ----------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `gender` tinyint DEFAULT '0' COMMENT '性别 0:未知 1:男 2:女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `signature` varchar(200) DEFAULT NULL COMMENT '个性签名',
  `status` tinyint DEFAULT '1' COMMENT '状态 0:禁用 1:正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------------------
-- 歌手表
-- ----------------------------------------
DROP TABLE IF EXISTS `singer`;
CREATE TABLE `singer` (
  `singer_id` bigint NOT NULL AUTO_INCREMENT COMMENT '歌手ID',
  `name` varchar(50) NOT NULL COMMENT '歌手名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `intro` text COMMENT '简介',
  `gender` tinyint DEFAULT '0' COMMENT '性别 0:未知 1:男 2:女 3:组合',
  `country` varchar(50) DEFAULT NULL COMMENT '国籍',
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `status` tinyint DEFAULT '0' COMMENT '状态 0:未认证 1:已认证 2:禁用',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户ID（歌手账号）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`singer_id`),
  KEY `idx_name` (`name`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌手表';

-- ----------------------------------------
-- 专辑表
-- ----------------------------------------
DROP TABLE IF EXISTS `album`;
CREATE TABLE `album` (
  `album_id` bigint NOT NULL AUTO_INCREMENT COMMENT '专辑ID',
  `name` varchar(100) NOT NULL COMMENT '专辑名',
  `singer_id` bigint NOT NULL COMMENT '歌手ID',
  `cover` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
  `description` text COMMENT '专辑描述',
  `release_date` date DEFAULT NULL COMMENT '发行日期',
  `status` tinyint DEFAULT '1' COMMENT '状态 0:下架 1:上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`album_id`),
  KEY `idx_singer_id` (`singer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_release_date` (`release_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专辑表';

-- ----------------------------------------
-- 歌曲表
-- ----------------------------------------
DROP TABLE IF EXISTS `song`;
CREATE TABLE `song` (
  `song_id` bigint NOT NULL AUTO_INCREMENT COMMENT '歌曲ID',
  `name` varchar(100) NOT NULL COMMENT '歌曲名',
  `singer_id` bigint NOT NULL COMMENT '歌手ID',
  `album_id` bigint DEFAULT NULL COMMENT '专辑ID',
  `duration` int DEFAULT '0' COMMENT '时长（秒）',
  `url` varchar(255) NOT NULL COMMENT '音频文件URL',
  `cover` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
  `lyrics` text COMMENT '歌词（LRC格式）',
  `description` varchar(500) DEFAULT NULL COMMENT '歌曲描述',
  `genre` varchar(50) DEFAULT NULL COMMENT '流派',
  `language` varchar(20) DEFAULT NULL COMMENT '语言',
  `release_date` date DEFAULT NULL COMMENT '发行日期',
  `play_count` int DEFAULT '0' COMMENT '播放次数',
  `like_count` int DEFAULT '0' COMMENT '收藏次数',
  `status` tinyint DEFAULT '1' COMMENT '状态 0:下架 1:上架 2:审核中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`song_id`),
  KEY `idx_name` (`name`),
  KEY `idx_singer_id` (`singer_id`),
  KEY `idx_album_id` (`album_id`),
  KEY `idx_status` (`status`),
  KEY `idx_play_count` (`play_count`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲表';

-- ----------------------------------------
-- 歌单表
-- ----------------------------------------
DROP TABLE IF EXISTS `playlist`;
CREATE TABLE `playlist` (
  `playlist_id` bigint NOT NULL AUTO_INCREMENT COMMENT '歌单ID',
  `name` varchar(100) NOT NULL COMMENT '歌单名',
  `cover` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
  `description` text COMMENT '描述',
  `user_id` bigint NOT NULL COMMENT '创建者用户ID',
  `play_count` int DEFAULT '0' COMMENT '播放次数',
  `song_count` int DEFAULT '0' COMMENT '歌曲数量',
  `status` tinyint DEFAULT '1' COMMENT '状态 0:私密 1:公开',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`playlist_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_play_count` (`play_count`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单表';

-- ----------------------------------------
-- 歌单歌曲关联表
-- ----------------------------------------
DROP TABLE IF EXISTS `playlist_song`;
CREATE TABLE `playlist_song` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `playlist_id` bigint NOT NULL COMMENT '歌单ID',
  `song_id` bigint NOT NULL COMMENT '歌曲ID',
  `sort` int DEFAULT '0' COMMENT '排序号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_playlist_song` (`playlist_id`,`song_id`),
  KEY `idx_song_id` (`song_id`),
  KEY `idx_playlist_id` (`playlist_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单歌曲关联表';

-- ----------------------------------------
-- 用户收藏表
-- ----------------------------------------
DROP TABLE IF EXISTS `user_collect`;
CREATE TABLE `user_collect` (
  `collect_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID（歌曲/歌单）',
  `target_type` tinyint NOT NULL COMMENT '类型 1:歌曲 2:歌单',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`collect_id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_id`,`target_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- ----------------------------------------
-- 用户历史记录表
-- ----------------------------------------
DROP TABLE IF EXISTS `user_history`;
CREATE TABLE `user_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `song_id` bigint NOT NULL COMMENT '歌曲ID',
  `play_duration` int DEFAULT '0' COMMENT '播放时长（秒）',
  `play_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '播放时间',
  PRIMARY KEY (`history_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_song_id` (`song_id`),
  KEY `idx_play_time` (`play_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户历史记录表';

-- ----------------------------------------
-- 消息通知表
-- ----------------------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `type` tinyint DEFAULT '1' COMMENT '类型 1:系统消息 2:关注歌手更新 3:评论 4:点赞',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读 0:未读 1:已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- ----------------------------------------
-- 歌手关注表
-- ----------------------------------------
DROP TABLE IF EXISTS `user_follow_singer`;
CREATE TABLE `user_follow_singer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `singer_id` bigint NOT NULL COMMENT '歌手ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_singer` (`user_id`,`singer_id`),
  KEY `idx_singer_id` (`singer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注歌手表';

-- ----------------------------------------
-- 搜索热词表
-- ----------------------------------------
DROP TABLE IF EXISTS `search_keyword`;
CREATE TABLE `search_keyword` (
  `keyword_id` bigint NOT NULL AUTO_INCREMENT,
  `keyword` varchar(50) NOT NULL COMMENT '关键词',
  `search_count` int DEFAULT '0' COMMENT '搜索次数',
  `last_search_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyword_id`),
  UNIQUE KEY `uk_keyword` (`keyword`),
  KEY `idx_search_count` (`search_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索热词表';

SET FOREIGN_KEY_CHECKS = 1;
