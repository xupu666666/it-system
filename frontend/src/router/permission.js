import router from './index'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

// 免登录白名单
const whiteList = ['/login', '/404']

// 路由权限映射
const routePermissionMap = {
  '/maintenance/orders': 'maintenance.view',
  '/maintenance/statistics': 'maintenance.view',

  '/inventory/items': 'inventory.view',
  '/inventory/check': 'inventory.check',
  '/inventory/report': 'inventory.view',

  '/network/devices': 'network.view',
  '/network/topology': 'network.topology',
  '/network/terminal': 'network.terminal',
  '/network/ipam': 'network.view',

  '/supplies/requests': 'supplies.view',
  '/supplies/inventory': 'supplies.view',
  '/supplies/purchase': 'supplies.view',
  '/supplies/return': 'supplies.view',

  '/system/users': 'system.user',
  '/system/departments': 'system.department',
  '/system/roles': 'system.role'
}

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 检查用户是否已登录
  const hasToken = authStore.token

  if (hasToken) {
    if (to.path === '/login') {
      // 已登录用户重定向到首页
      next({ path: '/' })
    } else {
      // 判断用户角色和权限是否已加载
      const hasRoles = authStore.roles && authStore.roles.length > 0

      if (hasRoles) {
        // 检查路由权限
        if (checkRoutePermission(to.path, authStore)) {
          next()
        } else {
          ElMessage.error('您没有访问该页面的权限')
          next('/404')
        }
      } else {
        try {
          // 刷新Token（如有需要）
          await authStore.checkAndRefreshToken()

          // 检查路由权限
          if (checkRoutePermission(to.path, authStore)) {
            next()
          } else {
            ElMessage.error('您没有访问该页面的权限')
            next('/404')
          }
        } catch (error) {
          // Token校验失败，重定向到登录页
          authStore.logout()
          ElMessage.error('登录状态已失效，请重新登录')
          next('/login')
        }
      }
    }
  } else {
    // 未登录用户
    if (whiteList.includes(to.path)) {
      // 白名单路由直接访问
      next()
    } else {
      // 重定向到登录页
      next('/login')
    }
  }
})

// 检查路由权限
function checkRoutePermission(path, authStore) {
  // 首页和白名单页面无需权限
  if (path === '/' || path === '/dashboard' || whiteList.includes(path)) {
    return true
  }

  // 检查是否为系统管理员（拥有所有权限）
  if (authStore.roles.includes('admin')) {
    return true
  }

  // 检查路径权限
  const requiredPermission = routePermissionMap[path]
  if (requiredPermission) {
    return authStore.hasPermission(requiredPermission)
  }

  // 未设置权限的路由默认允许访问
  return true
}

export default router