import request from '@/utils/request'

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/api/system/users',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(id) {
  return request({
    url: `/api/system/users/${id}`,
    method: 'get'
  })
}

// 创建用户
export function createUser(data) {
  return request({
    url: '/api/system/users',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id, data) {
  return request({
    url: `/api/system/users/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  console.log('删除用户API调用，ID:', id, '类型:', typeof id)
  return request({
    url: `/api/system/users/${id}`,
    method: 'delete'
  })
}

// 批量删除用户
export function batchDeleteUsers(ids) {
  console.log('批量删除用户API调用，IDs:', ids)
  return request({
    url: '/api/system/users/batch',
    method: 'delete',
    data: { ids }
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/api/system/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 重置用户密码
export function resetUserPassword(id, password) {
  return request({
    url: `/api/system/users/${id}/password`,
    method: 'put',
    data: { password }
  })
}
