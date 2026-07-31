<template>
  <div class="data-stats">
    <h2>📊 数据统计</h2>

    <!-- 数据概览卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">总用户数</div>
        <div class="stat-value">{{ stats.totalUsers }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">总歌曲数</div>
        <div class="stat-value">{{ stats.totalSongs }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">总播放量</div>
        <div class="stat-value">{{ formatCount(stats.totalPlayCount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">待审核</div>
        <div class="stat-value" style="color:#fb923c">{{ stats.pendingCount }}</div>
      </div>
    </div>

    <!-- 柱状图 -->
    <div class="card chart-card">
      <h3>📈 数据概览</h3>
      <div class="chart-container">
        <div class="bar-item">
          <div class="bar-label">用户</div>
          <div class="bar-track">
            <div class="bar-fill" :style="{width: barWidth(stats.totalUsers), background: '#46f6e6'}"></div>
          </div>
          <div class="bar-value">{{ stats.totalUsers }}</div>
        </div>
        <div class="bar-item">
          <div class="bar-label">歌曲</div>
          <div class="bar-track">
            <div class="bar-fill" :style="{width: barWidth(stats.totalSongs), background: '#925dff'}"></div>
          </div>
          <div class="bar-value">{{ stats.totalSongs }}</div>
        </div>
        <div class="bar-item">
          <div class="bar-label">播放量</div>
          <div class="bar-track">
            <div class="bar-fill" :style="{width: barWidth(stats.totalPlayCount), background: '#fbbf24'}"></div>
          </div>
          <div class="bar-value">{{ formatCount(stats.totalPlayCount) }}</div>
        </div>
      </div>
    </div>

    <!-- 近7天统计 -->
    <div class="card" v-if="dailyStats.length > 0">
      <h3>📅 近7天趋势</h3>
      <div class="daily-chart">
        <div class="daily-item" v-for="d in dailyStats" :key="d.statDate">
          <div class="daily-bar-group">
            <div class="daily-bar" :style="{height: dailyHeight(d.newUsers)}"></div>
          </div>
          <div class="daily-label">{{ formatDay(d.statDate) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { adminApi } from '../api'

const stats = ref({ totalUsers: 0, totalSongs: 0, totalPlayCount: 0, totalFollowers: 0, pendingCount: 0 })
const dailyStats = ref([])

onMounted(async () => {
  await Promise.all([loadStats(), loadDaily(), loadPendingCount()])
})

const loadStats = async () => {
  try {
    const res = await adminApi.statsOverview()
    const d = res?.data?.data || res?.data || {}
    stats.value = {
      totalUsers: d.totalUsers || 0,
      totalSongs: d.totalSongs || 0,
      totalPlayCount: d.totalPlayCount || 0,
      totalFollowers: d.totalFollowers || 0,
      pendingCount: d.pendingCount || 0
    }
  } catch (e) { console.error(e) }
}
// 加载真实的待审核歌曲数量（statsOverview 可能不含该字段，单独回查）
const loadPendingCount = async () => {
  try {
    const res = await adminApi.pendingSongs()
    const d = res?.data?.data || res?.data || []
    stats.value.pendingCount = Array.isArray(d) ? d.length : 0
  } catch (e) { stats.value.pendingCount = 0 }
}

const loadDaily = async () => {
  try {
    const res = await adminApi.statsDaily()
    const d = res?.data || res || []
    dailyStats.value = Array.isArray(d) ? d : []
  } catch (e) { dailyStats.value = [] }
}

const maxStat = computed(() => Math.max(
  stats.value.totalUsers || 0,
  stats.value.totalSongs || 0,
  stats.value.totalPlayCount || 0,
  1
))

const barWidth = (val) => {
  const v = val || 0
  const pct = Math.max((v / maxStat.value) * 100, 2)
  return pct + '%'
}

const maxDaily = computed(() => {
  const max = Math.max(...dailyStats.value.map(d => d.newUsers || 0), 1)
  return max
})

const dailyHeight = (val) => {
  const v = val || 0
  const pct = Math.max((v / maxDaily.value) * 100, 5)
  return pct + '%'
}

const formatCount = (n) => {
  if (!n && n !== 0) return '0'
  if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toString()
}

const formatDay = (d) => {
  if (!d) return ''
  const date = new Date(d)
  return `${date.getMonth() + 1}/${date.getDate()}`
}
</script>

<style scoped>
.data-stats { max-width: 1100px; margin: 0 auto; }
.data-stats h2 { margin-bottom: 24px; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #1e1e2e; border-radius: 12px; padding: 20px; text-align: center; }
.stat-label { color: #888; font-size: 0.85rem; margin-bottom: 8px; }
.stat-value { font-size: 2rem; font-weight: 700; color: #42b983; }
.card { background: #1e1e2e; border-radius: 12px; padding: 20px; margin-bottom: 20px; }
.card h3 { margin: 0 0 16px; }
.chart-card { margin-bottom: 24px; }
.chart-container { display: flex; flex-direction: column; gap: 12px; }
.bar-item { display: flex; align-items: center; gap: 12px; }
.bar-label { width: 60px; font-size: 0.85rem; color: #9b9daa; text-align: right; flex-shrink: 0; }
.bar-track { flex: 1; height: 28px; background: rgba(255,255,255,0.04); border-radius: 6px; overflow: hidden; position: relative; }
.bar-fill { height: 100%; border-radius: 6px; transition: width 0.6s ease; min-width: 4px; }
.bar-value { width: 80px; font-size: 0.85rem; color: #f3f2ef; font-weight: 600; flex-shrink: 0; }
.daily-chart { display: flex; align-items: flex-end; gap: 8px; height: 150px; padding-top: 10px; }
.daily-item { flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; }
.daily-bar-group { flex: 1; width: 100%; display: flex; align-items: flex-end; justify-content: center; }
.daily-bar { width: 60%; background: linear-gradient(180deg, #46f6e6, #2ba89f); border-radius: 4px 4px 0 0; min-height: 4px; transition: height 0.5s ease; }
.daily-label { font-size: 0.7rem; color: #888; margin-top: 6px; }
</style>
