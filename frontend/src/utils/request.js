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

// Refresh token on 401
request.interceptors.response.use(
  res => res.data,
  async err => {
    if (err.response?.status === 401) {
      const refresh = localStorage.getItem('refreshToken')
      if (refresh) {
        try {
          const { data } = await axios.post('/api/auth/refresh', { refreshToken: refresh })
          const { token } = data.data
          localStorage.setItem('token', token)
          err.config.headers.Authorization = `Bearer ${token}`
          return request(err.config)
        } catch {
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          window.location.href = '/#/login'
        }
      }
    }
    return Promise.reject(err)
  }
)

export default request
