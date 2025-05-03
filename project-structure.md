# ICT系统项目结构

## 前端结构 (Vue.js + Element UI)
```
frontend/
├── public/                  # 静态资源
├── src/
│   ├── assets/              # 图片、字体等资源
│   ├── components/          # 通用组件
│   ├── views/               # 页面视图
│   │   ├── maintenance/     # 维修费用模块
│   │   ├── inventory/       # 资产盘点模块
│   │   ├── network/         # 网络设备模块
│   │   └── supplies/        # 物品领用模块
│   ├── router/              # 路由配置
│   ├── store/               # Vuex状态管理
│   ├── utils/               # 工具函数
│   ├── api/                 # API请求
│   ├── App.vue              # 根组件
│   └── main.js              # 入口文件
├── package.json             # 依赖配置
└── vue.config.js            # Vue配置
```

## 后端结构 (Spring Boot)
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/ict/system/
│   │   │   ├── controller/      # API控制器
│   │   │   │   ├── maintenance/ # 维修费用控制器
│   │   │   │   ├── inventory/   # 资产盘点控制器
│   │   │   │   ├── network/     # 网络设备控制器
│   │   │   │   └── supplies/    # 物品领用控制器
│   │   │   ├── service/         # 业务逻辑
│   │   │   ├── repository/      # 数据访问
│   │   │   ├── model/           # 数据模型
│   │   │   ├── config/          # 配置类
│   │   │   ├── util/            # 工具类
│   │   │   └── IctSystemApplication.java # 启动类
│   │   └── resources/
│   │       ├── application.properties    # 应用配置
│   │       └── static/                   # 静态资源
│   └── test/                             # 测试代码
├── pom.xml                              # Maven配置
└── README.md                            # 项目说明
```

## 数据库结构 (MongoDB)
- 维修费用集合 (MaintenanceOrders)
- 资产盘点集合 (InventoryItems)
- 网络设备集合 (NetworkDevices)
- 物品领用集合 (SupplyRequests)
- 用户集合 (Users)
- 部门集合 (Departments)
- 供应商集合 (Suppliers)

## 技术栈
- 前端: Vue.js 3, Element UI Plus, jsPlumb (拓扑图), SheetJS (Excel处理)
- 后端: Spring Boot 2.7+, MongoDB Driver, JSch (SSH连接)
- 数据库: MongoDB 5.0+, Redis 6.0+ (缓存)
- 容器化: Docker
- CI/CD: GitHub Actions 