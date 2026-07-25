<template>
  <div class="home">
    <!-- Ambient Background -->
    <div class="ambient-orb orb-1"></div>
    <div class="ambient-orb orb-2"></div>
    <div class="ambient-orb orb-3"></div>

    <!-- Hero Section -->
    <section class="hero fade-in-up">
      <div class="hero-bg-glow"></div>
      <div class="hero-content">
        <span class="hero-badge">
          <span class="badge-dot"></span>
          <span>在线音乐平台</span>
        </span>
        <h1 class="gradient-text">悦享音乐<br/>随心而动</h1>
        <p>探索海量音乐库，发现属于你的旋律。从热门金曲到独立音乐，总有一首打动你。</p>
        <div class="hero-actions">
          <button class="btn-primary" @click="$router.push('/search')">
            <span>🔍</span> 开始探索
          </button>
          <button class="btn-ghost" @click="scrollToHot">
            <span>🔥</span> 热门推荐
          </button>
        </div>
        <div class="hero-quick-stats">
          <div class="qs-item">
            <span class="qs-value">{{ hotSongs.length }}+</span>
            <span class="qs-label">热门曲目</span>
          </div>
          <div class="qs-item">
            <span class="qs-value">{{ hotPlaylists.length }}+</span>
            <span class="qs-label">精选歌单</span>
          </div>
          <div class="qs-item">
            <span class="qs-value">8</span>
            <span class="qs-label">入驻歌手</span>
          </div>
        </div>
      </div>
      <div class="hero-visual">
        <div class="hero-album-stack">
          <div class="album-card album-1">🎸</div>
          <div class="album-card album-2">🎹</div>
          <div class="album-card album-3">🎤</div>
        </div>
        <div class="hero-ring"></div>
        <div class="hero-ring ring-2"></div>
      </div>
    </section>

    <!-- Featured Spotlight -->
    <section class="fade-in-up" style="animation-delay: 80ms" v-if="hotSongs.length">
      <div class="spotlight-card" @click="hotSongs[0] && player.loadSong(hotSongs[0])">
        <div class="spotlight-bg" :style="hotSongs[0]?.cover ? { backgroundImage: 'url(' + hotSongs[0].cover + ')', backgroundSize: 'cover', backgroundPosition: 'center' } : {}">
          <div class="spotlight-orb"></div>
          <div class="spotlight-img-overlay" v-if="hotSongs[0]?.cover"></div>
        </div>
        <div class="spotlight-content">
          <span class="spotlight-tag">🔥 今日最热</span>
          <h3>{{ hotSongs[0]?.name || '—' }}</h3>
          <p>{{ hotSongs[0]?.singerName || '未知歌手' }}</p>
          <div class="spotlight-meta">
            <span class="eq-bars" v-if="player.isPlaying && player.currentSong?.songId === hotSongs[0]?.songId">
              <i class="eq-bar"></i><i class="eq-bar"></i><i class="eq-bar"></i><i class="eq-bar"></i>
            </span>
            <span class="spotlight-plays">{{ formatCount(hotSongs[0]?.playCount) }} 播放</span>
          </div>
          <button class="spotlight-play">▶</button>
        </div>
      </div>
    </section>

    <!-- Hot Songs Section -->
    <section class="section fade-in-up" style="animation-delay: 160ms" id="hot-section">
      <div class="ark-section-title">
        <h2>热门歌曲</h2>
        <span></span>
        <p class="subtitle">Hot Tracks · 按播放量排序</p>
      </div>
      <div class="media-grid" v-if="hotSongs.length">
        <div class="media-card" v-for="(item, idx) in hotSongs" :key="item.songId" @click="player.loadSong(item)">
          <div class="media-card-cover" :style="item.cover ? { backgroundImage: 'url(' + item.cover + ')', backgroundSize: 'cover', backgroundPosition: 'center' } : { background: coverGradient(covers[idx % covers.length]) }">
            <span class="cover-emoji" v-if="!item.cover">{{ covers[idx % covers.length] }}</span>
            <div class="play-overlay">
              <div class="play-icon">▶</div>
            </div>
            <div class="cover-rank" v-if="idx < 3">{{ idx + 1 }}</div>
          </div>
          <div class="media-card-info">
            <div class="media-card-title">{{ item.name }}</div>
            <div class="media-card-sub">{{ item.singerName || '未知歌手' }} · {{ formatCount(item.playCount) }}播放</div>
          </div>
        </div>
      </div>
      <div class="skeleton-grid" v-else-if="loading">
        <div class="skeleton-card" v-for="n in 8" :key="n">
          <div class="skeleton-cover"></div>
          <div class="skeleton-info">
            <div class="skeleton-line"></div>
            <div class="skeleton-line short"></div>
          </div>
        </div>
      </div>
      <div class="empty-state" v-else>
        <span class="empty-icon">🎧</span>
        <p>暂无热门歌曲</p>
      </div>
    </section>

    <!-- Recommended Playlists Section -->
    <section class="section fade-in-up" style="animation-delay: 240ms">
      <div class="ark-section-title">
        <h2>推荐歌单</h2>
        <span></span>
        <p class="subtitle">Curated Playlists · 编辑精选</p>
      </div>
      <div class="media-grid" v-if="hotPlaylists.length">
        <div class="media-card" v-for="(item, idx) in hotPlaylists" :key="item.playlistId" @click="$router.push('/playlist/' + item.playlistId)">
          <div class="media-card-cover" :style="item.cover ? { backgroundImage: 'url(' + item.cover + ')', backgroundSize: 'cover', backgroundPosition: 'center' } : { background: playlistGradient(idx) }">
            <span class="cover-emoji" v-if="!item.cover">{{ playlistCovers[idx % playlistCovers.length] }}</span>
            <div class="play-overlay">
              <div class="play-icon">▶</div>
            </div>
            <div class="playlist-badge">🎵 {{ item.songCount || '?' }}</div>
          </div>
          <div class="media-card-info">
            <div class="media-card-title">{{ item.name }}</div>
            <div class="media-card-sub">{{ formatCount(item.playCount) }} 次播放</div>
          </div>
        </div>
      </div>
      <div class="empty-state" v-else>
        <span class="empty-icon">📋</span>
        <p>暂无推荐歌单</p>
      </div>
    </section>

    <!-- New Releases Section -->
    <section class="section fade-in-up" style="animation-delay: 320ms">
      <div class="ark-section-title">
        <h2>最新上架</h2>
        <span></span>
        <p class="subtitle">New Releases · 新鲜出炉</p>
      </div>
      <div class="media-grid" v-if="newSongs.length">
        <div class="media-card" v-for="(item, idx) in newSongs" :key="item.songId" @click="player.loadSong(item)">
          <div class="media-card-cover" :style="item.cover ? { backgroundImage: 'url(' + item.cover + ')', backgroundSize: 'cover', backgroundPosition: 'center' } : { background: newGradient(idx) }">
            <span class="cover-emoji" v-if="!item.cover">{{ '🎵' }}</span>
            <div class="play-overlay">
              <div class="play-icon">▶</div>
            </div>
            <div class="new-badge">NEW</div>
          </div>
          <div class="media-card-info">
            <div class="media-card-title">{{ item.name }}</div>
            <div class="media-card-sub">{{ item.singerName || '未知歌手' }}</div>
          </div>
        </div>
      </div>
      <div class="empty-state" v-else>
        <span class="empty-icon">🎼</span>
        <p>暂无新歌</p>
      </div>
    </section>

    <!-- Stats Strip -->
    <section class="stats-strip glass fade-in-up" style="animation-delay: 400ms">
      <div class="stat-item">
        <div class="stat-icon">🎵</div>
        <span class="stat-value">{{ hotSongs.length }}</span>
        <span class="stat-label">热门曲目</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-icon">📋</div>
        <span class="stat-value">{{ hotPlaylists.length }}</span>
        <span class="stat-label">精选歌单</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-icon">🆕</div>
        <span class="stat-value">{{ newSongs.length }}</span>
        <span class="stat-label">最新上架</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-icon">▶</div>
        <span class="stat-value">{{ totalPlays }}</span>
        <span class="stat-label">总播放量</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { songApi, playlistApi } from '../api'
import { usePlayerStore } from '../store/player'

const player = usePlayerStore()

const covers = ['🎸', '🎹', '🎷', '🎺', '🎻', '🥁', '🎤', '🎧', '🎶', '🎵']
const playlistCovers = ['🌙', '🌊', '🔥', '❄️', '🌸', '⚡', '🎪', '🏔']

const loading = ref(true)
const hotSongs = ref([])
const hotPlaylists = ref([])
const newSongs = ref([])

const totalPlays = computed(() => {
  const playlistPlays = hotPlaylists.value.reduce((sum, p) => sum + (p.playCount || 0), 0)
  const songPlays = hotSongs.value.reduce((sum, s) => sum + (s.playCount || 0), 0)
  const total = playlistPlays + songPlays
  return total > 10000 ? (total / 10000).toFixed(1) + 'w' : total
})

const formatCount = (n) => {
  if (!n) return '0'
  return n > 10000 ? (n / 10000).toFixed(1) + 'w' : n
}

const coverGradients = [
  'linear-gradient(135deg, #46f6e6, #2ba89f)',
  'linear-gradient(135deg, #925dff, #6b3fd4)',
  'linear-gradient(135deg, #ff6b6b, #c92a2a)',
  'linear-gradient(135deg, #ffd43b, #fab005)',
  'linear-gradient(135deg, #69db7c, #37b24d)',
  'linear-gradient(135deg, #74c0fc, #1971c2)',
  'linear-gradient(135deg, #f783ac, #d6336c)',
  'linear-gradient(135deg, #ffa94d, #e8590c)',
]

const coverGradient = (emoji) => {
  const idx = emoji.charCodeAt(0) % coverGradients.length
  return coverGradients[idx]
}

const playlistGradients = [
  'linear-gradient(135deg, rgba(70,246,230,0.2), rgba(146,93,255,0.25))',
  'linear-gradient(135deg, rgba(146,93,255,0.2), rgba(255,107,107,0.2))',
  'linear-gradient(135deg, rgba(255,107,107,0.2), rgba(255,211,59,0.2))',
  'linear-gradient(135deg, rgba(105,219,124,0.2), rgba(70,246,230,0.2))',
  'linear-gradient(135deg, rgba(116,192,252,0.2), rgba(146,93,255,0.2))',
  'linear-gradient(135deg, rgba(247,131,172,0.2), rgba(214,51,108,0.2))',
  'linear-gradient(135deg, rgba(255,169,77,0.2), rgba(232,89,12,0.2))',
  'linear-gradient(135deg, rgba(70,246,230,0.15), rgba(55,178,77,0.2))',
]

const playlistGradient = (idx) => playlistGradients[idx % playlistGradients.length]

const newGradients = [
  'linear-gradient(135deg, #46f6e6, #1971c2)',
  'linear-gradient(135deg, #925dff, #d6336c)',
  'linear-gradient(135deg, #69db7c, #2ba89f)',
  'linear-gradient(135deg, #ffd43b, #e8590c)',
  'linear-gradient(135deg, #74c0fc, #6b3fd4)',
  'linear-gradient(135deg, #f783ac, #c92a2a)',
]

const newGradient = (idx) => newGradients[idx % newGradients.length]

const scrollToHot = () => {
  const el = document.getElementById('hot-section')
  const container = document.querySelector('.app-content')
  if (el && container) {
    const top = el.getBoundingClientRect().top - container.getBoundingClientRect().top + container.scrollTop
    container.scrollTo({ top: top - 20, behavior: 'smooth' })
  } else if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

const loadHotSongs = async () => {
  try {
    const res = await songApi.getHot(1, 10)
    hotSongs.value = res?.data?.songs || res?.songs || []
  } catch (e) {
    console.error('Failed to load hot songs:', e)
  }
}

const loadPlaylists = async () => {
  try {
    const res = await playlistApi.getHot(1, 10)
    const list = res?.data?.data || res?.data
    hotPlaylists.value = Array.isArray(list) ? list : []
  } catch (e) {
    console.error('Failed to load playlists:', e)
  }
}

const loadNewSongs = async () => {
  try {
    const res = await songApi.getNew(1, 10)
    newSongs.value = res?.data?.songs || res?.songs || []
  } catch (e) {
    console.error('Failed to load new songs:', e)
  }
}

onMounted(async () => {
  document.title = 'Music Dreamer · 悦享音乐'
  loading.value = true
  await Promise.all([loadHotSongs(), loadPlaylists(), loadNewSongs()])
  loading.value = false
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 3rem;
  position: relative;
  min-height: 150vh;
}

/* --- Hero --- */
.hero {
  position: relative;
  border-radius: 2rem;
  padding: clamp(2.5rem, 5vw, 4.5rem);
  margin-bottom: 2rem;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(70, 246, 230, 0.06), rgba(146, 93, 255, 0.08));
  border: 1px solid rgba(70, 246, 230, 0.12);
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 320px;
  z-index: 1;
}

.hero-bg-glow {
  position: absolute;
  top: -60%;
  right: -15%;
  width: 70%;
  height: 220%;
  background: radial-gradient(ellipse, rgba(70, 246, 230, 0.1), transparent 65%);
  pointer-events: none;
  animation: heroGlow 8s ease-in-out infinite alternate;
}

@keyframes heroGlow {
  0% { opacity: 0.5; transform: scale(1); }
  100% { opacity: 1; transform: scale(1.1); }
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 520px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.9rem;
  border-radius: 999px;
  background: rgba(70, 246, 230, 0.08);
  border: 1px solid rgba(70, 246, 230, 0.2);
  font-size: 0.72rem;
  color: #46f6e6;
  letter-spacing: 0.04em;
  margin-bottom: 1rem;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #46f6e6;
  box-shadow: 0 0 8px #46f6e6;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.hero h1 {
  font-family: "Noto Serif SC", serif;
  font-weight: 900;
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  line-height: 0.92;
  letter-spacing: -0.05em;
  margin: 0 0 1.2rem;
  text-transform: uppercase;
}

.hero p {
  font-size: 0.92rem;
  color: #9b9daa;
  margin-bottom: 1.75rem;
  line-height: 1.75;
  max-width: 420px;
}

.hero-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-bottom: 2rem;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.75rem;
  border-radius: 999px;
  background: linear-gradient(135deg, #46f6e6, #2ba89f);
  color: #080914;
  font-weight: 600;
  font-size: 0.85rem;
  transition: all 240ms ease;
  box-shadow: 0 4px 16px rgba(70, 246, 230, 0.25);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(70, 246, 230, 0.35);
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.75rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #f3f2ef;
  font-weight: 500;
  font-size: 0.85rem;
  transition: all 240ms ease;
}

.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(70, 246, 230, 0.3);
  transform: translateY(-2px);
}

.hero-quick-stats {
  display: flex;
  gap: 2rem;
}

.qs-item {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.qs-value {
  font-family: "Noto Serif SC", serif;
  font-weight: 900;
  font-size: 1.3rem;
  color: #46f6e6;
}

.qs-label {
  font-size: 0.65rem;
  color: #9b9daa;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

/* --- Hero Visual --- */
.hero-visual {
  position: relative;
  width: 240px;
  height: 240px;
  flex-shrink: 0;
}

.hero-album-stack {
  position: absolute;
  inset: 15%;
  perspective: 800px;
}

.album-card {
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  transition: transform 240ms ease;
}

.album-1 {
  background: linear-gradient(135deg, rgba(70, 246, 230, 0.15), rgba(146, 93, 255, 0.15));
  top: 0;
  left: 0;
  animation: float1 6s ease-in-out infinite;
}

.album-2 {
  background: linear-gradient(135deg, rgba(146, 93, 255, 0.15), rgba(255, 107, 107, 0.12));
  top: 20px;
  left: 30px;
  animation: float2 7s ease-in-out infinite 0.5s;
}

.album-3 {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.12), rgba(255, 211, 59, 0.12));
  top: 40px;
  left: 60px;
  animation: float3 8s ease-in-out infinite 1s;
}

@keyframes float1 {
  0%, 100% { transform: translateY(0) rotate(-3deg); }
  50% { transform: translateY(-12px) rotate(0deg); }
}
@keyframes float2 {
  0%, 100% { transform: translateY(0) rotate(2deg); }
  50% { transform: translateY(-8px) rotate(5deg); }
}
@keyframes float3 {
  0%, 100% { transform: translateY(0) rotate(-1deg); }
  50% { transform: translateY(-15px) rotate(2deg); }
}

.hero-ring {
  position: absolute;
  inset: 5%;
  border: 1px solid rgba(70, 246, 230, 0.15);
  border-radius: 50%;
  animation: spin 25s linear infinite;
}

.hero-ring::before {
  content: "";
  position: absolute;
  top: -4px;
  left: 50%;
  width: 8px;
  height: 8px;
  background: #46f6e6;
  border-radius: 50%;
  box-shadow: 0 0 12px #46f6e6;
}

.ring-2 {
  inset: -5%;
  border-color: rgba(146, 93, 255, 0.1);
  animation-direction: reverse;
  animation-duration: 35s;
}

.ring-2::before {
  background: #925dff;
  box-shadow: 0 0 12px #925dff;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* --- Spotlight --- */
.spotlight {
  margin-bottom: 2rem;
}

.spotlight-card {
  position: relative;
  border-radius: 1.5rem;
  padding: 2rem;
  overflow: hidden;
  cursor: pointer;
  transition: all 300ms ease;
  border: 1px solid rgba(70, 246, 230, 0.15);
  min-height: 160px;
  display: flex;
  align-items: center;
}

.spotlight-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(70, 246, 230, 0.12);
  border-color: rgba(70, 246, 230, 0.3);
}

.spotlight-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(70, 246, 230, 0.08), rgba(146, 93, 255, 0.12));
}

.spotlight-orb {
  position: absolute;
  top: -30%;
  right: -10%;
  width: 60%;
  height: 180%;
  background: radial-gradient(ellipse, rgba(70, 246, 230, 0.15), transparent 65%);
  animation: orbFloat 10s ease-in-out infinite;
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-20px, 10px); }
}

.spotlight-content {
  position: relative;
  z-index: 1;
  flex: 1;
}

.spotlight-tag {
  display: inline-block;
  padding: 0.25rem 0.7rem;
  border-radius: 999px;
  background: rgba(255, 107, 107, 0.12);
  border: 1px solid rgba(255, 107, 107, 0.25);
  font-size: 0.68rem;
  color: #ff6b6b;
  margin-bottom: 0.75rem;
}

.spotlight-content h3 {
  font-family: "Noto Serif SC", serif;
  font-weight: 900;
  font-size: 1.8rem;
  margin: 0 0 0.3rem;
  color: #f3f2ef;
}

.spotlight-content p {
  color: #9b9daa;
  font-size: 0.88rem;
  margin-bottom: 0.75rem;
}

.spotlight-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.spotlight-plays {
  font-size: 0.75rem;
  color: #9b9daa;
}

.spotlight-play {
  position: absolute;
  right: 2rem;
  top: 50%;
  transform: translateY(-50%);
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #46f6e6, #2ba89f);
  color: #080914;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(70, 246, 230, 0.3);
  transition: all 240ms ease;
  z-index: 2;
}

.spotlight-card:hover .spotlight-play {
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 8px 28px rgba(70, 246, 230, 0.4);
}

/* --- Sections --- */
.section {
  margin-bottom: 2.5rem;
}

/* --- Cover enhancements --- */
.cover-emoji {
  position: relative;
  z-index: 1;
  transition: transform 300ms ease;
}

.media-card:hover .cover-emoji {
  transform: scale(1.15);
}

.cover-rank {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: rgba(8, 9, 20, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 700;
  color: #46f6e6;
  z-index: 3;
  font-family: var(--ark-mono);
}

.playlist-badge {
  position: absolute;
  bottom: 8px;
  right: 8px;
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
  background: rgba(8, 9, 20, 0.7);
  backdrop-filter: blur(4px);
  font-size: 0.65rem;
  color: #f3f2ef;
  z-index: 3;
}

.new-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a24);
  font-size: 0.6rem;
  font-weight: 700;
  color: #fff;
  z-index: 3;
  letter-spacing: 0.05em;
}

/* --- Skeleton --- */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 20px;
}

.skeleton-card {
  border-radius: var(--ark-family-radius);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.skeleton-cover {
  aspect-ratio: 1;
  background: linear-gradient(90deg, rgba(255,255,255,0.04) 25%, rgba(255,255,255,0.08) 50%, rgba(255,255,255,0.04) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-line {
  height: 12px;
  border-radius: 4px;
  background: linear-gradient(90deg, rgba(255,255,255,0.04) 25%, rgba(255,255,255,0.08) 50%, rgba(255,255,255,0.04) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-line.short {
  width: 60%;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* --- Empty State --- */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  color: #9b9daa;
  border-radius: 2rem;
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

/* --- Stats Strip --- */
.stats-strip {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.75rem 2rem;
  border-radius: 2rem;
  margin-top: 1rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  flex: 1;
}

.stat-icon {
  font-size: 1.3rem;
  margin-bottom: 0.25rem;
}

.stat-value {
  font-family: "Noto Serif SC", serif;
  font-weight: 900;
  font-size: 1.5rem;
  color: #46f6e6;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 0.65rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #9b9daa;
}

.stat-divider {
  width: 1px;
  height: 36px;
  background: rgba(255, 255, 255, 0.08);
}

/* --- Responsive --- */
@media (max-width: 768px) {
  .hero {
    flex-direction: column;
    text-align: center;
    padding: 2rem 1.5rem;
  }

  .hero p { max-width: 100%; }
  .hero-actions { justify-content: center; }
  .hero-quick-stats { justify-content: center; }

  .hero-visual {
    width: 160px;
    height: 160px;
    margin-top: 1.5rem;
  }

  .album-card { width: 80px; height: 80px; font-size: 1.8rem; }

  .spotlight-card { padding: 1.5rem; }
  .spotlight-content h3 { font-size: 1.3rem; }
  .spotlight-play { width: 44px; height: 44px; font-size: 1rem; right: 1.5rem; }

  .stats-strip {
    flex-wrap: wrap;
    gap: 1rem;
  }

  .stat-divider { display: none; }
  .stat-item { flex: 0 0 calc(50% - 0.5rem); }
}
</style>
