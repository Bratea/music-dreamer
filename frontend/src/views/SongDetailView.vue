<template>
  <div class="song-detail" v-if="song">
    <!-- Immersive Header -->
    <div class="sd-hero">
      <div class="sd-hero-bg">
        <div class="sd-hero-orb orb-a"></div>
        <div class="sd-hero-orb orb-b"></div>
      </div>
      <div class="sd-hero-content">
        <div class="sd-cover-wrap">
          <div class="sd-cover" :style="song.cover ? { backgroundImage: 'url(' + song.cover + ')', backgroundSize: 'cover', backgroundPosition: 'center' } : {}">
            <img v-if="song.coverUrl" :src="song.coverUrl" :alt="song.name" />
            <span v-if="!song.cover && !song.coverUrl" class="sd-cover-emoji">🎵</span>
          </div>
          <div class="sd-cover-glow"></div>
        </div>
        <div class="sd-info">
          <span class="sd-type">单曲 · Single</span>
          <h1 class="sd-title">{{ song.name }}</h1>
          <p class="sd-singer" v-if="song.singerName">{{ song.singerName }}</p>
          <div class="sd-tags">
            <span class="sd-tag" v-if="song.genre">{{ song.genre }}</span>
            <span class="sd-tag" v-if="song.language">{{ song.language }}</span>
          </div>
          <div class="sd-stats">
            <div class="sd-stat">
              <span class="sd-stat-val">{{ formatCount(song.playCount) }}</span>
              <span class="sd-stat-lbl">播放</span>
            </div>
            <div class="sd-stat">
              <span class="sd-stat-val">{{ formatCount(song.likeCount) }}</span>
              <span class="sd-stat-lbl">收藏</span>
            </div>
            <div class="sd-stat">
              <span class="sd-stat-val">{{ formatDuration(song.duration) }}</span>
              <span class="sd-stat-lbl">时长</span>
            </div>
          </div>
          <div class="sd-actions">
            <button class="sd-btn sd-btn-primary" @click="onPlay">
              <span>▶</span> 播放
            </button>
            <button class="sd-btn" @click="onCollect">
              {{ collected ? '❤️ 已收藏' : '🤍 收藏' }}
            </button>
            <button class="sd-btn" @click="showLyricsFullscreen = true">
              📖 歌词
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Lyrics / Related -->
    <div class="sd-body">
      <div class="sd-lyrics glass" @click="showLyricsFullscreen = true">
        <h3>歌词 · Lyrics <span class="lyrics-hint-click">点击展开 →</span></h3>
        <div class="lyrics-content" v-if="song.lyrics">
          <p v-for="(line, i) in song.lyrics.split('\n').slice(0, 12)" :key="i">{{ line }}</p>
        </div>
        <div class="lyrics-empty" v-else>
          <span class="lyrics-empty-ico">🎼</span>
          <p>暂无歌词</p>
          <p class="lyrics-hint">纯音乐欣赏</p>
        </div>
        <div class="lyrics-fade" v-if="song.lyrics && song.lyrics.split('\n').length > 12"></div>
      </div>
      <div class="sd-related glass">
        <h3>相似推荐 · Related</h3>
        <div class="related-list">
          <div class="related-loading" v-if="relatedLoading">
            <div class="related-skeleton" v-for="n in 4" :key="n"></div>
          </div>
          <template v-else>
            <div class="related-item" v-for="(item, i) in relatedSongs" :key="item.songId || i" @click="switchSong(item)">
              <span class="related-cover">{{ item.cover || '🎵' }}</span>
              <div class="related-meta">
                <div class="related-name">{{ item.name }}</div>
                <div class="related-singer">{{ item.singerName }}</div>
              </div>
              <span class="related-play">▶</span>
            </div>
            <div class="related-empty" v-if="!relatedSongs.length">暂无相似推荐</div>
          </template>
        </div>
      </div>
    </div>

    <!-- Fullscreen Lyrics Overlay -->
    <Teleport to="body">
      <div class="lyrics-overlay" v-if="showLyricsFullscreen" @click.self="showLyricsFullscreen = false">
        <div class="lyrics-modal glass-strong">
          <div class="lyrics-modal-header">
            <div class="lm-info">
              <h2>{{ song.name }}</h2>
              <p>{{ song.singerName }}</p>
            </div>
            <button class="lm-close" @click="showLyricsFullscreen = false">✕</button>
          </div>
          <div class="lyrics-modal-body">
            <div class="lm-lyrics" v-if="song.lyrics">
              <p v-for="(line, i) in song.lyrics.split('\n')" :key="i" :class="{ empty: !line.trim() }">{{ line || ' ' }}</p>
            </div>
            <div class="lm-empty" v-else>
              <span class="lm-empty-ico">🎼</span>
              <p>暂无歌词</p>
              <p class="lm-empty-hint">纯音乐欣赏</p>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>

  <div class="song-loading" v-else>
    <div class="loading-ring"></div>
    <p>加载中...</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { songApi } from '../api'
import { usePlayerStore } from '../store/player'

const route = useRoute()
const router = useRouter()
const player = usePlayerStore()

const song = ref(null)
const collected = ref(false)
const showLyricsFullscreen = ref(false)
const relatedSongs = ref([])
const relatedLoading = ref(false)

const loadRelated = async (id) => {
  relatedLoading.value = true
  try {
    const res = await songApi.getSimilar(id, 6)
    relatedSongs.value = res?.songs || []
  } catch (e) {
    relatedSongs.value = []
  } finally {
    relatedLoading.value = false
  }
}

const formatCount = (n) => {
  if (!n) return '0'
  return n > 10000 ? (n / 10000).toFixed(1) + 'w' : n
}

onMounted(async () => {
  document.title = '歌曲详情 · Music Dreamer'
  const id = route.params.id
  try {
    const res = await songApi.getById(id)
    const data = res?.data || res
    if (data) {
      song.value = data
      document.title = `${data.name} · Music Dreamer`
      loadRelated(data.songId || id)
    }
  } catch (e) {
    song.value = {
      songId: id,
      name: '夜曲',
      singerName: '周杰伦',
      playCount: 128000,
      likeCount: 5600,
      duration: 237,
      cover: '🌙',
      genre: '流行',
      language: '国语',
      lyrics: '一群嗜血的蚂蚁 被腐肉所吸引\n我面无表情看孤独的风景\n失去你 爱恨开始分明\n失去你 还有什么事好关心\n当鸽子不再象征和平\n我终于提醒我\n广场上喂食的是秃鹰\n我用漂亮的押韵\n形容一笔易碎的爱情',
    }
    relatedSongs.value = [
      { songId: 2, name: '晴天', singerName: '周杰伦', cover: '☀️' },
      { songId: 3, name: '稻香', singerName: '周杰伦', cover: '🌾' },
      { songId: 5, name: '七里香', singerName: '周杰伦', cover: '🌸' },
    ]
  }
})

const onPlay = () => {
  if (song.value) player.loadSong(song.value)
}

const onCollect = async () => {
  if (!song.value) return
  try {
    if (collected.value) {
      const res = await songApi.uncollect(song.value.songId)
      if (res?.code === 200) {
        collected.value = false
        ElMessage.success('已取消收藏')
      }
    } else {
      const res = await songApi.collect(song.value.songId)
      if (res?.code === 200) {
        collected.value = true
        ElMessage.success('收藏成功')
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const switchSong = (item) => {
  router.push('/song/' + item.songId)
}

const formatDuration = (s) => {
  if (!s) return '0:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}:${sec.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.song-detail {
  max-width: 1100px;
  margin: 0 auto;
}

/* --- Hero --- */
.sd-hero {
  position: relative;
  border-radius: 2rem;
  padding: clamp(2rem, 4vw, 3.5rem);
  margin-bottom: 2rem;
  overflow: hidden;
  min-height: 300px;
  display: flex;
  align-items: center;
}

.sd-hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(70, 246, 230, 0.06), rgba(146, 93, 255, 0.08));
  z-index: 0;
}

.sd-hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  pointer-events: none;
}

.orb-a {
  width: 300px;
  height: 300px;
  background: rgba(70, 246, 230, 0.12);
  top: -30%;
  right: -5%;
  animation: orbFloat 12s ease-in-out infinite;
}

.orb-b {
  width: 250px;
  height: 250px;
  background: rgba(146, 93, 255, 0.1);
  bottom: -20%;
  left: 10%;
  animation: orbFloat 15s ease-in-out infinite reverse;
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, -15px); }
}

.sd-hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 2.5rem;
  align-items: center;
  width: 100%;
}

.sd-cover-wrap {
  position: relative;
  flex-shrink: 0;
}

.sd-cover {
  width: 220px;
  height: 220px;
  border-radius: 1.5rem;
  background: linear-gradient(135deg, rgba(70, 246, 230, 0.1), rgba(146, 93, 255, 0.1));
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 4rem;
  overflow: hidden;
  position: relative;
  z-index: 1;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.sd-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sd-cover-glow {
  position: absolute;
  inset: -10%;
  background: radial-gradient(ellipse, rgba(70, 246, 230, 0.15), transparent 70%);
  z-index: 0;
  animation: glowPulse 4s ease-in-out infinite alternate;
}

@keyframes glowPulse {
  0% { opacity: 0.5; transform: scale(1); }
  100% { opacity: 1; transform: scale(1.05); }
}

.sd-info {
  flex: 1;
}

.sd-type {
  font-family: var(--ark-mono);
  font-size: 0.62rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #46f6e6;
}

.sd-title {
  font-family: var(--ark-display);
  font-weight: 900;
  font-size: clamp(1.8rem, 4vw, 2.8rem);
  line-height: 1.1;
  margin: 0.5rem 0 0.25rem;
  background: linear-gradient(135deg, #f3f2ef, #46f6e6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sd-singer {
  font-size: 1rem;
  color: #9b9daa;
  margin-bottom: 0.75rem;
}

.sd-tags {
  display: flex;
  gap: 0.4rem;
  margin-bottom: 1rem;
}

.sd-tag {
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  background: rgba(70, 246, 230, 0.08);
  border: 1px solid rgba(70, 246, 230, 0.15);
  font-size: 0.68rem;
  color: #46f6e6;
}

.sd-stats {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.sd-stat {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.sd-stat-val {
  font-family: var(--ark-display);
  font-weight: 900;
  font-size: 1.2rem;
  color: #f3f2ef;
}

.sd-stat-lbl {
  font-size: 0.65rem;
  color: #9b9daa;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.sd-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.sd-btn {
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

.sd-btn:hover {
  border-color: #46f6e6;
  color: #46f6e6;
  transform: translateY(-2px);
}

.sd-btn-primary {
  background: linear-gradient(135deg, #46f6e6, #2ba89f);
  color: #080914;
  border-color: transparent;
  box-shadow: 0 4px 16px rgba(70, 246, 230, 0.25);
}

.sd-btn-primary:hover {
  background: linear-gradient(135deg, #5ffff5, #36c4bb);
  color: #080914;
  box-shadow: 0 6px 24px rgba(70, 246, 230, 0.35);
}

/* --- Body --- */
.sd-body {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 1.5rem;
}

.sd-lyrics, .sd-related {
  border-radius: 1.5rem;
  padding: 1.5rem;
}

.sd-lyrics h3, .sd-related h3 {
  font-size: 0.72rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #46f6e6;
  margin-bottom: 1rem;
  font-family: var(--ark-mono);
}

.lyrics-content p {
  padding: 0.4rem 0;
  line-height: 2;
  color: #c8c9d0;
  font-size: 0.95rem;
}

.lyrics-empty {
  text-align: center;
  padding: 2rem;
  color: #9b9daa;
}

.lyrics-empty-ico {
  font-size: 2rem;
  opacity: 0.4;
}

.lyrics-hint {
  font-size: 0.8rem;
  margin-top: 0.5rem;
}

.lyrics-hint-click {
  font-size: 0.65rem;
  color: #46f6e6;
  font-weight: 400;
  margin-left: 0.5rem;
  opacity: 0.7;
}

.sd-lyrics {
  cursor: pointer;
  transition: all 240ms ease;
  position: relative;
}

.sd-lyrics:hover {
  border-color: rgba(70, 246, 230, 0.2);
  box-shadow: 0 4px 20px rgba(70, 246, 230, 0.06);
}

.lyrics-fade {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(transparent, rgba(8, 9, 20, 0.8));
  pointer-events: none;
  border-radius: 0 0 1.5rem 1.5rem;
}

/* --- Related --- */
.related-list {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.related-loading {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.related-skeleton {
  height: 54px;
  border-radius: 1rem;
  background: linear-gradient(90deg, rgba(255,255,255,0.04) 25%, rgba(255,255,255,0.08) 50%, rgba(255,255,255,0.04) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.related-empty {
  text-align: center;
  padding: 1.5rem;
  color: #9b9daa;
  font-size: 0.82rem;
}

.related-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem;
  border-radius: 1rem;
  cursor: pointer;
  transition: all 240ms ease;
}

.related-item:hover {
  background: rgba(255, 255, 255, 0.04);
  transform: translateX(4px);
}

.related-cover {
  width: 42px;
  height: 42px;
  border-radius: 0.75rem;
  background: linear-gradient(135deg, rgba(70, 246, 230, 0.08), rgba(146, 93, 255, 0.08));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.related-meta {
  flex: 1;
  min-width: 0;
}

.related-name {
  font-size: 0.82rem;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.related-singer {
  font-size: 0.72rem;
  color: #9b9daa;
}

.related-play {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(70, 246, 230, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  color: #46f6e6;
  opacity: 0;
  transition: all 240ms ease;
}

.related-item:hover .related-play {
  opacity: 1;
}

/* --- Lyrics Fullscreen Overlay --- */
.lyrics-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(8, 9, 20, 0.95);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 240ms ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.lyrics-modal {
  width: 100%;
  max-width: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  border-radius: 1.5rem;
  overflow: hidden;
}

.lyrics-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.lm-info h2 {
  font-family: var(--ark-display);
  font-weight: 700;
  font-size: 1.2rem;
  margin: 0;
}

.lm-info p {
  color: #9b9daa;
  font-size: 0.82rem;
  margin-top: 0.25rem;
}

.lm-close {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #9b9daa;
  font-size: 0.9rem;
  transition: all 240ms ease;
}

.lm-close:hover {
  background: rgba(255, 107, 107, 0.1);
  border-color: #ff6b6b;
  color: #ff6b6b;
}

.lyrics-modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
}

.lm-lyrics {
  text-align: center;
}

.lm-lyrics p {
  padding: 0.5rem 0;
  line-height: 2.2;
  color: #c8c9d0;
  font-size: 1rem;
  transition: color 240ms ease;
}

.lm-lyrics p.empty {
  padding: 0.25rem 0;
}

.lm-lyrics p:hover {
  color: #46f6e6;
}

.lm-empty {
  text-align: center;
  padding: 3rem;
  color: #9b9daa;
}

.lm-empty-ico {
  font-size: 3rem;
  opacity: 0.4;
}

.lm-empty p {
  margin-top: 1rem;
}

.lm-empty-hint {
  font-size: 0.8rem;
  opacity: 0.6;
}

/* --- Loading --- */
.song-loading {
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
  .sd-hero-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 1.5rem;
  }

  .sd-cover {
    width: 170px;
    height: 170px;
  }

  .sd-tags { justify-content: center; }
  .sd-stats { justify-content: center; }
  .sd-actions { justify-content: center; }

  .sd-body {
    grid-template-columns: 1fr;
  }
}
</style>
