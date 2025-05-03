<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapse': isCollapse }">
      <div class="logo-container">
        <img src="@/assets/logo.png" class="sidebar-logo" />
        <h1 class="sidebar-title" v-if="!isCollapse">ICT 系统管理</h1>
      </div>

      <!-- 侧边菜单 - 使用自定义菜单组件 -->
      <el-scrollbar>
        <custom-menu
          :routes="routes"
          :is-collapse="isCollapse"
        />
      </el-scrollbar>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <div class="navbar">
        <div class="navbar-left">
          <el-icon class="toggle-sidebar" @click="toggleSidebar">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <breadcrumb />
        </div>

        <div class="navbar-right">
          <el-dropdown trigger="click">
            <div class="avatar-container">
              <el-avatar :size="30" :src="userInfo.avatar || defaultAvatar" />
              <span class="user-name">{{ userInfo.name || '管理员' }}</span>
              <span class="admin-icon">👑</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handlePersonalInfo">个人信息</el-dropdown-item>
                <el-dropdown-item @click="handleChangePassword">修改密码</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容主区域 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import CustomMenu from './components/CustomMenu.vue'
import Breadcrumb from './components/Breadcrumb.vue'
// 使用logo.png替代缺失的default-avatar.png
import defaultAvatar from '@/assets/logo.png'
import { useAuthStore } from '@/store/v2/auth'

const router = useRouter()

// 侧边栏状态
const isCollapse = ref(false)
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 注意：activeMenu 已移至 CustomMenu 组件中使用

// 获取路由表中的菜单项，过滤掉隐藏的路由
const routes = computed(() => {
  return router.options.routes.filter(route => {
    return !route.meta?.hidden
  })
})

// 获取用户信息从状态管理中
const authStore = useAuthStore()
const userInfo = computed(() => {
  // 调试输出
  console.log('当前用户信息:', authStore.currentUser)
  console.log('当前用户角色:', authStore.roles)
  return authStore.currentUser || { name: '管理员', avatar: '' }
})

// 注意：由于我们现在使用 emoji 图标，不再需要判断管理员身份

// 个人信息
const handlePersonalInfo = () => {
  // 实现跳转到个人信息页面的逻辑
}

// 修改密码
const handleChangePassword = () => {
  // 实现修改密码的逻辑
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    await authStore.logout()
  }).catch(() => {})
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  width: 240px;
  height: 100%;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 1001;
  overflow: hidden;
  transition: all 0.3s ease;
  background: linear-gradient(180deg, #1E293B 0%, #0F172A 100%);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);

  /* 修复菜单样式 */
  :deep(.el-scrollbar__wrap) {
    overflow-x: hidden;
  }

  :deep(.sidebar-menu) {
    border-right: none;
    width: 100%;

    &.el-menu--collapse {
      width: 70px;
    }
  }

  :deep(.el-sub-menu__title) {
    &:hover {
      background-color: rgba(255, 255, 255, 0.05) !important;
    }
  }

  :deep(.el-menu-item) {
    &:hover {
      background-color: rgba(255, 255, 255, 0.05) !important;
    }

    &.is-active {
      background-color: rgba(59, 130, 246, 0.1) !important;
    }
  }
}

.sidebar-container.is-collapse {
  width: 70px;

  .sidebar-logo {
    margin-right: 0;
  }
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.sidebar-logo {
  width: 36px;
  height: 36px;
  border-radius: var(--border-radius-md);
  margin-right: 12px;
  transition: all 0.3s;
}

.sidebar-title {
  display: inline-block;
  margin: 0;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  letter-spacing: 0.5px;
  transition: opacity 0.3s;
}

.main-container {
  min-height: 100%;
  margin-left: 240px;
  position: relative;
  transition: all 0.3s ease;
  width: calc(100% - 240px);
  background-color: var(--background-color);
}

.sidebar-container.is-collapse + .main-container {
  margin-left: 70px;
  width: calc(100% - 70px);
}

.navbar {
  height: 64px;
  overflow: hidden;
  position: relative;
  background: var(--background-light);
  box-shadow: var(--shadow-md);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid var(--border-light);
}

.navbar-left {
  display: flex;
  align-items: center;
}

.toggle-sidebar {
  font-size: 22px;
  cursor: pointer;
  margin-right: 20px;
  color: var(--text-regular);
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--border-radius-md);
  transition: all 0.3s;

  &:hover {
    background-color: var(--background-dark);
    color: var(--primary-color);
  }
}

.avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--border-radius-md);
  transition: all 0.3s;

  &:hover {
    background-color: var(--background-dark);
  }
}

.user-name {
  margin-left: 10px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.admin-icon {
  margin-left: 8px;
  font-size: 18px;
  color: #F59E0B;
  filter: drop-shadow(0 0 2px rgba(245, 158, 11, 0.5));
  animation: shine 2s infinite;
}

@keyframes shine {
  0% { opacity: 0.7; }
  50% { opacity: 1; }
  100% { opacity: 0.7; }
}

.app-main {
  padding: 24px;
  min-height: calc(100vh - 64px);
  background-color: var(--background-color);
  overflow: auto;
}

/* 路由过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>