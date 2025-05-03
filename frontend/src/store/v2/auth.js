import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { authApi } from '@/api/v2'

// 用户认证状态存储 (V2版本)
export const useAuthStore = defineStore('authV2', {
  state: () => ({
    token: localStorage.getItem('token_v2') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo_v2') || '{}'),
    permissions: JSON.parse(localStorage.getItem('permissions_v2') || '[]'),
    roles: JSON.parse(localStorage.getItem('roles_v2') || '[]'),
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
      // 超级管理员默认拥有所有权限（兼容大小写）
      if (state.roles.some(role => role.toUpperCase() === 'ADMIN')) {
        return true
      }
      return state.permissions.includes(permission)
    },

    // 检查是否拥有指定权限中的任意一个
    hasAnyPermission: (state) => (permissionList) => {
      // 超级管理员默认拥有所有权限（兼容大小写）
      if (state.roles.some(role => role.toUpperCase() === 'ADMIN')) {
        return true
      }
      return permissionList.some(permission => state.permissions.includes(permission))
    },

    // 检查是否拥有指定角色
    hasRole: (state) => (role) => {
      return state.roles.includes(role)
    },

    // 检查是否拥有指定角色中的任意一个
    hasAnyRole: (state) => (roleList) => {
      return roleList.some(role => state.roles.includes(role))
    }
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
        localStorage.removeItem('token_v2')
        localStorage.removeItem('userInfo_v2')
        localStorage.removeItem('roles_v2')
        localStorage.removeItem('permissions_v2')

        // 跳转到登录页
        router.push('/login')
      }
    },

    // 设置Token
    setToken(token) {
      this.token = token
      localStorage.setItem('token_v2', token)
    },

    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo_v2', JSON.stringify(userInfo))
    },

    // 设置角色
    setRoles(roles) {
      this.roles = roles
      localStorage.setItem('roles_v2', JSON.stringify(roles))
    },

    // 设置权限
    setPermissions(permissions) {
      this.permissions = permissions
      localStorage.setItem('permissions_v2', JSON.stringify(permissions))
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
