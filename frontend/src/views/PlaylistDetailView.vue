<template>
  <div class="playlist-detail" v-if="playlist">
    <!-- Header -->
    <div class="pl-hero">
      <div class="pl-hero-bg">
        <div class="pl-hero-orb"></div>
      </div>
      <div class="pl-hero-content">
        <div class="pl-cover" :style="playlist.cover ? { backgroundImage: 'url(' + playlist.cover + ')', backgroundSize: 'cover', backgroundPosition: 'center' } : {}">
          <img v-if="playlist.coverUrl" :src="playlist.coverUrl" :alt="playlist.name" />
          <span v-if="!playlist.cover && !playlist.coverUrl" class="pl-cover-emoji">🎶</span>
        </div>
        <div class="pl-info">
          <span class="pl-type">歌单 · Playlist</span>
          <h1 class="pl-title">{{ playlist.name }}</h1>
          <p class="pl-desc">{{ playlist.description || '暂无描述' }}</p>
          <div class="pl-stats">
            <span>▶ {{ formatCount(playlist.playCount) }} 播放</span>
            <span>🎵 {{ songs.length || playlist.songCount || 0 }} 首歌曲</span>
          </div>
          <div class="pl-actions">
            <button class="pl-btn pl-btn-primary" @click="playAll">▶ 播放全部</button>
            <button class="pl-btn" @click="shufflePlay">🔀 随机播放</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Song List -->
    <div class="pl-songs glass">
      <div class="pl-songs-header">
        <span class="col-idx">#</span>
        <span class="col-title">歌曲</span>
        <span class="col-singer">歌手</span>
        <span class="col-dur">时长</span>
      </div>
      <div class="pl-songs-list">
        <div class="pl-song-item" v-for="(s, i) in songs" :key="s.songId || i" @click="playSong(s)">
          <span class="col-idx">{{ i + 1 }}</span>
          <span class="col-title">{{ s.name }}</span>
          <span class="col-singer">{{ s.singerName }}</span>
          <span class="col-dur">{{ formatDur(s.duration) }}</span>
        </div>
      </div>
      <div class="pl-empty" v-if="!songs.length">
        <span class="pl-empty-ico">🎵</span>
        <p>歌单暂无歌曲</p>
      </div>
    </div>
  </div>

  <div class="pl-loading" v-else>
    <div class="loading-ring"></div>
    <p>加载中...</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { playlistApi, songApi } from '../api'
import { usePlayerStore } from '../store/player'

const route = useRoute()
const player = usePlayerStore()

const playlist = ref(null)
const songs = ref([])

const formatCount = (n) => {
  if (!n) return '0'
  return n > 10000 ? (n / 10000).toFixed(1) + 'w' : n
}

onMounted(async () => {
  const id = route.params.id
  document.title = '歌单详情 · Music Dreamer'
  try {
    const res = await playlistApi.getById(id)
    const data = res?.data || res
    if (data) {
      playlist.value = data.playlist || data
      // 后端只返回 songId，使用 batch 端点一次性获取完整歌曲信息，避免 N+1
      const rawSongs = data.songs || []
      const songIds = rawSongs.map(s => s.songId).filter(Boolean)
      if (songIds.length > 0) {
        try {
          const batchRes = await songApi.getByIds(songIds)
          const batchData = batchRes?.data || batchRes
          songs.value = Array.isArray(batchData) ? batchData : []
        } catch (e) {
          songs.value = []
        }
      } else {
        songs.value = []
      }
      document.title = `${playlist.value.name} · Music Dreamer`
    }
  } catch (e) {
    console.error('加载歌单失败:', e)
  }
})

const playAll = () => {
  if (songs.value.length) player.loadSong(songs.value[0])
}

const shufflePlay = () => {
  if (!songs.value.length) return
  const idx = Math.floor(Math.random() * songs.value.length)
  player.loadSong(songs.value[idx])
}

const playSong = (s) => {
  player.loadSong(s)
}

const formatDur = (s) => {
  if (!s) return '0:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}:${sec.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.playlist-detail {
  max-width: 1100px;
  margin: 0 auto;
}

/* --- Hero --- */
.pl-hero {
  position: relative;
  border-radius: 2rem;
  padding: clamp(2rem, 4vw, 3rem);
  margin-bottom: 2rem;
  overflow: hidden;
  min-height: 260px;
  display: flex;
  align-items: center;
}

.pl-hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(146, 93, 255, 0.08), rgba(70, 246, 230, 0.06));
  z-index: 0;
}

.pl-hero-orb {
  position: absolute;
  width: 250px;
  height: 250px;
  top: -20%;
  right: -5%;
  background: radial-gradient(ellipse, rgba(146, 93, 255, 0.12), transparent 70%);
  animation: orbFloat 10s ease-in-out infinite;
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-15px, 10px); }
}

.pl-hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 2.5rem;
  align-items: center;
  width: 100%;
}

.pl-cover {
  width: 200px;
  height: 200px;
  flex-shrink: 0;
  border-radius: 1.5rem;
  background: linear-gradient(135deg, rgba(146, 93, 255, 0.12), rgba(70, 246, 230, 0.08));
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3.5rem;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
}

.pl-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pl-cover-emoji {
  position: relative;
  z-index: 1;
}

.pl-info {
  flex: 1;
}

.pl-type {
  font-family: var(--ark-mono);
  font-size: 0.62rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #925dff;
}

.pl-title {
  font-family: var(--ark-display);
  font-weight: 900;
  font-size: clamp(1.6rem, 3.5vw, 2.4rem);
  line-height: 1.1;
  margin: 0.5rem 0 0.5rem;
  background: linear-gradient(135deg, #f3f2ef, #925dff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.pl-desc {
  color: #9b9daa;
  font-size: 0.88rem;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.pl-stats {
  display: flex;
  gap: 1.25rem;
  margin-bottom: 1.5rem;
  font-size: 0.8rem;
  color: #9b9daa;
}

.pl-actions {
  display: flex;
  gap: 0.75rem;
}

.pl-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.65rem 1.4rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #f3f2ef;
  font-size: 0.82rem;
  font-weight: 500;
  transition: all 240ms ease;
}

.pl-btn:hover {
  border-color: #925dff;
  color: #925dff;
  transform: translateY(-2px);
}

.pl-btn-primary {
  background: linear-gradient(135deg, #925dff, #6b3fd4);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 16px rgba(146, 93, 255, 0.25);
}

.pl-btn-primary:hover {
  background: linear-gradient(135deg, #a875ff, #7b4fe4);
  color: #fff;
  box-shadow: 0 6px 24px rgba(146, 93, 255, 0.35);
}

/* --- Song List --- */
.pl-songs {
  border-radius: 1.5rem;
  padding: 1.5rem;
}

.pl-songs-header {
  display: grid;
  grid-template-columns: 40px 1fr 150px 80px;
  gap: 12px;
  padding: 0.5rem 1rem;
  font-size: 0.68rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #9b9daa;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  margin-bottom: 0.5rem;
}

.pl-songs-list {
  display: flex;
  flex-direction: column;
}

.pl-song-item {
  display: grid;
  grid-template-columns: 40px 1fr 150px 80px;
  gap: 12px;
  padding: 0.75rem 1rem;
  border-radius: 0.75rem;
  align-items: center;
  cursor: pointer;
  transition: all 240ms ease;
}

.pl-song-item:hover {
  background: rgba(255, 255, 255, 0.04);
  transform: translateX(4px);
}

.col-idx {
  color: #9b9daa;
  font-family: var(--ark-mono);
  font-size: 0.72rem;
  text-align: center;
}

.col-title {
  font-weight: 500;
  font-size: 0.88rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.col-singer {
  color: #9b9daa;
  font-size: 0.8rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.col-dur {
  color: #9b9daa;
  font-size: 0.78rem;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.pl-empty {
  text-align: center;
  padding: 3rem;
  color: #9b9daa;
}

.pl-empty-ico {
  font-size: 2rem;
  opacity: 0.4;
}

/* --- Loading --- */
.pl-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  gap: 1rem;
  color: #9b9daa;
}

.loading-ring {
  width: 36px;
  height: 36px;
  border: 2px solid rgba(70, 246, 230, 0.15);
  border-top-color: #46f6e6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* --- Responsive --- */
@media (max-width: 768px) {
  .pl-hero-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 1.5rem;
  }

  .pl-cover {
    width: 150px;
    height: 150px;
  }

  .pl-actions { justify-content: center; }
  .pl-songs-header { display: none; }
  .pl-song-item { grid-template-columns: 30px 1fr 60px; }
  .col-singer { display: none; }
}
</style>
