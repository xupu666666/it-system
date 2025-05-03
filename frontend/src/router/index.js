import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/v2/auth'

// 布局组件
const Layout = () => import('@/layout/index.vue')

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/v2/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', hidden: true }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/reset-password/index.vue'),
    meta: { title: '重置密码', hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { title: '仪表盘', icon: 'el-icon-menu' },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '概览',
          icon: 'el-icon-data-board'
        }
      },
      {
        path: 'reports',
        name: 'DashboardReports',
        component: () => import('@/views/dashboard/reports.vue'),
        meta: {
          title: '数据报表',
          icon: 'el-icon-pie-chart'
        }
      }
    ]
  },
  // 维修费用模块
  {
    path: '/maintenance',
    component: Layout,
    redirect: '/maintenance/orders',
    meta: { title: '维修费用', icon: 'el-icon-s-tools' },
    children: [
      {
        path: 'orders',
        name: 'MaintenanceOrders',
        component: () => import('@/views/maintenance/orders/index.vue'),
        meta: {
          title: '维修单管理',
          icon: 'el-icon-document',
          permissions: ['maintenance.view']
        }
      },
      {
        path: 'statistics',
        name: 'MaintenanceStatistics',
        component: () => import('@/views/maintenance/statistics/index.vue'),
        meta: {
          title: '费用统计',
          icon: 'el-icon-data-line',
          permissions: ['maintenance.view']
        }
      },

    ]
  },
  // 资产盘点模块
  {
    path: '/inventory',
    component: Layout,
    redirect: '/inventory/items',
    meta: { title: '资产盘点', icon: 'el-icon-s-goods' },
    children: [
      {
        path: 'items',
        name: 'InventoryItems',
        component: () => import('@/views/inventory/items/index.vue'),
        meta: {
          title: '资产列表',
          icon: 'el-icon-tickets'
        }
      },
      {
        path: 'check',
        name: 'InventoryCheck',
        component: () => import('@/views/inventory/check/index.vue'),
        meta: {
          title: '资产盘点',
          icon: 'el-icon-check'
          // 移除权限限制，便于测试
          // permissions: ['inventory.check']
        }
      },
      {
        path: 'report',
        name: 'InventoryReport',
        component: () => import('@/views/inventory/report/index.vue'),
        meta: {
          title: '资产报表',
          icon: 'el-icon-s-data',
          permissions: ['inventory.view']
        }
      }
    ]
  },
  // 网络设备模块
  {
    path: '/network',
    component: Layout,
    redirect: '/network/devices',
    meta: { title: '网络设备', icon: 'el-icon-connection' },
    children: [
      {
        path: 'devices',
        name: 'NetworkDevices',
        component: () => import('@/views/network/devices/index.vue'),
        meta: {
          title: '设备管理',
          icon: 'el-icon-cpu',
          permissions: ['network.view']
        }
      },
      {
        path: 'topology',
        name: 'NetworkTopology',
        component: () => import('@/views/network/topology/index.vue'),
        meta: {
          title: '网络拓扑',
          icon: 'el-icon-share',
          permissions: ['network.topology']
        }
      },
      {
        path: 'terminal',
        name: 'NetworkTerminal',
        component: () => import('@/views/network/terminal/index.vue'),
        meta: {
          title: 'SSH终端',
          icon: 'el-icon-terminal',
          permissions: ['network.terminal']
        }
      }
    ]
  },
  // 物品领用模块
  {
    path: '/supplies',
    component: Layout,
    redirect: '/supplies/requests',
    meta: { title: '物品领用', icon: 'el-icon-box' },
    children: [
      {
        path: 'requests',
        name: 'SuppliesRequests',
        component: () => import('@/views/supplies/requests/index.vue'),
        meta: {
          title: '领用申请',
          icon: 'el-icon-shopping-cart-full',
          permissions: ['supplies.view']
        }
      },
      {
        path: 'inventory',
        name: 'SuppliesInventory',
        component: () => import('@/views/supplies/inventory/index.vue'),
        meta: {
          title: '物品库存',
          icon: 'el-icon-goods',
          permissions: ['supplies.view']
        }
      },
      {
        path: 'return',
        name: 'SuppliesReturn',
        component: () => import('@/views/supplies/return/index.vue'),
        meta: {
          title: '物品归还',
          icon: 'el-icon-sell',
          permissions: ['supplies.view']
        }
      }
    ]
  },
  // 系统管理模块
  {
    path: '/system',
    component: Layout,
    redirect: '/system/users',
    meta: { title: '系统管理', icon: 'el-icon-setting' },
    children: [
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/system/users/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'el-icon-user',
          permissions: ['system.user.view']
        }
      }
    ]
  },
  {
    path: '/404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
]

// 先注释掉一些不存在的路由组件
// {
//   path: 'roles',
//   name: 'Roles',
//   component: () => import('@/views/system/roles/index.vue'),
//   meta: {
//     title: '角色管理',
//     icon: 'el-icon-s-check',
//     permissions: ['system.role.view']
//   }
// },
// {
//   path: 'menus',
//   name: 'Menus',
//   component: () => import('@/views/system/menus/index.vue'),
//   meta: {
//     title: '菜单管理',
//     icon: 'el-icon-menu',
//     permissions: ['system.menu.view']
//   }
// },
// {
//   path: 'logs',
//   name: 'Logs',
//   component: () => import('@/views/system/logs/index.vue'),
//   meta: {
//     title: '操作日志',
//     icon: 'el-icon-document',
//     permissions: ['system.log.view']
//   }
// }

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - ICT系统管理平台` : 'ICT系统管理平台'

  // 白名单路由，不需要登录即可访问
  const whiteList = ['/login', '/register', '/reset-password', '/404']

  // 从本地存储中获取token
  const token = localStorage.getItem('token_v2')

  if (token) {
    // 已登录状态
    if (to.path === '/login') {
      // 如果已登录且要访问登录页，重定向到首页
      next({ path: '/' })
    } else {
      // 判断是否有用户信息
      const hasUserInfo = localStorage.getItem('userInfo_v2')

      if (hasUserInfo) {
        // 如果有用户信息，直接放行
        next()
      } else {
        try {
          // 如果没有用户信息，获取用户信息
          const authStore = useAuthStore()
          await authStore.getUserInfo()

          // 获取用户信息成功，放行
          next()
        } catch (error) {
          // 获取用户信息失败，清除token并重定向到登录页
          const authStore = useAuthStore()
          await authStore.logout()
          next(`/login?redirect=${to.path}`)
        }
      }
    }
  } else {
    // 未登录状态
    if (whiteList.includes(to.path)) {
      // 白名单路由，直接放行
      next()
    } else {
      // 非白名单路由，重定向到登录页
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router