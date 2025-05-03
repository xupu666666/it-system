<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <div class="logo">
          <img src="@/assets/logo.png" alt="Logo" class="logo-image" />
          <h1 class="title">ICT系统管理平台</h1>
        </div>
        <p class="description">用户注册</p>
      </div>

      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" class="register-form" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input
            v-model.lazy="registerForm.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="name">
          <el-input
            v-model.lazy="registerForm.name"
            placeholder="真实姓名"
            :prefix-icon="UserFilled"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model.lazy="registerForm.email"
            placeholder="邮箱"
            :prefix-icon="Message"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="department">
          <el-select
            v-model="registerForm.department"
            placeholder="选择部门"
            size="large"
            style="width: 100%"
          >
            <el-option label="信息技术部" value="信息技术部" />
            <el-option label="人力资源部" value="人力资源部" />
            <el-option label="财务部" value="财务部" />
            <el-option label="市场部" value="市场部" />
            <el-option label="销售部" value="销售部" />
            <el-option label="研发部" value="研发部" />
          </el-select>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model.lazy="registerForm.password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model.lazy="registerForm.confirmPassword"
            placeholder="确认密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="register-button" @click="handleRegister">
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>

        <div class="login-link">
          已有账号？<el-link type="primary" @click="goToLogin">立即登录</el-link>
        </div>
      </el-form>

      <div class="register-footer">
        <p class="copyright">© 2023 ICT系统管理平台 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, Message, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/v2'

export default defineComponent({
  name: 'Register',
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const loading = ref(false)

    // 注册表单数据
    const registerForm = reactive({
      username: '',
      name: '',
      email: '',
      department: '',
      password: '',
      confirmPassword: ''
    })

    // 表单验证规则
    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
      ],
      name: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
      ],
      department: [
        { required: true, message: '请选择部门', trigger: 'change' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== registerForm.password) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }

    // 处理注册
    const handleRegister = async () => {
      if (!registerFormRef.value) return

      registerFormRef.value.validate(async (valid) => {
        if (!valid) return

        loading.value = true

        try {
          // 准备注册数据
          const registerData = {
            username: registerForm.username,
            name: registerForm.name,
            email: registerForm.email,
            department: registerForm.department,
            password: registerForm.password
          }

          // 调用注册API
          await authApi.register(registerData)

          ElMessage({
            type: 'success',
            message: '注册成功，请登录'
          })

          // 跳转到登录页
          router.push('/login')
        } catch (error) {
          console.error('注册失败:', error)
          
          let errorMsg = '注册失败'
          
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
      registerFormRef,
      registerForm,
      registerRules,
      loading,
      handleRegister,
      goToLogin,
      User,
      UserFilled,
      Message,
      Lock
    }
  }
})
</script>

<style lang="scss" scoped>
.register-container {
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #1f4788 0%, #2b78e4 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.register-card {
  width: 450px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.register-header {
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

.register-form {
  margin-bottom: 20px;
}

.register-button {
  width: 100%;
}

.login-link {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #666;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
}

.copyright {
  font-size: 12px;
  color: #999;
}
</style>
