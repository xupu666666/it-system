import request from '@/utils/request'

// 获取物品申请列表
export function getSupplyRequestList(params) {
  return request({
    url: '/api/supplies/requests',
    method: 'get',
    params
  })
}

// 获取物品申请详情
export function getSupplyRequestDetail(id) {
  return request({
    url: `/api/supplies/requests/${id}`,
    method: 'get'
  })
}

// 创建物品申请
export function createSupplyRequest(data) {
  return request({
    url: '/api/supplies/requests',
    method: 'post',
    data
  })
}

// 更新物品申请
export function updateSupplyRequest(id, data) {
  return request({
    url: `/api/supplies/requests/${id}`,
    method: 'put',
    data
  })
}

// 删除物品申请
export function deleteSupplyRequest(id) {
  return request({
    url: `/api/supplies/requests/${id}`,
    method: 'delete'
  })
}

// 审批物品申请
export function approveSupplyRequest(id, approved, comment) {
  return request({
    url: `/api/supplies/requests/${id}/approve`,
    method: 'put',
    data: {
      approved,
      comment
    }
  })
}

// 物品归还
export function returnSupplyItems(id, items) {
  return request({
    url: `/api/supplies/requests/${id}/return`,
    method: 'put',
    data: { items }
  })
}

// 获取物品库存列表
export function getSupplyInventoryList(params) {
  return request({
    url: '/api/supplies/inventory',
    method: 'get',
    params
  })
}

// 更新物品库存
export function updateSupplyInventory(id, data) {
  return request({
    url: `/api/supplies/inventory/${id}`,
    method: 'put',
    data
  })
}

// 添加物品库存
export function addSupplyInventory(data) {
  return request({
    url: '/api/supplies/inventory',
    method: 'post',
    data
  })
}

// 获取物品类型列表
export function getSupplyTypes() {
  return request({
    url: '/api/supplies/types',
    method: 'get'
  })
}

// 获取物品申请状态列表
export function getSupplyRequestStatuses() {
  return request({
    url: '/api/supplies/statuses',
    method: 'get'
  })
}
