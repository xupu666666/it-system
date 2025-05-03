import request from '@/utils/request'

// 获取网络设备列表
export function getNetworkDeviceList(params) {
  return request({
    url: '/api/network/devices',
    method: 'get',
    params
  })
}

// 获取网络设备详情
export function getNetworkDeviceDetail(id) {
  return request({
    url: `/api/network/devices/${id}`,
    method: 'get'
  })
}

// 创建网络设备
export function createNetworkDevice(data) {
  return request({
    url: '/network/devices',
    method: 'post',
    data
  })
}

// 更新网络设备
export function updateNetworkDevice(id, data) {
  return request({
    url: `/network/devices/${id}`,
    method: 'put',
    data
  })
}

// 删除网络设备
export function deleteNetworkDevice(id) {
  return request({
    url: `/network/devices/${id}`,
    method: 'delete'
  })
}

// 批量删除网络设备
export function batchDeleteNetworkDevices(ids) {
  return request({
    url: '/network/devices/batch',
    method: 'delete',
    data: { ids }
  })
}

// 获取网络拓扑数据
export function getNetworkTopology() {
  return request({
    url: '/network/topology',
    method: 'get'
  })
}

// 测试设备连接
export function testDeviceConnection(id) {
  return request({
    url: `/network/devices/${id}/test`,
    method: 'post'
  })
}

// 获取设备配置
export function getDeviceConfiguration(id) {
  return request({
    url: `/network/devices/${id}/config`,
    method: 'get'
  })
}

// 更新设备配置
export function updateDeviceConfiguration(id, data) {
  return request({
    url: `/network/devices/${id}/config`,
    method: 'put',
    data
  })
}

// 获取SSH终端会话
export function createSshSession(id) {
  return request({
    url: `/network/devices/${id}/ssh`,
    method: 'post'
  })
}

// 发送SSH命令
export function sendSshCommand(sessionId, command) {
  return request({
    url: `/network/ssh/${sessionId}/command`,
    method: 'post',
    data: { command }
  })
}

// 关闭SSH会话
export function closeSshSession(sessionId) {
  return request({
    url: `/network/ssh/${sessionId}`,
    method: 'delete'
  })
}

// 获取网络设备类型列表
export function getNetworkDeviceTypes() {
  return request({
    url: '/network/types',
    method: 'get'
  })
}

// 获取网络设备状态列表
export function getNetworkDeviceStatuses() {
  return request({
    url: '/network/statuses',
    method: 'get'
  })
}
