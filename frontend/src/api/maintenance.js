import request from '@/utils/request'

// 获取维修单列表
export function getMaintenanceList(params) {
  console.log('调用获取维修单列表API，参数:', params)
  return request({
    url: '/api/maintenance/orders',
    method: 'get',
    params
  }).then(response => {
    console.log('获取维修单列表API响应:', response)
    return response
  }).catch(error => {
    console.error('获取维修单列表API错误:', error)
    throw error
  })
}

// 获取维修单详情
export function getMaintenanceDetail(id) {
  return request({
    url: `/api/maintenance/orders/${id}`,
    method: 'get'
  })
}

// 创建维修单
export function createMaintenanceOrder(data) {
  console.log('创建维修单，数据:', JSON.stringify(data))
  return request({
    url: '/api/maintenance/orders',
    method: 'post',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json, text/plain, */*'
    },
    data
  })
}

// 更新维修单
export function updateMaintenanceOrder(id, data) {
  return request({
    url: `/api/maintenance/orders/${id}`,
    method: 'put',
    data
  })
}

// 删除维修单
export function deleteMaintenanceOrder(id) {
  return request({
    url: `/api/maintenance/orders/${id}`,
    method: 'delete'
  })
}

// 批量删除维修单
export function batchDeleteMaintenanceOrders(ids) {
  return request({
    url: '/api/maintenance/orders/batch',
    method: 'delete',
    data: { ids }
  })
}

// 更新维修单状态
export function updateMaintenanceStatus(id, status, comment, cost) {
  const data = {
    status,
    comment
  }

  // 如果提供了维修金额，则添加到请求数据中
  if (cost !== undefined) {
    data.cost = cost
  }

  return request({
    url: `/api/maintenance/orders/${id}/status`,
    method: 'put',
    data
  })
}

// 获取维修历史
export function getMaintenanceHistory(assetId) {
  return request({
    url: `/api/maintenance/history/${assetId}`,
    method: 'get'
  })
}

// 获取维修统计数据
export function getMaintenanceStatistics(params) {
  return request({
    url: '/api/maintenance/statistics',
    method: 'get',
    params
  })
}

// 导出维修单
export function exportMaintenanceOrders(params) {
  console.log('导出维修单，参数:', params);
  return request({
    url: '/api/maintenance/orders/export',
    method: 'get',
    params,
    responseType: 'blob',
    // 添加超时时间，导出大文件可能需要更长时间
    timeout: 60000,
    // 添加错误处理
    validateStatus: function (status) {
      return status >= 200 && status < 500; // 默认只接受2xx的状态码，这里扩展到接受所有非500错误
    }
  })
}

// 获取维修类型列表
export function getMaintenanceTypes() {
  return request({
    url: '/api/maintenance/types',
    method: 'get'
  })
}

// 获取维修状态列表
export function getMaintenanceStatuses() {
  return request({
    url: '/api/maintenance/statuses',
    method: 'get'
  })
}

// 获取所有维修单用过的下拉选项
export function getMaintenanceOrderFilters() {
  return request({
    url: '/api/maintenance/orders/filters',
    method: 'get'
  })
}
