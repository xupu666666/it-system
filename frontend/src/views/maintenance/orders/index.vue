<template>
  <div class="maintenance-orders-container">
    <el-card class="search-card">
      <div class="search-container">
        <el-form :model="searchForm" inline>
          <el-form-item label="维修单号">
            <el-input v-model="searchForm.orderNo" placeholder="请输入维修单号" clearable />
          </el-form-item>
          <el-form-item label="申请人">
            <el-input v-model="searchForm.applicant" placeholder="请输入申请人" clearable />
          </el-form-item>
          <el-form-item label="维修类型">
            <el-select v-model="searchForm.type" placeholder="请选择维修类型" clearable>
              <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="申请时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="table-card">
      <div class="table-header">
        <div class="left">
          <el-button type="primary" @click="handleAdd">新增维修单</el-button>
          <el-button :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
          <el-button :disabled="selectedRows.length === 0" @click="handleBatchExport">批量导出</el-button>
        </div>
        <div class="right">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索关键字"
            style="width: 200px"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #suffix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <el-table
        :data="tableData"
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="维修单号" width="120" />
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="department" label="申请部门" width="120" />
        <el-table-column prop="description" label="故障描述" show-overflow-tooltip />
        <el-table-column prop="type" label="维修类型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceCode" label="设备编码" width="120">
          <template #default="scope">
            <span>{{ scope.row.deviceCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" width="120" />
        <el-table-column prop="handler" label="经手人" width="100" />
        <el-table-column prop="cost" label="维修金额" width="100">
          <template #default="scope">
            <span>{{ scope.row.cost ? `¥${scope.row.cost.toFixed(2)}` : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" sortable />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button link type="primary" size="small" @click="handleUpdateStatus(scope.row)" v-if="scope.row.status !== '已付款'">
              {{ getNextStatusButton(scope.row.status) }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑维修单弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="650px">
      <el-form
        ref="orderFormRef"
        :model="orderForm"
        :rules="orderRules"
        label-width="100px"
        status-icon
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="维修单号" prop="orderNo">
              <el-input v-model="orderForm.orderNo" placeholder="请输入维修单号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请人" prop="applicant">
              <el-input v-model="orderForm.applicant" placeholder="请输入申请人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申请部门" prop="department">
              <el-select
                v-model="orderForm.department"
                placeholder="请选择申请部门"
                filterable
                style="width: 100%"
              >
                <el-option v-for="item in departmentOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经手人" prop="handler">
              <el-select v-model="orderForm.handler" placeholder="请选择经手人" style="width: 100%">
                <el-option v-for="item in handlerOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维修类型" prop="type">
              <el-select v-model="orderForm.type" placeholder="请选择维修类型" style="width: 100%">
                <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备编码" prop="deviceCode">
              <el-input v-model="orderForm.deviceCode" placeholder="请输入设备编码" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplier">
              <el-input
                v-model="orderForm.supplier"
                placeholder="请输入供应商"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否走账" prop="needInvoice">
              <el-radio-group v-model="orderForm.needInvoice">
                <el-radio :label="true">是</el-radio>
                <el-radio :label="false">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障描述" prop="description">
              <el-input
                v-model="orderForm.description"
                type="textarea"
                :rows="4"
                placeholder="请描述故障情况"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="orderForm.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent, ref, reactive, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMaintenanceList,
  getMaintenanceDetail,
  createMaintenanceOrder,
  updateMaintenanceOrder,
  deleteMaintenanceOrder,
  batchDeleteMaintenanceOrders,
  updateMaintenanceStatus,
  exportMaintenanceOrders
} from '@/api/maintenance'
import emitter from '@/utils/eventBus'

export default defineComponent({
  name: 'MaintenanceOrders',
  components: {
    Search
  },
  setup() {
    // 表格加载状态
    const loading = ref(false)

    // 搜索表单
    const searchForm = reactive({
      orderNo: '',
      applicant: '',
      type: '',
      status: '',
      dateRange: [],
      keyword: ''
    })

    // 分页参数
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })

    // 表格选中行
    const selectedRows = ref([])

    // 表格数据
    const tableData = ref([])

    // 对话框参数
    const dialogVisible = ref(false)
    const dialogTitle = ref('新增维修单')
    const isEdit = ref(false)

    // 维修单表单
    const orderForm = reactive({
      id: '',
      orderNo: '',
      applicant: '',
      department: '',
      description: '',
      type: '',
      deviceCode: '', // 将金额改为设备编码
      supplier: '',
      handler: '',
      status: '待处理',
      createTime: '',
      needInvoice: true,
      remark: ''
    })

    // 表单校验规则
    const orderRules = {
      orderNo: [
        { required: true, message: '请输入维修单号', trigger: 'blur' },
        { pattern: /^[A-Za-z0-9-_]+$/, message: '维修单号只能包含字母、数字、横线和下划线', trigger: 'blur' }
      ],
      applicant: [
        { required: true, message: '请输入申请人', trigger: 'blur' }
      ],
      department: [
        { required: true, message: '请选择申请部门', trigger: 'change' }
      ],
      handler: [
        { required: true, message: '请选择经手人', trigger: 'change' }
      ],
      type: [
        { required: true, message: '请选择维修类型', trigger: 'change' }
      ],
      deviceCode: [
        { required: true, message: '请输入设备编码', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请描述故障情况', trigger: 'blur' }
      ]
    }

    // 下拉选项
    const typeOptions = [
      { value: '硬件维修', label: '硬件维修' },
      { value: '软件故障', label: '软件故障' },
      { value: '网络问题', label: '网络问题' },
      { value: '打印设备', label: '打印设备' },
      { value: '显示设备', label: '显示设备' },
      { value: '其他', label: '其他' }
    ]

    const statusOptions = [
      { value: '待处理', label: '待处理' },
      { value: '维修中', label: '维修中' },
      { value: '已完成', label: '已完成' },
      { value: '已付款', label: '已付款' }
    ]

    const departmentOptions = [
      { value: 'APU-PCD', label: 'APU-PCD' },
      { value: 'APU-PD1', label: 'APU-PD1' },
      { value: 'APU-PD2', label: 'APU-PD2' },
      { value: 'APU-PD3', label: 'APU-PD3' },
      { value: 'APU-SMT', label: 'APU-SMT' },
      { value: 'GCM', label: 'GCM' },
      { value: 'Site Procurement', label: 'Site Procurement' },
      { value: 'PPM', label: 'PPM' },
      { value: 'ICT', label: 'ICT' },
      { value: 'MIS', label: 'MIS' },
      { value: 'Regional Team', label: 'Regional Team' },
      { value: 'Finance', label: 'Finance' },
      { value: 'TSD', label: 'TSD' },
      { value: 'Engineering', label: 'Engineering' },
      { value: 'SCM', label: 'SCM' },
      { value: 'QAD', label: 'QAD' },
      { value: 'HR', label: 'HR' },
      { value: 'ETD', label: 'ETD' },
      { value: 'Commercial - Sales', label: 'Commercial - Sales' },
      { value: 'Commercial - BU', label: 'Commercial - BU' },
      { value: 'Logistic', label: 'Logistic' },
      { value: 'PMD', label: 'PMD' },
      { value: 'D&D', label: 'D&D' },
      { value: 'GM Office', label: 'GM Office' }
    ]

    const handlerOptions = [
      { value: '蔡海林', label: '蔡海林' },
      { value: '郭松岳', label: '郭松岳' },
      { value: '徐普', label: '徐普' }
    ]

    // 供应商已改为手动输入，不再需要选项列表
    // 保留变量以避免引用错误
    const supplierOptions = []

    // 获取维修类型标签样式
    const getTypeTagType = (type) => {
      const typeMap = {
        '硬件维修': 'danger',
        '软件故障': 'warning',
        '网络问题': 'info',
        '打印设备': 'success',
        '显示设备': 'primary',
        '其他': ''
      }
      return typeMap[type] || ''
    }

    // 获取状态标签样式
    const getStatusTagType = (status) => {
      const statusMap = {
        '待处理': 'info',
        '维修中': 'warning',
        '已完成': 'success',
        '已付款': 'primary'
      }
      return statusMap[status] || ''
    }

    // 获取下一状态按钮文字
    const getNextStatusButton = (status) => {
      const statusMap = {
        '待处理': '开始维修',
        '维修中': '标记完成',
        '已完成': '标记付款',
        '已付款': ''
      }
      return statusMap[status] || ''
    }

    // 初始化表格数据
    const initTableData = () => {
      loading.value = true

      // 构建查询参数
      const params = {
        page: pagination.currentPage - 1, // 后端页码从0开始
        size: pagination.pageSize,
        orderNo: searchForm.orderNo || undefined,
        applicant: searchForm.applicant || undefined,
        type: searchForm.type || undefined,
        status: searchForm.status || undefined
      }

      // 添加日期范围参数
      if (searchForm.dateRange && searchForm.dateRange.length === 2) {
        params.startDate = searchForm.dateRange[0]
        params.endDate = searchForm.dateRange[1]
      }

      // 添加关键字搜索
      if (searchForm.keyword) {
        params.keyword = searchForm.keyword
      }

      // 调用后端API获取数据
      try {
        console.log('开始获取维修单列表，参数:', params)
        getMaintenanceList(params)
          .then(response => {
            console.log('维修单列表原始响应:', response)

            // 直接使用响应数据，不需要再访问 response.data
            if (response) {
              // 检查响应格式
              let content = []
              let totalElements = 0

              if (Array.isArray(response)) {
                // 直接返回数组的情况
                console.log('响应是数组格式')
                content = response
                totalElements = response.length
              } else if (response.content) {
                // 包含 content 字段的对象
                console.log('响应包含 content 字段')
                content = response.content
                totalElements = response.totalElements || 0
              } else {
                // 其他情况，尝试使用整个响应
                console.log('使用整个响应作为数据')
                content = [response]
                totalElements = 1
              }

              console.log('提取的内容:', content)

              // 转换后端数据格式为前端所需格式
              tableData.value = content.map(item => {
                console.log('处理单个项目:', item)
                return {
                  id: item.id,
                  orderNo: item.orderNo,
                  applicant: item.applicant,
                  department: item.department,
                  description: item.description,
                  type: item.type,
                  deviceCode: item.equipmentCode || '', // 使用设备编码
                  supplier: item.supplier,
                  handler: item.handler,
                  cost: item.cost ? parseFloat(item.cost) : null, // 添加维修金额
                  status: convertStatus(item.status),
                  createTime: item.createdAt ? new Date(item.createdAt).toLocaleString() : '',
                  needInvoice: item.isWalkingBill,
                  remark: item.notes
                }
              })

              // 设置分页总数
              pagination.total = totalElements

              console.log('处理后的表格数据:', tableData.value)
            } else {
              // 如果没有数据或数据格式不正确，显示空表格
              console.log('响应为空:', response)
              tableData.value = []
              pagination.total = 0
            }
          })
          .catch(error => {
            // 静默处理错误，不显示任何错误消息
            console.error('获取维修单列表出错:', error)
            tableData.value = []
            pagination.total = 0
          })
          .finally(() => {
            loading.value = false
          })
      } catch (error) {
        // 捕获任何可能的错误，确保不会显示错误提示
        console.log('维修单列表加载过程中出现错误，但不显示错误提示')
        tableData.value = []
        pagination.total = 0
        loading.value = false
      }
    }

    // 状态转换函数（后端枚举值转为前端显示值）
    const convertStatus = (status) => {
      const statusMap = {
        'PENDING': '待处理',
        'IN_PROGRESS': '维修中',
        'COMPLETED': '已完成',
        'PAID': '已付款',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    }

    // 状态转换函数（前端显示值转为后端枚举值）
    const convertStatusToBackend = (status) => {
      const statusMap = {
        '待处理': 'PENDING',
        '维修中': 'IN_PROGRESS',
        '已完成': 'COMPLETED',
        '已付款': 'PAID',
        '已取消': 'CANCELLED'
      }
      return statusMap[status] || status
    }

    // 处理搜索
    const handleSearch = () => {
      pagination.currentPage = 1
      initTableData()
    }

    // 重置搜索条件
    const resetSearch = () => {
      Object.keys(searchForm).forEach(key => {
        searchForm[key] = key === 'dateRange' ? [] : ''
      })
      handleSearch()
    }

    // 处理分页变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      initTableData()
    }

    const handleCurrentChange = (page) => {
      pagination.currentPage = page
      initTableData()
    }

    // 表格选择变化
    const handleSelectionChange = (selection) => {
      selectedRows.value = selection
    }

    // 新增维修单
    const handleAdd = () => {
      dialogTitle.value = '新增维修单'
      isEdit.value = false

      // 重置表单
      Object.keys(orderForm).forEach(key => {
        orderForm[key] = key === 'status' ? '待处理' : key === 'needInvoice' ? true : ''
      })

      // 不再自动生成订单号，让用户手动输入
      orderForm.orderNo = ''

      dialogVisible.value = true
    }

    // 编辑维修单
    const handleEdit = (row) => {
      dialogTitle.value = '编辑维修单'
      isEdit.value = true

      // 拷贝数据
      Object.keys(orderForm).forEach(key => {
        orderForm[key] = row[key]
      })

      dialogVisible.value = true
    }

    // 查看详情
    const handleDetail = (row) => {
      ElMessage.info(`查看维修单${row.orderNo}的详情`)
      // TODO: 实现详情页面
    }

    // 删除维修单
    const handleDelete = async (row) => {
      try {
        await deleteMaintenanceOrder(row.id)
            ElMessage.success('删除成功')
        emitter.emit('refresh-maintenance-statistics')
            initTableData()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    }

    // 批量删除
    const handleBatchDelete = () => {
      if (selectedRows.value.length === 0) return

      ElMessageBox.confirm(
        `确认删除已选中的 ${selectedRows.value.length} 条记录吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        loading.value = true
        const ids = selectedRows.value.map(row => row.id)
        batchDeleteMaintenanceOrders(ids)
          .then(() => {
            ElMessage.success(`成功删除 ${selectedRows.value.length} 条记录`)
            emitter.emit('refresh-maintenance-statistics')
            initTableData()
          })
          .catch(error => {
            console.error('批量删除维修单失败:', error)
            ElMessage.error('批量删除维修单失败，请稍后重试')
          })
          .finally(() => {
            loading.value = false
          })
      }).catch(() => {})
    }

    // 批量导出
    const handleBatchExport = () => {
      if (selectedRows.value.length === 0) return

      loading.value = true
      const ids = selectedRows.value.map(row => row.id)

      // 调用导出API
      exportMaintenanceOrders({ ids: ids.join(',') })
        .then(response => {
          // 创建Blob对象
          const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })

          // 创建下载链接
          const link = document.createElement('a')
          link.href = URL.createObjectURL(blob)
          link.download = `维修单导出_${new Date().toLocaleDateString()}.xlsx`
          link.click()

          // 释放URL对象
          URL.revokeObjectURL(link.href)

          ElMessage.success(`已导出 ${selectedRows.value.length} 条记录`)
        })
        .catch(error => {
          console.error('导出维修单失败:', error)
          ElMessage.error('导出维修单失败，请稍后重试')
        })
        .finally(() => {
          loading.value = false
        })
    }

    // 更新状态
    const handleUpdateStatus = (row) => {
      const statusFlow = {
        '待处理': '维修中',
        '维修中': '已完成',
        '已完成': '已付款'
      }

      const nextStatus = statusFlow[row.status]
      if (!nextStatus) return

      // 如果下一个状态是"已付款"，需要输入维修金额
      if (nextStatus === '已付款') {
        ElMessageBox.prompt(
          `请输入维修单 ${row.orderNo} 的维修金额：`,
          '输入维修金额',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPattern: /^[0-9]+(\.[0-9]{1,2})?$/,
            inputErrorMessage: '请输入有效的金额（最多两位小数）',
            inputPlaceholder: '例如：100.50',
            type: 'info'
          }
        ).then(({ value }) => {
          loading.value = true
          // 转换为后端状态值
          const backendStatus = convertStatusToBackend(nextStatus)
          // 调用API更新状态，同时传递维修金额
          updateMaintenanceStatus(row.id, backendStatus, `状态更新为${nextStatus}，维修金额：${value}元`, parseFloat(value))
            .then(() => {
              row.status = nextStatus
              // 更新本地显示的金额
              row.cost = parseFloat(value)
              ElMessage.success(`状态已更新为"${nextStatus}"，维修金额：${value}元`)
              // 触发统计页面刷新
              emitter.emit('refresh-maintenance-statistics')
            })
            .catch(error => {
              console.error('更新状态失败:', error)
              ElMessage.error('更新状态失败，请稍后重试')
            })
            .finally(() => {
              loading.value = false
            })
        }).catch(() => {})
      } else {
        // 其他状态变更，不需要输入金额
        ElMessageBox.confirm(
          `确认将维修单 ${row.orderNo} 状态修改为"${nextStatus}"吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info'
          }
        ).then(() => {
          loading.value = true
          // 转换为后端状态值
          const backendStatus = convertStatusToBackend(nextStatus)
          // 调用API更新状态
          updateMaintenanceStatus(row.id, backendStatus, `状态更新为${nextStatus}`)
            .then(() => {
              row.status = nextStatus
              ElMessage.success(`状态已更新为"${nextStatus}"`)
            })
            .catch(error => {
              console.error('更新状态失败:', error)
              ElMessage.error('更新状态失败，请稍后重试')
            })
            .finally(() => {
              loading.value = false
            })
        }).catch(() => {})
      }
    }

    // 表单引用
    const orderFormRef = ref(null)

    // 提交表单
    const submitForm = () => {
      // 表单验证
      orderFormRef.value.validate(valid => {
        if (!valid) {
          ElMessage.warning('请完善表单信息')
          return
        }

        loading.value = true

        // 准备提交数据
        const submitData = {
          id: orderForm.id,
          orderNo: orderForm.orderNo.trim(), // 确保去除前后空格
          applicant: orderForm.applicant,
          department: orderForm.department,
          description: orderForm.description,
          type: orderForm.type,
          assetId: orderForm.deviceCode.trim(), // 设备编码映射到后端的assetId
          supplier: orderForm.supplier,
          handler: orderForm.handler,
          status: convertStatusToBackend(orderForm.status),
          isWalkingBill: orderForm.needInvoice,
          notes: orderForm.remark
        }

        // 根据是否编辑模式选择API
        const apiCall = isEdit.value
          ? updateMaintenanceOrder(orderForm.id, submitData)
          : createMaintenanceOrder(submitData)

        apiCall
          .then(() => {
            dialogVisible.value = false
            ElMessage.success(isEdit.value ? '维修单更新成功' : '维修单创建成功')
            emitter.emit('refresh-maintenance-statistics')
            initTableData()
          })
          .catch(error => {
            console.error(isEdit.value ? '更新维修单失败:' : '创建维修单失败:', error)
            ElMessage.error(isEdit.value ? '更新维修单失败，请稍后重试' : '创建维修单失败，请稍后重试')
          })
          .finally(() => {
            loading.value = false
          })
      })
    }

    onMounted(() => {
      initTableData()
    })

    return {
      loading,
      searchForm,
      pagination,
      selectedRows,
      tableData,
      dialogVisible,
      dialogTitle,
      orderForm,
      orderRules,
      orderFormRef,
      typeOptions,
      statusOptions,
      departmentOptions,
      handlerOptions,
      supplierOptions,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      handleAdd,
      handleEdit,
      handleDetail,
      handleDelete,
      handleBatchDelete,
      handleBatchExport,
      handleUpdateStatus,
      submitForm,
      getTypeTagType,
      getStatusTagType,
      getNextStatusButton
    }
  }
})
</script>

<style lang="scss" scoped>
.maintenance-orders-container {
  .search-card {
    margin-bottom: 15px;
  }

  .table-card {
    .table-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 15px;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  /* 设备编码不需要特殊样式 */
}
</style>