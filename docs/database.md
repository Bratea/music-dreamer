# 数据库设计

## 1. 用户表 (user)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| user_id | bigint | 主键，用户ID |
| username | varchar(50) | 用户名 |
| password | varchar(255) | 密码（BCrypt加密） |
| email | varchar(100) | 邮箱 |
| phone | varchar(20) | 手机号 |
| avatar | varchar(255) | 头像URL |
| nickname | varchar(50) | 昵称 |
| gender | tinyint | 性别 0:未知 1:男 2:女 |
| birthday | date | 生日 |
| signature | varchar(200) | 个性签名 |
| status | tinyint | 状态 0:禁用 1:正常 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

**索引**: idx_email, idx_phone, idx_username

## 2. 歌手表 (singer)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| singer_id | bigint | 主键，歌手ID |
| name | varchar(50) | 歌手名 |
| avatar | varchar(255) | 头像 |
| intro | text | 简介 |
| gender | tinyint | 性别 |
| country | varchar(50) | 国籍 |
| birthday | date | 出生日期 |
| status | tinyint | 状态 0:未认证 1:已认证 2:禁用 |
| user_id | bigint | 关联用户ID（歌手账号） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

**索引**: idx_name, idx_user_id

## 3. 歌曲表 (song)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| song_id | bigint | 主键，歌曲ID |
| name | varchar(100) | 歌曲名 |
| singer_id | bigint | 歌手ID |
| album_id | bigint | 专辑ID（可选） |
| duration | int | 时长（秒） |
| url | varchar(255) | 音频文件URL |
| cover | varchar(255) | 封面图片URL |
| lyrics | text | 歌词（LRC格式） |
| description | varchar(500) | 歌曲描述 |
| genre | varchar(50) | 流派 |
| language | varchar(20) | 语言 |
| release_date | date | 发行日期 |
| play_count | int | 播放次数 |
| like_count | int | 收藏次数 |
| status | tinyint | 状态 0:下架 1:上架 2:审核中 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

**索引**: idx_name, idx_singer_id, idx_album_id, idx_status

## 4. 歌单表 (playlist)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| playlist_id | bigint | 主键，歌单ID |
| name | varchar(100) | 歌单名 |
| cover | varchar(255) | 封面图片URL |
| description | text | 描述 |
| user_id | bigint | 创建者用户ID |
| play_count | int | 播放次数 |
| song_count | int | 歌曲数量 |
| status | tinyint | 状态 0:私密 1:公开 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

**索引**: idx_user_id, idx_status

## 5. 歌单歌曲关联表 (playlist_song)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | bigint | 主键 |
| playlist_id | bigint | 歌单ID |
| song_id | bigint | 歌曲ID |
| sort | int | 排序号 |
| create_time | datetime | 添加时间 |

**唯一索引**: uk_playlist_song (playlist_id, song_id)

## 6. 用户收藏表 (user_collect)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| collect_id | bigint | 主键 |
| user_id | bigint | 用户ID |
| target_id | bigint | 目标ID（歌曲/歌单） |
| target_type | tinyint | 类型 1:歌曲 2:歌单 |
| create_time | datetime | 收藏时间 |

**唯一索引**: uk_user_target (user_id, target_id, target_type)
**索引**: idx_user_id, idx_target_id

## 7. 用户历史记录表 (user_history)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| history_id | bigint | 主键 |
| user_id | bigint | 用户ID |
| song_id | bigint | 歌曲ID |
| play_duration | int | 播放时长（秒） |
| play_time | datetime | 播放时间 |

**索引**: idx_user_id, idx_song_id, idx_play_time

## 8. 消息通知表 (notification)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| notification_id | bigint | 主键 |
| user_id | bigint | 接收通知的用户ID |
| title | varchar(200) | 通知标题 |
| content | text | 通知内容 |
| type | tinyint | 类型 1:系统消息 2:关注歌手更新 3:评论 |
| is_read | tinyint | 是否已读 0:未读 1:已读 |
| create_time | datetime | 创建时间 |

**索引**: idx_user_id, idx_is_read

## 9. 专辑表 (album) 【可选】

| 字段名 | 类型 | 说明 |
|--------|------|------|
| album_id | bigint | 主键 |
| name | varchar(100) | 专辑名 |
| singer_id | bigint | 歌手ID |
| cover | varchar(255) | 封面 |
| description | text | 描述 |
| release_date | date | 发行日期 |
| status | tinyint | 状态 |
| create_time | datetime | 创建时间 |

---

## 分库分表建议

初期单库即可，数据量超过500万时考虑：

- **user_history 按 user_id 分表**（按时间范围或用户哈希）
- **user_collect 按 user_id 分表**
- **playlist_song 按 playlist_id 分表**

## 读写分离

- 主库：写操作（用户收藏、播放历史）
- 从库：读操作（歌曲列表、歌单详情、用户信息）

## 缓存策略

| 数据类型 | 缓存Key | 过期时间 | 说明 |
|---------|---------|----------|------|
| 热门歌单 | hot:playlist | 1小时 | 热门歌单Top100 |
| 高频歌曲 | hot:song | 1小时 | 高频播放歌曲 |
| 用户信息 | user:{userId} | 30分钟 | 用户基本信息 |
| 歌手信息 | singer:{singerId} | 1小时 | 歌手详情 |
| 歌曲详情 | song:{songId} | 2小时 | 歌曲元数据 |
| 搜索词 | search:keyword | 7天 | 热门搜索词 |
