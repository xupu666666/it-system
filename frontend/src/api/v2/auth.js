import request from '@/utils/request'

// 用户登录
export function login(data) {
  console.log('发送登录请求 (V2)，数据:', JSON.stringify(data))

  // 尝试使用原生fetch API
  console.log('尝试使用原生fetch API发送登录请求')
  return new Promise((resolve, reject) => {
    fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
      },
      credentials: 'include',
      body: JSON.stringify(data)
    })
    .then(response => {
      console.log('收到fetch响应:', response.status, response.statusText)
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`)
      }
      return response.json()
    })
    .then(data => {
      console.log('fetch响应数据:', data)
      resolve(data)
    })
    .catch(error => {
      console.error('fetch请求失败:', error)

      // 如果fetch失败，回退到axios
      console.log('回退到axios发送请求')
      request({
        url: '/api/auth/login',
        method: 'post',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        data: data
      })
      .then(resolve)
      .catch(reject)
    })
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

// 刷新令牌
export function refreshToken() {
  return request({
    url: '/api/auth/refresh',
    method: 'post'
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

// 请求重置密码
export function requestPasswordReset(data) {
  return request({
    url: '/api/auth/request-reset-password',
    method: 'post',
    data
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/api/auth/reset-password',
    method: 'post',
    data
  })
}
