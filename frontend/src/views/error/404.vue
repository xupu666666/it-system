<template>
  <div class="error-container">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <div class="error-image">
        <el-image :src="'/404.png'" alt="404 Not Found">
          <template #error>
            <div class="error-placeholder">
              <el-icon class="error-icon"><WarningFilled /></el-icon>
            </div>
          </template>
        </el-image>
      </div>
      <h2 class="error-title">页面不存在</h2>
      <p class="error-description">抱歉，您访问的页面不存在或已被删除</p>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { useRouter } from 'vue-router'
import { WarningFilled } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'Error404',
  components: {
    WarningFilled
  },
  setup() {
    const router = useRouter()
    
    const goHome = () => {
      router.push('/')
    }
    
    const goBack = () => {
      router.go(-1)
    }
    
    return {
      goHome,
      goBack
    }
  }
})
</script>

<style lang="scss" scoped>
.error-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  
  .error-content {
    text-align: center;
    padding: 40px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    max-width: 500px;
    
    .error-code {
      font-size: 80px;
      font-weight: bold;
      color: #409EFF;
      margin: 0 0 20px;
    }
    
    .error-image {
      margin: 20px auto;
      width: 200px;
      height: 200px;
      
      .error-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #f5f7fa;
        
        .error-icon {
          font-size: 48px;
          color: #909399;
        }
      }
    }
    
    .error-title {
      font-size: 24px;
      color: #303133;
      margin: 20px 0 10px;
    }
    
    .error-description {
      font-size: 16px;
      color: #606266;
      margin-bottom: 30px;
    }
    
    .error-actions {
      .el-button {
        margin: 0 10px;
      }
    }
  }
}
</style> 