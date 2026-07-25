SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE music_dreamer;

INSERT IGNORE INTO user (username, password, email, nickname, gender, signature, status) VALUES
('qianqi',  '$2a$10$y2p2aXO/xXNhvsS7sUUTB.CH4AEmVGdnejfEhAs8LPijj0TTbjkH6', 'qian@music.com',  '钱七', 1, '电子音乐迷', 1),
('sunba',   '$2a$10$y2p2aXO/xXNhvsS7sUUTB.CH4AEmVGdnejfEhAs8LPijj0TTbjkH6', 'sun@music.com',   '孙八', 2, '爵士爱好者', 1),
('zhoujiu', '$2a$10$y2p2aXO/xXNhvsS7sUUTB.CH4AEmVGdnejfEhAs8LPijj0TTbjkH6', 'zhou@music.com',  '周九', 1, '说唱达人',   1),
('wushi',   '$2a$10$y2p2aXO/xXNhvsS7sUUTB.CH4AEmVGdnejfEhAs8LPijj0TTbjkH6', 'wu@music.com',    '吴十', 2, '古风音乐控', 0);

INSERT IGNORE INTO user_role (user_id, role_id) VALUES (6,1),(7,1),(8,1),(9,1);


INSERT IGNORE INTO song (name, singer_id, album_id, duration, url, lyrics, genre, language, release_date, play_count, like_count, status) VALUES
('喜欢你', 4, 5, 240, '/music/xihuanni.mp3', '喜欢你', '流行', '粤语', '2014-08-11', 1870000, 105000, 1),
('画', 4, 5, 210, '/music/hua.mp3', '画', '流行', '国语', '2016-01-05', 1560000, 88000, 1),
('句号', 4, 5, 255, '/music/juhao.mp3', '句号', '流行', '国语', '2019-12-27', 2340000, 134000, 1),
('超能力', 4, 5, 230, '/music/chaonengli.mp3', '超能力', '流行', '国语', '2026-07-20', 12000, 800, 2),
('透明', 4, 5, 245, '/music/touming.mp3', '透明', '流行', '国语', '2026-07-18', 8500, 500, 2),
('睡公主', 4, 5, 260, '/music/shuigongzhu.mp3', '睡公主', '流行', '粤语', '2009-10-27', 980000, 52000, 0),
('AINY', 4, 5, 225, '/music/ainy.mp3', 'AINY', '流行', '国语', '2010-08-13', 760000, 41000, 0);


INSERT IGNORE INTO song (name, singer_id, album_id, duration, url, lyrics, genre, language, release_date, play_count, like_count, status) VALUES
('不为谁而作的歌', 2, 3, 280, '/music/buweishui.mp3', '不为谁而作的歌', '流行', '国语', '2015-12-03', 1890000, 108000, 1),
('将故事写成我们', 2, 3, 290, '/music/jianggushi.mp3', '将故事写成我们', '流行', '国语', '2019-09-17', 1670000, 95000, 1),
('新地球', 2, 3, 265, '/music/xindiqiu.mp3', '新地球', '流行', '国语', '2014-12-27', 1450000, 82000, 2);


INSERT IGNORE INTO singer_apply (user_id, real_name, id_card, intro, gender, country, status, apply_time, review_time, reject_reason) VALUES
(2, '张三丰', '110101199001011234', '独立音乐人，擅长民谣吉他弹唱', 1, '中国内地', 0, '2026-07-20 10:30:00', NULL, NULL),
(3, '李美琳', '310101199508152345', '音乐学院毕业，主修声乐', 2, '中国内地', 0, '2026-07-21 14:20:00', NULL, NULL),
(6, '钱小强', '440301199210203456', '电子音乐制作人，DJ经验5年', 1, '中国内地', 0, '2026-07-22 09:15:00', NULL, NULL),
(7, '孙小美', '510101199807014567', '爵士歌手，曾在多个酒吧驻唱', 2, '中国内地', 2, '2026-07-15 16:00:00', '2026-07-16 10:00:00', '材料不完整'),
(8, '周小龙', '330101199312156789', '说唱歌手，地下说唱比赛冠军', 1, '中国内地', 2, '2026-07-10 11:30:00', '2026-07-12 15:00:00', '身份证照片不清晰');


INSERT IGNORE INTO song_audit (song_id, status, audit_time, auditor_id, reject_reason) VALUES
(34, 1, '2026-07-20 12:00:00', 1, NULL),
(35, 2, '2026-07-19 14:00:00', 1, '歌词内容不符合平台规范'),
(39, 1, '2026-07-22 10:00:00', 1, NULL);


INSERT IGNORE INTO user_collect (user_id, target_id, target_type) VALUES
(4,2,1),(4,5,1),(4,10,1),(4,14,1),(4,19,1),(4,25,1),(4,31,1),(4,1,2),(4,3,2),(4,9,2),(4,10,2);


INSERT IGNORE INTO user_history (user_id, song_id, play_duration, play_time) VALUES
(4,2,269,'2026-07-23 08:15:00'),(4,5,299,'2026-07-23 08:20:00'),(4,10,205,'2026-07-23 09:00:00'),
(4,14,265,'2026-07-23 10:30:00'),(4,19,261,'2026-07-23 11:45:00'),(4,25,258,'2026-07-23 14:00:00'),
(4,31,267,'2026-07-23 15:30:00'),(4,7,267,'2026-07-23 16:00:00'),(4,11,235,'2026-07-23 17:15:00'),
(4,22,245,'2026-07-23 18:30:00'),(4,3,223,'2026-07-24 07:30:00'),(4,16,265,'2026-07-24 08:00:00'),
(4,28,285,'2026-07-24 08:30:00'),(4,33,289,'2026-07-24 09:00:00'),(4,1,342,'2026-07-24 09:30:00');


INSERT IGNORE INTO notification (user_id, title, content, type, is_read) VALUES
(4,'歌手认证通过','恭喜你已通过歌手认证，现在可以上传发布歌曲了！',1,1),
(4,'歌曲发布成功','你发布的歌曲《超能力》已进入审核队列，1-3个工作日内完成审核。',1,0),
(4,'歌曲审核拒绝','你发布的歌曲《透明》未通过审核，原因：歌词内容不符合平台规范。',1,0),
(4,'周杰伦发布了新专辑','你关注的歌手周杰伦发布了新专辑《最伟大的作品》，快来抢先收听！',2,0),
(4,'李荣浩巡演公告','你关注的歌手李荣浩宣布2026年全国巡演即将开启，北京站已开启预售！',2,0),
(4,'你的收藏被点赞','你收藏的歌曲《晴天》今日新增256个赞，登上热门榜Top 3！',4,1),
(4,'系统维护通知','平台将于本周日凌晨2:00-4:00进行系统升级维护。',1,0),
(4,'新歌推荐','根据你的听歌偏好，为你推荐《孤勇者》《起风了》《漠河舞厅》',1,0);


INSERT IGNORE INTO user_follow_singer (user_id, singer_id) VALUES (4,1),(4,2),(4,3),(4,5),(4,6),(4,7),(4,8);


INSERT IGNORE INTO user_collect (user_id, target_id, target_type) VALUES (6,7,1),(6,13,1),(6,22,1),(6,2,2),(6,9,2);
INSERT IGNORE INTO user_history (user_id, song_id, play_duration, play_time) VALUES (6,7,267,'2026-07-23 09:00:00'),(6,13,282,'2026-07-23 09:30:00'),(6,22,245,'2026-07-23 10:00:00');
INSERT IGNORE INTO notification (user_id, title, content, type, is_read) VALUES (6,'欢迎加入 Music Dreamer','感谢你的注册！',1,0);
INSERT IGNORE INTO user_follow_singer (user_id, singer_id) VALUES (6,1),(6,3);

INSERT IGNORE INTO user_collect (user_id, target_id, target_type) VALUES (7,11,1),(7,14,1),(7,26,1),(7,3,2),(7,6,2);
INSERT IGNORE INTO user_history (user_id, song_id, play_duration, play_time) VALUES (7,11,235,'2026-07-23 20:00:00'),(7,14,265,'2026-07-23 20:30:00'),(7,26,265,'2026-07-23 21:00:00');
INSERT IGNORE INTO notification (user_id, title, content, type, is_read) VALUES (7,'欢迎加入 Music Dreamer','感谢你的注册！',1,1);
INSERT IGNORE INTO user_follow_singer (user_id, singer_id) VALUES (7,4),(7,8);

INSERT IGNORE INTO user_collect (user_id, target_id, target_type) VALUES (8,19,1),(8,28,1),(8,33,1),(8,2,2),(8,5,2);
INSERT IGNORE INTO user_history (user_id, song_id, play_duration, play_time) VALUES (8,19,261,'2026-07-23 22:00:00'),(8,28,285,'2026-07-23 22:30:00'),(8,33,289,'2026-07-23 23:00:00');
INSERT IGNORE INTO notification (user_id, title, content, type, is_read) VALUES (8,'欢迎加入 Music Dreamer','感谢你的注册！',1,0);
INSERT IGNORE INTO user_follow_singer (user_id, singer_id) VALUES (8,6),(8,7);


INSERT IGNORE INTO playlist (name, description, user_id, play_count, song_count, status) VALUES
('电子迷幻 赛博朋克之夜', '适合深夜编程的电子音乐合集', 6, 178000, 8, 1),
('爵士酒馆 微醺夜晚', '慵懒爵士乐，配一杯红酒', 7, 145000, 6, 1),
('说唱工厂 地下之声', '中文说唱精选', 8, 213000, 7, 1),
('古风雅韵 诗意中国', '古风歌曲精选', 7, 189000, 6, 1);


INSERT IGNORE INTO playlist_song (playlist_id, song_id, sort) VALUES
(11,1,1),(11,7,2),(11,13,3),(11,17,4),(11,22,5),(11,28,6),(11,34,7),(11,37,8),
(12,4,1),(12,11,2),(12,14,3),(12,20,4),(12,26,5),(12,29,6),
(13,19,1),(13,28,2),(13,33,3),(13,22,4),(13,17,5),(13,10,6),(13,25,7),
(14,6,1),(14,9,2),(14,15,3),(14,21,4),(14,27,5),(14,30,6);


INSERT IGNORE INTO search_keyword (keyword, search_count, last_search_time) VALUES
('邓紫棋',11200,'2026-07-24 08:30:00'),('林俊杰',9500,'2026-07-24 07:45:00'),
('泡沫',8200,'2026-07-23 19:00:00'),('江南',7100,'2026-07-23 18:30:00'),
('浮夸',6000,'2026-07-23 17:00:00'),('喜欢你',5200,'2026-07-24 09:00:00'),
('句号',4800,'2026-07-24 09:30:00'),('富士山下',3900,'2026-07-23 16:00:00');

SET FOREIGN_KEY_CHECKS = 1;
