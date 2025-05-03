# 贡献指南

感谢您考虑为ICT系统管理平台做出贡献！这个文档提供了如何参与项目开发的指南。

## 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
  - [报告Bug](#报告bug)
  - [提出新功能](#提出新功能)
  - [提交代码](#提交代码)
- [开发流程](#开发流程)
  - [分支管理](#分支管理)
  - [提交信息规范](#提交信息规范)
  - [代码风格](#代码风格)
- [本地开发环境](#本地开发环境)
- [测试](#测试)
- [文档](#文档)
- [版本发布流程](#版本发布流程)

## 行为准则

本项目采用开放、友好、包容的态度欢迎所有贡献者。请尊重其他贡献者，保持专业和建设性的交流。

## 如何贡献

### 报告Bug

如果您发现了Bug，请通过GitHub Issues报告，并提供以下信息：

1. 清晰的Bug描述
2. 重现步骤
3. 预期行为与实际行为
4. 截图（如适用）
5. 环境信息（浏览器、操作系统等）

### 提出新功能

如果您有新功能建议，请通过GitHub Issues提出，并提供以下信息：

1. 功能描述
2. 使用场景
3. 实现思路（可选）

### 提交代码

1. Fork本仓库
2. 创建您的特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建一个Pull Request

## 开发流程

### 分支管理

- `main`: 主分支，保持稳定，随时可发布
- `develop`: 开发分支，包含最新的开发代码
- `feature/*`: 特性分支，用于开发新功能
- `bugfix/*`: 修复分支，用于修复Bug
- `release/*`: 发布分支，用于准备新版本发布

### 提交信息规范

提交信息应遵循以下格式：

```
<类型>(<范围>): <描述>

[可选的详细描述]

[可选的脚注]
```

类型包括：
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码风格更改（不影响代码运行）
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

例如：
```
feat(inventory): 添加资产批量导入功能

添加了通过Excel文件批量导入资产的功能，支持以下字段：
- 资产编号
- 资产名称
- 资产类型
- 购买日期
- 所属部门

Closes #123
```

### 代码风格

#### 后端（Java）

- 遵循Google Java代码风格
- 使用4个空格缩进
- 类名使用PascalCase（如`AssetController`）
- 方法名和变量名使用camelCase（如`getAssetById`）
- 常量使用全大写下划线分隔（如`MAX_RETRY_COUNT`）

#### 前端（Vue.js）

- 遵循Vue.js风格指南
- 使用2个空格缩进
- 组件名使用PascalCase（如`AssetList.vue`）
- 方法名和变量名使用camelCase（如`fetchAssetList`）
- CSS类名使用kebab-case（如`asset-item`）

## 本地开发环境

### 后端

1. 安装JDK 17或更高版本
2. 安装Maven
3. 安装MySQL 8.0+
4. 克隆仓库并进入后端目录
   ```bash
   git clone https://github.com/yourusername/it-system.git
   cd it-system/backend
   ```
5. 配置数据库连接（在`application.yml`中）
6. 运行应用
   ```bash
   mvn spring-boot:run
   ```

### 前端

1. 安装Node.js 14或更高版本
2. 克隆仓库并进入前端目录
   ```bash
   git clone https://github.com/yourusername/it-system.git
   cd it-system/frontend
   ```
3. 安装依赖
   ```bash
   npm install
   ```
4. 运行开发服务器
   ```bash
   npm run serve
   ```

## 测试

### 后端测试

```bash
cd backend
mvn test
```

### 前端测试

```bash
cd frontend
npm run test:unit
```

## 文档

- API文档使用Swagger自动生成
- 前端组件文档使用VuePress生成
- 请确保您的代码包含适当的注释和文档

## 版本发布流程

1. 从`develop`分支创建`release`分支
2. 在`release`分支上进行最终测试和修复
3. 将`release`分支合并到`main`分支
4. 在`main`分支上创建版本标签
5. 将`release`分支合并回`develop`分支

感谢您的贡献！
