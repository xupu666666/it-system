<template>
  <div v-if="!item.hidden">
    <!-- 无子菜单 -->
    <template v-if="!hasChildren(item)">
      <el-menu-item :index="resolvePath(item.path)" @click="navigateTo(resolvePath(item.path))">
        <el-icon v-if="item.meta && item.meta.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <template #title>
          <span>{{ item.meta.title }}</span>
        </template>
      </el-menu-item>
    </template>
    
    <!-- 有子菜单 -->
    <el-sub-menu v-else :index="resolvePath(item.path)">
      <template #title>
        <el-icon v-if="item.meta && item.meta.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta.title }}</span>
      </template>
      
      <!-- 递归渲染子菜单 -->
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(child.path)"
      />
    </el-sub-menu>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { useRouter } from 'vue-router'
import path from 'path-browserify'

export default defineComponent({
  name: 'SidebarItem',
  props: {
    item: {
      type: Object,
      required: true
    },
    basePath: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    const router = useRouter()
    
    // 判断是否有子菜单
    const hasChildren = (item) => {
      return item.children && item.children.length > 0 && !item.hidden
    }
    
    // 解析路径
    const resolvePath = (routePath) => {
      if (routePath.startsWith('/')) {
        return routePath
      }
      return path.resolve(props.basePath, routePath)
    }
    
    // 导航到指定路径
    const navigateTo = (path) => {
      router.push(path)
    }
    
    return {
      hasChildren,
      resolvePath,
      navigateTo
    }
  }
})
</script> 