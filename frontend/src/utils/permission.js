import { useAuthStore } from '@/store/auth'

/**
 * 权限指令 v-permission
 * 用法：
 * 1. 单个权限: v-permission="'permission.name'"
 * 2. 多个权限(满足任意一个): v-permission="{ any: ['permission.name1', 'permission.name2'] }"
 * 3. 多个权限(必须全部满足): v-permission="{ all: ['permission.name1', 'permission.name2'] }"
 */
export const permission = {
  mounted(el, binding) {
    const authStore = useAuthStore()
    const value = binding.value

    let hasPermission = false

    if (typeof value === 'string') {
      // 单个权限
      hasPermission = authStore.hasPermission(value)
    } else if (value && typeof value === 'object') {
      // 多个权限
      if (value.any && Array.isArray(value.any)) {
        hasPermission = authStore.hasAnyPermission(value.any)
      } else if (value.all && Array.isArray(value.all)) {
        hasPermission = authStore.hasAllPermissions(value.all)
      }
    }

    if (!hasPermission) {
      if (el.parentNode) {
        el.parentNode.removeChild(el)
      } else {
        el.style.display = 'none'
      }
    }
  }
}

/**
 * 角色指令 v-role
 * 用法：
 * 1. 单个角色: v-role="'admin'"
 * 2. 多个角色(满足任意一个): v-role="{ any: ['admin', 'manager'] }"
 * 3. 多个角色(必须全部满足): v-role="{ all: ['admin', 'manager'] }"
 */
export const role = {
  mounted(el, binding) {
    const authStore = useAuthStore()
    const roles = authStore.getUserRoles
    const value = binding.value

    let hasRole = false

    if (typeof value === 'string') {
      // 单个角色
      hasRole = roles.includes(value)
    } else if (value && typeof value === 'object') {
      // 多个角色
      if (value.any && Array.isArray(value.any)) {
        hasRole = value.any.some(role => roles.includes(role))
      } else if (value.all && Array.isArray(value.all)) {
        hasRole = value.all.every(role => roles.includes(role))
      }
    }

    if (!hasRole) {
      if (el.parentNode) {
        el.parentNode.removeChild(el)
      } else {
        el.style.display = 'none'
      }
    }
  }
}

// 权限检查函数
export const checkPermission = (permission) => {
  const authStore = useAuthStore()
  return authStore.hasPermission(permission)
}

// 角色检查函数
export const checkRole = (role) => {
  const authStore = useAuthStore()
  return authStore.getUserRoles.includes(role)
}

// 导出权限指令和角色指令
export default {
  install(app) {
    app.directive('permission', permission)
    app.directive('role', role)
  }
} 