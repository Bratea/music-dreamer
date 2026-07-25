<template>
  <div class="login-page">
    <div class="login-ambient">
      <div class="login-orb orb-1"></div>
      <div class="login-orb orb-2"></div>
    </div>

    <div class="login-card glass-strong">
      <div class="login-brand">
        <div class="login-mark"></div>
        <h1>Music Dreamer</h1>
        <p>悦享音乐 · 随心而动</p>
      </div>

      <!-- Tab Switch -->
      <div class="login-tabs">
        <button :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>

      <!-- Login Form -->
      <form v-if="mode === 'login'" @submit.prevent="onLogin" class="login-form">
        <div class="form-group">
          <label>用户名 / 邮箱</label>
          <input v-model="loginForm.username" required placeholder="请输入用户名或邮箱" autocomplete="username" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="loginForm.password" type="password" required placeholder="请输入密码" autocomplete="current-password" />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <!-- Register Form -->
      <form v-else @submit.prevent="onRegister" class="login-form">
        <div class="form-group">
          <label>用户名 *</label>
          <input v-model="registerForm.username" required placeholder="3-20 位字母数字" autocomplete="username" />
        </div>
        <div class="form-group">
          <label>邮箱 *</label>
          <input v-model="registerForm.email" type="email" required placeholder="example@mail.com" autocomplete="email" />
        </div>
        <div class="form-group">
          <label>密码 *</label>
          <input v-model="registerForm.password" type="password" required placeholder="至少 6 位" autocomplete="new-password" />
        </div>
        <div class="form-group">
          <label>确认密码 *</label>
          <input v-model="registerForm.confirmPassword" type="password" required placeholder="再次输入密码" autocomplete="new-password" />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'

const router = useRouter()
const mode = ref('login')
const loading = ref(false)
const error = ref('')

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', email: '', password: '', confirmPassword: '' })

const onLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    const data = await authApi.login(loginForm)
    if (data?.code === 200) {
      localStorage.setItem('token', data.data.token)
      localStorage.setItem('refreshToken', data.data.refreshToken)
      window.location.href = '/'
    } else {
      error.value = data?.message || '登录失败'
    }
  } catch (e) {
    error.value = e?.response?.data?.message || '用户名或密码错误'
  } finally {
    loading.value = false
  }
}

const onRegister = async () => {
  error.value = ''
  if (registerForm.password !== registerForm.confirmPassword) {
    error.value = '两次密码不一致'
    return
  }
  loading.value = true
  try {
    const { confirmPassword, ...formData } = registerForm
    const data = await authApi.register(formData)
    if (data?.code === 200) {
      // 注册成功后自动登录
      const loginData = await authApi.login({ username: registerForm.username, password: registerForm.password })
      if (loginData?.code === 200) {
        localStorage.setItem('token', loginData.data.token)
        localStorage.setItem('refreshToken', loginData.data.refreshToken)
        window.location.href = '/'
      }
    } else {
      error.value = data?.message || '注册失败'
    }
  } catch (e) {
    error.value = e?.response?.data?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #080914;
  overflow: hidden;
}

.login-ambient { position: absolute; inset: 0; pointer-events: none; }
.login-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}
.orb-1 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, #46f6e6, transparent);
  top: -100px; left: -100px;
  animation: float 8s ease-in-out infinite;
}
.orb-2 {
  width: 350px; height: 350px;
  background: radial-gradient(circle, #925dff, transparent);
  bottom: -80px; right: -80px;
  animation: float 10s ease-in-out infinite reverse;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, -30px); }
}

.login-card {
  position: relative;
  z-index: 1;
  width: 400px;
  padding: 2.5rem;
  border-radius: 1.5rem;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
}

.login-brand { text-align: center; margin-bottom: 2rem; }
.login-mark {
  width: 40px; height: 40px;
  border: 2.5px solid #46f6e6;
  border-radius: 8px;
  transform: rotate(45deg);
  clip-path: polygon(0 0, 100% 0, 100% 55%, 55% 55%, 55% 100%, 0 100%);
  margin: 0 auto 1rem;
}
.login-brand h1 {
  font-family: "Noto Serif SC", serif;
  font-size: 1.4rem;
  font-weight: 900;
  color: #f3f2ef;
  margin-bottom: 0.3rem;
}
.login-brand p {
  font-size: 0.75rem;
  color: #9b9daa;
  letter-spacing: 0.05em;
}

.login-tabs {
  display: flex;
  gap: 0.25rem;
  padding: 0.25rem;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 999px;
  margin-bottom: 1.5rem;
}
.login-tabs button {
  flex: 1;
  padding: 0.5rem;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 500;
  color: #9b9daa;
  transition: all 240ms ease;
}
.login-tabs button.active {
  background: #46f6e6;
  color: #080914;
  font-weight: 600;
}

.login-form { display: flex; flex-direction: column; gap: 1rem; }

.form-group { display: flex; flex-direction: column; gap: 0.35rem; }
.form-group label {
  font-size: 0.72rem;
  color: #9b9daa;
  font-weight: 500;
  letter-spacing: 0.02em;
}
.form-group input {
  padding: 0.65rem 0.85rem;
  border-radius: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.03);
  color: #f3f2ef;
  font-size: 0.82rem;
  transition: all 240ms ease;
  outline: none;
}
.form-group input:focus {
  border-color: #46f6e6;
  background: rgba(70, 246, 230, 0.04);
}
.form-group input::placeholder { color: #5a5c6e; }

.error-msg {
  font-size: 0.72rem;
  color: #ff6b6b;
  text-align: center;
  margin: 0;
}

.btn-login {
  padding: 0.7rem;
  border-radius: 0.75rem;
  background: linear-gradient(135deg, #46f6e6, #2ba89f);
  color: #080914;
  font-size: 0.85rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  transition: all 240ms ease;
  margin-top: 0.25rem;
}
.btn-login:hover:not(:disabled) {
  background: linear-gradient(135deg, #5ffff5, #36c4bb);
  box-shadow: 0 4px 20px rgba(70, 246, 230, 0.3);
  transform: translateY(-1px);
}
.btn-login:disabled { opacity: 0.6; cursor: not-allowed; }

.glass-strong {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}
</style>
