<template>
  <div class="my-profile">
    <h2 class="page-title">👤 我的</h2>

    <div class="tabs">
      <button :class="{ active: tab === 'collections' }" @click="tab = 'collections'">
        ❤️ 收藏 <span class="tab-count">{{ collections.length }}</span>
      </button>
      <button :class="{ active: tab === 'history' }" @click="tab = 'history'">
        🕐 历史 <span class="tab-count">{{ history.length }}</span>
      </button>
      <button :class="{ active: tab === 'notifications' }" @click="tab = 'notifications'">
        🔔 通知 <span class="tab-count">{{ unreadCount }}</span>
      </button>
      <button :class="{ active: tab === 'following' }" @click="tab = 'following'">
        ⭐ 关注 <span class="tab-count">{{ following.length }}</span>
      </button>
    </div>

    <!-- 收藏 -->
    <div v-if="tab === 'collections'" class="card">
      <h3>❤️ 我的收藏</h3>
      <div v-if="loading" class="loading-state"><div class="loading-ring"></div></div>
      <div v-else-if="collections.length === 0" class="empty-state">
        <span class="empty-icon">❤️</span>
        <p>暂无收藏，去发现好音乐吧</p>
      </div>
      <div v-else class="song-list">
        <div v-for="c in collections" :key="c.collectId" class="song-item">
          <div class="song-cover" @click="$router.push('/song/' + c.songId)">{{ c.cover || '🎵' }}</div>
          <div class="song-info" @click="$router.push('/song/' + c.songId)">
            <div class="song-name">{{ c.name }}</div>
            <div class="song-artist">{{ c.singerName }}</div>
          </div>
          <div class="song-meta">
            <span class="badge">{{ c.targetType === 1 ? '歌曲' : '歌单' }}</span>
            <span class="time">{{ formatDate(c.createTime) }}</span>
          </div>
          <button @click="uncollect(c)" class="btn-sm btn-danger">取消收藏</button>
        </div>
      </div>
    </div>

    <!-- 播放历史 -->
    <div v-if="tab === 'history'" class="card">
      <div class="card-header">
        <h3>🕐 播放历史</h3>
        <button @click="onClearHistory" class="btn-sm btn-danger">清空历史</button>
      </div>
      <div v-if="loading" class="loading-state"><div class="loading-ring"></div></div>
      <div v-else-if="history.length === 0" class="empty-state">
        <span class="empty-icon">🕐</span>
        <p>暂无播放历史</p>
      </div>
      <div v-else class="song-list">
        <div v-for="h in history" :key="h.historyId" class="song-item">
          <div class="song-cover" @click="$router.push('/song/' + h.songId)">{{ h.cover || '🎵' }}</div>
          <div class="song-info" @click="$router.push('/song/' + h.songId)">
            <div class="song-name">{{ h.name }}</div>
            <div class="song-artist">{{ h.singerName }}</div>
          </div>
          <div class="song-meta">
            <span class="time">播放 {{ h.playDuration || 0 }}s</span>
            <span class="time">{{ formatDate(h.playTime) }}</span>
          </div>
          <button @click="player.loadSong(h)" class="btn-sm">▶ 播放</button>
        </div>
      </div>
    </div>

    <!-- 通知 -->
    <div v-if="tab === 'notifications'" class="card">
      <h3>🔔 消息通知</h3>
      <div v-if="loading" class="loading-state"><div class="loading-ring"></div></div>
      <div v-else-if="notifications.length === 0" class="empty-state">
        <span class="empty-icon">🔔</span>
        <p>暂无通知</p>
      </div>
      <div v-else class="notif-list">
        <div v-for="n in notifications" :key="n.notificationId"
             :class="['notif-item', { unread: n.isRead === 0 }]"
             @click="markRead(n.notificationId)">
          <div class="notif-type">{{ typeEmoji(n.type) }}</div>
          <div class="notif-body">
            <div class="notif-title">{{ n.title }}</div>
            <div class="notif-content">{{ n.content }}</div>
            <div class="notif-time">{{ formatDate(n.createTime) }}</div>
          </div>
          <span v-if="n.isRead === 0" class="unread-dot"></span>
        </div>
      </div>
    </div>

    <!-- 关注歌手 -->
    <div v-if="tab === 'following'" class="card">
      <h3>⭐ 关注的歌手</h3>
      <div v-if="loading" class="loading-state"><div class="loading-ring"></div></div>
      <div v-else-if="following.length === 0" class="empty-state">
        <span class="empty-icon">⭐</span>
        <p>还没有关注任何歌手</p>
      </div>
      <div v-else class="singer-grid">
        <div v-for="s in following" :key="s.singerId" class="singer-card">
          <div class="singer-avatar">{{ s.avatar || '🎤' }}</div>
          <div class="singer-name">{{ s.name }}</div>
          <div class="singer-intro">{{ s.intro || '暂无简介' }}</div>
          <button @click="unfollow(s.singerId)" class="btn-sm btn-danger">取消关注</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated, computed } from 'vue'
import { meApi } from '../api'
import { usePlayerStore } from '../store/player'
import { ElMessage } from 'element-plus'

const player = usePlayerStore()
const tab = ref('collections')
const loading = ref(false)
const collections = ref([])
const history = ref([])
const notifications = ref([])
const following = ref([])

const unreadCount = computed(() => notifications.value.filter(n => n.isRead === 0).length)

const init = () => {
  document.title = '我的 · Music Dreamer'
  loadAll()
}

onMounted(init)
onActivated(init)

const loadAll = async () => {
  loading.value = true
  await Promise.all([loadCollections(), loadHistory(), loadNotifications(), loadFollowing()])
  loading.value = false
}

const loadCollections = async () => {
  try {
    const { data } = await meApi.getCollections()
    collections.value = (data?.data || []).map(c => ({
      ...c,
      name: c.targetType === 1 ? `歌曲 #${c.targetId}` : `歌单 #${c.targetId}`,
      singerName: '',
      cover: ''
    }))
  } catch (e) { collections.value = [] }
}

const loadHistory = async () => {
  try {
    const { data } = await meApi.getHistory()
    history.value = (data?.data || []).map(h => ({
      ...h,
      name: `歌曲 #${h.songId}`,
      singerName: '',
      cover: ''
    }))
  } catch (e) { history.value = [] }
}

const loadNotifications = async () => {
  try {
    const { data } = await meApi.getNotifications()
    notifications.value = data?.data || []
  } catch (e) { notifications.value = [] }
}

const loadFollowing = async () => {
  try {
    const { data } = await meApi.getFollowing()
    following.value = (data?.data || []).map(f => ({
      singerId: f.singerId,
      name: `歌手 #${f.singerId}`,
      intro: '',
      avatar: ''
    }))
  } catch (e) { following.value = [] }
}

const uncollect = async (c) => {
  const { data } = await meApi.uncollect(c.targetId, c.targetType)
  data?.code === 200 ? (ElMessage.success('已取消收藏'), loadCollections()) : ElMessage.error('操作失败')
}
const onClearHistory = async () => {
  const { data } = await meApi.clearHistory()
  data?.code === 200 ? (ElMessage.success('已清空'), loadHistory()) : ElMessage.error('操作失败')
}
const markRead = async (id) => {
  const { data } = await meApi.markNotificationRead(id)
  if (data?.code === 200) {
    const n = notifications.value.find(x => x.notificationId === id)
    if (n) n.isRead = 1
  }
}
const unfollow = async (singerId) => {
  const { data } = await meApi.unfollowSinger(singerId)
  data?.code === 200 ? (ElMessage.success('已取消关注'), loadFollowing()) : ElMessage.error('操作失败')
}

const formatDate = (t) => t ? new Date(t).toLocaleDateString() : '-'
const typeEmoji = (t) => ({ 1: '📢', 2: '🎵', 3: '💬', 4: '❤️' }[t] || '📌')
</script>

<style scoped>
.my-profile { max-width: 900px; margin: 0 auto; }

.page-title {
  font-family: var(--ark-display);
  font-weight: 900;
  font-size: clamp(1.5rem, 3vw, 2rem);
  margin-bottom: 1.5rem;
  text-transform: uppercase;
  letter-spacing: -0.04em;
}

/* --- Tabs --- */
.tabs {
  display: flex;
  gap: 0.4rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  padding: 0.25rem;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  width: fit-content;
}

.tabs button {
  padding: 0.5rem 1.1rem;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 500;
  color: #9b9daa;
  transition: all 240ms ease;
  white-space: nowrap;
}

.tabs button:hover {
  color: #f3f2ef;
  background: rgba(255, 255, 255, 0.06);
}

.tabs button.active {
  background: #46f6e6;
  color: #080914;
  font-weight: 600;
}

.tab-count {
  font-size: 0.68rem;
  opacity: 0.7;
  margin-left: 0.2rem;
}

/* --- Card --- */
.card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 1.5rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.card h3 {
  font-size: 0.72rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #46f6e6;
  margin-bottom: 1rem;
  font-family: var(--ark-mono);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.card-header h3 { margin-bottom: 0; }

/* --- Loading --- */
.loading-state {
  display: flex;
  justify-content: center;
  padding: 2rem;
}

.loading-ring {
  width: 32px;
  height: 32px;
  border: 2px solid rgba(70, 246, 230, 0.15);
  border-top-color: #46f6e6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* --- Empty --- */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  color: #9b9daa;
  border-radius: 1rem;
  border: 1px dashed rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.02);
}

.empty-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  opacity: 0.5;
}

.empty-state p {
  font-size: 0.85rem;
}

/* --- Song List --- */
.song-list {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.song-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 0.75rem;
  border-radius: 1rem;
  transition: background 240ms ease;
}

.song-item:hover {
  background: rgba(255, 255, 255, 0.04);
}

.song-cover {
  width: 46px;
  height: 46px;
  border-radius: 0.75rem;
  background: linear-gradient(135deg, rgba(70, 246, 230, 0.08), rgba(146, 93, 255, 0.08));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  cursor: pointer;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.song-info {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.song-name {
  font-weight: 600;
  font-size: 0.88rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist {
  color: #9b9daa;
  font-size: 0.78rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-meta {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-shrink: 0;
}

.badge {
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  font-size: 0.68rem;
  background: rgba(70, 246, 230, 0.08);
  border: 1px solid rgba(70, 246, 230, 0.2);
  color: #46f6e6;
}

.time {
  color: #9b9daa;
  font-size: 0.72rem;
}

/* --- Buttons --- */
.btn-sm {
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #c8c9d0;
  font-size: 0.72rem;
  transition: all 240ms ease;
}

.btn-sm:hover {
  border-color: #46f6e6;
  color: #46f6e6;
  background: rgba(70, 246, 230, 0.04);
}

.btn-sm.btn-danger {
  color: #ff6b6b;
  border-color: rgba(255, 107, 107, 0.25);
}

.btn-sm.btn-danger:hover {
  background: rgba(255, 107, 107, 0.08);
  border-color: #ff6b6b;
}

/* --- Notifications --- */
.notif-list {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.notif-item {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 1rem;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  cursor: pointer;
  transition: background 240ms ease;
  position: relative;
}

.notif-item:hover {
  background: rgba(255, 255, 255, 0.04);
}

.notif-item.unread {
  border-left: 3px solid #46f6e6;
}

.notif-type {
  font-size: 1.3rem;
  flex-shrink: 0;
}

.notif-body { flex: 1; }

.notif-title {
  font-weight: 600;
  font-size: 0.88rem;
}

.notif-content {
  color: #9b9daa;
  font-size: 0.82rem;
  margin-top: 0.2rem;
}

.notif-time {
  color: #9b9daa;
  font-size: 0.7rem;
  margin-top: 0.4rem;
  opacity: 0.6;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #46f6e6;
  margin-top: 6px;
  flex-shrink: 0;
  box-shadow: 0 0 8px rgba(70, 246, 230, 0.4);
}

/* --- Singer Grid --- */
.singer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.singer-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 1.25rem;
  padding: 1.25rem;
  text-align: center;
  transition: all 240ms ease;
}

.singer-card:hover {
  border-color: rgba(70, 246, 230, 0.15);
  transform: translateY(-2px);
}

.singer-avatar {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.singer-name {
  font-weight: 600;
  font-size: 0.9rem;
}

.singer-intro {
  color: #9b9daa;
  font-size: 0.75rem;
  margin: 0.5rem 0 0.75rem;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* --- Responsive --- */
@media (max-width: 768px) {
  .song-meta { display: none; }
  .tabs { width: 100%; justify-content: center; }
}
</style>
