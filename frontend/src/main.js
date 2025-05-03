import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './assets/styles/index.scss'
import './assets/styles/element-override.scss'
import './assets/styles/custom-menu.css'

// 导入存储工具类
import { cleanupStorage } from './utils/storageUtils'

// 导入错误处理工具
import ResizeObserverFix from './utils/resizeObserverFix'
import ErrorHandler from './utils/errorHandler'

// 清理可能损坏的存储数据，防止JSON解析错误
cleanupStorage()

// 创建pinia状态管理实例
const pinia = createPinia()

const app = createApp(App)

// 使用错误处理工具
app.use(ResizeObserverFix)
app.use(ErrorHandler)

// 使用ElementPlus，配置中文
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default',
  zIndex: 3000 // 设置更高的基础 z-index
})

// 使用路由
app.use(router)

// 使用状态管理
app.use(pinia)

app.mount('#app')