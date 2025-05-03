<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container" :class="{ 'is-collapsed': isCollapse }">
      <div class="logo-container">
        <img src="@/assets/logo.png" alt="Logo" class="logo-image" />
        <h1 class="logo-title" v-if="!isCollapse">ICT系统</h1>
      </div>
      
      <!-- 菜单 -->
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :unique-opened="true"
          :collapse-transition="false"
          mode="vertical"
        >
          <sidebar-item v-for="route in routes" :key="route.path" :item="route" :base-path="route.path" />
        </el-menu>
      </el-scrollbar>
    </div>
    
    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <div class="navbar">
        <div class="navbar-left">
          <el-icon class="toggle-icon" @click="toggleSidebar">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <breadcrumb />
        </div>
        
        <div class="navbar-right">
          <el-dropdown trigger="click">
            <div class="avatar-container">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">管理员</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item>修改密码</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 内容区域 -->
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

<script>
import { defineComponent, computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SidebarItem from './SidebarItem.vue'
import Breadcrumb from './Breadcrumb.vue'

export default defineComponent({
  name: 'Layout',
  components: {
    SidebarItem,
    Breadcrumb
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    // 控制侧边栏折叠状态
    const isCollapse = ref(false)
    const toggleSidebar = () => {
      isCollapse.value = !isCollapse.value
    }
    
    // 当前激活的菜单
    const activeMenu = computed(() => {
      const { meta, path } = route
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    })
    
    // 获取路由配置
    const routes = computed(() => {
      return router.options.routes.filter(route => !route.hidden)
    })
    
    return {
      isCollapse,
      toggleSidebar,
      activeMenu,
      routes
    }
  }
})
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.sidebar-container {
  width: 210px;
  height: 100%;
  background-color: #304156;
  transition: width 0.28s;
  overflow: hidden;
  
  &.is-collapsed {
    width: 64px;
  }
  
  .logo-container {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 10px;
    background-color: #263445;
    
    .logo-image {
      width: 32px;
      height: 32px;
      margin-right: 10px;
    }
    
    .logo-title {
      color: #fff;
      font-size: 18px;
      font-weight: bold;
      margin: 0;
      white-space: nowrap;
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  .navbar {
    height: 50px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 15px;
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    
    .navbar-left {
      display: flex;
      align-items: center;
      
      .toggle-icon {
        font-size: 20px;
        cursor: pointer;
        margin-right: 15px;
      }
    }
    
    .navbar-right {
      .avatar-container {
        display: flex;
        align-items: center;
        cursor: pointer;
        
        .username {
          margin-left: 8px;
          font-size: 14px;
        }
      }
    }
  }
  
  .app-main {
    flex: 1;
    padding: 15px;
    overflow-y: auto;
    background-color: #f5f7fa;
  }
}

// 过渡动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from,
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style> 