<template>
  <div class="reset-password-container">
    <div class="reset-password-card">
      <div class="reset-password-header">
        <div class="logo">
          <img src="@/assets/logo.png" alt="Logo" class="logo-image" />
          <h1 class="title">ICT系统管理平台</h1>
        </div>
        <p class="description">重置密码</p>
      </div>

      <div v-if="step === 1">
        <el-form ref="emailFormRef" :model="emailForm" :rules="emailRules" class="reset-password-form" @submit.prevent="handleRequestReset">
          <p class="form-description">请输入您的注册邮箱，我们将向您发送重置密码的链接</p>
          
          <el-form-item prop="email">
            <el-input
              v-model.lazy="emailForm.email"
              placeholder="邮箱"
              :prefix-icon="Message"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="reset-button" @click="handleRequestReset">
              {{ loading ? '提交中...' : '发送重置链接' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="step === 2">
        <div class="success-message">
          <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
          <h2>重置链接已发送</h2>
          <p>我们已向您的邮箱发送了密码重置链接，请查收邮件并点击链接重置密码。</p>
          <p>如果您没有收到邮件，请检查垃圾邮件文件夹，或者 <el-link type="primary" @click="step = 1">重新发送</el-link></p>
        </div>
      </div>

      <div v-if="step === 3">
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" class="reset-password-form" @submit.prevent="handleResetPassword">
          <p class="form-description">请设置您的新密码</p>
          
          <el-form-item prop="password">
            <el-input
              v-model.lazy="passwordForm.password"
              placeholder="新密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model.lazy="passwordForm.confirmPassword"
              placeholder="确认新密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleResetPassword"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="reset-button" @click="handleResetPassword">
              {{ loading ? '重置中...' : '重置密码' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="step === 4">
        <div class="success-message">
          <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
          <h2>密码重置成功</h2>
          <p>您的密码已成功重置，现在可以使用新密码登录。</p>
          <el-button type="primary" size="large" class="login-button" @click="goToLogin">
            返回登录
          </el-button>
        </div>
      </div>

      <div class="reset-password-footer">
        <p class="login-link">
          记起密码了？<el-link type="primary" @click="goToLogin">返回登录</el-link>
        </p>
        <p class="copyright">© 2023 ICT系统管理平台 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message, Lock, CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/v2'

export default defineComponent({
  name: 'ResetPassword',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const emailFormRef = ref(null)
    const passwordFormRef = ref(null)
    const loading = ref(false)
    const step = ref(1)

    // 如果URL中有token和email参数，说明是从邮件链接点击过来的
    if (route.query.token && route.query.email) {
      step.value = 3
    }

    // 邮箱表单数据
    const emailForm = reactive({
      email: ''
    })

    // 密码表单数据
    const passwordForm = reactive({
      token: route.query.token || '',
      email: route.query.email || '',
      password: '',
      confirmPassword: ''
    })

    // 邮箱表单验证规则
    const emailRules = {
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
      ]
    }

    // 密码表单验证规则
    const passwordRules = {
      password: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.password) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }

    // 处理请求重置密码
    const handleRequestReset = async () => {
      if (!emailFormRef.value) return

      emailFormRef.value.validate(async (valid) => {
        if (!valid) return

        loading.value = true

        try {
          // 调用请求重置密码API
          await authApi.requestPasswordReset({
            email: emailForm.email
          })

          // 显示成功消息
          step.value = 2
        } catch (error) {
          console.error('请求重置密码失败:', error)
          
          let errorMsg = '请求重置密码失败'
          
          if (error.response) {
            if (error.response.data && error.response.data.message) {
              errorMsg = error.response.data.message
            } else {
              errorMsg = `服务器错误 (${error.response.status})`
            }
          } else if (error.message) {
            errorMsg = error.message
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

    // 处理重置密码
    const handleResetPassword = async () => {
      if (!passwordFormRef.value) return

      passwordFormRef.value.validate(async (valid) => {
        if (!valid) return

        loading.value = true

        try {
          // 调用重置密码API
          await authApi.resetPassword({
            token: passwordForm.token,
            email: passwordForm.email,
            password: passwordForm.password
          })

          // 显示成功消息
          step.value = 4
        } catch (error) {
          console.error('重置密码失败:', error)
          
          let errorMsg = '重置密码失败'
          
          if (error.response) {
            if (error.response.data && error.response.data.message) {
              errorMsg = error.response.data.message
            } else {
              errorMsg = `服务器错误 (${error.response.status})`
            }
          } else if (error.message) {
            errorMsg = error.message
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

    // 跳转到登录页
    const goToLogin = () => {
      router.push('/login')
    }

    return {
      emailFormRef,
      passwordFormRef,
      emailForm,
      passwordForm,
      emailRules,
      passwordRules,
      loading,
      step,
      handleRequestReset,
      handleResetPassword,
      goToLogin,
      Message,
      Lock,
      CircleCheckFilled
    }
  }
})
</script>

<style lang="scss" scoped>
.reset-password-container {
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #1f4788 0%, #2b78e4 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.reset-password-card {
  width: 450px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.reset-password-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 10px;
}

.logo-image {
  width: 80px;
  height: 80px;
  margin-bottom: 10px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.description {
  font-size: 16px;
  color: #666;
  margin: 10px 0 0;
}

.form-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
  text-align: center;
}

.reset-password-form {
  margin-bottom: 20px;
}

.reset-button, .login-button {
  width: 100%;
}

.success-message {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 60px;
  color: #67c23a;
  margin-bottom: 20px;
}

.success-message h2 {
  font-size: 20px;
  color: #333;
  margin-bottom: 15px;
}

.success-message p {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
}

.login-button {
  margin-top: 20px;
}

.reset-password-footer {
  text-align: center;
  margin-top: 20px;
}

.login-link {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}

.copyright {
  font-size: 12px;
  color: #999;
}
</style>
