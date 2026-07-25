<template>
  <div class="app-layout">
    <!-- Side Rail -->
    <aside class="side-rail">
      <div class="rail-brand">
        <div class="rail-mark"></div>
        <span>MD</span>
      </div>
      <nav class="rail-nav">
        <router-link to="/" class="rail-link" :class="{ active: $route.path === '/' }">
          <span class="rail-ico">🏠</span>
          <span class="rail-txt">首页</span>
        </router-link>
        <router-link to="/search" class="rail-link" :class="{ active: $route.path === '/search' }">
          <span class="rail-ico">🔍</span>
          <span class="rail-txt">搜索</span>
        </router-link>
        <router-link to="/my" class="rail-link" :class="{ active: $route.path === '/my' }">
          <span class="rail-ico">👤</span>
          <span class="rail-txt">我的</span>
        </router-link>
        <template v-if="isSinger">
          <router-link to="/singer" class="rail-link" :class="{ active: $route.path.startsWith('/singer') }">
            <span class="rail-ico">🎤</span>
            <span class="rail-txt">歌手</span>
          </router-link>
        </template>
        <template v-if="isAdmin">
          <router-link to="/admin" class="rail-link" :class="{ active: $route.path === '/admin' }">
            <span class="rail-ico">🛡</span>
            <span class="rail-txt">管理</span>
          </router-link>
        </template>
      </nav>
      <div class="rail-foot">
        <span class="rail-ver">v1.0</span>
      </div>
    </aside>

    <!-- Main Area -->
    <div class="app-main">
      <!-- Top Bar -->
      <header class="app-topbar">
        <div class="tb-left">
          <button class="tb-back" v-if="showBack" @click="goBack">← 返回</button>
          <!-- Page Tabs -->
          <nav class="tb-tabs">
            <router-link to="/" class="tb-tab" :class="{ active: $route.path === '/' }">🏠 首页</router-link>
            <router-link to="/search" class="tb-tab" :class="{ active: $route.path === '/search' }">🔍 搜索</router-link>
            <router-link to="/my" class="tb-tab" :class="{ active: $route.path === '/my' }">👤 我的</router-link>
          </nav>
        </div>
        <div class="tb-right">
          <div class="tb-live"><i class="tb-dot"></i> Online</div>
          <div class="tb-user" v-if="userInfo">
            <span class="tb-ava">{{ userInfo.avatar || '👤' }}</span>
            <span class="tb-name">{{ userInfo.nickname || userInfo.username }}</span>
          </div>
          <button class="tb-logout" v-if="isLoggedIn" @click="logout">退出</button>
          <button class="tb-login" v-if="!isLoggedIn" @click="$router.push('/login')">登录 / 注册</button>
        </div>
      </header>

      <!-- Page Content -->
      <div class="app-content">
        <router-view v-slot="{ Component }">
          <keep-alive include="HomeView,SearchView,MyProfileView,SingerCenterView">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </div>

      <!-- Player Bar -->
      <footer class="app-player glass-strong">
        <div class="pl-glow" v-if="currentSong"></div>
        <div class="pl-left">
          <div class="pl-cover" v-if="currentSong" @click="$router.push('/song/' + currentSong.songId)">
            <span class="pl-cover-emoji">{{ currentSong.cover || '🎵' }}</span>
            <div class="pl-cover-spin" :class="{ playing: player.isPlaying }"></div>
          </div>
          <div class="pl-cover pl-cover-empty" v-else>
            <span class="pl-cover-emoji">🎵</span>
          </div>
          <div class="pl-meta" v-if="currentSong">
            <div class="pl-title">{{ currentSong.name }}</div>
            <div class="pl-artist">{{ currentSong.singerName }}</div>
          </div>
          <div class="pl-meta" v-else>
            <div class="pl-title pl-title-empty">未播放</div>
            <div class="pl-artist">选择一首歌曲开始</div>
          </div>
          <div class="pl-eq" v-if="currentSong">
            <div class="eq-bars" :class="{ paused: !player.isPlaying }">
              <i class="eq-bar"></i><i class="eq-bar"></i><i class="eq-bar"></i><i class="eq-bar"></i>
            </div>
          </div>
        </div>
        <div class="pl-center">
          <div class="pl-ctrls">
            <button class="pl-btn pl-mode" :title="'模式: ' + modeLabel" @click="player.playMode = player.playMode === 'sequence' ? 'loop' : player.playMode === 'loop' ? 'random' : 'sequence'">
              <span v-if="player.playMode === 'sequence'">🔁</span>
              <span v-else-if="player.playMode === 'loop'">🔂</span>
              <span v-else>🔀</span>
            </button>
            <button class="pl-btn" @click="player.prev" :disabled="!player.hasPrev">⏮</button>
            <button class="pl-btn pl-play" @click="player.togglePlay">{{ player.isPlaying ? '⏸' : '▶' }}</button>
            <button class="pl-btn" @click="player.next" :disabled="!player.hasNext">⏭</button>
          </div>
          <div class="pl-prog">
            <span class="pl-time">{{ fmt(player.currentTime) }}</span>
            <div class="pl-bar" @click="seek">
              <div class="pl-fill" :style="{ width: player.progress + '%' }"></div>
              <div class="pl-thumb" :style="{ left: player.progress + '%' }"></div>
            </div>
            <span class="pl-time">{{ fmt(player.duration) }}</span>
          </div>
        </div>
        <div class="pl-right">
          <span class="pl-vol-ico">🔊</span>
          <input type="range" min="0" max="100" v-model="player.volume" @input="onVolume" class="pl-vol" />
        </div>
        <audio ref="audioRef" :src="currentSong?.url" @timeupdate="onTimeUpdate"></audio>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usePlayerStore } from '../store/player'
import { authApi } from '../api'

const router = useRouter()
const route = useRoute()
const player = usePlayerStore()
const audioRef = ref(null)
const userInfo = ref(null)
const roles = ref([])
const token = ref(localStorage.getItem('token'))

const isLoggedIn = computed(() => !!token.value)
const isSinger = computed(() => roles.value.includes('SINGER') || roles.value.includes('ADMIN'))
const isAdmin = computed(() => roles.value.includes('ADMIN'))
const currentSong = computed(() => player.currentSong)
const showBack = computed(() => route.path !== '/')

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

const modeLabel = computed(() => ({
  sequence: '列表循环',
  loop: '单曲循环',
  random: '随机播放'
}[player.playMode]))

onMounted(() => {
  player.setAudio(audioRef.value)
  loadUserInfo()
})

const loadUserInfo = async () => {
  try {
    const data = await authApi.getUserInfo()
    if (data?.code === 200) {
      userInfo.value = data.data
      roles.value = data.data.roles || []
    }
  } catch (e) { /* not logged in */ }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  token.value = null
  roles.value = []
  userInfo.value = null
  router.push('/')
}

const onTimeUpdate = () => { player.currentTime = audioRef.value?.currentTime || 0 }
const seek = (e) => {
  const bar = e.currentTarget
  const pct = e.offsetX / bar.offsetWidth
  if (audioRef.value?.duration) audioRef.value.currentTime = pct * audioRef.value.duration
}
const onVolume = () => { if (audioRef.value) audioRef.value.volume = player.volume / 100 }
const fmt = (s) => {
  if (!s || isNaN(s)) return '0:00'
  const m = Math.floor(s / 60), sec = Math.floor(s % 60)
  return `${m}:${sec.toString().padStart(2, '0')}`
}
</script>

<style scoped>
/* ========== Root Layout ========== */
.app-layout {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #080914;
  color: #f3f2ef;
  font-family: "Space Grotesk", "Noto Sans SC", system-ui, sans-serif;
}

/* ========== Side Rail ========== */
.side-rail {
  width: 72px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.02);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  padding: 1rem 0;
}

.rail-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.5rem 0 1.5rem;
}

.rail-mark {
  width: 28px;
  height: 28px;
  border: 2px solid #46f6e6;
  border-radius: 6px;
  transform: rotate(45deg);
  clip-path: polygon(0 0, 100% 0, 100% 55%, 55% 55%, 55% 100%, 0 100%);
}

.rail-brand span {
  font-family: "Noto Serif SC", serif;
  font-weight: 900;
  font-size: 0.7rem;
  color: #46f6e6;
  letter-spacing: 0.05em;
}

.rail-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 0.5rem;
}

.rail-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  padding: 0.6rem 0.25rem;
  border-radius: 1rem;
  color: #9b9daa;
  font-size: 0.65rem;
  font-weight: 500;
  transition: all 240ms ease;
  position: relative;
}

.rail-link:hover {
  background: rgba(255, 255, 255, 0.04);
  color: #f3f2ef;
}

.rail-link.active {
  background: rgba(70, 246, 230, 0.08);
  color: #46f6e6;
}

.rail-link.active::before {
  content: "";
  position: absolute;
  left: -0.5rem;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: #46f6e6;
  border-radius: 0 2px 2px 0;
}

.rail-ico {
  font-size: 1.1rem;
}

.rail-txt {
  font-size: 0.6rem;
  letter-spacing: 0.02em;
}

.rail-foot {
  padding: 0.75rem 0.5rem;
  text-align: center;
}

.rail-ver {
  font-family: "IBM Plex Mono", monospace;
  font-size: 0.55rem;
  color: #9b9daa;
  letter-spacing: 0.08em;
}

/* ========== Main Area ========== */
.app-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

/* ========== Topbar ========== */
.app-topbar {
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  background: rgba(8, 9, 20, 0.92);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(12px);
}

.tb-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.tb-back {
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #9b9daa;
  font-size: 0.72rem;
  transition: all 240ms ease;
}

.tb-back:hover {
  color: #f3f2ef;
  border-color: #46f6e6;
  background: rgba(70, 246, 230, 0.04);
}

/* --- Page Tabs --- */
.tb-tabs {
  display: flex;
  gap: 0.25rem;
  padding: 0.25rem;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.tb-tab {
  padding: 0.4rem 1rem;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 500;
  color: #9b9daa;
  transition: all 240ms ease;
  white-space: nowrap;
}

.tb-tab:hover {
  color: #f3f2ef;
  background: rgba(255, 255, 255, 0.06);
}

.tb-tab.active {
  background: #46f6e6;
  color: #080914;
  font-weight: 600;
}

.tb-breadcrumb {
  font-size: 0.78rem;
  color: #9b9daa;
  letter-spacing: 0.04em;
}

.tb-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.tb-live {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.62rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #46f6e6;
  padding: 0.3rem 0.65rem;
  border-radius: 999px;
  border: 1px solid rgba(70, 246, 230, 0.2);
  background: rgba(70, 246, 230, 0.04);
}

.tb-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #46f6e6;
  animation: pulse 1.8s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.35; }
}

.tb-user {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.25rem 0.75rem 0.25rem 0.25rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.tb-ava {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: rgba(70, 246, 230, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
}

.tb-name {
  font-size: 0.75rem;
  color: #f3f2ef;
  font-weight: 500;
}

.tb-logout {
  padding: 0.35rem 0.65rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 107, 107, 0.25);
  color: #ff6b6b;
  font-size: 0.68rem;
  transition: background 240ms ease;
}

.tb-logout:hover {
  background: rgba(255, 107, 107, 0.08);
}

.tb-login {
  padding: 0.35rem 0.85rem;
  border-radius: 999px;
  border: 1px solid rgba(70, 246, 230, 0.3);
  color: #46f6e6;
  font-size: 0.72rem;
  font-weight: 500;
  transition: all 240ms ease;
}

.tb-login:hover {
  background: rgba(70, 246, 230, 0.08);
  border-color: #46f6e6;
}

/* ========== Content ========== */
.app-content {
  flex: 1;
  overflow-y: scroll;
  min-height: 0;
  padding: 2rem;
}

/* ========== Player Bar ========== */
.app-player {
  height: 80px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 0 1.5rem;
  gap: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  position: relative;
  overflow: hidden;
}

.pl-glow {
  position: absolute;
  bottom: -50%;
  left: 20%;
  width: 60%;
  height: 200%;
  background: radial-gradient(ellipse, rgba(70, 246, 230, 0.06), transparent 70%);
  pointer-events: none;
}

.pl-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 0 0 220px;
  cursor: pointer;
  position: relative;
  z-index: 1;
}

.pl-cover {
  width: 50px;
  height: 50px;
  border-radius: 0.85rem;
  background: linear-gradient(135deg, rgba(70, 246, 230, 0.12), rgba(146, 93, 255, 0.12));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
}

.pl-cover-emoji {
  position: relative;
  z-index: 1;
}

.pl-cover-spin {
  position: absolute;
  inset: 0;
  border: 2px solid transparent;
  border-top-color: #46f6e6;
  border-radius: 0.85rem;
  opacity: 0;
}

.pl-cover-spin.playing {
  opacity: 1;
  animation: spin 3s linear infinite;
}

.pl-cover-empty {
  opacity: 0.4;
}

.pl-meta {
  overflow: hidden;
  flex: 1;
  min-width: 0;
}

.pl-title {
  font-weight: 600;
  font-size: 0.82rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pl-title-empty {
  color: #9b9daa;
}

.pl-artist {
  color: #9b9daa;
  font-size: 0.72rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pl-eq {
  flex-shrink: 0;
}

.pl-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  position: relative;
  z-index: 1;
}

.pl-ctrls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.pl-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  color: #f3f2ef;
  transition: all 240ms ease;
}

.pl-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.08);
  transform: scale(1.1);
}

.pl-btn:disabled {
  opacity: 0.3;
  cursor: default;
}

.pl-mode {
  font-size: 0.75rem;
  opacity: 0.7;
}

.pl-mode:hover {
  opacity: 1;
}

.pl-play {
  width: 42px;
  height: 42px;
  font-size: 1rem;
  background: linear-gradient(135deg, #46f6e6, #2ba89f);
  color: #080914;
  box-shadow: 0 2px 12px rgba(70, 246, 230, 0.25);
}

.pl-play:hover {
  background: linear-gradient(135deg, #5ffff5, #36c4bb) !important;
  transform: scale(1.08);
  box-shadow: 0 4px 20px rgba(70, 246, 230, 0.35);
}

.pl-prog {
  width: 100%;
  max-width: 480px;
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.pl-bar {
  flex: 1;
  height: 4px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
  cursor: pointer;
  position: relative;
  transition: height 150ms ease;
}

.pl-bar:hover {
  height: 6px;
}

.pl-fill {
  height: 100%;
  background: linear-gradient(90deg, #46f6e6, #925dff);
  border-radius: 2px;
  transition: width 100ms linear;
  position: relative;
}

.pl-thumb {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background: #f3f2ef;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 150ms ease;
  box-shadow: 0 0 8px rgba(70, 246, 230, 0.4);
}

.pl-bar:hover .pl-thumb {
  opacity: 1;
}

.pl-time {
  color: #9b9daa;
  font-size: 0.65rem;
  width: 36px;
  text-align: center;
  font-variant-numeric: tabular-nums;
  font-family: var(--ark-mono);
}

.pl-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 0 0 130px;
  position: relative;
  z-index: 1;
}

.pl-vol-ico {
  font-size: 0.9rem;
}

.pl-vol {
  width: 72px;
  accent-color: #46f6e6;
  cursor: pointer;
}
</style>
