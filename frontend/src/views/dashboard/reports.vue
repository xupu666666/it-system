<template>
  <div class="dashboard-reports-container">
    <el-card class="filter-card">
      <div class="filter-header">
        <h3>数据报表</h3>
        <div class="filter-actions">
          <el-select v-model="reportType" placeholder="选择报表类型" style="width: 180px">
            <el-option label="维修费用统计" value="maintenance" />
            <el-option label="资产状态分布" value="inventory" />
            <el-option label="物品领用趋势" value="supplies" />
            <el-option label="网络设备状态" value="network" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 320px; margin-left: 10px"
          />
          <el-button type="primary" @click="generateReport" style="margin-left: 10px">生成报表</el-button>
          <el-button @click="exportReport" :disabled="!hasData">导出报表</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="chart-card" v-loading="loading">
      <div class="chart-container" v-if="hasData">
        <div ref="mainChart" class="main-chart"></div>
      </div>
      <div class="empty-data" v-else>
        <el-empty description="请选择报表类型并点击生成报表" />
      </div>
    </el-card>

    <el-card class="table-card" v-if="hasData">
      <div class="table-header">
        <h3>{{ reportTitles[reportType] }}详细数据</h3>
      </div>
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column v-for="col in tableColumns" :key="col.prop" :prop="col.prop" :label="col.label" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { defineComponent, ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

export default defineComponent({
  name: 'DashboardReports',
  setup() {
    // 图表实例
    const mainChart = ref(null)
    let chartInstance = null

    // 加载状态
    const loading = ref(false)

    // 报表类型
    const reportType = ref('maintenance')

    // 日期范围
    const dateRange = ref([
      new Date(new Date().getFullYear(), new Date().getMonth() - 1, 1).toISOString().split('T')[0],
      new Date().toISOString().split('T')[0]
    ])

    // 是否有数据
    const hasData = ref(false)

    // 表格数据
    const tableData = ref([])

    // 报表标题映射
    const reportTitles = {
      maintenance: '维修费用统计',
      inventory: '资产状态分布',
      supplies: '物品领用趋势',
      network: '网络设备状态'
    }

    // 表格列配置
    const tableColumns = computed(() => {
      switch (reportType.value) {
        case 'maintenance':
          return [
            { prop: 'date', label: '日期' },
            { prop: 'department', label: '部门' },
            { prop: 'type', label: '维修类型' },
            { prop: 'count', label: '维修单数量' },
            { prop: 'amount', label: '维修费用' }
          ]
        case 'inventory':
          return [
            { prop: 'type', label: '资产类型' },
            { prop: 'status', label: '状态' },
            { prop: 'count', label: '数量' },
            { prop: 'percentage', label: '占比' },
            { prop: 'value', label: '资产价值' }
          ]
        case 'supplies':
          return [
            { prop: 'date', label: '日期' },
            { prop: 'department', label: '部门' },
            { prop: 'category', label: '物品类别' },
            { prop: 'count', label: '领用数量' },
            { prop: 'status', label: '状态' }
          ]
        case 'network':
          return [
            { prop: 'type', label: '设备类型' },
            { prop: 'status', label: '状态' },
            { prop: 'count', label: '数量' },
            { prop: 'percentage', label: '占比' },
            { prop: 'location', label: '位置' }
          ]
        default:
          return []
      }
    })

    // 初始化图表
    const initChart = () => {
      if (chartInstance) {
        chartInstance.dispose()
      }
      
      if (mainChart.value) {
        chartInstance = echarts.init(mainChart.value)
        
        // 窗口大小变化时重新调整图表大小
        window.addEventListener('resize', () => {
          chartInstance.resize()
        })
      }
    }

    // 生成报表
    const generateReport = async () => {
      if (!reportType.value || !dateRange.value || dateRange.value.length !== 2) {
        ElMessage.warning('请选择完整的报表类型和日期范围')
        return
      }

      loading.value = true
      
      try {
        // 这里应该调用后端API获取报表数据
        // 模拟API请求延迟
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 生成模拟数据
        const mockData = generateMockData()
        
        // 更新表格数据
        tableData.value = mockData.tableData
        
        // 更新图表
        updateChart(mockData.chartData)
        
        hasData.value = true
      } catch (error) {
        console.error('生成报表失败:', error)
        ElMessage.error('生成报表失败')
      } finally {
        loading.value = false
      }
    }

    // 生成模拟数据
    const generateMockData = () => {
      const result = { tableData: [], chartData: {} }
      
      switch (reportType.value) {
        case 'maintenance':
          // 维修费用统计模拟数据
          result.tableData = [
            { date: '2023-04-01', department: '技术部', type: '硬件维修', count: 5, amount: 3500 },
            { date: '2023-04-05', department: '市场部', type: '打印设备', count: 2, amount: 1200 },
            { date: '2023-04-10', department: '财务部', type: '网络问题', count: 3, amount: 2000 },
            { date: '2023-04-15', department: '人事部', type: '软件故障', count: 4, amount: 1800 },
            { date: '2023-04-20', department: '技术部', type: '硬件维修', count: 6, amount: 4200 }
          ]
          
          result.chartData = {
            title: '维修费用统计',
            xAxis: ['技术部', '市场部', '财务部', '人事部', '运营部'],
            series: [
              {
                name: '维修单数量',
                type: 'bar',
                data: [11, 2, 3, 4, 0]
              },
              {
                name: '维修费用',
                type: 'line',
                yAxisIndex: 1,
                data: [7700, 1200, 2000, 1800, 0]
              }
            ]
          }
          break
          
        case 'inventory':
          // 资产状态分布模拟数据
          result.tableData = [
            { type: '电脑设备', status: '在用', count: 120, percentage: '60%', value: 360000 },
            { type: '电脑设备', status: '闲置', count: 30, percentage: '15%', value: 90000 },
            { type: '办公家具', status: '在用', count: 200, percentage: '80%', value: 100000 },
            { type: '网络设备', status: '在用', count: 50, percentage: '70%', value: 150000 },
            { type: '网络设备', status: '维修', count: 10, percentage: '14%', value: 30000 }
          ]
          
          result.chartData = {
            title: '资产状态分布',
            type: 'pie',
            data: [
              { value: 120, name: '电脑设备-在用' },
              { value: 30, name: '电脑设备-闲置' },
              { value: 200, name: '办公家具-在用' },
              { value: 50, name: '网络设备-在用' },
              { value: 10, name: '网络设备-维修' }
            ]
          }
          break
          
        case 'supplies':
          // 物品领用趋势模拟数据
          result.tableData = [
            { date: '2023-04-01', department: '技术部', category: '办公用品', count: 20, status: '已领用' },
            { date: '2023-04-05', department: '市场部', category: '打印耗材', count: 5, status: '已领用' },
            { date: '2023-04-10', department: '财务部', category: '办公用品', count: 15, status: '已领用' },
            { date: '2023-04-15', department: '人事部', category: '清洁用品', count: 10, status: '已领用' },
            { date: '2023-04-20', department: '技术部', category: '电子配件', count: 8, status: '已领用' }
          ]
          
          result.chartData = {
            title: '物品领用趋势',
            xAxis: ['4月1日', '4月5日', '4月10日', '4月15日', '4月20日', '4月25日', '4月30日'],
            series: [
              {
                name: '办公用品',
                type: 'line',
                stack: 'Total',
                data: [20, 5, 15, 8, 12, 6, 10]
              },
              {
                name: '打印耗材',
                type: 'line',
                stack: 'Total',
                data: [0, 5, 2, 3, 5, 2, 4]
              },
              {
                name: '清洁用品',
                type: 'line',
                stack: 'Total',
                data: [5, 2, 3, 10, 4, 6, 2]
              },
              {
                name: '电子配件',
                type: 'line',
                stack: 'Total',
                data: [2, 3, 5, 2, 8, 3, 5]
              }
            ]
          }
          break
          
        case 'network':
          // 网络设备状态模拟数据
          result.tableData = [
            { type: '路由器', status: '在线', count: 15, percentage: '94%', location: '总部' },
            { type: '交换机', status: '在线', count: 30, percentage: '97%', location: '总部' },
            { type: '防火墙', status: '在线', count: 5, percentage: '100%', location: '总部' },
            { type: '无线AP', status: '在线', count: 25, percentage: '89%', location: '分支机构' },
            { type: '无线AP', status: '离线', count: 3, percentage: '11%', location: '分支机构' }
          ]
          
          result.chartData = {
            title: '网络设备状态',
            type: 'pie',
            data: [
              { value: 15, name: '路由器-在线' },
              { value: 30, name: '交换机-在线' },
              { value: 5, name: '防火墙-在线' },
              { value: 25, name: '无线AP-在线' },
              { value: 3, name: '无线AP-离线' }
            ]
          }
          break
      }
      
      return result
    }

    // 更新图表
    const updateChart = (chartData) => {
      if (!chartInstance) {
        initChart()
      }
      
      let option = {}
      
      if (chartData.type === 'pie') {
        // 饼图配置
        option = {
          title: {
            text: chartData.title,
            left: 'center'
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            data: chartData.data.map(item => item.name)
          },
          series: [
            {
              name: chartData.title,
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false,
                position: 'center'
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: '18',
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: chartData.data
            }
          ]
        }
      } else {
        // 柱状图/折线图配置
        option = {
          title: {
            text: chartData.title,
            left: 'center'
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          legend: {
            data: chartData.series.map(item => item.name),
            bottom: '0%'
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '10%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: chartData.xAxis
          },
          yAxis: [
            {
              type: 'value',
              name: '数量',
              position: 'left'
            },
            {
              type: 'value',
              name: '金额',
              position: 'right',
              axisLabel: {
                formatter: '{value} 元'
              }
            }
          ],
          series: chartData.series
        }
      }
      
      chartInstance.setOption(option)
    }

    // 导出报表
    const exportReport = () => {
      ElMessage.success('报表导出成功')
    }

    // 组件挂载时初始化图表
    onMounted(() => {
      initChart()
    })

    // 组件卸载前销毁图表实例
    onBeforeUnmount(() => {
      if (chartInstance) {
        chartInstance.dispose()
        window.removeEventListener('resize', () => {
          chartInstance.resize()
        })
      }
    })

    // 监听报表类型变化
    watch(reportType, () => {
      if (hasData.value) {
        generateReport()
      }
    })

    return {
      mainChart,
      loading,
      reportType,
      dateRange,
      hasData,
      tableData,
      tableColumns,
      reportTitles,
      generateReport,
      exportReport
    }
  }
})
</script>

<style lang="scss" scoped>
.dashboard-reports-container {
  .filter-card {
    margin-bottom: 20px;
    
    .filter-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
      }
      
      .filter-actions {
        display: flex;
        align-items: center;
      }
    }
  }
  
  .chart-card {
    margin-bottom: 20px;
    
    .chart-container {
      width: 100%;
      
      .main-chart {
        width: 100%;
        height: 400px;
      }
    }
    
    .empty-data {
      height: 400px;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }
  
  .table-card {
    .table-header {
      margin-bottom: 15px;
      
      h3 {
        margin: 0;
      }
    }
  }
}
</style>
