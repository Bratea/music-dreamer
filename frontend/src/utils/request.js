import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  paramsSerializer: params => {
    const filtered = Object.fromEntries(
      Object.entries(params).filter(([, v]) => v != null && v !== '')
    );
    return new URLSearchParams(filtered).toString();
  },
})

// Attach token automatically
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Refresh token on 401 (avoid infinite loop with _retry flag)
request.interceptors.response.use(
  res => res.data,
  async err => {
    const originalConfig = err.config
    if (err.response?.status === 401 && !originalConfig._retry) {
      originalConfig._retry = true
      const refresh = localStorage.getItem('refreshToken')
      if (refresh) {
        try {
          const resp = await axios.post('/api/auth/refresh', { refreshToken: refresh })
          // CommonResult 结构: { code, message, data: { token, refreshToken, ... } }
          const loginData = resp?.data?.data || resp?.data
          const newToken = loginData?.token
          const newRefresh = loginData?.refreshToken || refresh
          if (newToken) {
            localStorage.setItem('token', newToken)
            localStorage.setItem('refreshToken', newRefresh)
            originalConfig.headers.Authorization = `Bearer ${newToken}`
            return request(originalConfig)
          }
        } catch {
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          // 路由使用 history 模式，应跳转到 /login 而非 /#/login
          window.location.href = '/login'
          return Promise.reject(err)
        }
      } else {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

export default request
