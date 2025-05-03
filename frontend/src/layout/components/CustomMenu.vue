<template>
  <div class="custom-menu">
    <!-- 遍历顶级菜单项 -->
    <div v-for="(route, index) in routes" :key="route.path" class="menu-item-container">
      <!-- 没有子菜单的菜单项 -->
      <router-link
        v-if="!hasChildren(route)"
        :to="route.path"
        class="menu-item"
        :class="{ 'is-active': isActive(route.path) }"
      >
        <el-icon v-if="route.meta && route.meta.icon">
          <component :is="route.meta.icon" />
        </el-icon>
        <span v-if="!isCollapse" class="menu-title">{{ route.meta?.title || '未命名菜单' }}</span>
      </router-link>

      <!-- 有子菜单的菜单项 -->
      <div
        v-else
        class="menu-item submenu-title"
        :class="{ 'is-active': isSubmenuActive(route) }"
        @click="toggleSubmenu(index)"
      >
        <div class="menu-content">
          <el-icon v-if="route.meta && route.meta.icon">
            <component :is="route.meta.icon" />
          </el-icon>
          <span v-if="!isCollapse" class="menu-title">{{ route.meta?.title || '未命名菜单' }}</span>
        </div>
        <el-icon v-if="!isCollapse" class="submenu-arrow" :class="{ 'is-open': openedMenus.includes(index) }">
          <ArrowDown />
        </el-icon>
      </div>

      <!-- 子菜单容器 -->
      <div
        v-if="hasChildren(route)"
        class="submenu"
        :class="{ 'is-open': openedMenus.includes(index), 'is-collapsed': isCollapse }"
        :style="getSubmenuStyle(index)"
      >
        <router-link
          v-for="child in route.children"
          :key="child.path"
          :to="resolvePath(route.path, child.path)"
          class="submenu-item"
          :class="{ 'is-active': isActive(resolvePath(route.path, child.path)) }"
        >
          <el-icon v-if="child.meta && child.meta.icon">
            <component :is="child.meta.icon" />
          </el-icon>
          <span class="menu-title">{{ child.meta?.title || '未命名菜单' }}</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import path from 'path-browserify'

const props = defineProps({
  routes: {
    type: Array,
    required: true
  },
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const openedMenus = ref([])

// 判断是否有子菜单
const hasChildren = (item) => {
  return item.children && item.children.length > 0
}

// 切换子菜单的展开/折叠状态
const toggleSubmenu = (index) => {
  const position = openedMenus.value.indexOf(index)
  if (position !== -1) {
    openedMenus.value.splice(position, 1)
  } else {
    // 如果设置了unique-opened，则关闭其他子菜单
    openedMenus.value = [index]
  }
}

// 判断菜单项是否激活
const isActive = (path) => {
  return route.path === path
}

// 判断子菜单是否激活
const isSubmenuActive = (item) => {
  if (item.path === route.path) {
    return true
  }
  if (item.children) {
    return item.children.some(child => {
      const childPath = resolvePath(item.path, child.path)
      return route.path === childPath || route.path.startsWith(childPath + '/')
    })
  }
  return false
}

// 解析路径
const resolvePath = (basePath, routePath) => {
  if (/^(https?:|mailto:|tel:)/.test(routePath)) {
    return routePath
  }
  return path.resolve(basePath, routePath)
}

// 获取子菜单样式
const getSubmenuStyle = (index) => {
  const baseStyle = {}
  
  // 如果菜单折叠，则使用绝对定位
  if (props.isCollapse) {
    baseStyle.position = 'absolute'
    baseStyle.left = '70px'
    baseStyle.top = `${index * 50}px`
    baseStyle.zIndex = 2000 + index
  }
  
  return baseStyle
}
</script>

<style scoped>
.custom-menu {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.menu-item-container {
  position: relative;
}

.menu-item, .submenu-title {
  height: 50px;
  line-height: 50px;
  padding: 0 20px;
  color: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
}

.menu-content {
  display: flex;
  align-items: center;
}

.menu-item:hover, .submenu-title:hover {
  background-color: rgba(255, 255, 255, 0.05);
}

.menu-item.is-active, .submenu-title.is-active {
  color: var(--primary-light);
  background-color: rgba(59, 130, 246, 0.1);
  border-right: 3px solid var(--primary-light);
}

.menu-title {
  margin-left: 12px;
  font-size: 14px;
}

.submenu-arrow {
  transition: transform 0.3s;
}

.submenu-arrow.is-open {
  transform: rotate(180deg);
}

.submenu {
  overflow: hidden;
  background-color: rgba(0, 0, 0, 0.1);
  max-height: 0;
  transition: max-height 0.3s ease;
}

.submenu.is-open {
  max-height: 500px; /* 足够大的高度以容纳所有子菜单项 */
}

.submenu.is-collapsed {
  background-color: #1a2234;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-height: 0;
  overflow: hidden;
}

.submenu.is-collapsed.is-open {
  max-height: 500px;
  padding: 5px 0;
}

.submenu-item {
  height: 40px;
  line-height: 40px;
  padding: 0 20px 0 40px;
  color: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
}

.submenu-item:hover {
  background-color: rgba(255, 255, 255, 0.05);
}

.submenu-item.is-active {
  color: var(--primary-light);
  background-color: rgba(59, 130, 246, 0.1);
}

.el-icon {
  width: 24px;
  text-align: center;
  font-size: 18px;
  vertical-align: middle;
}
</style>
