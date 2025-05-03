<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index" :to="item.path">
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script>
import { defineComponent, computed } from 'vue'
import { useRoute } from 'vue-router'

export default defineComponent({
  name: 'Breadcrumb',
  setup() {
    const route = useRoute()
    
    // 计算面包屑导航
    const breadcrumbs = computed(() => {
      // 过滤首页
      const matched = route.matched.filter(item => item.meta && item.meta.title && item.path !== '/')
      
      // 构建面包屑数据
      return matched.map(item => {
        return {
          path: item.path,
          title: item.meta.title
        }
      })
    })
    
    return {
      breadcrumbs
    }
  }
})
</script>

<style lang="scss" scoped>
.el-breadcrumb {
  margin-bottom: 0;
  line-height: 22px;
}
</style> 