<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <div class="welcome-section">
        <h1 class="welcome-text">欢迎使用ICT系统管理平台</h1>
        <p class="welcome-subtitle">今天是 {{ currentDateTime }}</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="Plus">快速操作</el-button>
      </div>
    </div>

    <!-- 统计卡片 - 现代化设计 -->
    <el-row :gutter="24" class="card-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="(item, index) in statCards" :key="index">
        <div class="stat-card-wrapper">
          <div class="stat-card" :class="`stat-card-${index + 1}`">
            <div class="stat-card-content">
              <div class="stat-card-icon">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <div class="stat-card-info">
                <div class="stat-card-value">{{ item.value }}</div>
                <div class="stat-card-label">{{ item.label }}</div>
              </div>
            </div>
            <div class="stat-card-footer">
              <span class="trend-indicator">
                <el-icon><component :is="item.trend === 'up' ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
                {{ item.trendValue }}
              </span>
              <span class="trend-period">较上月</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表和快捷功能区 - 现代化设计 -->
    <el-row :gutter="24" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon><Histogram /></el-icon>
                <span>费用统计</span>
              </div>
              <div class="card-actions">
                <el-radio-group v-model="timeRange" size="small">
                  <el-radio-button label="week">本周</el-radio-button>
                  <el-radio-button label="month">本月</el-radio-button>
                  <el-radio-button label="year">本年</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <div ref="expenseChart" class="chart"></div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card class="quick-links-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon><Grid /></el-icon>
                <span>快捷功能</span>
              </div>
            </div>
          </template>
          <div class="quick-links">
            <div class="quick-link" v-for="(link, index) in quickLinks" :key="index" @click="navigateTo(link.path)">
              <div class="quick-link-icon" :style="{
                background: `linear-gradient(135deg, ${link.color}15 0%, ${link.color}30 100%)`,
                borderColor: link.color
              }">
                <el-icon :style="{ color: link.color }"><component :is="link.icon" /></el-icon>
              </div>
              <span class="quick-link-label" :style="{
                color: link.color,
                textShadow: `0 1px 2px ${link.color}10`
              }">
                {{ link.label }}
                <span class="quick-link-underline" :style="{ background: link.color }"></span>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项和通知 - 现代化设计 -->
    <el-row :gutter="24" class="todo-row">
      <el-col :xs="24" :lg="12">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon><Calendar /></el-icon>
                <span>待处理事项</span>
              </div>
              <el-button type="primary" size="small" plain>查看全部</el-button>
            </div>
          </template>
          <div class="todo-list">
            <div class="todo-item" v-for="(item, index) in todoList" :key="index">
              <div class="todo-item-content">
                <div class="todo-item-title">{{ item.title }}</div>
                <div class="todo-item-meta">
                  <el-tag :type="item.typeTag" size="small" effect="light">{{ item.type }}</el-tag>
                  <span class="todo-item-date">{{ item.date }}</span>
                </div>
              </div>
              <div class="todo-item-action">
                <el-button type="primary" size="small" text>处理</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="notification-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon><Bell /></el-icon>
                <span>系统通知</span>
              </div>
              <el-button type="primary" size="small" plain>查看全部</el-button>
            </div>
          </template>
          <div class="notification-list">
            <div class="notification-item" v-for="(notice, index) in notifications" :key="index">
              <div class="notification-icon" :class="`notification-icon-${notice.type || 'info'}`">
                <el-icon><component :is="getNotificationIcon(notice.type)" /></el-icon>
              </div>
              <div class="notification-content">
                <div class="notification-title">{{ notice.title }}</div>
                <div class="notification-text">{{ notice.content }}</div>
                <div class="notification-time">{{ notice.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  Setting, Tools, Files, Money,
  Plus, Search, Box, Connection, User, PieChart,
  Calendar, Bell, Histogram,
  Warning, InfoFilled, CircleCheckFilled, CircleCloseFilled,
  Grid
} from '@element-plus/icons-vue'

export default defineComponent({
  name: 'Dashboard',
  components: {
    Setting, Tools, Files, Money,
    Plus, Search, Box, Connection, User, PieChart,
    Calendar, Bell, Histogram, Grid
  },
  setup() {
    const router = useRouter()
    const expenseChart = ref(null)
    const chart = ref(null)
    const timeRange = ref('month')

    // 日期时间格式化
    const currentDateTime = computed(() => {
      const now = new Date()
      const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      const weekday = weekdays[now.getDay()]
      return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekday} ${now.getHours()}:${now.getMinutes() < 10 ? '0' + now.getMinutes() : now.getMinutes()}`
    })

    // 统计卡片数据 - 增强版
    const statCards = ref([
      {
        label: '维修单总数',
        value: 258,
        icon: 'Setting',
        trend: 'up',
        trendValue: '+12%'
      },
      {
        label: '待处理维修',
        value: 12,
        icon: 'Tools',
        trend: 'down',
        trendValue: '-5%'
      },
      {
        label: '资产总数',
        value: 1892,
        icon: 'Files',
        trend: 'up',
        trendValue: '+3%'
      },
      {
        label: '当月费用',
        value: '￥32,159',
        icon: 'Money',
        trend: 'up',
        trendValue: '+8%'
      }
    ])

    // 快捷功能
    const quickLinks = ref([
      { label: '新增维修单', icon: 'Plus', path: '/maintenance/orders', color: '#3B82F6' }, // 蓝色
      { label: '资产查询', icon: 'Search', path: '/inventory/items', color: '#10B981' },    // 绿色
      { label: '物品领用', icon: 'Box', path: '/supplies/requests', color: '#F59E0B' },     // 黄色
      { label: '网络设备', icon: 'Connection', path: '/network/devices', color: '#8B5CF6' }, // 紫色
      { label: '用户管理', icon: 'User', path: '/system/users', color: '#EC4899' },         // 粉色
      { label: '数据报表', icon: 'PieChart', path: '/reports', color: '#EF4444' }           // 红色
    ])

    // 待办事项
    const todoList = ref([
      { title: '网络设备年度检修', type: '维护', typeTag: 'warning', date: '2023-04-22' },
      { title: '办公耗材采购审批', type: '审批', typeTag: 'primary', date: '2023-04-21' },
      { title: 'IT机房服务器维修', type: '维修', typeTag: 'danger', date: '2023-04-20' },
      { title: '新员工电脑配置', type: '配置', typeTag: 'success', date: '2023-04-20' }
    ])

    // 系统通知 - 增强版
    const notifications = ref([
      {
        title: '系统更新通知',
        content: '系统将于本周六晚间23:00-次日凌晨2:00进行版本更新，请做好相关准备。',
        time: '2小时前',
        type: 'info'
      },
      {
        title: '月度资产盘点提醒',
        content: '请各部门于本月25日前完成资产盘点工作，并提交盘点报告。',
        time: '1天前',
        type: 'warning'
      },
      {
        title: '维修费用超支预警',
        content: '市场部本月维修费用已超出预算的85%，请注意控制。',
        time: '2天前',
        type: 'error'
      }
    ])

    // 获取通知图标
    const getNotificationIcon = (type) => {
      switch(type) {
        case 'success': return 'CircleCheckFilled'
        case 'warning': return 'Warning'
        case 'error': return 'CircleCloseFilled'
        case 'info':
        default: return 'InfoFilled'
      }
    }

    // 导航到指定路径
    const navigateTo = (path) => {
      router.push(path)
    }

    // 初始化图表
    const initChart = () => {
      try {
        if (expenseChart.value) {
          // 确保DOM元素已经渲染完成
          setTimeout(() => {
            try {
              // 如果已经初始化过，先销毁
              if (chart.value) {
                chart.value.dispose()
              }

              // 重新初始化图表
              chart.value = echarts.init(expenseChart.value)

              const option = {
                tooltip: {
                  trigger: 'axis',
                  axisPointer: {
                    type: 'shadow'
                  }
                },
                legend: {
                  data: ['维修费用', '办公用品', '网络设备', '其他']
                },
                grid: {
                  left: '3%',
                  right: '4%',
                  bottom: '3%',
                  containLabel: true
                },
                xAxis: {
                  type: 'category',
                  data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月']
                },
                yAxis: {
                  type: 'value'
                },
                series: [
                  {
                    name: '维修费用',
                    type: 'bar',
                    stack: 'total',
                    emphasis: {
                      focus: 'series'
                    },
                    data: [7800, 9200, 5400, 8600, 7900, 6800, 8200]
                  },
                  {
                    name: '办公用品',
                    type: 'bar',
                    stack: 'total',
                    emphasis: {
                      focus: 'series'
                    },
                    data: [5300, 4900, 6200, 5100, 6700, 4500, 5800]
                  },
                  {
                    name: '网络设备',
                    type: 'bar',
                    stack: 'total',
                    emphasis: {
                      focus: 'series'
                    },
                    data: [15000, 8000, 12000, 9000, 20000, 13000, 11000]
                  },
                  {
                    name: '其他',
                    type: 'bar',
                    stack: 'total',
                    emphasis: {
                      focus: 'series'
                    },
                    data: [2200, 1800, 2500, 3100, 2700, 2300, 2600]
                  }
                ]
              }

              // 设置图表选项
              chart.value.setOption(option)
              console.log('图表初始化成功')
            } catch (err) {
              console.error('图表初始化失败:', err)
            }
          }, 100) // 延迟100ms确保DOM已渲染
        }
      } catch (error) {
        console.error('初始化图表出错:', error)
      }
    }

    // 监听窗口大小变化，调整图表
    const handleResize = () => {
      if (chart.value) {
        try {
          chart.value.resize()
        } catch (error) {
          console.error('调整图表大小失败:', error)
        }
      }
    }

    onMounted(() => {
      // 延迟初始化图表，确保DOM已完全渲染
      setTimeout(() => {
        initChart()
      }, 300)

      // 添加窗口大小变化监听
      window.addEventListener('resize', handleResize)
    })

    return {
      currentDateTime,
      statCards,
      quickLinks,
      todoList,
      notifications,
      expenseChart,
      timeRange,
      navigateTo,
      getNotificationIcon
    }
  }
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 24px;

  // 顶部欢迎区域
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;

    .welcome-section {
      .welcome-text {
        font-size: 28px;
        font-weight: 600;
        color: var(--text-primary);
        margin: 0 0 8px 0;
        letter-spacing: -0.5px;
      }

      .welcome-subtitle {
        font-size: 16px;
        color: var(--text-secondary);
        margin: 0;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  // 统计卡片区域
  .card-row {
    margin-bottom: 32px;

    .stat-card-wrapper {
      height: 100%;

      .stat-card {
        height: 100%;
        border-radius: var(--border-radius-lg);
        overflow: hidden;
        background: var(--background-light);
        box-shadow: var(--shadow-md);
        transition: transform 0.3s, box-shadow 0.3s;
        padding: 20px;

        &:hover {
          transform: translateY(-5px);
          box-shadow: var(--shadow-lg);
        }

        // 不同卡片的颜色主题
        &.stat-card-1 {
          border-top: 4px solid var(--primary-color);
          .stat-card-icon {
            color: var(--primary-color);
            background-color: rgba(59, 130, 246, 0.1);
          }
        }

        &.stat-card-2 {
          border-top: 4px solid var(--success-color);
          .stat-card-icon {
            color: var(--success-color);
            background-color: rgba(16, 185, 129, 0.1);
          }
        }

        &.stat-card-3 {
          border-top: 4px solid var(--warning-color);
          .stat-card-icon {
            color: var(--warning-color);
            background-color: rgba(245, 158, 11, 0.1);
          }
        }

        &.stat-card-4 {
          border-top: 4px solid var(--danger-color);
          .stat-card-icon {
            color: var(--danger-color);
            background-color: rgba(239, 68, 68, 0.1);
          }
        }

        .stat-card-content {
          display: flex;
          align-items: center;
          margin-bottom: 16px;

          .stat-card-icon {
            width: 48px;
            height: 48px;
            border-radius: var(--border-radius-md);
            display: flex;
            justify-content: center;
            align-items: center;
            margin-right: 16px;
            font-size: 24px;
          }

          .stat-card-info {
            flex: 1;

            .stat-card-value {
              font-size: 28px;
              font-weight: 700;
              color: var(--text-primary);
              margin-bottom: 4px;
              line-height: 1.2;
            }

            .stat-card-label {
              font-size: 14px;
              color: var(--text-secondary);
            }
          }
        }

        .stat-card-footer {
          display: flex;
          align-items: center;
          font-size: 14px;

          .trend-indicator {
            display: flex;
            align-items: center;
            margin-right: 8px;
            font-weight: 600;

            .el-icon {
              margin-right: 4px;
            }
          }

          .trend-period {
            color: var(--text-secondary);
          }
        }
      }
    }
  }

  // 图表和快捷功能区域
  .chart-row {
    margin-bottom: 32px;

    .chart-card {
      height: 100%;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .card-title {
          display: flex;
          align-items: center;
          font-weight: 600;

          .el-icon {
            margin-right: 8px;
            font-size: 18px;
            color: var(--primary-color);
          }
        }
      }

      .chart-container {
        height: 400px;

        .chart {
          height: 100%;
          width: 100%;
        }
      }
    }

    .quick-links-card {
      height: 100%;

      .quick-links {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 24px;
        padding: 8px;

        .quick-link {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 24px 16px;
          background-color: #f9fafb;
          border-radius: 20px;
          cursor: pointer;
          transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
          position: relative;
          overflow: hidden;
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);

          /* 背景光效 */
          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: linear-gradient(135deg, rgba(255,255,255,1) 0%, rgba(240,242,245,1) 100%);
            z-index: -1;
          }

          &:hover {
            transform: translateY(-6px);
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1),
                        0 10px 10px -5px rgba(0, 0, 0, 0.04);

            .quick-link-icon {
              transform: scale(1.08) translateY(-5px);
              box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1),
                          0 5px 15px rgba(0, 0, 0, 0.07);
            }

            .quick-link-label {
              transform: translateY(-2px);
              font-weight: 600;
              letter-spacing: 0.5px;
            }
          }

          .quick-link-icon {
            width: 64px;
            height: 64px;
            border-radius: 16px;
            display: flex;
            justify-content: center;
            align-items: center;
            margin-bottom: 16px;
            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            position: relative;
            overflow: hidden;
            box-shadow: 0 6px 16px -8px rgba(0, 0, 0, 0.08),
                        0 9px 28px 0 rgba(0, 0, 0, 0.05),
                        0 12px 48px 16px rgba(0, 0, 0, 0.03);
            border: 2px solid transparent;

            /* 内部光效 */
            &::before {
              content: '';
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              bottom: 0;
              background: linear-gradient(135deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0) 50%);
              z-index: 1;
            }

            /* 外部光晕 */
            &::after {
              content: '';
              position: absolute;
              top: -10%;
              left: -10%;
              right: -10%;
              bottom: -10%;
              background: radial-gradient(circle at center, rgba(255,255,255,0.8) 0%, rgba(255,255,255,0) 70%);
              opacity: 0.6;
              z-index: 1;
              pointer-events: none;
            }

            .el-icon {
              position: relative;
              z-index: 2;
              font-size: 28px;
              filter: drop-shadow(0 3px 5px rgba(0,0,0,0.15));
            }
          }

          .quick-link-label {
            font-size: 15px;
            font-weight: 500;
            transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
            margin-top: 8px;
            text-align: center;
            letter-spacing: 0.3px;
            position: relative;
            padding-bottom: 2px;

            .quick-link-underline {
              position: absolute;
              bottom: -4px;
              left: 50%;
              width: 0;
              height: 2px;
              transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
              transform: translateX(-50%);
              opacity: 0;
              border-radius: 2px;
            }

            /* 悬停时显示下划线 */
            .quick-link:hover & {
              .quick-link-underline {
                width: 70%;
                opacity: 0.7;
              }
            }
          }
        }
      }
    }
  }

  // 待办事项和通知区域
  .todo-row {
    .todo-card, .notification-card {
      height: 100%;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .card-title {
          display: flex;
          align-items: center;
          font-weight: 600;

          .el-icon {
            margin-right: 8px;
            font-size: 18px;
            color: var(--primary-color);
          }
        }
      }
    }

    .todo-list {
      .todo-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 0;
        border-bottom: 1px solid var(--border-light);

        &:last-child {
          border-bottom: none;
        }

        .todo-item-content {
          flex: 1;

          .todo-item-title {
            font-weight: 500;
            color: var(--text-primary);
            margin-bottom: 8px;
          }

          .todo-item-meta {
            display: flex;
            align-items: center;

            .todo-item-date {
              margin-left: 12px;
              font-size: 13px;
              color: var(--text-secondary);
            }
          }
        }
      }
    }

    .notification-list {
      .notification-item {
        display: flex;
        padding: 16px 0;
        border-bottom: 1px solid var(--border-light);

        &:last-child {
          border-bottom: none;
        }

        .notification-icon {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          justify-content: center;
          align-items: center;
          margin-right: 16px;
          flex-shrink: 0;

          &.notification-icon-info {
            background-color: rgba(59, 130, 246, 0.1);
            color: var(--primary-color);
          }

          &.notification-icon-warning {
            background-color: rgba(245, 158, 11, 0.1);
            color: var(--warning-color);
          }

          &.notification-icon-error {
            background-color: rgba(239, 68, 68, 0.1);
            color: var(--danger-color);
          }

          &.notification-icon-success {
            background-color: rgba(16, 185, 129, 0.1);
            color: var(--success-color);
          }
        }

        .notification-content {
          flex: 1;

          .notification-title {
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 6px;
          }

          .notification-text {
            font-size: 14px;
            color: var(--text-regular);
            margin-bottom: 6px;
            line-height: 1.5;
          }

          .notification-time {
            font-size: 12px;
            color: var(--text-secondary);
          }
        }
      }
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;

    .dashboard-header {
      flex-direction: column;
      align-items: flex-start;

      .welcome-section {
        margin-bottom: 16px;
      }
    }

    .card-row {
      .stat-card {
        margin-bottom: 16px;
      }
    }
  }
}
</style>