<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <h2>资产盘点任务管理</h2>
          <el-button type="primary" @click="showCreateTask = true">新建盘点任务</el-button>
        </div>
      </template>
      <div class="card-content">
        <el-form :inline="true" size="small" class="filter-form">
          <el-form-item label="工厂">
            <el-select v-model="filter.factory" placeholder="请选择工厂" style="width: 120px">
              <el-option label="PS" value="PS" />
              <el-option label="KC" value="KC" />
            </el-select>
          </el-form-item>
          <el-form-item label="部门">
            <el-input v-model="filter.department" placeholder="请输入部门" style="width: 160px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchTasks">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="tasks" style="width: 100%; margin-top: 20px" border>
          <!-- 隐藏ID列 -->
          <el-table-column label="工厂" width="80">
            <template #default="scope">
              {{ scope.row.factory }}
            </template>
          </el-table-column>
          <el-table-column label="部门" width="120">
            <template #default="scope">
              {{ scope.row.department }}
            </template>
          </el-table-column>
          <el-table-column label="任务名称">
            <template #default="scope">
              {{ scope.row.taskName }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              {{ scope.row.status }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240">
            <template #default="scope">
              <el-button size="small" @click="viewTask(scope.row)">查看</el-button>
              <el-button size="small" type="success" @click="completeTask(scope.row.id)" v-if="scope.row.status === '待盘点'">完成</el-button>
              <el-button size="small" type="danger" @click="deleteTask(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 新建任务弹窗 -->
    <el-dialog v-model="showCreateTask" title="新建盘点任务" width="400px">
      <el-form :model="newTask" label-width="80px">
        <el-form-item label="工厂">
          <el-select v-model="newTask.factory" placeholder="请选择工厂">
            <el-option label="PS" value="PS" />
            <el-option label="KC" value="KC" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="newTask.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="任务名称">
          <el-input v-model="newTask.taskName" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateTask = false">取消</el-button>
        <el-button type="primary" @click="createTask">创建</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情弹窗 -->
    <el-dialog v-model="showTaskDetail" :title="'盘点任务详情 - ' + (currentTask?.taskName || '')" width="900px">
      <div v-if="currentTask">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工厂">{{ currentTask.factory }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ currentTask.department }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ currentTask.taskName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ currentTask.status }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>盘点记录</el-divider>
        <div class="action-bar" style="margin-bottom: 15px; display: flex; justify-content: space-between;">
          <div>
            <el-upload
              action="#"
              :auto-upload="false"
              :show-file-list="true"
              :limit="1"
              :on-change="handleFileChange"
              :on-exceed="handleExceed"
              accept=".xlsx,.xls"
              style="display: inline-block; margin-right: 10px;"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon> 选择Excel文件
              </el-button>
            </el-upload>
            <el-button type="success" @click="uploadExcel" :disabled="!excelFile">
              <el-icon><Check /></el-icon> 上传并导入资产
            </el-button>
            <el-button type="info" @click="downloadTemplate">
              <el-icon><Download /></el-icon> 下载导入模板
            </el-button>
          </div>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索资产编号或名称"
              style="width: 200px; margin-right: 10px;"
              clearable
              @clear="filterRecords"
              @input="filterRecords"
            />
            <el-button type="primary" @click="filterRecords">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
          </div>
        </div>
        <el-table :data="filteredRecords" style="width: 100%" border>
          <!-- 隐藏资产ID列 -->
          <el-table-column label="资产编号" width="120">
            <template #default="scope">
              {{ scope.row.asset?.assetNo || '未知编号' }}
            </template>
          </el-table-column>
          <el-table-column label="资产名称" width="150">
            <template #default="scope">
              {{ scope.row.asset?.name || '未知资产' }}
            </template>
          </el-table-column>
          <el-table-column prop="checkStatus" label="盘点结果" width="100">
            <template #default="scope">
              <el-select v-model="scope.row.checkStatus" placeholder="请选择" @change="editRecord(scope.row)">
                <el-option label="正常" value="正常" />
                <el-option label="盘盈" value="盘盈" />
                <el-option label="盘亏" value="盘亏" />
                <el-option label="报废" value="报废" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="actualUser" label="实际使用人" width="120">
            <template #default="scope">
              <el-input
                v-model="scope.row.actualUser"
                @blur="editRecord(scope.row)"
                class="auto-filled-input"
                :placeholder="scope.row.actualUser ? '' : '系统未找到使用人'"
              />
            </template>
          </el-table-column>
          <el-table-column prop="actualLocation" label="实际地点" width="150">
            <template #default="scope">
              <el-input
                v-model="scope.row.actualLocation"
                @blur="editRecord(scope.row)"
                class="auto-filled-input"
                :placeholder="scope.row.actualLocation ? '' : '系统未找到位置'"
              />
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注">
            <template #default="scope">
              <el-input v-model="scope.row.remark" @blur="editRecord(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button
                size="small"
                type="info"
                @click="checkAssetDetails(scope.row.asset)"
              >
                查看源数据
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="tip-info" style="margin-top: 10px; font-size: 13px; color: #606266;">
          <el-icon><InfoFilled /></el-icon> 提示：系统已根据资产库信息自动填充"实际使用人"和"实际地点"，如有变更请直接修改。
        </div>
      </div>
      <template #footer>
        <el-button @click="showTaskDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { Upload, Download, Check, Search, InfoFilled } from '@element-plus/icons-vue'
import axios from 'axios'

const filter = reactive({ factory: '', department: '' })
const tasks = ref([])
const showCreateTask = ref(false)
const newTask = reactive({ factory: '', department: '', taskName: '' })
const showTaskDetail = ref(false)
const currentTask = ref(null)
const records = ref([])
const excelFile = ref(null)
const searchKeyword = ref('')

// 根据搜索关键字过滤记录
const filteredRecords = computed(() => {
  if (!searchKeyword.value) {
    // 确保每条记录都有资产信息
    return records.value.map(record => ensureAssetInfo(record));
  }

  const keyword = searchKeyword.value.toLowerCase();
  return records.value
    .filter(record => {
      // 安全访问资产属性，如果资产信息不存在则使用默认值
      const assetNo = record.asset?.assetNo?.toLowerCase() || '';
      const assetName = record.asset?.name?.toLowerCase() || '';
      const checkStatus = record.checkStatus?.toLowerCase() || '';
      const actualUser = record.actualUser?.toLowerCase() || '';
      const actualLocation = record.actualLocation?.toLowerCase() || '';

      // 扩大搜索范围，包括盘点结果、实际使用人和位置
      return assetNo.includes(keyword) ||
             assetName.includes(keyword) ||
             checkStatus.includes(keyword) ||
             actualUser.includes(keyword) ||
             actualLocation.includes(keyword);
    })
    .map(record => ensureAssetInfo(record)); // 确保每条记录都有资产信息
})

// 页面加载时初始化数据
onMounted(() => {
  console.log('组件加载完成，初始化数据...')
  fetchTasks()
})

function fetchTasks() {
  console.log('=== 开始获取任务列表 ===');

  // 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: '加载任务列表...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  let url = '/api/inventory-check-task'
  if (filter.factory && filter.department) {
    url = `/api/inventory-check-task/search?factory=${filter.factory}&department=${filter.department}`
  }

  console.log('请求URL:', url)

  // 增加时间戳参数，防止缓存问题
  const timestamp = new Date().getTime()
  const separator = url.includes('?') ? '&' : '?'
  url = `${url}${separator}_t=${timestamp}`

  console.log('添加时间戳后的URL:', url)

  axios.get(url)
    .then(res => {
      console.log('获取任务列表原始响应:', res)

      let data = res.data;

      // 如果数据是字符串格式，尝试解析为JSON对象
      if (typeof data === 'string') {
        try {
          // 首先尝试直接解析，有些情况下后端已经修复了循环引用问题
          try {
            data = JSON.parse(data);
            console.log('直接解析JSON成功');
          } catch (directError) {
            console.log('直接解析JSON失败，尝试清理后再解析');

            // 更安全的方法：使用正则表达式处理嵌套问题
            let cleanString = data;

            // 1. 先处理极端情况，如果超过一定长度，直接截断
            if (cleanString.length > 10000) {
              console.log('JSON字符串过长，执行截断处理');
              // 查找第一个有效JSON数组结束位置
              const firstArrayEnd = cleanString.indexOf('}]');
              if (firstArrayEnd > 0) {
                cleanString = cleanString.substring(0, firstArrayEnd + 2);
              } else {
                // 如果找不到数组结束，尝试获取第一个对象
                const firstObjEnd = cleanString.indexOf('}');
                if (firstObjEnd > 0) {
                  cleanString = cleanString.substring(0, firstObjEnd + 1);
                } else {
                  cleanString = '[]'; // 最后手段
                }
              }
            }

            // 2. 尝试简单替换，处理可能的null值和records循环引用
            cleanString = cleanString
              .replace(/,\s*"records":\[.*?\]/g, ',"records":[]')
              .replace(/,\s*"records":\[.*$/g, ',"records":[]}]')  // 处理不完整的JSON
              .replace(/:\s*null\s*,/g, ':null,')
              .replace(/:\s*null\s*}/g, ':null}')
              .replace(/,\s*}/g, '}'); // 修复可能的尾部逗号

            console.log('清理后的数据字符串:', cleanString.substring(0, 100) + '...');

            try {
              data = JSON.parse(cleanString);
              console.log('清理后解析JSON成功');
            } catch (cleanError) {
              console.error('清理后解析仍然失败:', cleanError);

              // 最后手段：提取有效的任务数据
              const taskPattern = /"id":\s*(\d+),\s*"factory":\s*"([^"]+)",\s*"department":\s*"([^"]+)",\s*"taskName":\s*"([^"]+)"/g;
              const tasks = [];
              let match;

              while ((match = taskPattern.exec(data)) !== null) {
                tasks.push({
                  id: parseInt(match[1]),
                  factory: match[2],
                  department: match[3],
                  taskName: match[4],
                  status: '待盘点' // 默认状态
                });
              }

              if (tasks.length > 0) {
                console.log('使用正则表达式提取到', tasks.length, '个任务');
                data = tasks;
              } else {
                console.error('所有解析方法都失败，设置为空数组');
                data = [];
              }
            }
          }
        } catch (e) {
          console.error('处理JSON字符串所有尝试均失败:', e);
          data = [];
        }
      }

      // 确保数据是数组类型
      if (Array.isArray(data)) {
        tasks.value = data;
        console.log('任务数组长度:', tasks.value.length)
        if (tasks.value.length > 0) {
          console.log('第一个任务示例:', JSON.stringify(tasks.value[0], null, 2))
          // 检查任务对象的属性
          console.log('任务属性:', Object.keys(tasks.value[0]))
          console.log('工厂:', tasks.value[0].factory)
          console.log('部门:', tasks.value[0].department)
          console.log('任务名称:', tasks.value[0].taskName)
          console.log('状态:', tasks.value[0].status)
        } else {
          console.log('任务列表为空')
        }
      } else {
        console.warn('处理后的数据不是数组类型:', typeof data);
        // 尝试从对象中提取数组
        if (data && typeof data === 'object') {
          if (Array.isArray(data.content)) {
            tasks.value = data.content;
            console.log('从对象中提取到数组，长度:', tasks.value.length);
          } else {
            // 尝试将对象转为数组
            const possibleArray = Object.values(data).find(item => Array.isArray(item));
            if (possibleArray) {
              tasks.value = possibleArray;
              console.log('从对象的值中找到数组，长度:', tasks.value.length);
            } else {
              // 最后尝试将单个对象包装成数组
              tasks.value = [data];
              console.log('将单个对象包装为数组');
            }
          }
        } else {
          tasks.value = []; // 设置为空数组
          console.log('无法处理的数据类型，设置为空数组');
        }
      }
      console.log('获取任务列表成功，最终任务数量:', tasks.value.length);

      // 检查每个任务的属性
      if (tasks.value.length > 0) {
        tasks.value.forEach((task, index) => {
          console.log(`任务 ${index + 1}:`, {
            id: task.id,
            factory: task.factory,
            department: task.department,
            taskName: task.taskName,
            status: task.status
          });
        });
      }

      console.log('=== 任务列表获取完成 ===');
    })
    .catch(error => {
      console.error('获取任务列表失败，详细错误:', error);
      if (error.response) {
        console.error('错误状态码:', error.response.status);
        console.error('错误数据:', error.response.data);
      }
      ElMessage.error('获取任务列表失败，请稍后再试');
      tasks.value = []; // 清空任务列表
    })
    .finally(() => {
      loading.close()
    })
}

function resetFilter() {
  filter.factory = ''
  filter.department = ''
  fetchTasks()
}

function createTask() {
  if (!newTask.factory || !newTask.department) {
    ElMessage.warning('请填写工厂和部门')
    return
  }

  // 打印要发送的数据
  console.log('准备创建盘点任务:', JSON.stringify(newTask))

  // 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: '创建任务中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  // 创建任务时确保taskName不为空
  if (!newTask.taskName || newTask.taskName.trim() === '') {
    newTask.taskName = `${newTask.factory}${newTask.department}盘点${new Date().toISOString().substring(0, 10)}`
    console.log('自动生成任务名称:', newTask.taskName)
  }

  axios.post('/api/inventory-check-task', newTask)
    .then(res => {
      console.log('创建任务成功，响应数据:', res.data)
      ElMessage.success('创建成功')
      showCreateTask.value = false

      // 重置表单
      newTask.factory = ''
      newTask.department = ''
      newTask.taskName = ''

      // 刷新任务列表
      console.log('准备刷新任务列表...')

      // 添加延时确保后端处理完成
      setTimeout(() => {
        fetchTasks()

        // 如果任务创建成功但列表刷新后仍为空，尝试再次刷新
        if (tasks.value.length === 0) {
          console.log('任务列表为空，3秒后再次尝试刷新...')
          setTimeout(fetchTasks, 3000)
        }
      }, 500)
    })
    .catch(error => {
      console.error('创建任务失败:', error)
      // 显示更详细的错误信息
      if (error.response) {
        console.error('错误状态码:', error.response.status)
        console.error('错误数据:', error.response.data)
        const errorMsg = error.response.data && error.response.data.message
          ? error.response.data.message
          : '创建任务失败，请稍后再试'
        ElMessage.error(errorMsg)
      } else {
        ElMessage.error('创建任务失败，请稍后再试')
      }
    })
    .finally(() => {
      loading.close()
    })
}

function deleteTask(id) {
  ElMessageBox.confirm('确定要删除该任务吗？', '提示', { type: 'warning' })
    .then(() => {
      // 显示加载状态
      const loading = ElLoading.service({
        lock: true,
        text: '删除任务中...',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      axios.delete(`/api/inventory-check-task/${id}`)
        .then(() => {
          ElMessage.success('删除成功')
          fetchTasks()
        })
        .catch(error => {
          console.error('删除任务失败:', error)
          // 显示更详细的错误信息
          const errorMsg = error.response && error.response.data && error.response.data.message
            ? error.response.data.message
            : '删除任务失败，请稍后再试'
          ElMessage.error(errorMsg)
          // 打印详细的错误信息以便调试
          if (error.response) {
            console.error('错误状态码:', error.response.status)
            console.error('错误数据:', error.response.data)
          }
        })
        .finally(() => {
          loading.close()
        })
    })
    .catch(() => {
      // 用户取消删除操作
      console.log('用户取消了删除操作')
    })
}

function viewTask(task) {
  currentTask.value = task
  showTaskDetail.value = true

  // 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: '加载盘点记录中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  axios.get(`/api/inventory-check-record/by-task/${task.id}`)
    .then(res => {
      // 使用增强的记录处理函数处理响应数据
      records.value = processRecordResponse(res.data);
      console.log('获取盘点记录成功, 长度:', records.value.length);
    })
    .catch(error => {
      console.error('获取盘点记录失败:', error);
      // 显示友好的错误提示
      ElMessage.error('获取盘点记录失败，请稍后再试');
      // 设置空记录，避免显示旧数据
      records.value = [];
    })
    .finally(() => {
      // 关闭加载状态
      loading.close()
    })
}

function editRecord(record) {
  // 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: '保存中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  axios.put(`/api/inventory-check-record/${record.id}`, record)
    .then(() => {
      console.log('更新盘点记录成功')
      // 不显示成功消息，避免频繁弹窗
    })
    .catch(error => {
      console.error('更新盘点记录失败:', error)
      ElMessage.error('更新盘点记录失败，请稍后再试')
    })
    .finally(() => {
      loading.close()
    })
}

// 处理文件选择
function handleFileChange(file) {
  if (file) {
    excelFile.value = file.raw
    console.log('已选择文件:', file.name)
  } else {
    excelFile.value = null
  }
}

// 处理文件数量超出限制
function handleExceed() {
  ElMessage.warning('只能上传一个文件')
}

// 过滤记录
function filterRecords() {
  console.log('过滤记录，关键字:', searchKeyword.value)
  // 过滤逻辑已在computed属性中实现
}

// 增强处理资产导入后的记录列表
function processRecordResponse(response) {
  // 先尝试转换响应
  let data = response;

  if (typeof data === 'string') {
    try {
      // 1. 先尝试直接解析
      data = JSON.parse(data);
    } catch (e) {
      console.error('直接解析JSON失败:', e);

      // 2. 尝试提取有效的JSON部分
      try {
        // 尝试获取任务ID，用于构建最小化的任务对象
        const taskId = currentTask.value?.id || 0;
        // 清理掉无限循环引用
        const cleanString = data
          .replace(/"task":\{.*?\},/g, `"task":{"id":${taskId}},`)
          .replace(/"records":\[.*?\],/g, '"records":[],')
          .replace(/,\s*\}/g, '}');

        console.log('清理后的数据前150字符:', cleanString.substring(0, 150) + '...');
        data = JSON.parse(cleanString);
      } catch (cleanError) {
        console.error('提取有效JSON部分失败:', cleanError);

        // 3. 使用正则表达式直接提取关键信息
        try {
          const records = [];
          const extractRecords = /\[\s*\{[^\[\]]*\}\s*\]/g.exec(data);
          if (extractRecords && extractRecords[0]) {
            try {
              // 提取出疑似数组的部分
              const possibleArrayStr = extractRecords[0]
                .replace(/"task":\{.*?\}/g, `"task":{"id":${currentTask.value?.id || 0}}`)
                .replace(/"records":\[.*?\]/g, '"records":[]');
              records.push(...JSON.parse(possibleArrayStr));
              console.log('成功使用正则提取数组部分');
            } catch (e) {
              console.error('解析提取的数组部分失败:', e);
            }
          }

          if (records.length === 0) {
            // 尝试逐个记录提取
            const regex = /"id":(\d+).*?"checkStatus":"([^"]*)"/g;
            let match;

            while ((match = regex.exec(data)) !== null) {
              records.push({
                id: parseInt(match[1]),
                task: { id: currentTask.value?.id || 0 },
                checkStatus: match[2] || '正常',
                asset: {
                  id: 0,
                  assetNo: '导入的资产',
                  name: '导入的资产'
                }
              });
            }
          }

          if (records.length > 0) {
            console.log('成功提取到', records.length, '个资产记录');
            data = records;
          } else {
            console.error('未能提取到任何资产记录');
            data = [];
          }
        } catch (regexError) {
          console.error('使用正则表达式提取失败:', regexError);
          data = [];
        }
      }
    }
  }

  // 确保数据是数组类型
  if (Array.isArray(data)) {
    return data.map(record => ensureAssetInfo(record));
  } else {
    console.warn('最终处理后的数据不是数组:', data);
    return []; // 设置为空数组
  }
}

// 上传Excel文件并导入资产
function uploadExcel() {
  if (!excelFile.value) {
    ElMessage.warning('请先选择Excel文件')
    return
  }

  if (!currentTask.value || !currentTask.value.id) {
    ElMessage.warning('当前任务信息不完整')
    return
  }

  // 检查文件类型
  const fileType = excelFile.value.type
  if (fileType !== 'application/vnd.ms-excel' &&
      fileType !== 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
    ElMessage.warning('请选择Excel文件(.xls或.xlsx)')
    console.error('不支持的文件类型:', fileType)
    return
  }

  // 检查文件大小
  if (excelFile.value.size > 10 * 1024 * 1024) { // 10MB
    ElMessage.warning('文件大小不能超过10MB')
    return
  }

  // 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: '正在上传并处理Excel文件...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  console.log('开始上传Excel文件，任务ID:', currentTask.value.id)

  const formData = new FormData()
  formData.append('file', excelFile.value)
  formData.append('taskId', String(currentTask.value.id))

  // 打印FormData内容，便于调试
  console.log('FormData内容:')
  for (let pair of formData.entries()) {
    console.log(pair[0] + ': ' + pair[1])
  }

  // 添加详细的日志
  console.log('文件名:', excelFile.value.name)
  console.log('文件大小:', excelFile.value.size, '字节')
  console.log('文件类型:', excelFile.value.type)

  // 使用request模块，利用已有的请求拦截器
  import('@/utils/request').then(module => {
    const request = module.default

    request({
      url: '/api/inventory-check-task/import-assets',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
      .then(response => {
        console.log('上传成功，响应数据:', response)
        ElMessage.success({
          message: `成功导入 ${response.count || 0} 个资产到盘点任务，实际使用人和存放位置已自动填充`,
          duration: 5000
        })

        // 刷新记录列表
        console.log('开始刷新记录列表...')
        return request({
          url: `/api/inventory-check-record/by-task/${currentTask.value.id}`,
          method: 'get'
        })
      })
    .then(response => {
      console.log('获取记录列表成功:', response)

      // 使用增强版处理函数
      records.value = processRecordResponse(response);
      console.log('最终处理后的记录数量:', records.value.length);

      excelFile.value = null // 清空文件选择

      // 重置上传组件
      const uploadRef = document.querySelector('.el-upload__input')
      if (uploadRef) {
        uploadRef.value = ''
      }
    })
    .catch(error => {
      console.error('导入资产失败:', error)

      // 提供更详细的错误信息
      let errorMessage = '导入资产失败';

      if (error.response) {
        // 服务器返回了错误状态码
        console.error('错误状态码:', error.response.status);
        console.error('错误数据:', error.response.data);
        console.error('响应头:', error.response.headers);

        if (error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message;
        } else {
          errorMessage = `服务器错误 (${error.response.status})`;
        }

        // 对于400错误，提供更具体的建议
        if (error.response.status === 400) {
          console.error('请求参数可能有误，检查文件格式和任务ID');

          // 显示更详细的错误信息
          ElMessage({
            type: 'error',
            dangerouslyUseHTMLString: true,
            message: `
              <strong>导入失败</strong><br>
              原因：${errorMessage}<br>
              建议操作：<br>
              1. 请确认Excel格式与下载的模板完全一致<br>
              2. 确保表头名称为: 资产编号, 备注<br>
              3. 检查是否有重复的资产编号<br>
              4. 尝试重新下载模板并填写
            `,
            duration: 10000
          });
          return;
        }
      } else if (error.request) {
        // 请求已发送但没有收到响应
        console.error('未收到响应:', error.request);
        errorMessage = '服务器未响应，请检查网络连接';
      } else {
        // 请求设置时出错
        console.error('请求错误:', error.message);
        errorMessage = error.message;
      }

      ElMessage.error(errorMessage);
    })
    .finally(() => {
      loading.close()
    })
  }).catch(error => {
    console.error('加载请求模块失败:', error)
    loading.close()
    ElMessage.error('加载请求模块失败')
  })
}

// 下载导入模板
function downloadTemplate() {
  // 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: '正在下载模板...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  console.log('=== 前端: 开始下载模板 ===')
  console.log('请求URL:', '/api/inventory-check-task/template')

  // 使用axios下载，利用已有的请求拦截器
  import('@/utils/request').then(module => {
    const request = module.default

    request({
      url: '/api/inventory-check-task/template',
      method: 'get',
      responseType: 'blob',
      headers: {
        'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'Cache-Control': 'no-cache'
      }
    })
      .then(blob => {
        console.log('成功获取Blob数据:')
        console.log('- 大小:', blob.size, '字节')
        console.log('- 类型:', blob.type)

        if (blob.size === 0) {
          throw new Error('下载的文件为空')
        }

        // 创建下载链接
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', '资产盘点导入模板.xlsx')
        document.body.appendChild(link)

        console.log('触发下载...')
        link.click()

        // 清理
        window.URL.revokeObjectURL(url)
        document.body.removeChild(link)

        console.log('=== 前端: 下载流程完成 ===')
        ElMessage.success('模板下载成功')
      })
      .catch(error => {
        console.error('=== 前端: 下载模板失败 ===')
        console.error('错误详情:', error)
        console.error('错误消息:', error.message)
        console.error('错误堆栈:', error.stack)

        // 显示更详细的错误消息
        ElMessage.error(`下载模板失败: ${error.message}`)
      })
      .finally(() => {
        loading.close()
      })
  }).catch(error => {
    console.error('加载请求模块失败:', error)
    loading.close()
    ElMessage.error('加载请求模块失败')
  })
}

// 查看详细资产信息（用于调试）
function checkAssetDetails(asset) {
  if (!asset || !asset.assetNo) {
    ElMessage.warning('资产信息不完整');
    return;
  }

  // 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: '查询资产信息...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  axios.get(`/api/inventory/items/findByAssetNo/${asset.assetNo}`)
    .then(res => {
      const assetInfo = res.data;
      ElMessageBox.alert(`
        <strong>资产详情：</strong><br/>
        资产编号: ${assetInfo.assetNo || '无'}<br/>
        资产名称: ${assetInfo.name || '无'}<br/>
        使用人: ${assetInfo.assignee || '无'}<br/>
        位置: ${assetInfo.location || '无'}<br/>
        楼层: ${assetInfo.floor || '无'}<br/>
      `, '资产信息调试', {
        dangerouslyUseHTMLString: true
      });
    })
    .catch(error => {
      console.error('查询资产失败:', error);
      // 显示更友好的错误信息
      const errorMsg = error.response?.status === 404
        ? `没有找到编号为 ${asset.assetNo} 的资产`
        : (error.response?.data?.message || error.message);

      ElMessage.error('查询资产信息失败: ' + errorMsg);

      // 如果是404错误，显示当前资产对象的基本信息
      if (error.response?.status === 404) {
        ElMessageBox.alert(`
          <strong>当前记录中的资产信息：</strong><br/>
          资产编号: ${asset.assetNo || '无'}<br/>
          资产名称: ${asset.name || '无'}<br/>
          <span style="color:#F56C6C">注意：此资产在系统中不存在，可能是导入时自动创建的临时记录</span>
        `, '临时资产信息', {
          dangerouslyUseHTMLString: true
        });
      }
    })
    .finally(() => {
      loading.close();
    });
}

// 处理可能的资产信息缺失问题
function ensureAssetInfo(record) {
  if (!record) return record;

  // 确保asset对象存在且有基本属性
  if (!record.asset || typeof record.asset !== 'object') {
    console.warn('记录的资产对象不存在:', record.id);
    record.asset = {
      id: record.id || 0,
      assetNo: `资产${record.id || 0}`,
      name: `导入的资产 #${record.id || 0}`
    };
  } else if (!record.asset.assetNo || !record.asset.name) {
    // 如果asset对象存在但缺少关键属性
    console.warn('记录的资产对象缺少关键属性:', record.id);
    record.asset = {
      ...record.asset,
      id: record.asset.id || record.id || 0,
      assetNo: record.asset.assetNo || `资产${record.id || 0}`,
      name: record.asset.name || `导入的资产 #${record.id || 0}`
    };
  }

  // 确保其他必要字段存在
  record.checkStatus = record.checkStatus || '正常';
  record.actualUser = record.actualUser || '';
  record.actualLocation = record.actualLocation || '';
  record.remark = record.remark || '';

  return record;
}

// 完成盘点任务
function completeTask(taskId) {
  // 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: '正在完成盘点任务...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  // 首先获取当前任务的完整信息
  const currentTaskData = tasks.value.find(t => t.id === taskId);
  if (!currentTaskData) {
    ElMessage.error('找不到任务信息');
    loading.close();
    return;
  }

  // 创建要更新的任务对象，保留原有属性
  const task = {
    ...currentTaskData,
    id: taskId,
    status: "已完成",
    updatedAt: new Date()
  }

  console.log('更新任务数据:', task);

  // 发送更新请求
  axios.put(`/api/inventory-check-task/${taskId}`, task)
    .then(response => {
      ElMessage.success('盘点任务已标记为完成')
      console.log('任务更新成功，响应:', response.data);

      // 直接更新本地数据，避免重新获取
      const index = tasks.value.findIndex(t => t.id === taskId);
      if (index !== -1) {
        tasks.value[index] = {
          ...tasks.value[index],
          status: "已完成"
        };
        console.log('本地任务数据已更新');
      }

      // 然后再刷新任务列表以确保数据同步
      setTimeout(() => {
        fetchTasks();
      }, 500);
    })
    .catch(error => {
      console.error('更新任务状态失败:', error)
      ElMessage.error('更新任务状态失败，请稍后再试')
    })
    .finally(() => {
      loading.close()
    })
}

fetchTasks()
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

.auto-filled-input {
  :deep(.el-input__inner) {
    background-color: rgba(144, 202, 249, 0.1);
    border-color: #90CAF9;
  }
}

.tip-info {
  background-color: #F8F9FA;
  padding: 8px 12px;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}
</style>
