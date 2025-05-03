<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
      <span v-if="index === breadcrumbs.length - 1" class="no-redirect">{{ item.meta.title }}</span>
      <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script>
import { defineComponent, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

export default defineComponent({
  setup() {
    const route = useRoute()
    const router = useRouter()
    const breadcrumbs = ref([])

    // 面包屑数据处理
    const getBreadcrumbs = () => {
      let matched = route.matched.filter(item => item.meta && item.meta.title && !item.meta.hidden)
      
      // 如果第一个不是Dashboard，则添加Dashboard
      const first = matched[0]
      if (first && first.path !== '/dashboard') {
        matched = [
          {
            path: '/dashboard',
            meta: { title: '首页' }
          },
          ...matched
        ]
      }
      
      breadcrumbs.value = matched
    }

    // 处理面包屑导航点击
    const handleLink = (item) => {
      router.push(item.path)
    }

    // 监听路由变化，更新面包屑
    watch(
      () => route.path,
      () => getBreadcrumbs(),
      { immediate: true }
    )

    return {
      breadcrumbs,
      handleLink
    }
  }
})
</script>

<style scoped>
.app-breadcrumb {
  display: inline-block;
  line-height: 50px;
  margin-left: 8px;
}

.app-breadcrumb .no-redirect {
  color: #97a8be;
  cursor: text;
}
</style> 