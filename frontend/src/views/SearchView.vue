<template>
  <div class="search-page">
    <!-- Search Hero -->
    <div class="search-hero">
      <div class="search-hero-orb"></div>
      <h1 class="search-title gradient-text">搜索 · Search</h1>
      <div class="search-box-wrapper">
        <div class="search-box glass">
          <span class="search-icon">🔍</span>
          <input
            v-model="keyword"
            @input="onInput"
            @keyup.enter="doSearch"
            @focus="showSuggestions = suggestions.length > 0"
            @blur="hideSuggestions"
            type="text"
            placeholder="搜索歌曲、歌手、歌词..."
            class="search-input"
          />
          <button @click="doSearch" class="search-btn">搜索</button>
        </div>

        <!-- Suggestions Dropdown -->
        <div class="suggestions-dropdown glass-strong" v-if="showSuggestions && suggestions.length">
          <div
            class="suggestion-item"
            v-for="item in suggestions"
            :key="item.songId"
            @mousedown="selectSuggestion(item)"
          >
            <span class="suggestion-icon">🎵</span>
            <div class="suggestion-info">
              <div class="suggestion-name" v-html="highlight(item.name)"></div>
              <div class="suggestion-singer">{{ item.singerName }}</div>
            </div>
            <span class="suggestion-play" @mousedown.stop="player.loadSong(item)">▶</span>
          </div>
        </div>
      </div>

      <div class="search-tips">
        <span class="tips-label">热门搜索:</span>
        <span class="tip-tag" @click="quickSearch('周杰伦')">周杰伦</span>
        <span class="tip-tag" @click="quickSearch('林俊杰')">林俊杰</span>
        <span class="tip-tag" @click="quickSearch('陈奕迅')">陈奕迅</span>
        <span class="tip-tag" @click="quickSearch('薛之谦')">薛之谦</span>
        <span class="tip-tag" @click="quickSearch('毛不易')">毛不易</span>
      </div>
    </div>

    <!-- Results -->
    <div class="search-results" v-if="results.length || loading">
      <div class="results-header">
        <button class="results-clear" @click="clearSearch">✕ 清除搜索</button>
        <span class="results-count">{{ loading ? '搜索中...' : `找到 ${total} 个结果` }}</span>
        <div class="eq-bars" v-if="loading">
          <i class="eq-bar"></i><i class="eq-bar"></i><i class="eq-bar"></i><i class="eq-bar"></i>
        </div>
      </div>
      <div class="results-list" v-if="!loading">
        <div class="result-item" v-for="(item, idx) in results" :key="item.songId">
          <span class="result-idx">{{ idx + 1 }}</span>
          <div class="result-cover" @click="player.loadSong(item)" :style="item.cover ? { backgroundImage: 'url(' + item.cover + ')', backgroundSize: 'cover', backgroundPosition: 'center' } : {}">
            <span v-if="!item.cover">🎵</span>
          </div>
          <div class="result-info" @click="$router.push('/song/' + item.songId)">
            <div class="result-name">{{ item.name }}</div>
            <div class="result-singer">{{ item.singerName }}</div>
          </div>
          <div class="result-actions">
            <button class="ra-btn" @click="player.loadSong(item)" title="播放">▶</button>
            <button class="ra-btn" @click="doCollect(item)" title="收藏">{{ collected[item.songId] ? '❤️' : '🤍' }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div class="search-empty" v-else-if="searched && !loading">
      <span class="empty-ico">🔍</span>
      <p>未找到相关内容</p>
      <p class="empty-hint">换个关键词试试</p>
      <button class="empty-back" @click="clearSearch">返回</button>
    </div>

    <!-- Initial State -->
    <div class="search-initial" v-else>
      <div class="initial-trending">
        <h3>🔥 搜索热词</h3>
        <div class="trending-tags">
          <span class="trending-tag" @click="quickSearch('晴天')">
            <span class="trending-rank">1</span> 晴天
          </span>
          <span class="trending-tag" @click="quickSearch('稻香')">
            <span class="trending-rank">2</span> 稻香
          </span>
          <span class="trending-tag" @click="quickSearch('十年')">
            <span class="trending-rank">3</span> 十年
          </span>
          <span class="trending-tag" @click="quickSearch('演员')">
            <span class="trending-rank">4</span> 演员
          </span>
          <span class="trending-tag" @click="quickSearch('光年之外')">
            <span class="trending-rank">5</span> 光年之外
          </span>
          <span class="trending-tag" @click="quickSearch('消愁')">
            <span class="trending-rank">6</span> 消愁
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { songApi } from '../api'
import { usePlayerStore } from '../store/player'

const player = usePlayerStore()
const keyword = ref('')
const loading = ref(false)
const results = ref([])
const total = ref(0)
const collected = reactive({})
const searched = ref(false)

const suggestions = ref([])
const showSuggestions = ref(false)
let debounceTimer = null
let blurTimer = null

const hideSuggestions = () => {
  blurTimer = setTimeout(() => { showSuggestions.value = false }, 200)
}

onMounted(() => {
  document.title = '搜索 · Music Dreamer'
})

const onInput = () => {
  clearTimeout(debounceTimer)
  if (!keyword.value.trim()) {
    suggestions.value = []
    showSuggestions.value = false
    return
  }
  debounceTimer = setTimeout(() => {
    fetchSuggestions()
  }, 300)
}

const fetchSuggestions = async () => {
  try {
    const res = await songApi.search({ keyword: keyword.value, page: 1, size: 8 })
    const data = res?.data || res
    suggestions.value = data?.songs || []
    showSuggestions.value = suggestions.value.length > 0
  } catch (e) {
    suggestions.value = []
  }
}

const selectSuggestion = (item) => {
  keyword.value = item.name
  showSuggestions.value = false
  doSearch()
}

const highlight = (text) => {
  if (!keyword.value.trim() || !text) return text
  const kw = keyword.value.trim()
  const idx = text.indexOf(kw)
  if (idx >= 0) {
    return text.substring(0, idx) + '<strong>' + text.substring(idx, idx + kw.length) + '</strong>' + text.substring(idx + kw.length)
  }
  return text
}

const doSearch = async () => {
  if (!keyword.value.trim()) return
  loading.value = true
  searched.value = true
  showSuggestions.value = false
  suggestions.value = []
  try {
    const res = await songApi.search({ keyword: keyword.value, page: 1, size: 20 })
    const data = res?.data || res
    results.value = data?.songs || []
    total.value = data?.total ?? results.value.length
  } catch (e) {
    results.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const quickSearch = (kw) => {
  keyword.value = kw
  doSearch()
}

const clearSearch = () => {
  keyword.value = ''
  results.value = []
  total.value = 0
  searched.value = false
  suggestions.value = []
  showSuggestions.value = false
}

const doCollect = async (song) => {
  try {
    if (collected[song.songId]) {
      await songApi.uncollect(song.songId)
      collected[song.songId] = false
    } else {
      await songApi.collect(song.songId)
      collected[song.songId] = true
    }
  } catch (e) {
    collected[song.songId] = !collected[song.songId]
  }
}
</script>

<style scoped>
.search-page {
  max-width: 900px;
  margin: 0 auto;
}

/* --- Hero --- */
.search-hero {
  text-align: center;
  margin-bottom: 2.5rem;
  position: relative;
  padding: 2rem 0;
}

.search-hero-orb {
  position: absolute;
  width: 300px;
  height: 300px;
  top: -20%;
  left: 50%;
  transform: translateX(-50%);
  background: radial-gradient(ellipse, rgba(70, 246, 230, 0.08), transparent 70%);
  pointer-events: none;
}

.search-title {
  font-family: var(--ark-display);
  font-weight: 900;
  font-size: clamp(2rem, 5vw, 3rem);
  margin-bottom: 1.5rem;
  text-transform: uppercase;
  letter-spacing: -0.04em;
}

/* --- Search Box --- */
.search-box-wrapper {
  position: relative;
  max-width: 560px;
  margin: 0 auto 1.25rem;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.5rem 0.4rem 1.25rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 240ms ease;
}

.search-box:focus-within {
  border-color: rgba(70, 246, 230, 0.4);
  box-shadow: 0 0 24px rgba(70, 246, 230, 0.1);
}

.search-icon {
  font-size: 1rem;
  opacity: 0.6;
}

.search-input {
  flex: 1;
  padding: 0.6rem 0;
  border: none;
  background: transparent;
  color: #f3f2ef;
  font-size: 0.9rem;
  outline: none;
}

.search-input::placeholder {
  color: #9b9daa;
}

.search-btn {
  padding: 0.6rem 1.5rem;
  border-radius: 999px;
  background: linear-gradient(135deg, #46f6e6, #2ba89f);
  color: #080914;
  font-weight: 600;
  font-size: 0.82rem;
  transition: all 240ms ease;
}

.search-btn:hover {
  transform: scale(1.03);
  box-shadow: 0 4px 16px rgba(70, 246, 230, 0.25);
}

/* --- Suggestions --- */
.suggestions-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  border-radius: 1rem;
  overflow: hidden;
  z-index: 100;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background 150ms ease;
}

.suggestion-item:hover {
  background: rgba(70, 246, 230, 0.06);
}

.suggestion-icon {
  font-size: 1.1rem;
  width: 32px;
  height: 32px;
  border-radius: 0.5rem;
  background: rgba(70, 246, 230, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.suggestion-info {
  flex: 1;
  min-width: 0;
}

.suggestion-name {
  font-size: 0.85rem;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suggestion-name :deep(strong) {
  color: #46f6e6;
}

.suggestion-singer {
  font-size: 0.72rem;
  color: #9b9daa;
}

.suggestion-play {
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

.suggestion-item:hover .suggestion-play {
  opacity: 1;
}

/* --- Tips --- */
.search-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  flex-wrap: wrap;
  font-size: 0.78rem;
  color: #9b9daa;
}

.tips-label {
  margin-right: 0.25rem;
}

.tip-tag {
  padding: 0.3rem 0.75rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: all 240ms ease;
  font-size: 0.75rem;
}

.tip-tag:hover {
  border-color: #46f6e6;
  color: #46f6e6;
  background: rgba(70, 246, 230, 0.04);
  transform: translateY(-1px);
}

/* --- Results --- */
.results-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.results-clear {
  padding: 0.3rem 0.75rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 107, 107, 0.25);
  color: #ff6b6b;
  font-size: 0.72rem;
  transition: all 240ms ease;
}

.results-clear:hover {
  background: rgba(255, 107, 107, 0.08);
  transform: translateY(-1px);
}

.results-count {
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #46f6e6;
  font-family: var(--ark-mono);
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1rem;
  border-radius: 1rem;
  transition: all 240ms ease;
}

.result-item:hover {
  background: rgba(255, 255, 255, 0.04);
  transform: translateX(4px);
}

.result-idx {
  width: 28px;
  text-align: center;
  font-family: var(--ark-mono);
  font-size: 0.72rem;
  color: #9b9daa;
}

.result-cover {
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
  border: 1px solid rgba(255, 255, 255, 0.04);
  transition: transform 240ms ease;
}

.result-item:hover .result-cover {
  transform: scale(1.08);
}

.result-info {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.result-name {
  font-weight: 600;
  font-size: 0.88rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-singer {
  color: #9b9daa;
  font-size: 0.78rem;
}

.result-actions {
  display: flex;
  gap: 0.4rem;
}

.ra-btn {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  transition: all 240ms ease;
}

.ra-btn:hover {
  background: rgba(70, 246, 230, 0.08);
  border-color: #46f6e6;
  transform: scale(1.1);
}

/* --- Empty --- */
.search-empty {
  text-align: center;
  padding: 4rem 0;
}

.empty-ico {
  font-size: 3rem;
  opacity: 0.4;
}

.search-empty p {
  margin-top: 1rem;
  color: #9b9daa;
}

.empty-hint {
  font-size: 0.8rem;
  margin-top: 0.5rem;
  opacity: 0.6;
}

.empty-back {
  margin-top: 1.5rem;
  padding: 0.5rem 1.25rem;
  border-radius: 999px;
  border: 1px solid rgba(70, 246, 230, 0.3);
  color: #46f6e6;
  font-size: 0.82rem;
  transition: all 240ms ease;
}

.empty-back:hover {
  background: rgba(70, 246, 230, 0.08);
  transform: translateY(-2px);
}

/* --- Initial State --- */
.search-initial {
  padding: 2rem 0;
}

.initial-trending h3 {
  font-size: 0.72rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #46f6e6;
  margin-bottom: 1rem;
  font-family: var(--ark-mono);
}

.trending-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.trending-tag {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  cursor: pointer;
  transition: all 240ms ease;
  font-size: 0.82rem;
}

.trending-tag:hover {
  border-color: rgba(70, 246, 230, 0.3);
  background: rgba(70, 246, 230, 0.04);
  transform: translateY(-2px);
}

.trending-rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: rgba(70, 246, 230, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.65rem;
  font-weight: 700;
  color: #46f6e6;
  font-family: var(--ark-mono);
}
</style>
