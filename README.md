# Music Dreamer 悦享音乐

基于 Spring Cloud Alibaba 微服务架构的音乐平台，提供歌曲播放、推荐、收藏、歌单管理、用户历史记录、消息通知等功能。

## 技术栈

| 层级 | 技术 | 版本/说明 |
|------|------|----------|
| **语言** | Java | **21**（LTS） |
| **后端框架** | Spring Boot | **3.3.5** |
| **微服务** | Spring Cloud Alibaba | **2023.0.1.0**（Nacos + Sentinel + OpenFeign） |
| **网关** | Spring Cloud Gateway | 统一入口、JWT 鉴权、限流 |
| **注册/配置中心** | Nacos | 2.x 服务发现 + 配置管理 |
| **数据库** | MySQL | 8.0 + **MyBatis-Plus 3.5.5**（Spring Boot 3 兼容） |
| **缓存** | Redis | 6.x（热点数据、播放量统计、分布式锁） |
| **搜索引擎** | Elasticsearch | **7.17.25**（全文检索 + ik_max_word 分词 + Java API Client） |
| **分布式锁** | Redisson | **3.37.0**（并发收藏/点赞控制） |
| **认证** | JWT（jjwt） | **0.12.6** Access Token(15min) + Refresh Token(7天) + 角色声明 |
| **权限** | Spring Security + @EnableMethodSecurity | 基于角色的接口鉴权 |
| **API 文档** | Knife4j | OpenAPI 3.0 在线文档 |
| **单元测试** | JUnit 5 + Mockito + AssertJ | 核心业务逻辑覆盖 |
| **容器化** | Docker + Docker Compose | 一键启动全链路（全部服务 + 基础设施） |
| **前端** | Vue 3 + Vite | Pinia + Element Plus + Axios + 路由守卫 |
| **CI/CD** | GitHub Actions | 后端构建 + 前端 lint/build + SQL 检查 |
| **消息队列** | RabbitMQ 3.x | Topic 交换机 + 歌曲发布/删除/下架 3 队列 |
| **数据同步** | Canal | MySQL binlog → Elasticsearch 实时同步 |

## 系统架构

```
┌─────────────┐
│   Vue 前端   │
└──────┬──────┘
       │ HTTP
┌──────▼──────────────────────────────────────────┐
│       Spring Cloud Gateway（统一入口）            │
│       · JWT 鉴权                                  │
│       · 请求转发                                  │
└──┬──┬──┬──┬──────────────────────────────────┬──┘
   │  │  │  │                                  │
   ▼  ▼  ▼  ▼                                  ▼
┌─────┐┌─────┐┌──────┐┌─────────┐   ┌─────────────────┐
│user ││song ││play- ││search   │   │song-process     │
│svc  ││svc  ││list  ││svc      │   │svc              │
│:8081││:8082││:8083 ││:8084    │   │:8085            │
└──┬──┘└──┬──┘└──────┘└─────────┘   └────────┬────────┘
   │      │                                    │
   │      │  RabbitMQ 消息队列                  │
   │      │  (topic: music.exchange)            │
   │      │  · queue.song.publish ──→ 歌曲发布   │
   │      │  · queue.song.delete  ──→ 歌曲删除   │
   │      │  · queue.song.offline ──→ 歌曲下架   │
   │      └────────────────────────────────────→│
   │                                          
   ▼                                         
┌──────────┐    ┌──────────┐    ┌──────────┐
│  MySQL   │◄──►│  Redis   │◄──►│    ES    │
│  :3306   │    │  :6379   │    │  :9200   │
└──────────┘    └──────────┘    └──────────┘

Nacos :8848（注册/配置）     RabbitMQ :5672 / :15672（管理UI）
```

> **部署**：全部服务（前端 + 网关 + 6 个微服务 + 5 个基础设施）通过 `docker-compose up -d` 一键启动。

## 核心功能

### 用户端
- [x] 用户注册/登录（JWT + BCrypt）
- [x] 歌曲搜索（ES 多字段全文检索）
- [x] 歌曲播放（音频流 + 播放量统计）
- [x] 歌曲收藏/取消收藏（toggle）
- [x] 歌单创建/管理（增删改查 + 添加/移除歌曲）
- [x] 热门歌单推荐（播放量 TopN）
- [x] 播放历史记录
- [x] 个性化推荐（流派偏好 + 相似歌曲 + 每日推荐）
- [x] 消息通知（系统消息、标记已读）
- [x] 关注/取消关注歌手

### 歌手端
- [x] 歌手认证（申请 + 管理员审核）
- [x] 歌曲上传/发布（进审核队列 + RabbitMQ 异步处理）
- [x] 歌曲管理（上下架、编辑、我的歌曲列表）

### 管理端
- [x] 用户管理（增删改查、封禁/解封）
- [x] 歌手管理（认证审核）
- [x] 歌曲管理（审核通过/拒绝、强制下架）
- [x] 数据统计（播放量、用户量、近7天趋势）

## 项目结构

```
music-dreamer/
├── backend/                          # Java 21 + Spring Boot 3.3 微服务后端
│   ├── pom.xml                       # 父工程（6 模块，Spring Boot 3 兼容）
│   ├── gateway/                      # API 网关（:8080，JWT 鉴权 + 限流 + 路由）
│   ├── services/
│   │   ├── user-service/             # 用户服务（:8081）Auth + 角色 + 歌手认证 + 管理后台
│   │   ├── song-service/             # 歌曲服务（:8082）CRUD + 推荐 + 收藏 + RabbitMQ 生产者
│   │   ├── playlist-service/         # 歌单服务（:8083）歌单 + 收藏 + 历史 + 通知 + 关注
│   │   ├── search-service/           # 搜索服务（:8084）ES Java API Client + Canal binlog 同步
│   │   └── song-process-service/     # 歌曲异步处理（:8085）RabbitMQ 消费者
│   └── sql/                          # 初始化 SQL
├── docker/
│   ├── docker-compose.yml            # 一键启动全栈（基础设施 + 全部后端服务 + 前端）
│   └── Dockerfile                    # 各服务 Dockerfile（基于 eclipse-temurin-21）
├── frontend/                         # Vue 3 前端（Vite + Pinia + Element Plus）
│   ├── Dockerfile                    # 多阶段构建（Node 18 编译 → nginx 运行）
│   ├── nginx.conf                    # SPA 路由 + API 代理配置
│   └── src/
│       ├── api/index.js              # 所有后端接口（auth/song/admin/me）
│       ├── router/index.js           # Vue Router + 路由守卫
│       ├── views/                    # 页面组件
│       │   ├── LayoutView.vue        # 主布局（动态菜单）
│       │   ├── SingerCenterView.vue  # 🎤 歌手中心
│       │   ├── SingerApplyView.vue   # 📝 歌手认证申请
│       │   ├── AdminPanelView.vue    # 🛡 管理后台
│       │   └── MyProfileView.vue     # 👤 我的（收藏/历史/通知/关注）
│       └── store/                    # Pinia Store（播放器/用户状态）
└── docs/                             # 架构/数据库文档
```

## 快速开始

### 方式一：Docker 一键启动（推荐）

> 前提：安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)

```bash
cd docker && docker-compose up -d
```

一键启动全部服务（基础设施 + 后端 6 个微服务 + 前端），无需手动安装 JDK/Node/MySQL 等。

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 80 | http://localhost |
| API 网关 | 8080 | http://localhost:8080 |
| 用户服务 | 8081 | Auth + 角色 + 管理后台 |
| 歌曲服务 | 8082 | CRUD + 推荐 + 收藏 |
| 歌单服务 | 8083 | 歌单 + 收藏 + 历史 + 通知 |
| 搜索服务 | 8084 | ES 全文检索 |
| 歌曲处理服务 | 8085 | RabbitMQ 消费者 |
| MySQL | 3307 | root/root123456（docker-compose 映射 3307→3306） |
| Redis | 6379 | requirepass redis123456 |
| Elasticsearch | 9200 | 单节点开发模式 |
| Nacos | 8848 | 控制台 http://localhost:8848/nacos |
| RabbitMQ | 5672 / 15672 | musicdreamer / music123456；管理UI http://localhost:15672 |

```bash
# 停止所有服务
cd docker && docker-compose down

# 仅启动基础设施（自行开发后端时使用）
cd docker && docker-compose up -d mysql redis elasticsearch nacos rabbitmq
```

### 方式二：本地开发模式

> 需要本地安装：JDK 21、Maven 3.9+、Node 18、MySQL 8、Redis

```bash
# 1. 启动本地 MySQL + Redis

# 2. 导入数据库（按数字前缀顺序执行，03 引用了 05 的 song_id 31-33，需全部导入）
mysql -u root -p < sql/01_schema.sql
mysql -u root -p < sql/02_role-singer-audit.sql
mysql -u root -p music_dreamer < sql/03_mock_data.sql
mysql -u root -p music_dreamer < sql/04_cover_images.sql
mysql -u root -p music_dreamer < sql/05_rich_mock_data.sql

# 3. 启动后端（按顺序，开 6 个终端）
cd backend
mvn spring-boot:run -pl services/user-service
mvn spring-boot:run -pl services/song-service
mvn spring-boot:run -pl services/playlist-service
mvn spring-boot:run -pl services/search-service
mvn spring-boot:run -pl services/song-process-service
mvn spring-boot:run -pl gateway

# 4. 启动前端
cd frontend && npm install && npm run dev
```

前端 dev server: http://localhost:5173 → 代理到 Gateway http://localhost:8080

> **提示：** Docker 模式下数据库初始化 SQL 已通过 `docker-compose.yml` 挂载到 MySQL 容器自动执行，无需手动导入。

### Elasticsearch 数据灌入

MySQL 中的歌曲数据需要同步到 Elasticsearch 才能使用全文搜索。提供两种方式：

**方式 A：一键灌入脚本（推荐）**

```bash
# 先确保 MySQL 和 ES 已启动并有数据
pip install pymysql elasticsearch
python3 docker/seed_es.py          # 灌入数据
python3 docker/seed_es.py --reset  # 删除旧索引重新灌入
```

**方式 B：Canal 实时同步（自动）**

search-service 内置了 Canal 监听 MySQL binlog，当歌曲数据变更时会自动同步到 ES。
首次部署时请先执行方式 A 灌入历史数据，后续增量变更由 Canal 自动处理。

## API 文档

### Knife4j 在线文档

| 服务 | 地址 |
|------|------|
| Gateway 入口 | http://localhost:8080/doc.html |
| 用户服务 | http://localhost:8081/doc.html |
| 歌曲服务 | http://localhost:8082/doc.html |
| 歌单服务 | http://localhost:8083/doc.html |
| 搜索服务 | http://localhost:8084/doc.html |

### 核心接口速查

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 登录 | POST | `/api/auth/login` | 用户名/邮箱 + 密码 |
| 注册 | POST | `/api/auth/register` | 用户名、邮箱、密码 |
| 获取用户信息 | GET | `/api/auth/user/info` | Header: Bearer Token |
| 查询歌曲 | GET | `/api/song/{id}` | 歌曲详情 |
| 新增歌曲 | POST | `/api/song` | 管理员/歌手 |
| 更新歌曲 | PUT | `/api/song` | 歌曲信息 |
| 删除歌曲 | DELETE | `/api/song/{id}` | 删除歌曲 |
| 播放+统计 | POST | `/api/song/{id}/play` | 播放量+1 |
| 热门歌曲 | GET | `/api/song/hot` | 播放量 TopN |
| 相似推荐 | GET | `/api/song/{id}/similar` | 同流派推荐 |
| 搜索 | GET | `/api/search?keyword=xxx` | ES 全文检索 |
| 创建歌单 | POST | `/api/playlist` | status=1 公开/0 私密 |
| 歌单详情 | GET | `/api/playlist/{id}` | 含歌曲列表 |
| 添加歌曲到歌单 | POST | `/api/playlist/{id}/song/{songId}` | sort 排序参数 |
| 移除歌单歌曲 | DELETE | `/api/playlist/{id}/song/{songId}` | 自动更新 songCount |
| 热门歌单 | GET | `/api/playlist/hot` | 按播放量排序 |
| 用户歌单 | GET | `/api/playlist/user/{userId}` | 用户创建的歌单 |

### 请求/响应示例

**登录**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "test",
  "password": "123456"
}

// Response 200
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbG...NiJ9...",
    "refreshToken": "eyJhbG...NiJ9...",
    "userId": 1,
    "username": "test",
    "nickname": "Tester",
    "expireIn": 86400000
  }
}
```

**搜索**
```http
GET /api/search?keyword=晴天&page=1&size=20

// Response 200
{
  "code": 200,
  "data": {
    "songs": [{ "songId": 1, "name": "晴天", "singerName": "周杰伦", "playCount": 99999 }],
    "total": 1, "page": 1, "size": 20
  }
}
```

## 技术亮点

| 技术 | 实现 |
|------|------|
| 微服务拆分 | 6 个独立服务（user / song / playlist / search / song-process / gateway），OpenFeign 跨服务调用 |
| 注册配置中心 | Nacos 2.x 服务发现 + 配置管理 |
| 服务调用 | OpenFeign 声明式 REST 客户端，替代直接 Java 跨模块依赖 |
| 流量控制 | Sentinel 限流 + 降级 + 熔断 |
| 缓存策略 | Redis 缓存热门数据，播放量 INCR + 定时持久化 |
| 分布式锁 | Redisson 防止并发收藏/点赞数据不一致 |
| 全文检索 | Elasticsearch Java API Client + ik_max_word 分词 + 字段权重 name^3/singerName^2 |
| 推荐算法 | 热门 / 个性化（流派偏好）/ 相似歌曲 / 每日推荐 |
| 用户交互 | 收藏（歌曲/歌单 toggle）、播放历史、消息通知、关注歌手 |
| 数据同步 | Canal 监听 binlog 异步同步 MySQL → Elasticsearch |
| JWT 认证 | JJWT 0.12.x，Access Token(15min) + Refresh Token(7天) |
| 权限控制 | Spring Security + @EnableMethodSecurity + BCrypt 密码加密 |
| API 文档 | Knife4j (OpenAPI 3.0) 在线文档，所有接口 @Tag/@Operation 注解 |
| 单元测试 | Mockito + AssertJ 覆盖核心业务逻辑 |
| 容器化 | 多阶段构建 Dockerfile + Docker Compose 一键启动全栈（Java 21 基础镜像） |
| 前端 | Vue 3 + Pinia + Element Plus + Axios 拦截器 + 401 自动刷新 + nginx SPA 路由 |
| CI/CD | GitHub Actions：后端构建（JDK 21）+ 前端 lint/build + SQL 检查 |

## 推荐算法详解

| 策略 | 实现 | 缓存 |
|------|------|------|
| 热门推荐 | 播放量 Top100，分页返回 | 1 小时 TTL |
| 个性化推荐 | 基于用户历史听歌流派偏好（Top3 流派匹配） | 无 |
| 相似歌曲 | 同流派 + 排除已听，按播放量排序 | 无 |
| 每日推荐 | 个性化结果按 userId 缓存 | 1 天 TTL |

## Redis 缓存策略

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `play:count:{songId}` | Counter | 6 小时 | 播放量计数器，定时持久化 |
| `recommend:hot:songs` | List | 1 小时 | 热门歌曲 Top100 |
| `recommend:daily:{userId}` | List | 1 天 | 每日个性化推荐 |
| `user:{userId}` | Hash | 30 分钟 | 用户基本信息（预留） |
| `song:{songId}` | Hash | 2 小时 | 歌曲详情（预留） |

## ES 索引设计

- **Index**: `song`
- **分词器**: `ik_max_word`（索引）/ `ik_smart`（搜索）
- **字段权重**: `name^3` > `singerName^2` > `lyrics` > `genre`
- **Mapping**: songId(long) + name(text) + singerName(text) + lyrics(text) + genre(keyword) + language(keyword) + playCount(integer) + releaseDate(date)

## 单元测试

| 测试类 | 覆盖场景 |
|--------|---------|
| `AuthServiceTest` | 登录成功/失败、注册成功/重复用户名 |
| `SongServiceTest` | 歌曲 CRUD 基本操作 |
| `RecommendServiceTest` | 热门缓存命中/未命中场景 |

```bash
cd backend
mvn test -pl services/user-service,services/song-service,services/playlist-service
```

## 开发计划

| 阶段 | 状态 | 内容 |
|------|------|------|
| Phase 1 | ✅ | 基础架构、用户/歌曲/歌单核心功能 |
| Phase 2 | ✅ | ES 搜索、Redis 缓存、播放量统计 |
| Phase 3 | ✅ | 推荐系统、消息通知、Knife4j 文档 |
| Phase 4 | ✅ | 前端骨架、播放器、API 层、Pinia store |
| Phase 5 | ✅ | **歌手端**：角色权限、歌手认证、歌曲发布/审核、我的歌曲管理 |
| Phase 6 | ✅ | **管理后台**：用户/歌手/歌曲审核、数据统计 |
| Phase 7 | ✅ | **用户端**：关注歌手、我的收藏/播放历史/消息通知 |
| Phase 8 | ✅ | **技术升级**：Java 11→21、Spring Boot 2.7→3.3、javax→jakarta、ES 新客户端、Feign 重构 |
| Phase 9 | ⏳ | 部署上线、性能调优、移动端适配 |

## 新增接口速查

### 歌曲服务 — 收藏 (`/api/song/collect`)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/song/collect` | 收藏歌曲 `{songId}`（已收藏则取消） |
| DELETE | `/song/collect/{songId}` | 取消收藏歌曲 |
| GET | `/song/collect/{songId}` | 是否已收藏该歌曲 |

### 歌单服务 — 用户交互 (`/api/me`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/me/collections?targetType=` | 我的收藏列表（1=歌曲 2=歌单） |
| POST | `/me/collect` | 收藏 `{targetId, targetType}` |
| DELETE | `/me/collect/{targetId}?targetType=` | 取消收藏 |
| GET | `/me/collect/{targetId}?targetType=` | 是否已收藏 |
| GET | `/me/history?limit=` | 播放历史（默认 50 条） |
| DELETE | `/me/history` | 清空播放历史 |
| GET | `/me/notifications` | 我的消息通知列表 |
| PUT | `/me/notification/{id}/read` | 标记通知已读 |
| POST | `/me/follow` | 关注歌手 `{singerId}`（已关注则取消） |
| DELETE | `/me/follow/{singerId}` | 取消关注歌手 |
| GET | `/me/follow/{singerId}` | 是否已关注某歌手 |
| GET | `/me/following` | 我关注的歌手列表 |

### 歌手端 (`/api/singer`)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/singer/apply` | 提交歌手认证申请 |
| GET | `/singer/apply/my` | 查看我的认证状态 |
| GET | `/singer/admin/applies` | [管理员] 待审核列表 |
| PUT | `/singer/admin/apply/{id}/approve` | [管理员] 审核通过 |
| PUT | `/singer/admin/apply/{id}/reject` | [管理员] 审核拒绝 |
| POST | `/singer/song/publish` | 发布歌曲（自动进审核 + 触发 MQ） |
| GET | `/singer/song/my?singerId=&status=` | 我的歌曲列表 |
| PUT | `/singer/song/{id}/edit` | 编辑歌曲 |
| DELETE | `/singer/song/{id}` | 删除歌曲 |
| PUT | `/singer/song/{id}/offline` | 下架歌曲 |
| PUT | `/singer/song/{id}/online` | 重新提审 |
| GET | `/singer/manage/is-singer` | 判断是否歌手 |

### 管理端 (`/api/admin`)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/users?page=&size=` | 用户列表（分页） |
| PUT | `/admin/user/{id}/disable?status=` | 封禁/解封用户（0=封禁 1=正常） |
| DELETE | `/admin/user/{id}` | 删除用户 |
| GET | `/admin/role/list` | 角色列表 |
| GET | `/admin/role/user/{userId}` | 用户角色 |
| POST | `/admin/role/assign?userId=&roleId=` | 分配角色 |
| DELETE | `/admin/role/revoke?userId=&roleId=` | 撤销角色 |
| GET | `/admin/songs?status=&page=&size=` | 歌曲列表 |
| GET | `/admin/songs/pending` | 待审核歌曲 |
| PUT | `/admin/song/{id}/audit/pass` | 审核通过 |
| PUT | `/admin/song/{id}/audit/reject?reason=` | 审核拒绝 |
| PUT | `/admin/song/{id}/offline` | 强制下架 |
| GET | `/admin/stats/overview` | 数据概览 |
| GET | `/admin/stats/daily` | 近7天每日统计 |

## 贡献

欢迎提交 Issue 和 Pull Request！

## License

MIT
