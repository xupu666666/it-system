<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <img src="@/assets/logo.png" alt="Logo" class="logo-image" />
          <h1 class="title">ICT系统管理平台</h1>
        </div>
        <p class="description">综合IT管理和资产跟踪系统</p>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model.lazy="loginForm.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model.lazy="loginForm.password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item class="remember-me">
          <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码</el-link>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-button" @click="handleLogin">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>

        <div class="login-tips">
          <el-alert
            title="默认用户: admin, 密码: 123456"
            type="info"
            :closable="false"
            show-icon
          />
        </div>
      </el-form>

      <div class="login-footer">
        <p class="copyright">© 2023 ICT系统管理平台 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/v2/auth'
import { authApi } from '@/api/v2'
import config from '@/config'

export default defineComponent({
  name: 'Login',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const loginFormRef = ref(null)
    const loading = ref(false)

    // 登录表单数据
    const loginForm = reactive({
      username: localStorage.getItem('rememberedUsername') || '',
      password: '',
      remember: !!localStorage.getItem('rememberedUsername')
    })

    // 表单验证规则 - 优化验证触发时机
    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'submit' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'submit' }
      ]
    }

    // 处理登录
    const handleLogin = async () => {
      if (!loginFormRef.value) return

      loginFormRef.value.validate(async (valid) => {
        if (!valid) return

        loading.value = true

        try {
          console.log('尝试登录:', loginForm.username, loginForm.password)
          console.log('API URL:', config.api.baseUrl)

          // 直接调用API，跳过store
          const response = await authApi.login({
            username: loginForm.username,
            password: loginForm.password
          })

          console.log('登录成功，响应:', response)

          // 保存登录信息到store
          console.log('登录响应详情:', JSON.stringify(response))
          authStore.setToken(response.token)

          // 构建用户信息对象
          const userInfo = {
            id: response.id,
            username: response.username,
            name: response.name,
            email: response.email,
            department: response.department,
            avatar: response.avatar
          }
          authStore.setUserInfo(userInfo)
          authStore.setRoles(response.roles)
          authStore.setPermissions(response.permissions)

          // 如果记住密码，保存用户名
          if (loginForm.remember) {
            localStorage.setItem('rememberedUsername', loginForm.username)
          } else {
            localStorage.removeItem('rememberedUsername')
          }

          ElMessage({
            type: 'success',
            message: '登录成功'
          })

          // 跳转到首页
          router.push('/')
        } catch (error) {
          console.error('登录失败详情:', error)

          let errorMsg = '登录失败'
          if (error.response) {
            errorMsg += `，服务器返回: ${error.response.status}`
            console.error('错误状态码:', error.response.status)
            console.error('错误头部:', error.response.headers)

            if (error.response.data && error.response.data.message) {
              errorMsg += ` - ${error.response.data.message}`
              console.error('错误消息:', error.response.data.message)
            }
          } else if (error.message) {
            errorMsg += `: ${error.message}`
            console.error('错误消息:', error.message)
          }

          // 尝试检查服务器是否在线
          try {
            console.log('检查服务器状态...')
            // 使用正确的路径检查服务器状态
            const pingResponse = await fetch(`${config.api.baseUrl}/api/auth/login`, { method: 'OPTIONS' })
            console.log('服务器响应:', pingResponse.status, pingResponse.statusText)
          } catch (pingError) {
            console.error('服务器状态检查失败:', pingError)
            errorMsg += '，无法连接到服务器，请确认后端服务已启动'
          }

          ElMessage({
            type: 'error',
            message: errorMsg
          })
        } finally {
          loading.value = false
        }
      })
    }

    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      handleLogin,
      User,
      Lock
    }
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #1E293B 0%, #0F172A 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;

  // 背景装饰元素
  &::before, &::after {
    content: '';
    position: absolute;
    width: 300px;
    height: 300px;
    border-radius: 50%;
    opacity: 0.1;
  }

  &::before {
    background: var(--primary-gradient);
    top: -100px;
    right: -100px;
  }

  &::after {
    background: var(--primary-gradient);
    bottom: -100px;
    left: -100px;
  }

  .login-card {
    width: 420px;
    padding: 40px;
    background-color: var(--background-light);
    border-radius: var(--border-radius-xl);
    box-shadow: var(--shadow-xl);
    position: relative;
    z-index: 1;
    animation: fadeIn 0.5s ease-out;

    .login-header {
      text-align: center;
      margin-bottom: 36px;

      .logo {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-bottom: 16px;

        .logo-image {
          width: 48px;
          height: 48px;
          margin-right: 12px;
          border-radius: var(--border-radius-md);
        }

        .title {
          font-size: 24px;
          color: var(--text-primary);
          margin: 0;
          font-weight: 700;
          letter-spacing: -0.5px;
        }
      }

      .description {
        font-size: 16px;
        color: var(--text-secondary);
        margin: 0;
      }
    }

    .login-form {
      .el-form-item {
        margin-bottom: 24px;
      }

      .el-input {
        --el-input-height: 48px;

        .el-input__wrapper {
          border-radius: var(--border-radius-md);
          box-shadow: 0 0 0 1px var(--border-color);
          transition: all 0.3s;

          &:hover, &:focus-within {
            box-shadow: 0 0 0 1px var(--primary-color);
          }
        }
      }

      .remember-me {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 32px;
      }

      .login-button {
        width: 100%;
        height: 48px;
        border-radius: var(--border-radius-md);
        font-size: 16px;
        font-weight: 500;
        background: var(--primary-gradient);
        border: none;
        transition: all 0.3s;

        &:hover, &:focus {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        }
      }
    }

    .login-footer {
      margin-top: 36px;
      text-align: center;

      .copyright {
        font-size: 13px;
        color: var(--text-secondary);
        margin: 0;
      }
    }

    .login-tips {
      margin-top: 24px;

      .el-alert {
        border-radius: var(--border-radius-md);
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式调整
@media (max-width: 480px) {
  .login-container {
    .login-card {
      width: 90%;
      padding: 30px 20px;
    }
  }
}
</style>