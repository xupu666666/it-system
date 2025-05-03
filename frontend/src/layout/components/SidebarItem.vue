<template>
  <div v-if="!item.meta || !item.meta.hidden">
    <!-- 如果没有子菜单或子菜单都是隐藏的 -->
    <template v-if="hasOneShowingChild(item.children, item) && (!onlyOneChild.children || onlyOneChild.noShowingChildren) && !item.alwaysShow">
      <app-link v-if="onlyOneChild.meta" :to="resolvePath(onlyOneChild.path)">
        <el-menu-item :index="resolvePath(onlyOneChild.path)" :class="{'submenu-title-noDropdown': !isNest}">
          <el-icon v-if="onlyOneChild.meta && onlyOneChild.meta.icon"><component :is="onlyOneChild.meta.icon" /></el-icon>
          <template #title>
            <span v-if="onlyOneChild.meta && onlyOneChild.meta.title">{{ onlyOneChild.meta.title }}</span>
          </template>
        </el-menu-item>
      </app-link>
    </template>

    <!-- 如果有子菜单 -->
    <el-sub-menu
      v-else
      :index="resolvePath(item.path)"
      popper-append-to-body
      :popper-offset="12"
      :teleported="true"
      :popper-class="'submenu-popper-' + resolvePath(item.path).replace(/\//g, '-')"
      class="sidebar-submenu"
    >
      <template #title>
        <el-icon v-if="item.meta && item.meta.icon"><component :is="item.meta.icon" /></el-icon>
        <span v-if="item.meta && item.meta.title">{{ item.meta.title }}</span>
      </template>

      <!-- 递归渲染子菜单 -->
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :is-nest="true"
        :base-path="resolvePath(child.path)"
        class="sidebar-submenu-item"
      />
    </el-sub-menu>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { isExternal } from '@/utils/validate'
import AppLink from './Link.vue'
import path from 'path-browserify'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  isNest: {
    type: Boolean,
    default: false
  },
  basePath: {
    type: String,
    default: ''
  }
})

const onlyOneChild = ref(null)

// 判断是否只有一个可显示的子菜单
const hasOneShowingChild = (children = [], parent) => {
  if (!children) {
    children = []
  }

  const showingChildren = children.filter(item => {
    if (item.meta && item.meta.hidden) {
      return false
    } else {
      // 临时设置
      onlyOneChild.value = item
      return true
    }
  })

  // 如果只有一个子菜单可显示
  if (showingChildren.length === 1) {
    return true
  }

  // 如果没有子菜单则显示父菜单
  if (showingChildren.length === 0) {
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
    return true
  }

  return false
}

// 解析路径
const resolvePath = (routePath) => {
  if (isExternal(routePath)) {
    return routePath
  }
  if (isExternal(props.basePath)) {
    return props.basePath
  }
  return path.resolve(props.basePath, routePath)
}
</script>

<style scoped>
.el-menu-item, .el-sub-menu {
  font-size: 14px;
  height: 50px;
  line-height: 50px;
}

.el-menu-item .el-icon, .el-sub-menu .el-icon {
  margin-right: 12px;
  width: 24px;
  text-align: center;
  font-size: 18px;
  vertical-align: middle;
}

.el-menu-item.is-active {
  color: var(--primary-light);
  background-color: rgba(59, 130, 246, 0.1);
  border-right: 3px solid var(--primary-light);
}

/* 修复菜单项文本对齐 */
:deep(.el-menu-item), :deep(.el-sub-menu__title) {
  display: flex;
  align-items: center;
  padding: 0 20px !important;
}

/* 修复子菜单样式 */
:deep(.el-sub-menu__title) {
  &:hover {
    background-color: rgba(255, 255, 255, 0.05) !important;
  }
}

:deep(.el-menu--collapse) {
  width: 70px;
}

/* 修复折叠菜单图标位置 */
:deep(.el-menu--collapse .el-menu-item), :deep(.el-menu--collapse .el-sub-menu__title) {
  justify-content: center;

  .el-icon {
    margin: 0;
  }
}

/* 子菜单样式已移至全局样式文件 */
</style>