import request from '@/utils/request'

// 用户登录
export function login(data) {
  console.log('发送登录请求，数据:', JSON.stringify(data))
  console.log('API基础URL:', process.env.VUE_APP_BASE_API)

  // 添加更多请求头和调试信息
  return request({
    url: '/api/auth/login',
    method: 'post',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json, text/plain, */*',
      'X-Requested-With': 'XMLHttpRequest',
      'Cache-Control': 'no-cache',
      'Pragma': 'no-cache'
    },
    withCredentials: false, // 确保不发送cookie，以避免CORS预检请求问题
    data: data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/api/auth/info',
    method: 'get'
  })
}

// 刷新Token
export function refreshToken() {
  return request({
    url: '/api/auth/refresh',
    method: 'post'
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}
