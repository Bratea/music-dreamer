-- ============================================
-- Music Dreamer 封面图片更新
-- 为歌手、专辑、歌曲、歌单添加精美封面图片
-- 图片来源：Lorem Picsum (picsum.photos) - 高质量摄影图片
-- 使用 seed 参数确保每个ID对应固定图片
-- ============================================

USE music_dreamer;

-- ----------------------------------------
-- 1. 歌手头像 (方形头像风格)
-- ----------------------------------------
UPDATE `singer` SET `avatar` = 'https://picsum.photos/seed/jaychou/300/300' WHERE `singer_id` = 1;      -- 周杰伦
UPDATE `singer` SET `avatar` = 'https://picsum.photos/seed/jjlin/300/300' WHERE `singer_id` = 2;         -- 林俊杰
UPDATE `singer` SET `avatar` = 'https://picsum.photos/seed/eason/300/300' WHERE `singer_id` = 3;         -- 陈奕迅
UPDATE `singer` SET `avatar` = 'https://picsum.photos/seed/gem/300/300' WHERE `singer_id` = 4;           -- 邓紫棋
UPDATE `singer` SET `avatar` = 'https://picsum.photos/seed/maobuyi/300/300' WHERE `singer_id` = 5;      -- 毛不易
UPDATE `singer` SET `avatar` = 'https://picsum.photos/seed/xuezhiqian/300/300' WHERE `singer_id` = 6;    -- 薛之谦
UPDATE `singer` SET `avatar` = 'https://picsum.photos/seed/lironghao/300/300' WHERE `singer_id` = 7;      -- 李荣浩
UPDATE `singer` SET `avatar` = 'https://picsum.photos/seed/faye/300/300' WHERE `singer_id` = 8;          -- 王菲

-- ----------------------------------------
-- 2. 专辑封面 (精美摄影)
-- ----------------------------------------
UPDATE `album` SET `cover` = 'https://picsum.photos/seed/album1/400/400' WHERE `album_id` = 1;   -- 叶惠美
UPDATE `album` SET `cover` = 'https://picsum.photos/seed/album2/400/400' WHERE `album_id` = 2;   -- 十一月的萧邦
UPDATE `album` SET `cover` = 'https://picsum.photos/seed/album3/400/400' WHERE `album_id` = 3;   -- 她说
UPDATE `album` SET `cover` = 'https://picsum.photos/seed/album4/400/400' WHERE `album_id` = 4;   -- U87
UPDATE `album` SET `cover` = 'https://picsum.photos/seed/album5/400/400' WHERE `album_id` = 5;   -- 新的心跳
UPDATE `album` SET `cover` = 'https://picsum.photos/seed/album6/400/400' WHERE `album_id` = 6;   -- 平凡的一天
UPDATE `album` SET `cover` = 'https://picsum.photos/seed/album7/400/400' WHERE `album_id` = 7;   -- 意外
UPDATE `album` SET `cover` = 'https://picsum.photos/seed/album8/400/400' WHERE `album_id` = 8;   -- 模特

-- ----------------------------------------
-- 3. 歌曲封面 (使用对应专辑封面，保持一致性)
-- ----------------------------------------
-- 周杰伦 - 叶惠美 (song_id 1-3)
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/album1/400/400' WHERE `song_id` IN (1, 2, 3);
-- 周杰伦 - 十一月的萧邦 (song_id 4-6)
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/album2/400/400' WHERE `song_id` IN (4, 5, 6);
-- 林俊杰 - 她说 (song_id 7-9)
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/album3/400/400' WHERE `song_id` IN (7, 8, 9);
-- 陈奕迅 - U87 (song_id 10-12)
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/album4/400/400' WHERE `song_id` IN (10, 11, 12);
-- 邓紫棋 - 新的心跳 (song_id 13-15)
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/album5/400/400' WHERE `song_id` IN (13, 14, 15);
-- 毛不易 - 平凡的一天 (song_id 16-18)
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/album6/400/400' WHERE `song_id` IN (16, 17, 18);
-- 薛之谦 - 意外 (song_id 19-21)
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/album7/400/400' WHERE `song_id` IN (19, 20, 21);
-- 李荣浩 - 模特 (song_id 22-24)
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/album8/400/400' WHERE `song_id` IN (22, 23, 24);
-- 王菲 - 单曲 (song_id 25-27)
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/faye1/400/400' WHERE `song_id` = 25; -- 红豆
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/faye2/400/400' WHERE `song_id` = 26; -- 传奇
UPDATE `song` SET `cover` = 'https://picsum.photos/seed/faye3/400/400' WHERE `song_id` = 27; -- 匆匆那年

-- ----------------------------------------
-- 4. 歌单封面 (精选图片)
-- ----------------------------------------
UPDATE `playlist` SET `cover` = 'https://picsum.photos/seed/playlist1/400/400' WHERE `playlist_id` = 1;   -- 深夜电台
UPDATE `playlist` SET `cover` = 'https://picsum.photos/seed/playlist2/400/400' WHERE `playlist_id` = 2;   -- 公路之歌
UPDATE `playlist` SET `cover` = 'https://picsum.photos/seed/playlist3/400/400' WHERE `playlist_id` = 3;   -- 雨天小憩
UPDATE `playlist` SET `cover` = 'https://picsum.photos/seed/playlist4/400/400' WHERE `playlist_id` = 4;   -- 燃脂必备
UPDATE `playlist` SET `cover` = 'https://picsum.photos/seed/playlist5/400/400' WHERE `playlist_id` = 5;   -- 怀旧金曲
UPDATE `playlist` SET `cover` = 'https://picsum.photos/seed/playlist6/400/400' WHERE `playlist_id` = 6;   -- 睡前轻音乐
UPDATE `playlist` SET `cover` = 'https://picsum.photos/seed/playlist7/400/400' WHERE `playlist_id` = 7;   -- 咖啡馆背景
UPDATE `playlist` SET `cover` = 'https://picsum.photos/seed/playlist8/400/400' WHERE `playlist_id` = 8;   -- 毕业季
UPDATE `playlist` SET `cover` = 'https://picsum.photos/seed/playlist9/400/400' WHERE `playlist_id` = 9;   -- 华语流行
UPDATE `playlist` SET `cover` = 'https://picsum.photos/seed/playlist10/400/400' WHERE `playlist_id` = 10; -- 民谣精选
