import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { authApi } from '@/api'

// 定义权限列表
const permissions = {
  // 维修费用模块权限
  'maintenance.view': '查看维修单列表',
  'maintenance.add': '新增维修单',
  'maintenance.edit': '编辑维修单',
  'maintenance.delete': '删除维修单',
  'maintenance.export': '导出维修单',
  'maintenance.update-status': '更新维修单状态',

  // 资产盘点模块权限
  'inventory.view': '查看资产列表',
  'inventory.add': '新增资产',
  'inventory.edit': '编辑资产',
  'inventory.delete': '删除资产',
  'inventory.import': '导入资产',
  'inventory.export': '导出资产',
  'inventory.check': '资产盘点',

  // 网络设备模块权限
  'network.view': '查看网络设备',
  'network.add': '新增网络设备',
  'network.edit': '编辑网络设备',
  'network.delete': '删除网络设备',
  'network.topology': '查看网络拓扑',
  'network.terminal': '使用SSH终端',

  // 物品领用模块权限
  'supplies.view': '查看物品领用',
  'supplies.apply': '申请物品领用',
  'supplies.approve': '审批物品领用',
  'supplies.return': '物品归还',

  // 系统管理模块权限
  'system.user': '用户管理',
  'system.role': '角色权限管理',
  'system.department': '部门管理',
  'system.log': '系统日志'
}

// 定义角色权限模板
const rolePermissionTemplates = {
  'admin': [ // 系统管理员，拥有所有权限
    ...Object.keys(permissions)
  ],
  'manager': [ // 部门经理，拥有大部分权限，但不能管理系统
    'maintenance.view', 'maintenance.add', 'maintenance.edit', 'maintenance.update-status',
    'inventory.view', 'inventory.check', 'inventory.export',
    'network.view', 'network.topology',
    'supplies.view', 'supplies.apply', 'supplies.approve',
  ],
  'it_staff': [ // IT人员，专注于网络设备和资产管理
    'maintenance.view', 'maintenance.add', 'maintenance.edit', 'maintenance.update-status',
    'inventory.view', 'inventory.add', 'inventory.edit', 'inventory.check',
    'network.view', 'network.add', 'network.edit', 'network.topology', 'network.terminal',
    'supplies.view', 'supplies.apply',
  ],
  'general_staff': [ // 普通员工，只有基本查看和申请权限
    'maintenance.view', 'maintenance.add',
    'inventory.view',
    'supplies.view', 'supplies.apply', 'supplies.return',
  ]
}

// 用户认证状态存储
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]'),
    roles: JSON.parse(localStorage.getItem('roles') || '[]'),
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,

    // 获取用户信息
    currentUser: (state) => state.userInfo,

    // 获取用户角色
    getUserRoles: (state) => state.roles,

    // 获取用户权限列表
    getUserPermissions: (state) => state.permissions,

    // 检查是否有某项权限
    hasPermission: (state) => (permission) => {
      // 超级管理员默认拥有所有权限
      if (state.roles.includes('admin')) {
        return true
      }
      return state.permissions.includes(permission)
    },

    // 检查是否拥有指定权限中的任意一个
    hasAnyPermission: (state) => (permissionList) => {
      // 超级管理员默认拥有所有权限
      if (state.roles.includes('admin')) {
        return true
      }
      return permissionList.some(permission => state.permissions.includes(permission))
    },

    // 检查是否拥有指定权限中的所有权限
    hasAllPermissions: (state) => (permissionList) => {
      // 超级管理员默认拥有所有权限
      if (state.roles.includes('admin')) {
        return true
      }
      return permissionList.every(permission => state.permissions.includes(permission))
    },

    // 获取所有权限列表（用于管理界面展示）
    getAllPermissions: () => {
      return Object.entries(permissions).map(([key, label]) => ({ key, label }))
    },

    // 获取角色权限模板
    getRolePermissionTemplates: () => rolePermissionTemplates
  },

  actions: {
    // 登录
    async login(username, password) {
      try {
        // 调用登录API
        const response = await authApi.login({ username, password })

        // 保存认证信息
        this.setToken(response.token)
        this.setUserInfo(response.userInfo)
        this.setRoles(response.roles)
        this.setPermissions(response.permissions)

        return true
      } catch (error) {
        ElMessage.error('登录失败：' + (error.message || '未知错误'))
        return false
      }
    },

    // 获取用户信息
    async getUserInfo() {
      try {
        const response = await authApi.getUserInfo()

        // 更新用户信息
        this.setUserInfo(response.userInfo)
        this.setRoles(response.roles)
        this.setPermissions(response.permissions)

        return response
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return null
      }
    },

    // 退出登录
    async logout() {
      try {
        // 调用退出登录API
        await authApi.logout()
      } catch (error) {
        console.error('退出登录失败:', error)
      } finally {
        // 无论API是否成功，都清除本地状态
        this.token = ''
        this.userInfo = {}
        this.roles = []
        this.permissions = []

        // 清除本地存储
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('roles')
        localStorage.removeItem('permissions')

        // 跳转到登录页
        router.push('/login')
      }
    },

    // 设置Token
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    // 设置角色
    setRoles(roles) {
      this.roles = roles
      localStorage.setItem('roles', JSON.stringify(roles))
    },

    // 设置权限
    setPermissions(permissions) {
      this.permissions = permissions
      localStorage.setItem('permissions', JSON.stringify(permissions))
    },

    // 检查并刷新Token
    async checkAndRefreshToken() {
      try {
        // 如果token不存在，直接返回false
        if (!this.token) {
          return false
        }

        // 调用刷新Token API
        const response = await authApi.refreshToken()

        // 更新token
        this.setToken(response.token)
        return true
      } catch (error) {
        console.error('刷新Token失败:', error)
        // Token刷新失败，清除登录状态
        this.logout()
        return false
      }
    }
  }
})