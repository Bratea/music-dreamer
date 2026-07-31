<template>
  <div class="singer-apply">
    <h2>📝 歌手认证申请</h2>

    <!-- 已有申请 -->
    <div v-if="existingApply" class="card status-card">
      <h3>当前状态：{{ statusText(existingApply.status) }}</h3>
      <div v-if="existingApply.status === 0" class="pending-info">
        <p>⏳ 你的申请正在审核中，请耐心等待</p>
        <p class="apply-time">提交时间：{{ formatDate(existingApply.applyTime) }}</p>
      </div>
      <div v-else-if="existingApply.status === 1" class="approved-info">
        <p>✅ 认证通过！你现在是认证歌手</p>
      </div>
      <div v-else class="rejected-info">
        <p>❌ 申请被拒绝：{{ existingApply.rejectReason || '无' }}</p>
        <button @click="showForm = true" class="btn-primary">重新申请</button>
      </div>
    </div>

    <!-- 申请表单 -->
    <div v-if="!existingApply || existingApply.status !== 0" class="card">
      <h3>{{ existingApply ? '重新提交认证申请' : '提交认证申请' }}</h3>
      <p class="tip">认证后即可上传发布歌曲，享受歌手专属功能</p>
      <form @submit.prevent="onSubmit" class="apply-form">
        <div class="form-row">
          <label>真实姓名 *</label>
          <input v-model="form.realName" required placeholder="与身份证一致" />
        </div>
        <div class="form-row">
          <label>身份证号 *</label>
          <input v-model="form.idCard" required placeholder="18位身份证号" maxlength="18" />
        </div>
        <div class="form-row">
          <label>歌手名</label>
          <input v-model="form.stageName" placeholder="艺名 / 歌手名" />
        </div>
        <div class="form-row">
          <label>性别</label>
          <select v-model="form.gender">
            <option :value="0">未知</option>
            <option :value="1">男</option>
            <option :value="2">女</option>
            <option :value="3">组合</option>
          </select>
        </div>
        <div class="form-row">
          <label>国籍/地区</label>
          <input v-model="form.country" placeholder="如：中国" />
        </div>
        <div class="form-row">
          <label>出生日期</label>
          <input v-model="form.birthday" type="date" />
        </div>
        <div class="form-row">
          <label>歌手简介</label>
          <textarea v-model="form.intro" rows="3" placeholder="介绍一下你自己"></textarea>
        </div>
        <div class="form-row">
          <label>头像URL</label>
          <input v-model="form.avatar" placeholder="头像图片地址" />
        </div>
        <button type="submit" :disabled="submitting" class="btn-primary">
          {{ submitting ? '提交中...' : '提交认证申请' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { singerApi } from '../api'
import { ElMessage } from 'element-plus'

const existingApply = ref(null)
const showForm = ref(true)
const submitting = ref(false)
const form = reactive({
  realName: '', idCard: '', stageName: '', intro: '', gender: 0, country: '', birthday: '', avatar: ''
})

const statusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '未知')
const formatDate = (t) => t ? new Date(t).toLocaleDateString() : '-'

onMounted(async () => {
  try {
    const { data } = await singerApi.getMyApply()
    existingApply.value = data?.data
    if (existingApply.value?.status === 0) showForm.value = false
  } catch (e) { /* no apply yet */ }
})

const onSubmit = async () => {
  submitting.value = true
  try {
    const { data } = await singerApi.apply(form)
    if (data?.code === 200) {
      ElMessage.success('认证申请已提交，等待审核')
      existingApply.value = data.data
      showForm.value = false
    } else {
      ElMessage.error(data?.message || '提交失败')
    }
  } catch (e) { ElMessage.error('提交失败') }
  finally { submitting.value = false }
}
</script>

<style scoped>
.singer-apply { max-width: 600px; margin: 0 auto; }
.singer-apply h2 { margin-bottom: 24px; }
.card { background: #1e1e2e; border-radius: 12px; padding: 24px; margin-bottom: 20px; }
.card h3 { margin: 0 0 16px; }
.tip { color: #888; font-size: 0.85rem; margin-bottom: 16px; }
.status-card { text-align: center; }
.pending-info p { color: #fb923c; font-size: 1.1rem; }
.approved-info p { color: #4ade80; font-size: 1.1rem; }
.rejected-info p { color: #ff6b6b; margin-bottom: 12px; }
.apply-time { color: #666; font-size: 0.8rem; margin-top: 8px; }
.apply-form { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.apply-form .form-row.full { grid-column: 1 / -1; }
.form-row { display: flex; flex-direction: column; gap: 6px; }
.form-row label { font-size: 0.85rem; color: #aaa; }
.form-row input, .form-row textarea, .form-row select {
  background: #16213e; border: 1px solid #333; border-radius: 6px;
  padding: 10px 14px; color: #eee; font-size: 0.9rem;
}
.form-row input:focus, .form-row textarea:focus, .form-row select:focus { border-color: #42b983; outline: none; }
.btn-primary { background: #42b983; color: #fff; border: none; padding: 12px 24px; border-radius: 6px; cursor: pointer; font-weight: 600; grid-column: 1 / -1; font-size: 1rem; }
.btn-primary:disabled { opacity: 0.5; }
</style>
