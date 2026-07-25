<template>
  <div class="singer-center">
    <h2>🎤 歌手中心</h2>

    <!-- 未认证歌手提示 -->
    <div v-if="!isSinger" class="notice-card warn">
      <p>你还不是认证歌手，<router-link to="/singer/apply">立即申请认证 →</router-link></p>
    </div>

    <!-- 发布新歌曲 -->
    <div class="card">
      <h3>📀 发布新歌曲</h3>
      <form @submit.prevent="onPublish" class="publish-form">
        <div class="form-row">
          <label>歌曲名 *</label>
          <input v-model="form.name" required placeholder="输入歌曲名" />
        </div>
        <div class="form-row">
          <label>流派</label>
          <input v-model="form.genre" placeholder="如：流行、摇滚" />
        </div>
        <div class="form-row">
          <label>语言</label>
          <input v-model="form.language" placeholder="如：中文、英文" />
        </div>
        <div class="form-row">
          <label>歌词</label>
          <textarea v-model="form.lyrics" rows="3" placeholder="LRC格式歌词"></textarea>
        </div>
        <div class="form-row">
          <label>封面URL</label>
          <input v-model="form.cover" placeholder="封面图片地址" />
        </div>
        <div class="form-row">
          <label>音频URL *</label>
          <input v-model="form.url" required placeholder="音频文件地址" />
        </div>
        <div class="form-row">
          <label>时长（秒）</label>
          <input v-model.number="form.duration" type="number" min="0" />
        </div>
        <div class="form-row">
          <label>简介</label>
          <textarea v-model="form.description" rows="2"></textarea>
        </div>
        <button type="submit" :disabled="publishing" class="btn-primary">
          {{ publishing ? '发布中...' : '发布歌曲（进入审核）' }}
        </button>
      </form>
    </div>

    <!-- 我的歌曲 -->
    <div class="card">
      <div class="card-header">
        <h3>🎵 我的歌曲</h3>
        <div class="filter-tabs">
          <button :class="{ active: statusFilter === null }" @click="statusFilter = null">全部</button>
          <button :class="{ active: statusFilter === 2 }" @click="statusFilter = 2">审核中</button>
          <button :class="{ active: statusFilter === 1 }" @click="statusFilter = 1">已上架</button>
          <button :class="{ active: statusFilter === 0 }" @click="statusFilter = 0">已下架</button>
        </div>
      </div>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="songs.length === 0" class="empty">暂无歌曲</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>歌曲名</th>
            <th>流派</th>
            <th>状态</th>
            <th>播放量</th>
            <th>发布时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in songs" :key="s.songId">
            <td>{{ s.name }}</td>
            <td>{{ s.genre || '-' }}</td>
            <td>
              <span :class="['badge', statusClass(s.status)]">
                {{ statusText(s.status) }}
              </span>
            </td>
            <td>{{ s.playCount }}</td>
            <td>{{ formatDate(s.createTime) }}</td>
            <td>
              <button v-if="s.status === 2" @click="onOffline(s.songId)" class="btn-sm">下架</button>
              <button v-if="s.status === 0" @click="onResubmit(s.songId)" class="btn-sm btn-primary">重新提交</button>
              <button v-if="s.status !== 2" @click="onEdit(s)" class="btn-sm">编辑</button>
              <button @click="onDelete(s.songId)" class="btn-sm btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { songApi, authApi } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const roles = ref([])
const isSinger = computed(() => roles.value.includes('SINGER') || roles.value.includes('ADMIN'))
const loading = ref(false)
const songs = ref([])
const statusFilter = ref(null)
const publishing = ref(false)
const form = reactive({
  name: '', singerId: null, albumId: null, duration: 0,
  url: '', cover: '', lyrics: '', description: '', genre: '', language: '', releaseDate: ''
})

const statusText = (s) => ({ 0: '已下架', 1: '已上架', 2: '审核中' }[s] || '未知')
const statusClass = (s) => ({ 0: 'gray', 1: 'green', 2: 'orange' }[s] || '')
const formatDate = (t) => t ? new Date(t).toLocaleDateString() : '-'

onMounted(async () => {
  try {
    const userInfo = await authApi.getUserInfo()
    if (userInfo?.code === 200) {
      roles.value = userInfo.data?.roles || []
    }
  } catch (e) { /* not logged in */ }
  try {
    const { data } = await songApi.mySongs(null)
    songs.value = data?.data || []
  } catch (e) { songs.value = [] }
})

const loadSongs = async () => {
  loading.value = true
  try {
    const { data } = await songApi.mySongs(statusFilter.value)
    songs.value = data?.data || []
  } finally { loading.value = false }
}

const onPublish = async () => {
  publishing.value = true
  try {
    const payload = { ...form }
    const res = await songApi.publish(payload)
    // Axios interceptor already unwraps res.data, so res = {code, message, data}
    if (res?.code === 200) {
      ElMessage.success('发布成功，进入审核队列')
      form.name = form.url = ''
      form.cover = form.lyrics = form.description = form.genre = form.language = ''
      loadSongs()
    } else {
      ElMessage.error(res?.message || '发布失败')
    }
  } catch (e) {
    ElMessage.error('发布失败: ' + (e?.message || '未知错误'))
  }
  finally { publishing.value = false }
}

const onOffline = async (id) => {
  const { data } = await songApi.offlineSong(id)
  data?.code === 200 ? (ElMessage.success('已下架'), loadSongs()) : ElMessage.error('操作失败')
}
const onResubmit = async (id) => {
  const { data } = await songApi.reSubmitSong(id)
  data?.code === 200 ? (ElMessage.success('已重新提交审核'), loadSongs()) : ElMessage.error('操作失败')
}
const onEdit = (s) => { router.push(`/song/${s.songId}`) }
const onDelete = async (id) => {
  const { data } = await songApi.deleteSong(id)
  data?.code === 200 ? (ElMessage.success('已删除'), loadSongs()) : ElMessage.error('删除失败')
}

watch(statusFilter, loadSongs)
</script>

<style scoped>
.singer-center { max-width: 900px; margin: 0 auto; }
.singer-center h2 { margin-bottom: 24px; }
.notice-card { padding: 16px; border-radius: 8px; margin-bottom: 20px; }
.notice-card.warn { background: #fff3cd; border: 1px solid #ffc107; color: #856404; }
.notice-card.warn a { color: #42b983; font-weight: 600; }
.card { background: #1e1e2e; border-radius: 12px; padding: 20px; margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.card-header h3 { margin: 0; }
.filter-tabs { display: flex; gap: 8px; }
.filter-tabs button { padding: 4px 12px; border-radius: 6px; border: 1px solid #444; background: transparent; color: #888; cursor: pointer; }
.filter-tabs button.active { background: #42b983; color: #fff; border-color: #42b983; }
.publish-form { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.publish-form .form-row.full { grid-column: 1 / -1; }
.form-row { display: flex; flex-direction: column; gap: 4px; }
.form-row label { font-size: 0.8rem; color: #888; }
.form-row input, .form-row textarea, .form-row select {
  background: #16213e; border: 1px solid #333; border-radius: 6px;
  padding: 8px 12px; color: #eee; font-size: 0.9rem;
}
.form-row input:focus, .form-row textarea:focus { border-color: #42b983; outline: none; }
.btn-primary { background: #42b983; color: #fff; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: 600; grid-column: 1 / -1; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 10px; text-align: left; border-bottom: 1px solid #2a2a4e; }
.data-table th { color: #888; font-size: 0.8rem; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; }
.badge.green { background: #1a3a2a; color: #4ade80; }
.badge.orange { background: #3a2a1a; color: #fb923c; }
.badge.gray { background: #2a2a2a; color: #888; }
.btn-sm { background: #16213e; border: 1px solid #444; color: #ccc; padding: 4px 10px; border-radius: 4px; cursor: pointer; margin-right: 4px; font-size: 0.8rem; }
.btn-sm:hover { border-color: #42b983; color: #42b983; }
.btn-sm.btn-primary { background: #42b983; border-color: #42b983; color: #fff; }
.btn-sm.btn-danger { color: #ff6b6b; border-color: #ff6b6b; }
.loading, .empty { text-align: center; padding: 40px; color: #666; }
</style>
