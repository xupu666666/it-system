<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <img src="@/assets/logo.png" alt="Logo" class="logo-image" />
          <h1 class="title">ICT系统管理平台 <span class="version">V2</span></h1>
        </div>
        <p class="description">综合IT管理和资产跟踪系统 - 新版登录</p>
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
          <el-link type="primary" :underline="false" @click="goToResetPassword">忘记密码</el-link>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-button" @click="handleLogin">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>

        <div class="login-tips">
          <el-alert
            title="默认用户: admin, 密码: admin"
            type="info"
            :closable="false"
            show-icon
          />
        </div>

        <div class="register-link">
          没有账号？<el-link type="primary" @click="goToRegister">立即注册</el-link>
        </div>
      </el-form>

      <div class="login-footer">
        <p class="copyright">© 2023 ICT系统管理平台 V2 版权所有</p>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/v2/auth'
import { authApi } from '@/api/v2'
import config from '@/config'

export default defineComponent({
  name: 'LoginV2',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const loginFormRef = ref(null)
    const loading = ref(false)

    // 登录表单数据
    const loginForm = reactive({
      username: localStorage.getItem('rememberedUsername_v2') || '',
      password: '',
      remember: !!localStorage.getItem('rememberedUsername_v2')
    })

    // 表单验证规则
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
          console.log('尝试登录 (V2):', loginForm.username, loginForm.password)
          console.log('API配置:', config.api)
          console.log('API基础URL:', config.api.baseUrl)

          // 尝试使用axios直接发送请求
          console.log('尝试使用axios直接发送请求')
          try {
            // 导入axios
            const axios = require('axios')

            // 创建axios实例
            const axiosInstance = axios.create({
              baseURL: 'http://localhost:8100',
              timeout: 10000,
              withCredentials: true,
              headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
              }
            })

            // 发送请求
            const response = await axiosInstance.post('/api/auth/login', {
              username: loginForm.username,
              password: loginForm.password
            })

            console.log('axios响应状态:', response.status)
            console.log('axios响应头:', response.headers)
            console.log('axios响应数据:', response.data)

            // 保存登录信息到store
            authStore.setToken(response.data.token)
            authStore.setUserInfo(response.data.userInfo)
            authStore.setRoles(response.data.roles)
            authStore.setPermissions(response.data.permissions)

            // 如果记住密码，保存用户名
            if (loginForm.remember) {
              localStorage.setItem('rememberedUsername_v2', loginForm.username)
            } else {
              localStorage.removeItem('rememberedUsername_v2')
            }

            ElMessage({
              type: 'success',
              message: '登录成功'
            })

            // 跳转到首页
            router.push('/')
            return
          } catch (axiosError) {
            console.error('axios请求失败:', axiosError)

            if (axiosError.response) {
              // 服务器返回了错误状态码
              console.error('axios错误响应:', axiosError.response.status, axiosError.response.data)

              let errorMsg = '登录失败'
              if (axiosError.response.data && axiosError.response.data.message) {
                errorMsg = axiosError.response.data.message
              } else {
                errorMsg = `服务器错误 (${axiosError.response.status})`
              }

              ElMessage({
                type: 'error',
                message: errorMsg
              })
            } else if (axiosError.request) {
              // 请求已发送但没有收到响应
              console.error('axios请求无响应:', axiosError.request)
              ElMessage({
                type: 'error',
                message: '服务器无响应，请检查后端服务是否正常运行'
              })
            } else {
              // 请求配置有误
              console.error('axios请求配置错误:', axiosError.message)
              ElMessage({
                type: 'error',
                message: `请求错误: ${axiosError.message}`
              })
            }

            console.log('回退到XMLHttpRequest发送请求')
          }

          // 尝试使用原生XMLHttpRequest
          console.log('尝试使用原生XMLHttpRequest发送登录请求')
          try {
            const xhr = new XMLHttpRequest()
            xhr.open('POST', 'http://localhost:8100/api/auth/login', true)
            xhr.setRequestHeader('Content-Type', 'application/json')
            xhr.setRequestHeader('Accept', 'application/json')
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest')
            xhr.withCredentials = true

            xhr.onreadystatechange = function() {
              if (xhr.readyState === 4) {
                console.log('XHR状态:', xhr.readyState)
                console.log('XHR状态码:', xhr.status)
                console.log('XHR响应头:', xhr.getAllResponseHeaders())

                if (xhr.status >= 200 && xhr.status < 300) {
                  try {
                    const responseData = JSON.parse(xhr.responseText)
                    console.log('XHR响应数据:', responseData)

                    // 保存登录信息到store
                    authStore.setToken(responseData.token)
                    authStore.setUserInfo(responseData.userInfo)
                    authStore.setRoles(responseData.roles)
                    authStore.setPermissions(responseData.permissions)

                    // 如果记住密码，保存用户名
                    if (loginForm.remember) {
                      localStorage.setItem('rememberedUsername_v2', loginForm.username)
                    } else {
                      localStorage.removeItem('rememberedUsername_v2')
                    }

                    ElMessage({
                      type: 'success',
                      message: '登录成功'
                    })

                    // 跳转到首页
                    router.push('/')
                  } catch (parseError) {
                    console.error('解析响应失败:', parseError)
                    ElMessage({
                      type: 'error',
                      message: '解析响应失败'
                    })
                  }
                } else {
                  console.error('XHR请求失败:', xhr.status, xhr.statusText)
                  ElMessage({
                    type: 'error',
                    message: `请求失败: ${xhr.status} ${xhr.statusText}`
                  })
                }

                loading.value = false
              }
            }

            xhr.onerror = function(error) {
              console.error('XHR请求错误:', error)
              ElMessage({
                type: 'error',
                message: '网络错误，请检查后端服务是否正常运行'
              })
              loading.value = false
            }

            const data = JSON.stringify({
              username: loginForm.username,
              password: loginForm.password
            })

            xhr.send(data)
            return
          } catch (xhrError) {
            console.error('XHR请求失败:', xhrError)
            console.log('回退到fetch发送请求')
          }

          // 尝试使用原生fetch API
          console.log('尝试使用原生fetch API发送登录请求')
          try {
            const fetchResponse = await fetch('http://localhost:8100/api/auth/login', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
              },
              credentials: 'include',
              body: JSON.stringify({
                username: loginForm.username,
                password: loginForm.password
              })
            })

            console.log('收到fetch响应:', fetchResponse.status, fetchResponse.statusText)

            if (!fetchResponse.ok) {
              throw new Error(`HTTP error! Status: ${fetchResponse.status}`)
            }

            const responseData = await fetchResponse.json()
            console.log('fetch响应数据:', responseData)

            // 保存登录信息到store
            authStore.setToken(responseData.token)
            authStore.setUserInfo(responseData.userInfo)
            authStore.setRoles(responseData.roles)
            authStore.setPermissions(responseData.permissions)

            // 如果记住密码，保存用户名
            if (loginForm.remember) {
              localStorage.setItem('rememberedUsername_v2', loginForm.username)
            } else {
              localStorage.removeItem('rememberedUsername_v2')
            }

            ElMessage({
              type: 'success',
              message: '登录成功'
            })

            // 跳转到首页
            router.push('/')
            return
          } catch (fetchError) {
            console.error('fetch请求失败:', fetchError)
            console.log('回退到axios发送请求')
          }

          // 如果fetch失败，回退到axios
          // 调用API
          const response = await authApi.login({
            username: loginForm.username,
            password: loginForm.password
          })

          console.log('登录成功，响应:', response)

          // 保存登录信息到store
          authStore.setToken(response.token)
          authStore.setUserInfo(response.userInfo)
          authStore.setRoles(response.roles)
          authStore.setPermissions(response.permissions)

          // 如果记住密码，保存用户名
          if (loginForm.remember) {
            localStorage.setItem('rememberedUsername_v2', loginForm.username)
          } else {
            localStorage.removeItem('rememberedUsername_v2')
          }

          ElMessage({
            type: 'success',
            message: '登录成功'
          })

          // 跳转到首页
          router.push('/')
        } catch (error) {
          console.error('登录失败:', error)

          let errorMsg = '登录失败'

          if (error.response) {
            if (error.response.status === 401) {
              errorMsg = '用户名或密码错误'
            } else if (error.response.data && error.response.data.message) {
              errorMsg = error.response.data.message
            } else {
              errorMsg = `服务器错误 (${error.response.status})`
            }
          } else if (error.message) {
            errorMsg = error.message
          }

          // 尝试检查服务器是否在线
          try {
            console.log('检查服务器状态...')
            // 使用正确的路径检查服务器状态
            const pingResponse = await fetch('http://localhost:8100/api/auth/login', { method: 'OPTIONS' })
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

    // 跳转到注册页
    const goToRegister = () => {
      router.push('/register')
    }

    // 跳转到重置密码页
    const goToResetPassword = () => {
      router.push('/reset-password')
    }

    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      handleLogin,
      goToRegister,
      goToResetPassword,
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
  background: linear-gradient(135deg, #1a365d 0%, #2563eb 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 400px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.login-header {
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
  position: relative;
}

.version {
  font-size: 14px;
  color: #fff;
  background-color: #2563eb;
  padding: 2px 6px;
  border-radius: 4px;
  position: absolute;
  top: -8px;
  right: -30px;
}

.description {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.remember-me {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.login-button {
  width: 100%;
}

.login-tips {
  margin-top: 20px;
}

.register-link {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #666;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
}

.copyright {
  font-size: 12px;
  color: #999;
}
</style>
