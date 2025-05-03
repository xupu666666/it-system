<template>
  <div class="page-container">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-title">{{ card.title }}</div>
          <div class="stat-value">{{ card.prefix }}{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选区 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="时间范围">
          <el-date-picker v-model="filters.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="filters.department" placeholder="全部部门" clearable filterable>
            <el-option v-for="item in departments" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修类型">
          <el-select v-model="filters.type" placeholder="全部类型" clearable filterable>
            <el-option v-for="item in types" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="filters.supplier" placeholder="全部供应商" clearable filterable>
            <el-option v-for="item in suppliers" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 120px;">
            <el-option
              v-for="(value, key) in statusMap"
              :key="key"
              :label="value"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="success" @click="exportExcel" icon="el-icon-download">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据可视化区 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <div class="chart-header">
            <div class="chart-title">费用趋势</div>
            <div class="chart-actions">
              <el-switch
                v-model="autoRefresh"
                active-text="自动刷新"
                @change="toggleAutoRefresh"
                style="margin-right: 10px;"
              />
              <el-button type="primary" size="small" icon="Refresh" @click="refreshData" :loading="refreshing">刷新数据</el-button>
            </div>
          </div>
          <div ref="trendChart" style="height:350px; flex: 1;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <div class="chart-header">
            <div class="chart-title">类型分布</div>
          </div>
          <div ref="typePieChart" style="height:350px; flex: 1;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 明细表格 -->
    <el-card class="table-card">
      <div class="table-title">维修费用明细</div>
      <el-table :data="tableData" style="width: 100%" highlight-current-row>
        <el-table-column prop="orderNo" label="维修单号" width="120" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="type" label="维修类型" width="120" />
        <el-table-column prop="supplier" label="供应商" width="120" />
        <el-table-column prop="cost" label="费用(元)" width="100" />
        <el-table-column prop="date" label="维修日期" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            {{ statusMap[row.status] || row.status }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getMaintenanceStatistics, getMaintenanceList, getMaintenanceTypes, getMaintenanceStatuses, exportMaintenanceOrders, getMaintenanceOrderFilters } from '@/api/maintenance'
import { ElMessage } from 'element-plus'
import emitter from '@/utils/eventBus'

const statusMap = {
  'PENDING': '待处理',
  'IN_PROGRESS': '维修中',
  'COMPLETED': '已完成',
  'PAID': '已付款',
  'CANCELLED': '已取消'
}

const statistics = reactive({ totalCost: 0, orderCount: 0, avgCost: 0, maxCost: 0 })
const filters = reactive({ dateRange: '', department: '', type: '', supplier: '', status: '' })
const departments = ref([])
const types = ref([])
const statuses = ref([])
const suppliers = ref([])
const tableData = ref([])
const trendChart = ref(null)
const typePieChart = ref(null)
const refreshing = ref(false) // 刷新状态变量
const autoRefresh = ref(false) // 自动刷新开关
const refreshInterval = ref(null) // 自动刷新定时器

const statCards = computed(() => [
  { title: '总费用', value: statistics.totalCost || 0, prefix: '￥' },
  { title: '维修单数量', value: statistics.orderCount || 0, prefix: '' },
  { title: '平均费用', value: statistics.avgCost || 0, prefix: '￥' },
  { title: '最高费用', value: statistics.maxCost || 0, prefix: '￥' }
])

const fetchAll = async () => {
  try {
    // 先获取表格数据
    await fetchTableData()
    // 再获取和处理统计数据
    await fetchStatistics()
  } catch (e) {
    ElMessage.error('获取数据失败')
  }
}

const fetchStatistics = async () => {
  try {
    const params = buildParams()
    const res = await getMaintenanceStatistics(params)
    Object.assign(statistics, res)

    // 构造图表数据
    const trendData = {
      x: [],
      y: []
    }

    // 使用表格数据构造趋势数据
    if (tableData.value.length > 0) {
      const dateMap = new Map()
      tableData.value.forEach(item => {
        const date = item.date.split(' ')[0] // 只取日期部分
        const cost = Number(item.cost) || 0
        if (dateMap.has(date)) {
          dateMap.set(date, dateMap.get(date) + cost)
        } else {
          dateMap.set(date, cost)
        }
      })

      // 按日期排序
      const sortedDates = Array.from(dateMap.keys()).sort()
      trendData.x = sortedDates
      trendData.y = sortedDates.map(date => dateMap.get(date))
    }

    // 构造类型分布数据
    const typePieData = {
      data: []
    }

    // 使用表格数据构造类型分布数据
    if (tableData.value.length > 0) {
      const typeMap = new Map()
      tableData.value.forEach(item => {
        const type = item.type
        const cost = Number(item.cost) || 0
        if (typeMap.has(type)) {
          typeMap.set(type, typeMap.get(type) + cost)
        } else {
          typeMap.set(type, cost)
        }
      })

      typePieData.data = Array.from(typeMap.entries()).map(([name, value]) => ({
        name,
        value
      }))
    }

    // 渲染图表
    renderCharts(trendData, typePieData)
  } catch (e) {
    console.error('统计数据处理失败:', e)
    ElMessage.error('获取统计数据失败')
  }
}

const fetchTableData = async () => {
  try {
    const params = buildParams()
    const res = await getMaintenanceList(params)
    let rawData = []
    if (Array.isArray(res)) {
      rawData = res
    } else if (res && Array.isArray(res.content)) {
      rawData = res.content
    }
    // 转换为表格需要的字段
    tableData.value = rawData.map(item => ({
      orderNo: item.orderNo,
      department: item.department,
      type: item.type,
      supplier: item.supplier,
      cost: item.cost,
      date: item.createdAt ? new Date(item.createdAt).toLocaleString() : '',
      status: statusMap[item.status] || item.status
    }))
    suppliers.value = [...new Set(tableData.value.map(i => i.supplier).filter(Boolean))]
  } catch (e) {
    ElMessage.error('获取明细数据失败')
  }
}

const fetchFilters = async () => {
  try {
    const res = await getMaintenanceOrderFilters()
    // 部门直接使用字符串数组
    departments.value = res.departments || []
    types.value = res.types || []
    suppliers.value = res.suppliers || []
    // 不再需要映射状态，因为在选择器中直接使用 statusMap
  } catch (e) {
    ElMessage.error('获取下拉选项失败')
  }
}

const buildParams = () => {
  const params = {}
  if (filters.dateRange && filters.dateRange.length === 2) {
    params.startDate = filters.dateRange[0]
    params.endDate = filters.dateRange[1]
  }
  if (filters.department) {
    params.department = filters.department
  }
  if (filters.type) {
    params.type = filters.type
  }
  if (filters.supplier) {
    params.supplier = filters.supplier
  }
  if (filters.status) {
    // 直接使用选中的状态值（已经是英文状态码）
    params.status = filters.status
  }
  return params
}

const exportExcel = async () => {
  try {
    const params = buildParams()
    const res = await exportMaintenanceOrders(params)
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '维修费用明细.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

const renderCharts = (trendData, typePieData) => {
  // 费用趋势
  if (trendChart.value) {
    const trend = echarts.init(trendChart.value)
    trend.clear() // 清除旧的图表
    trend.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          const data = params[0]
          return `${data.name}<br/>${data.seriesName}：${data.value}元`
        }
      },
      xAxis: {
        type: 'category',
        data: trendData?.x || [],
        axisLabel: {
          rotate: 45,
          interval: 0,
          formatter: function(value) {
            // 将日期格式化为 MM-DD 格式
            const date = new Date(value)
            const month = (date.getMonth() + 1).toString().padStart(2, '0')
            const day = date.getDate().toString().padStart(2, '0')
            return `${month}-${day}`
          },
          margin: 14,
          align: 'center'
        }
      },
      yAxis: {
        type: 'value',
        name: '费用(元)',
        nameTextStyle: {
          padding: [0, 0, 0, 30]
        }
      },
      grid: {
        left: '10%',
        right: '5%',
        bottom: '20%',  // 增加底部空间
        containLabel: true
      },
      series: [{
        name: '费用',
        type: 'line',
        smooth: true,
        lineStyle: { width: 4, color: new echarts.graphic.LinearGradient(0,0,1,0,[{offset:0,color:'#6dd5fa'},{offset:1,color:'#2980b9'}]) },
        areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#6dd5fa88'},{offset:1,color:'#fff0'}]) },
        data: trendData?.y || [],
        symbol: 'circle',
        symbolSize: 10,
        emphasis: { focus: 'series' },
        animationDuration: 1200
      }]
    })

    // 监听容器大小变化，自动调整图表大小
    window.addEventListener('resize', () => trend.resize())
  }

  // 类型分布
  if (typePieChart.value) {
    const pie = echarts.init(typePieChart.value)
    pie.clear() // 清除旧的图表
    pie.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}元 ({d}%)'
      },
      legend: {
        top: '0%',
        left: 'center',
        type: 'scroll',
        orient: 'horizontal',
        itemGap: 20,
        textStyle: {
          fontSize: 12
        },
        padding: 5
      },
      series: [{
        name: '类型分布',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '55%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
          shadowBlur: 10,
          shadowColor: 'rgba(0,0,0,0.15)'
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}\n{c}元 ({d}%)',
          fontWeight: 'bold',
          color: '#333',
          backgroundColor: 'rgba(255,255,255,0.7)',
          padding: [4, 8],
          borderRadius: 4,
          distanceToLabelLine: 5
        },
        labelLine: {
          show: true,
          smooth: 0.2,
          length: 15,
          length2: 20,
          maxSurfaceAngle: 80
        },
        labelLayout: {
          hideOverlap: true,
          moveOverlap: 'shiftY'
        },
        data: typePieData?.data || [],
        animationType: 'scale',
        animationEasing: 'elasticOut',
        animationDelay: () => Math.random() * 200
      }]
    })

    // 监听容器大小变化，自动调整图表大小
    window.addEventListener('resize', () => pie.resize())
  }
}

const handleSearch = async () => {
  try {
    ElMessage.info('正在查询...')
    await fetchAll()
    ElMessage.success('查询完成')
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

const resetFilters = () => {
  filters.dateRange = ''
  filters.department = ''
  filters.type = ''
  filters.supplier = ''
  filters.status = ''
  handleSearch() // 使用handleSearch替代直接调用fetchAll
}

// 刷新数据函数
const refreshData = async () => {
  try {
    refreshing.value = true
    ElMessage.info('正在刷新数据...')
    await fetchAll()
    ElMessage.success('数据刷新成功')
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('刷新数据失败')
  } finally {
    refreshing.value = false
  }
}

// 切换自动刷新
const toggleAutoRefresh = (value) => {
  if (value) {
    // 开启自动刷新，每30秒刷新一次
    refreshInterval.value = setInterval(() => {
      console.log('自动刷新数据...')
      fetchAll() // 静默刷新，不显示消息
    }, 30000) // 30秒
    ElMessage.success('已开启自动刷新（每30秒）')
  } else {
    // 关闭自动刷新
    if (refreshInterval.value) {
      clearInterval(refreshInterval.value)
      refreshInterval.value = null
    }
    ElMessage.info('已关闭自动刷新')
  }
}

onMounted(() => {
  emitter.on('refresh-maintenance-statistics', fetchAll)
  fetchFilters()
  fetchAll()
})

onUnmounted(() => {
  // 移除事件监听
  emitter.off('refresh-maintenance-statistics', fetchAll)

  // 清除自动刷新定时器
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
    refreshInterval.value = null
  }

  // 移除窗口大小变化监听
  window.removeEventListener('resize', () => {
    if (trendChart.value) {
      echarts.getInstanceByDom(trendChart.value)?.resize()
    }
    if (typePieChart.value) {
      echarts.getInstanceByDom(typePieChart.value)?.resize()
    }
  })
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
  .stat-cards { margin-bottom: 20px; }
  .stat-card {
    text-align: center;
    transition: box-shadow 0.3s, transform 0.3s;
    .stat-title { color: #888; font-size: 15px; margin-bottom: 8px; }
    .stat-value { font-size: 28px; font-weight: bold; color: #2980b9; letter-spacing: 1px; }
    &:hover {
      box-shadow: 0 6px 24px 0 rgba(41,128,185,0.12);
      transform: translateY(-2px) scale(1.03);
    }
  }

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .chart-title {
      font-size: 16px;
      font-weight: bold;
    }

    .chart-actions {
      display: flex;
      align-items: center;
    }
  }
  .filter-card {
    margin-bottom: 20px;
    :deep(.el-select) {
      .el-input__wrapper {
        padding: 0 8px;
      }
      .el-select__tags {
        padding: 2px;
      }
    }
  }
  .charts-row {
    margin-bottom: 20px;

    .chart-card {
      height: 100%;
      display: flex;
      flex-direction: column;

      .el-card__body {
        flex: 1;
        display: flex;
        flex-direction: column;
      }
    }
  }

  .chart-title { font-size: 16px; font-weight: 500; margin-bottom: 10px; }
  .table-card { }
  .table-title { font-size: 16px; font-weight: 500; margin-bottom: 10px; }
  :deep(.el-table__row--current) {
    background: linear-gradient(90deg, #e0f7fa 0%, #f5fafd 100%) !important;
    transition: background 0.4s;
  }
}
</style>
