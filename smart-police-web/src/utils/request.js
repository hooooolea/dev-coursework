import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截：自动附加 Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截：统一处理错误
request.interceptors.response.use(
  res => {
    // blob/arraybuffer 流直接透传，不做 JSON 解析
    if (res.config.responseType === 'blob' || res.config.responseType === 'arraybuffer') {
      return res.data
    }
    const data = res.data
    if (data.code === 200) return data
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(data)
  },
  err => {
    const status = err.response?.status
    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('accessToken')
      router.push('/login')
    } else if (status === 403) {
      ElMessage.error('无权限访问')
      router.push('/403')
    } else {
      ElMessage.error(err.response?.data?.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
