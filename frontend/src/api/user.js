import request from '@/utils/request'

// 获取用户列表
export function getUserList(params) {
  console.log('获取用户列表，参数:', params);

  // 真实API调用
  return request({
    url: '/api/system/users',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(id) {
  console.log(`获取用户详情，ID: ${id}`);

  // 真实API调用
  return request({
    url: `/api/system/users/${id}`,
    method: 'get'
  })
}

// 创建用户
export function createUser(data) {
  console.log('创建用户:', data);

  // 真实API调用
  return request({
    url: '/api/system/users',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id, data) {
  console.log(`更新用户，ID: ${id}，数据:`, data);

  // 真实API调用
  return request({
    url: `/api/system/users/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id, username) {
  console.log(`删除用户API调用，ID: ${id}, 用户名: ${username}, 类型: ${typeof id}`);

  // 确保ID是数字类型
  const userId = typeof id === 'string' ? parseInt(id) : id;
  console.log(`转换后的用户ID: ${userId}, 类型: ${typeof userId}`);

  // 真实API调用
  return request({
    url: `/api/system/users/${userId}`,
    method: 'delete',
    // 添加调试信息
    headers: {
      'X-Debug-Info': `Deleting user ${username} with ID ${userId}`
    }
  })
}

// 批量删除用户
export function batchDeleteUsers(userIds) {
  console.log('批量删除用户ID:', userIds);

  // 确保所有ID都是数字类型
  const ids = Array.isArray(userIds) ? userIds.map(id => {
    return typeof id === 'string' ? parseInt(id) : id;
  }) : userIds;

  console.log('处理后的批量删除用户ID:', ids);

  // 真实API调用
  return request({
    url: '/api/system/users/batch',
    method: 'delete',
    data: { ids: ids }
  })
}

// 重置用户密码
export function resetPassword(id, password) {
  console.log(`重置用户密码，ID: ${id}，密码: ${password}`);

  // 真实API调用
  return request({
    url: `/api/system/users/${id}/password`,
    method: 'put',
    data: { password }
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  console.log(`更新用户状态，ID: ${id}，状态: ${status}`);

  // 真实API调用
  return request({
    url: `/api/system/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 获取用户角色列表
export function getRoleList() {
  console.log('获取角色列表');

  // 真实API调用
  return request({
    url: '/api/system/roles',
    method: 'get'
  })
}

// 获取部门列表
export function getDepartmentList() {
  console.log('获取部门列表');

  // 真实API调用
  return request({
    url: '/api/system/departments',
    method: 'get'
  })
}

// 导入用户
export function importUsers(file) {
  console.log('导入用户，文件:', file);

  // 真实API调用
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/api/system/users/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 导出用户
export function exportUsers(params) {
  console.log('导出用户，参数:', params);

  // 真实API调用
  return request({
    url: '/api/system/users/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 这个函数已不再需要，因为我们使用数据库存储用户数据
// 保留此函数是为了避免可能的引用错误
export function resetLocalUsers() {
  console.log('此功能已不再需要，因为我们使用数据库存储用户数据');
  return Promise.resolve({ message: "操作已忽略" });
}
