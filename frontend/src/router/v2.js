import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/v2/auth'
import { ElMessage } from 'element-plus'

// 布局组件
const Layout = () => import('@/layout/index.vue')

// 路由配置
const routes = [
  {
    path: '/login-v2',
    name: 'LoginV2',
    component: () => import('@/views/login/v2/index.vue'),
    meta: { title: '登录 V2', hidden: true }
  },
  {
    path: '/v2',
    component: Layout,
    redirect: '/v2/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'DashboardV2',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘 V2', icon: 'el-icon-menu' }
      }
    ]
  },
  // 系统管理模块
  {
    path: '/v2/system',
    component: Layout,
    redirect: '/v2/system/users',
    meta: { title: '系统管理 V2', icon: 'el-icon-setting' },
    children: [
      {
        path: 'users',
        name: 'UsersV2',
        component: () => import('@/views/system/users/index.vue'),
        meta: {
          title: '用户管理 V2',
          icon: 'el-icon-user',
          permissions: ['system.user.view']
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - ICT系统管理平台` : 'ICT系统管理平台'

  // 白名单路由，不需要登录即可访问
  const whiteList = ['/login-v2', '/404']

  // 从本地存储中获取token
  const token = localStorage.getItem('token_v2')

  if (token) {
    // 已登录状态
    if (to.path === '/login-v2') {
      // 如果已登录且要访问登录页，重定向到首页
      next({ path: '/v2' })
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
          ElMessage.error('登录状态已失效，请重新登录')
          next(`/login-v2?redirect=${to.path}`)
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
      next(`/login-v2?redirect=${to.path}`)
    }
  }
})

export default router
