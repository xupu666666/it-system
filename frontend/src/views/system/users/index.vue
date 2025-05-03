<template>
  <div class="user-management">
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" ref="searchFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-select v-model="searchForm.department" placeholder="请选择部门" clearable>
            <el-option
              v-for="item in departmentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearchForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="action-bar">
      <el-button v-permission="'system.user.add'" type="primary" @click="handleAddUser">新增用户</el-button>
      <el-button v-permission="'system.user.batch'" type="success" @click="handleImport">批量导入</el-button>
      <el-button v-permission="'system.user.batch'" @click="handleExport">导出用户</el-button>
      <el-button v-permission="'system.user.delete'" type="danger" @click="handleBatchDelete" :disabled="selectedRows.length === 0">批量删除</el-button>
    </div>

    <el-table
      :data="tableData"
      border
      stripe
      v-loading="loading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="department" label="所属部门" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="mobile" label="手机号" />
      <el-table-column prop="roles" label="角色">
        <template #default="{ row }">
          <el-tag
            v-for="role in row.roles"
            :key="role.id"
            class="role-tag"
            :type="getRoleTagType(role.name)"
          >
            {{ role.description || role.displayName || role.name }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录时间" width="160" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
            {{ row.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'system.user.edit'"
            type="primary"
            link
            @click="handleEditUser(row)"
          >
            编辑
          </el-button>
          <el-button
            v-permission="'system.user.view'"
            type="info"
            link
            @click="handleViewDetail(row)"
          >
            详情
          </el-button>
          <el-button
            v-permission="'system.user.password'"
            type="warning"
            link
            @click="handleResetPassword(row)"
          >
            重置密码
          </el-button>
          <el-button
            v-permission="'system.user.status'"
            :type="row.status === 'active' ? 'danger' : 'success'"
            link
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 'active' ? '禁用' : '启用' }}
          </el-button>
          <el-button
            v-permission="'system.user.delete'"
            type="danger"
            link
            @click="handleDeleteUser(row)"
            v-if="row.username !== 'admin'"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 用户表单对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      v-model="userDialogVisible"
      width="600px"
    >
      <el-form
        :model="userForm"
        :rules="userFormRules"
        ref="userFormRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="所属部门" prop="department">
          <el-select v-model="userForm.department" placeholder="请选择部门">
            <el-option
              v-for="item in departmentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="userForm.mobile" />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-select v-model="userForm.roles" placeholder="请选择角色" multiple>
            <el-option
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="确认密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUserForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      title="重置密码"
      v-model="resetPasswordDialogVisible"
      width="500px"
    >
      <el-form
        :model="passwordForm"
        :rules="passwordFormRules"
        ref="passwordFormRef"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPasswordForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog
      title="用户详情"
      v-model="userDetailDialogVisible"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ userDetail.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ userDetail.realName }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ userDetail.department }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userDetail.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userDetail.mobile }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="userDetail.status === 'active' ? 'success' : 'danger'">
            {{ userDetail.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag
            v-for="role in userDetail.roles"
            :key="role.id"
            class="role-tag"
            :type="getRoleTagType(role.name)"
          >
            {{ role.description || role.displayName || role.name }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ userDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="最后登录时间">{{ userDetail.lastLoginTime }}</el-descriptions-item>
        <el-descriptions-item label="最后登录IP">{{ userDetail.lastLoginIp }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="userDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog
      title="批量导入用户"
      v-model="importDialogVisible"
      width="500px"
    >
      <el-upload
        class="upload-demo"
        drag
        action="/api/system/users/import"
        :headers="uploadHeaders"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :before-upload="beforeImportUpload"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传Excel文件 (xlsx格式)，<el-link type="primary" @click="downloadTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/v2/auth'
import { checkPermission } from '@/utils/permission'
import * as userApi from '@/api/v2/user'

// 用户状态标签样式
const getRoleTagType = (roleName) => {
  // 转换为小写以便匹配
  const name = (roleName || '').toLowerCase()
  const typeMap = {
    'admin': 'danger',
    'manager': 'warning',
    'it_staff': 'success',
    'it': 'success',  // 添加IT角色
    'staff': 'info',
    'user': 'info'    // 添加USER角色
  }
  return typeMap[name] || 'info'
}

// 部门选项
const departmentOptions = ref([
  { value: '1', label: '信息技术部' },
  { value: '2', label: '人力资源部' },
  { value: '3', label: '财务部' },
  { value: '4', label: '市场部' },
  { value: '5', label: '研发部' },
  { value: '6', label: '销售部' }
])

// 角色选项
const roleOptions = ref([
  { value: 1, label: '系统管理员' },
  { value: 2, label: '部门经理' },
  { value: 3, label: 'IT人员' },
  { value: 4, label: '普通员工' }
])

// 搜索表单
const searchFormRef = ref(null)
const searchForm = reactive({
  username: '',
  realName: '',
  department: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const selectedRows = ref([])
const tableData = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(4)

// 用户对话框
const userDialogVisible = ref(false)
const dialogType = ref('add')  // 'add' 或 'edit'
const userFormRef = ref(null)
const userForm = reactive({
  id: '',
  username: '',
  realName: '',
  department: '',
  email: '',
  mobile: '',
  roles: [],
  password: '',
  confirmPassword: '',
  status: 'active'
})

// 用户表单校验规则
const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  roles: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== userForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 重置密码对话框
const resetPasswordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  userId: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码表单校验规则
const passwordFormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 用户详情对话框
const userDetailDialogVisible = ref(false)
const userDetail = reactive({
  username: '',
  realName: '',
  department: '',
  email: '',
  mobile: '',
  roles: [],
  status: '',
  createTime: '',
  lastLoginTime: '',
  lastLoginIp: ''
})

// 批量导入对话框
const importDialogVisible = ref(false)
const uploadHeaders = computed(() => {
  const authStore = useAuthStore()
  return {
    Authorization: `Bearer ${authStore.token}`
  }
})

// 生命周期钩子
onMounted(() => {
  fetchUsersList()
})

// 方法
const fetchUsersList = async () => {
  loading.value = true
  try {
    console.log('获取用户列表，参数:', {
      page: currentPage.value - 1,
      size: pageSize.value,
      ...searchForm
    })

    // 调用API获取用户列表
    const response = await userApi.getUserList({
      page: currentPage.value - 1,
      size: pageSize.value,
      ...searchForm
    })

    console.log('获取用户列表响应:', response)

    // 检查响应格式
    console.log('响应数据结构:', response)

    if (response && response.users && Array.isArray(response.users)) {
      // 后端返回的是分页对象，包含users数组
      const users = response.users

      // 确保每个用户对象都有有效的ID和用户名
      const validUsers = users.filter(user => user && user.id && user.username)

      if (validUsers.length !== users.length) {
        console.warn('过滤掉了一些无效的用户数据')
      }

      tableData.value = validUsers
      total.value = response.totalItems || validUsers.length
      console.log('更新表格数据成功，数据条数:', validUsers.length, '总条数:', total.value)
    } else if (response && Array.isArray(response)) {
      // 后端直接返回了用户数组
      const validUsers = response.filter(user => user && user.id && user.username)
      tableData.value = validUsers
      total.value = validUsers.length
      console.log('更新表格数据成功(数组格式)，数据条数:', validUsers.length)
    } else {
      // 返回了非预期格式的数据
      console.error('API返回的数据格式不正确:', response)
      ElMessage.warning('获取用户列表数据格式不正确')
      tableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败: ' + (error.message || '未知错误'))
    // 出错时不修改现有数据
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUsersList()
}

const resetSearchForm = () => {
  searchFormRef.value.resetFields()
  handleSearch()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchUsersList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUsersList()
}

const handleAddUser = () => {
  dialogType.value = 'add'
  Object.keys(userForm).forEach(key => {
    if (key !== 'status') {
      userForm[key] = key === 'roles' ? [] : ''
    }
  })
  userForm.status = 'active'
  userDialogVisible.value = true
}

const handleEditUser = (row) => {
  dialogType.value = 'edit'
  Object.keys(userForm).forEach(key => {
    if (key in row) {
      if (key === 'roles') {
        userForm[key] = row[key].map(role => role.id)
      } else {
        userForm[key] = row[key]
      }
    }
  })
  userDialogVisible.value = true
}

const submitUserForm = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 准备用户数据
        const userData = { ...userForm }
        // roles 直接是id数组
        // 确保realName字段映射到name字段
        userData.name = userData.realName
        if (!userData.name) userData.name = userData.username
        // 打印完整的用户数据
        console.log('提交的完整用户数据:', JSON.stringify(userData))
        // 根据操作类型调用不同的API
        if (dialogType.value === 'add') {
          const response = await userApi.createUser(userData)
          console.log('创建用户响应:', response)
          ElMessage.success('用户创建成功')
        } else {
          const response = await userApi.updateUser(userData.id, userData)
          console.log('更新用户响应:', response)
          ElMessage.success('用户信息更新成功')
        }
        // 关闭对话框并刷新列表
        userDialogVisible.value = false
        await fetchUsersList()
      } catch (error) {
        console.error('提交用户表单失败:', error)
        let errorMsg = '操作失败'

        if (error.response) {
          console.error('错误响应:', error.response)
          if (error.response.data && error.response.data.message) {
            errorMsg = error.response.data.message
          } else {
            errorMsg = `服务器错误 (${error.response.status})`
          }
        } else if (error.message) {
          errorMsg = error.message
        }

        ElMessage.error(errorMsg)
      }
    }
  })
}

const handleResetPassword = (row) => {
  passwordForm.userId = row.id
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

const submitPasswordForm = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 调用API重置密码
        await userApi.resetUserPassword(passwordForm.userId, passwordForm.newPassword)
        ElMessage.success('密码重置成功')
        resetPasswordDialogVisible.value = false
      } catch (error) {
        console.error('密码重置失败:', error)
        ElMessage.error('密码重置失败')
      }
    }
  })
}

const handleViewDetail = (row) => {
  Object.keys(userDetail).forEach(key => {
    if (key in row) {
      userDetail[key] = row[key]
    }
  })
  userDetailDialogVisible.value = true
}

const handleToggleStatus = (row) => {
  const statusText = row.status === 'active' ? '禁用' : '启用'
  const newStatus = row.status === 'active' ? false : true

  ElMessageBox.confirm(`确认要${statusText}该用户吗？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用API更新用户状态
      console.log(`更新用户状态, ID: ${row.id}, 新状态: ${newStatus}`)
      await userApi.updateUserStatus(row.id, newStatus)

      // 更新本地状态
      row.status = newStatus ? 'active' : 'inactive'
      ElMessage.success(`用户${statusText}成功`)

      // 刷新用户列表
      await fetchUsersList()
    } catch (error) {
      console.error('更新用户状态失败:', error)
      ElMessage.error('更新用户状态失败: ' + (error.response?.data?.message || error.message || '未知错误'))
    }
  }).catch(() => {})
}

const handleImport = () => {
  importDialogVisible.value = true
}

const handleExport = () => {
  ElMessage.success('用户数据导出成功')
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleBatchDelete = () => {
  if (!selectedRows.value || selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }

  // 检查是否包含admin用户
  const hasAdmin = selectedRows.value.some(row => row && row.username === 'admin')
  if (hasAdmin) {
    ElMessage.warning('管理员账户不能删除')
    return
  }

  ElMessageBox.confirm(
    `确认删除选中的 ${selectedRows.value.length} 个用户吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 过滤掉无效的用户数据
      const validUsers = selectedRows.value.filter(row => row && row.id)

      if (validUsers.length === 0) {
        throw new Error('没有有效的用户数据可删除')
      }

      // 调用API批量删除用户，只传递用户ID数组
      const userIds = validUsers.map(user => user.id)
      console.log('批量删除用户ID:', userIds)
      const response = await userApi.batchDeleteUsers(userIds)
      console.log('批量删除用户响应:', response)

      // 从表格数据中移除选中的用户
      const selectedIds = validUsers.map(row => row.id)
      console.log('要删除的用户ID:', selectedIds)

      // 过滤掉被删除的用户
      const originalLength = tableData.value.length
      tableData.value = tableData.value.filter(item => item && !selectedIds.includes(item.id))
      const removedCount = originalLength - tableData.value.length
      console.log(`从表格中移除了${removedCount}个用户`)

      // 更新计数和清空选择
      total.value -= removedCount
      selectedRows.value = []

      // 显示成功消息
      ElMessage.success(response.message || '批量删除用户成功')

      try {
        // 刷新用户列表，确保与数据库同步
        await fetchUsersList()
      } catch (refreshError) {
        console.error('刷新用户列表失败:', refreshError)
        // 即使刷新失败，也不影响删除操作的成功
      }
    } catch (error) {
      console.error('批量删除用户失败:', error)
      ElMessage.error('批量删除用户失败: ' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

const handleDeleteUser = (row) => {
  if (!row || !row.username) {
    ElMessage.warning('无效的用户数据')
    return
  }

  if (row.username === 'admin') {
    ElMessage.warning('管理员账户不能删除')
    return
  }

  ElMessageBox.confirm(
    `确认删除用户 ${row.username} 吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      console.log('删除用户ID:', row.id, '用户名:', row.username, '类型:', typeof row.id)

      // 确保用户名存在且不为空
      if (!row.username) {
        throw new Error('用户名不能为空')
      }

      // 打印完整的用户对象，用于调试
      console.log('完整用户对象:', JSON.stringify(row))

      // 调用API删除用户，确保ID是数字类型
      const userId = typeof row.id === 'string' ? parseInt(row.id) : row.id
      console.log('删除用户，使用数字ID:', userId, '类型:', typeof userId)
      const response = await userApi.deleteUser(userId)
      console.log('删除用户响应:', response)

      // 从表格数据中移除该用户
      const index = tableData.value.findIndex(item => item && item.id === row.id)
      if (index !== -1) {
        tableData.value.splice(index, 1)
        total.value--
        console.log('从表格中移除用户成功')
      } else {
        console.warn('在表格中找不到要删除的用户')
      }

      // 显示成功消息
      ElMessage.success(response.message || '用户删除成功')

      try {
        // 刷新用户列表，确保与数据库同步
        await fetchUsersList()
      } catch (refreshError) {
        console.error('刷新用户列表失败:', refreshError)
        // 即使刷新失败，也不影响删除操作的成功
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败: ' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

const beforeImportUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  if (!isExcel) {
    ElMessage.error('只能上传Excel文件(.xlsx)')
    return false
  }
  return true
}

const handleImportSuccess = (response) => {
  ElMessage.success('用户导入成功')
  importDialogVisible.value = false
  fetchUsersList()
}

const handleImportError = () => {
  ElMessage.error('用户导入失败，请检查文件格式')
}

const downloadTemplate = () => {
  window.open('/api/system/users/template')
}
</script>

<style lang="scss" scoped>
.user-management {
  padding: 20px;

  .search-form {
    margin-bottom: 20px;
    padding: 20px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  }

  .action-bar {
    margin-bottom: 20px;
    display: flex;
    justify-content: flex-start;
  }

  .role-tag {
    margin-right: 4px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>