# API一致性规范

## 概述

为了保持API的一致性和可维护性，本项目采用统一的API路径结构。所有API路径都遵循以下格式：

```
/api/{模块名}/{资源名}
```

例如：
- `/api/auth/login` - 认证模块的登录API
- `/api/system/users` - 系统模块的用户资源API
- `/api/inventory/items` - 资产盘点模块的物品资源API
- `/api/maintenance/orders` - 维修费用模块的订单资源API

## 模块划分

本系统主要包含以下模块：

1. **auth** - 认证相关API
   - 登录、注册、获取用户信息等

2. **system** - 系统管理API
   - 用户管理、角色管理、权限管理等

3. **inventory** - 资产盘点API
   - 资产列表、资产详情、资产盘点等

4. **maintenance** - 维修费用API
   - 维修单管理、维修历史、费用统计等

5. **network** - 网络设备API
   - 设备管理、拓扑图、SSH终端等

6. **supplies** - 物品领用API
   - 物品申请、申请审批、物品归还等

## 资源命名规范

资源名称应使用复数形式，表示资源集合。例如：

- `users` - 用户集合
- `roles` - 角色集合
- `items` - 物品集合
- `orders` - 订单集合

## HTTP方法使用规范

- **GET** - 获取资源
- **POST** - 创建资源
- **PUT** - 更新资源
- **DELETE** - 删除资源

## 示例

### 认证API

```
GET /api/auth/info - 获取当前用户信息
POST /api/auth/login - 用户登录
POST /api/auth/register - 用户注册
POST /api/auth/logout - 用户登出
```

### 用户管理API

```
GET /api/system/users - 获取用户列表
GET /api/system/users/{id} - 获取用户详情
POST /api/system/users - 创建用户
PUT /api/system/users/{id} - 更新用户
DELETE /api/system/users/{id} - 删除用户
```

### 资产盘点API

```
GET /api/inventory/items - 获取资产列表
GET /api/inventory/items/{id} - 获取资产详情
POST /api/inventory/items - 创建资产
PUT /api/inventory/items/{id} - 更新资产
DELETE /api/inventory/items/{id} - 删除资产
```

### 维修费用API

```
GET /api/maintenance/orders - 获取维修单列表
GET /api/maintenance/orders/{id} - 获取维修单详情
POST /api/maintenance/orders - 创建维修单
PUT /api/maintenance/orders/{id} - 更新维修单
DELETE /api/maintenance/orders/{id} - 删除维修单
```

## 注意事项

1. 所有API路径必须以`/api`开头
2. 模块名和资源名使用小写字母
3. 复杂操作可以使用子资源或自定义动词，例如：
   - `/api/system/users/{id}/password` - 重置用户密码
   - `/api/maintenance/orders/{id}/status` - 更新维修单状态

遵循这些规范可以使API更加一致、直观和易于维护。
