# ICT系统管理平台

这是一个综合性的ICT管理系统，用于管理企业的IT资产、维修、网络设备和物品领用等。

## 功能特点

- **资产盘点管理**：跟踪企业IT资产的生命周期、位置和状态
- **维修费用管理**：记录和跟踪设备的维修历史和相关费用
- **网络设备管理**：管理网络设备的配置、状态和拓扑
- **物品领用管理**：管理办公用品的申请、审批和发放

## 技术栈

- 前端: Vue.js 3, Element UI Plus, jsPlumb (拓扑图), SheetJS (Excel处理)
- 后端: Spring Boot 2.7+, MySQL Driver, JSch (SSH连接)
- 数据库: MySQL 8.0+ (本地直接安装，非Docker容器)
- 容器化: Docker

> 注意：本项目已从MongoDB迁移到MySQL，且MySQL是直接安装在本地系统上的，而不是通过Docker容器运行。

## 目录结构

```
ict-system/
├── frontend/          # Vue.js前端应用
├── backend/           # Spring Boot后端应用
├── docker-compose.yml # Docker部署配置
├── start-app.ps1      # 开发环境一键启动脚本
└── README.md          # 项目说明文档
```

## 部署选项

### 1. 使用Docker Compose (推荐用于生产环境)

这是最推荐的部署方式，它会自动构建和启动所有服务，包括MongoDB数据库、后端API服务和前端应用服务。

#### 前提条件

- 安装 [Docker](https://docs.docker.com/get-docker/)
- 安装 [Docker Compose](https://docs.docker.com/compose/install/)

#### 部署步骤

1. 克隆仓库
   ```
   git clone <repository-url>
   cd ict-system
   ```

2. 构建并启动服务
   ```
   docker-compose up -d
   ```

3. 访问应用
   - 前端界面: http://localhost
   - 后端API: http://localhost/api

4. 停止服务
   ```
   docker-compose down
   ```

### 2. 使用PowerShell脚本 (推荐用于开发环境)

我们提供了一个PowerShell脚本，用于在开发环境中快速启动前端和后端服务。

#### 前提条件

- Node.js 和 npm
- Java 17+ 和 Maven
- MongoDB 实例
- PowerShell 5.0+

#### 启动步骤

1. 运行启动脚本
   ```
   ./start-app.ps1
   ```

2. 访问应用
   - 前端界面: http://localhost:8080
   - 后端API: http://localhost:8090/api

### 3. 手动启动 (适用于自定义部署)

如果需要单独启动前端或后端服务，可以按照以下步骤操作。

#### 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

#### 启动前端服务

```bash
cd frontend
npm install
npm run serve
```

## 环境变量配置

### 后端环境变量

| 变量名 | 描述 | 默认值 |
|-------|------|--------|
| SPRING_DATASOURCE_URL | MySQL数据库URL | jdbc:mysql://localhost:3306/ict_system |
| SPRING_DATASOURCE_USERNAME | MySQL用户名 | root |
| SPRING_DATASOURCE_PASSWORD | MySQL密码 | xp198997 |
| SERVER_PORT | 服务器端口 | 8090 |

> 注意：本项目使用的是本地直接安装的MySQL，而不是Docker容器。如需使用Docker容器运行MySQL，请参考docker-compose.yml中的注释配置。

### 前端环境变量

| 变量名 | 描述 | 默认值 |
|-------|------|--------|
| VUE_APP_BASE_API | 后端API基础路径 | http://localhost:8090/api |

## 故障排除

### 端口冲突

如果出现端口冲突错误 (如"端口8090已被使用")，可以通过以下方式解决：

1. 使用任务管理器或命令行查找并终止占用端口的进程
   ```
   netstat -ano | findstr :8090
   taskkill /F /PID <进程ID>
   ```

2. 修改应用程序配置使用不同的端口
   - 后端: 修改`application.properties`中的`server.port`
   - 前端: 在`.env`文件中更新API地址

## 数据库备份与恢复

### 备份MongoDB数据

```
docker exec ict-mongodb mongodump --out /backup
docker cp ict-mongodb:/backup ./backup
```

### 恢复MongoDB数据

```
docker cp ./backup ict-mongodb:/backup
docker exec ict-mongodb mongorestore /backup
```

## 用户账号

系统内置了以下用户账号用于测试：

| 用户名 | 密码 | 角色 |
|-------|------|------|
| admin | 123456 | 系统管理员 |
| manager | 123456 | 部门经理 |
| it | 123456 | IT专员 |
| user | 123456 | 普通员工 |

## 功能模块

### 资产盘点模块

- 资产列表：查看所有IT资产
- 资产详情：查看单个资产的详细信息
- 资产盘点：定期盘点资产
- 资产报表：导出资产报表

### 维修费用模块

- 维修单管理：创建和跟踪维修工单
- 维修历史：查看设备维修历史
- 费用统计：分析维修费用

### 网络设备模块

- 设备管理：管理网络设备
- 拓扑图：可视化网络拓扑
- SSH终端：远程连接设备

### 物品领用模块

- 物品申请：申请领用办公用品
- 申请审批：审批领用申请
- 物品归还：归还物品

## API文档

后端API文档可以通过访问以下地址获得：
- Swagger UI: http://localhost:8090/api/swagger-ui.html

### API一致性规范

为了保持API的一致性和可维护性，本项目采用统一的API路径结构。所有API路径都遵循以下格式：

```
/api/{模块名}/{资源名}
```

例如：
- `/api/auth/login` - 认证模块的登录API
- `/api/system/users` - 系统模块的用户资源API
- `/api/inventory/items` - 资产盘点模块的物品资源API
- `/api/maintenance/orders` - 维修费用模块的订单资源API

详细的API一致性规范请参考[API一致性规范文档](api-consistency.md)。

## 贡献

欢迎提交Issue和Pull Request来帮助改进这个项目。

## 许可证

[MIT License](LICENSE)

