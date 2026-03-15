import axios from 'axios'
import { useUserStore } from '../stores/user'
import router from '../router'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    // 处理统一的 ApiResponse 格式
    const res = response.data
    if (res && res.code !== undefined) {
      // 如果返回了 ApiResponse 格式
      if (res.code === 200) {
        // 成功，提取 data 字段返回
        return res.data !== undefined ? res.data : res
      } else {
        // 业务错误，抛出异常
        const error = new Error(res.message || '请求失败')
        error.response = { data: res, status: res.code }
        return Promise.reject(error)
      }
    }
    // 旧格式直接返回
    return response
  },
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default api
