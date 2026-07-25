<template>
  <div class="admin-panel">
    <h2>🛡 管理后台</h2>

    <!-- 数据概览 -->
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
        <div class="stat-label">待审核歌曲</div>
        <div class="stat-value" style="color:#fb923c">{{ pendingCount }}</div>
      </div>
    </div>

    <!-- 数据统计图表 -->
    <div class="card chart-card">
      <h3>📊 数据统计</h3>
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
        <div class="bar-item">
          <div class="bar-label">待审核</div>
          <div class="bar-track">
            <div class="bar-fill" :style="{width: barWidth(pendingCount), background: '#fb923c'}"></div>
          </div>
          <div class="bar-value">{{ pendingCount }}</div>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tabs">
      <button :class="{ active: tab === 'songs' }" @click="tab = 'songs'">🎵 歌曲审核</button>
      <button :class="{ active: tab === 'singers' }" @click="tab = 'singers'">🎤 歌手认证</button>
      <button :class="{ active: tab === 'users' }" @click="tab = 'users'">👥 用户管理</button>
    </div>

    <!-- 歌曲审核 -->
    <div v-if="tab === 'songs'" class="card">
      <h3>🎵 待审核歌曲 ({{ pendingSongs.length }})</h3>
      <div v-if="loading" class="loading">加载中...</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>歌曲名</th>
            <th>歌手ID</th>
            <th>流派</th>
            <th>语言</th>
            <th>上传时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in pendingSongs" :key="s.songId">
            <td>{{ s.name }}</td>
            <td>{{ s.singerId }}</td>
            <td>{{ s.genre || '-' }}</td>
            <td>{{ s.language || '-' }}</td>
            <td>{{ formatDate(s.createTime) }}</td>
            <td>
              <button @click="auditPass(s.songId)" class="btn-sm btn-green">通过</button>
              <button @click="showReject(s.songId)" class="btn-sm btn-danger">拒绝</button>
              <button @click="playSong(s)" class="btn-sm">试听</button>
            </td>
          </tr>
        </tbody>
        <tr v-if="pendingSongs.length === 0">
          <td colspan="6" class="empty-row">暂无待审核歌曲</td>
        </tr>
      </table>
      <audio v-if="playingSong" :src="playingSong.url" controls style="width:100%;margin-top:12px"></audio>
    </div>

    <!-- 歌手认证 -->
    <div v-if="tab === 'singers'" class="card">
      <h3>🎤 待审核认证 ({{ pendingApplies.length }})</h3>
      <div v-if="loading" class="loading">加载中...</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>申请人</th>
            <th>真实姓名</th>
            <th>身份证</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in pendingApplies" :key="a.applyId">
            <td>{{ a.userId }}</td>
            <td>{{ a.realName }}</td>
            <td>{{ maskIdCard(a.idCard) }}</td>
            <td>{{ formatDate(a.applyTime) }}</td>
            <td>
              <button @click="approveApply(a.applyId)" class="btn-sm btn-green">通过</button>
              <button @click="rejectApply(a.applyId)" class="btn-sm btn-danger">拒绝</button>
            </td>
          </tr>
        </tbody>
        <tr v-if="pendingApplies.length === 0">
          <td colspan="5" class="empty-row">暂无待审核申请</td>
        </tr>
      </table>
    </div>

    <!-- 用户管理 -->
    <div v-if="tab === 'users'" class="card">
      <h3>👥 用户列表 ({{ userTotal }})</h3>
      <div v-if="loading" class="loading">加载中...</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in userList" :key="u.userId">
            <td>{{ u.userId }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.nickname }}</td>
            <td>{{ u.email }}</td>
            <td>
              <span v-for="r in (u.roles || [])" :key="r" class="role-badge">{{ r }}</span>
            </td>
            <td>
              <span :class="['badge', u.status === 1 ? 'green' : 'red']">
                {{ u.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>
              <button v-if="u.status === 1" @click="toggleUserStatus(u.userId, 0)" class="btn-sm btn-danger">封禁</button>
              <button v-else @click="toggleUserStatus(u.userId, 1)" class="btn-sm btn-green">解封</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 拒绝原因弹窗 -->
    <div v-if="rejectSongId" class="modal-overlay" @click.self="rejectSongId = null">
      <div class="modal">
        <h3>拒绝原因</h3>
        <textarea v-model="rejectReason" rows="3" placeholder="请输入拒绝原因"></textarea>
        <div class="modal-actions">
          <button @click="confirmReject" class="btn-danger">确认拒绝</button>
          <button @click="rejectSongId = null">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { adminApi, songApi } from '../api'
import { ElMessage } from 'element-plus'

const tab = ref('songs')
const loading = ref(false)
const stats = reactive({ totalUsers: 0, totalSongs: 0, totalPlayCount: 0 })
const pendingSongs = ref([])
const pendingApplies = ref([])
const userList = ref([])
const userTotal = ref(0)
const pendingCount = ref(0)
const rejectSongId = ref(null)
const rejectReason = ref('')
const playingSong = ref(null)

onMounted(async () => {
  await Promise.all([loadStats(), loadPendingSongs(), loadPendingApplies(), loadUsers()])
})

const loadStats = async () => {
  try {
    const res = await adminApi.statsOverview()
    const d = res?.data?.data || res?.data || {}
    stats.totalUsers = d.totalUsers || 0
    stats.totalSongs = d.totalSongs || 0
    stats.totalPlayCount = d.totalPlayCount || 0
  } catch (e) { console.error('loadStats error:', e) }
}
const loadPendingSongs = async () => {
  loading.value = true
  try {
    const res = await adminApi.pendingSongs()
    const d = res?.data?.data || res?.data || []
    pendingSongs.value = Array.isArray(d) ? d : []
    pendingCount.value = pendingSongs.value.length
  } catch (e) { console.error('loadPendingSongs error:', e); pendingSongs.value = []; pendingCount.value = 0 }
  finally { loading.value = false }
}
const loadPendingApplies = async () => {
  try {
    const res = await adminApi.getPendingApplies()
    const d = res?.data?.data || res?.data || []
    pendingApplies.value = Array.isArray(d) ? d : []
  } catch (e) { pendingApplies.value = [] }
}
const loadUsers = async () => {
  try {
    const res = await adminApi.userList(1, 50)
    const d = res?.data?.data || res?.data || {}
    userList.value = d.list || []
    userTotal.value = d.total || 0
  } catch (e) { userList.value = []; userTotal.value = 0 }
}

const auditPass = async (id) => {
  const { data } = await adminApi.auditPass(id)
  data?.code === 200 ? (ElMessage.success('已通过'), loadPendingSongs(), loadStats()) : ElMessage.error('操作失败')
}
const showReject = (id) => { rejectSongId.value = id; rejectReason.value = '' }
const confirmReject = async () => {
  if (!rejectReason.value.trim()) return message.warning('请填写拒绝原因')
  const { data } = await adminApi.auditReject(rejectSongId.value, rejectReason.value)
  data?.code === 200 ? (ElMessage.success('已拒绝'), rejectSongId.value = null, loadPendingSongs()) : ElMessage.error('操作失败')
}
const approveApply = async (id) => {
  const { data } = await singerApi.approveApply(id)
  data?.code === 200 ? (ElMessage.success('已通过'), loadPendingApplies()) : ElMessage.error('操作失败')
}
const rejectApply = async (id) => {
  const reason = prompt('请输入拒绝原因：')
  if (!reason) return
  const { data } = await singerApi.rejectApply(id, reason)
  data?.code === 200 ? (ElMessage.success('已拒绝'), loadPendingApplies()) : ElMessage.error('操作失败')
}
const toggleUserStatus = async (id, status) => {
  const { data } = await adminApi.disableUser(id, status)
  data?.code === 200 ? (ElMessage.success(status === 0 ? '已封禁' : '已解封'), loadUsers()) : ElMessage.error('操作失败')
}
const playSong = (s) => { playingSong.value = playingSong.value?.songId === s.songId ? null : s }
const formatDate = (t) => t ? new Date(t).toLocaleDateString() : '-'
const maskIdCard = (id) => id ? id.substring(0, 4) + '****' + id.substring(id.length - 4) : '-'
const formatCount = (n) => {
  if (!n && n !== 0) return '0'
  if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toString()
}
</script>

<style scoped>
.admin-panel { max-width: 1100px; margin: 0 auto; }
.admin-panel h2 { margin-bottom: 24px; }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #1e1e2e; border-radius: 12px; padding: 20px; text-align: center; }
.stat-label { color: #888; font-size: 0.85rem; margin-bottom: 8px; }
.stat-value { font-size: 2rem; font-weight: 700; color: #42b983; }
.tabs { display: flex; gap: 8px; margin-bottom: 20px; }
.tabs button { padding: 8px 20px; border-radius: 6px; border: 1px solid #444; background: transparent; color: #888; cursor: pointer; font-size: 0.9rem; }
.tabs button.active { background: #42b983; color: #fff; border-color: #42b983; }
.card { background: #1e1e2e; border-radius: 12px; padding: 20px; margin-bottom: 20px; }
.card h3 { margin: 0 0 16px; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 10px; text-align: left; border-bottom: 1px solid #2a2a4e; }
.data-table th { color: #888; font-size: 0.8rem; }
.empty-row { text-align: center; color: #666; padding: 30px; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; }
.badge.green { background: #1a3a2a; color: #4ade80; }
.badge.red { background: #3a1a1a; color: #ff6b6b; }
.badge.gray { background: #2a2a2a; color: #888; }
.role-badge { display: inline-block; padding: 2px 6px; margin: 1px; border-radius: 4px; font-size: 0.7rem; background: #16213e; border: 1px solid #42b983; color: #42b983; margin-right: 4px; }
.btn-sm { background: #16213e; border: 1px solid #444; color: #ccc; padding: 4px 10px; border-radius: 4px; cursor: pointer; font-size: 0.8rem; margin-right: 4px; }
.btn-sm:hover { border-color: #42b983; color: #42b983; }
.btn-sm.btn-green { color: #4ade80; border-color: #4ade80; }
.btn-sm.btn-danger { color: #ff6b6b; border-color: #ff6b6b; }
.loading, .empty { text-align: center; padding: 40px; color: #666; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #1e1e2e; border-radius: 12px; padding: 24px; width: 400px; }
.modal h3 { margin: 0 0 16px; }
.modal textarea { width: 100%; background: #16213e; border: 1px solid #333; border-radius: 6px; padding: 10px; color: #eee; }
.modal-actions { margin-top: 16px; display: flex; gap: 8px; justify-content: flex-end; }
.btn-danger { background: #ff6b6b; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
</style>
