<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <h2>{{ title }}</h2>
        </div>
      </template>
      <div class="card-content">
        <el-alert
          title="此页面正在开发中"
          type="info"
          description="该功能模块尚未完成，敬请期待！"
          :closable="false"
          show-icon
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'TemplatePage',
  props: {
    title: {
      type: String,
      default: '页面标题'
    }
  },
  setup() {
    return {}
  }
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
  
  .page-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .card-content {
      margin-top: 20px;
    }
  }
}
</style>
