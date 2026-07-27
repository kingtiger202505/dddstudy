# 电商商品报价议价系统

## 项目简介
完整的报价议价系统，包含后端 Spring Boot 服务和前端 H5 页面（支持微信小程序）。

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.18
- **ORM**: MyBatis-Plus 3.5.3.1
- **数据库**: H2 (开发测试) / MySQL (生产)
- **并发控制**: 乐观锁 (@Version 注解 + CAS 更新)
- **架构**: DDD 领域驱动设计

### 前端
- **框架**: Vue 3 + uni-app
- **构建工具**: Vite
- **目标平台**: H5、微信小程序

## 功能特性

### 核心功能
- ✅ 创建报价单
- ✅ 查询报价单详情
- ✅ 买家还价
- ✅ 卖家接受/拒绝还价
- ✅ 买家接受报价（扣减库存）
- ✅ 卖家拒绝报价
- ✅ 状态机流转控制
- ✅ 领域事件发布与监听

### 技术特性
- ✅ 乐观锁并发控制（无悲观锁）
- ✅ MyBatis-Plus 集成
- ✅ 自动填充创建/更新时间
- ✅ 逻辑删除
- ✅ RESTful API
- ✅ 跨域支持
- ✅ 异步事件处理

## 快速开始

### 后端启动
```bash
cd quotation-system/backend
mvn spring-boot:run
```
服务地址：http://localhost:8080
H2 控制台：http://localhost:8080/h2-console

### 前端启动
```bash
cd quotation-system/frontend
npm install
npm run dev
```
H5 访问：http://localhost:3000

### 编译微信小程序
```bash
cd quotation-system/frontend
npm run build:mp-weixin
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/quotation | 创建报价单 |
| GET | /api/quotation/{id} | 查询详情 |
| POST | /api/quotation/counter-offer | 买家还价 |
| POST | /api/quotation/{id}/accept-counter-offer | 卖家接受还价 |
| POST | /api/quotation/{id}/reject-counter-offer | 卖家拒绝还价 |
| POST | /api/quotation/{id}/accept | 买家接受报价 |
| POST | /api/quotation/{id}/reject | 卖家拒绝报价 |

## 数据库表结构

```sql
CREATE TABLE quotation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    original_price DECIMAL(10,2) NOT NULL,
    current_price DECIMAL(10,2) NOT NULL,
    min_acceptable_price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    version INT NOT NULL DEFAULT 1,  -- 乐观锁版本号
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0
);
```

## 状态流转

```
PENDING (待处理)
  ├── counterOffer → COUNTER_OFFER (已还价)
  ├── acceptQuotation → ACCEPTED (已接受)
  └── rejectQuotation → REJECTED (已拒绝)

COUNTER_OFFER (已还价)
  ├── counterOffer → COUNTER_OFFER (再次还价)
  ├── acceptCounterOffer → ACCEPTED (已接受)
  ├── acceptQuotation → ACCEPTED (已接受)
  └── rejectCounterOffer → REJECTED (已拒绝)
```

## 并发控制方案

采用**乐观锁 + CAS**方式：
1. 使用 `@Version` 注解标记版本号字段
2. 更新时检查版本号是否匹配
3. 库存扣减使用 CAS 原子操作
4. 无 `SELECT ... FOR UPDATE` 悲观锁

## 目录结构

```
quotation-system/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/main/java/
│   │   └── com/example/quotation/
│   │       ├── domain/         # 领域层
│   │       │   ├── model/      # 聚合根
│   │       │   ├── repository/ # 仓储接口
│   │       │   └── event/      # 领域事件
│   │       ├── application/    # 应用层
│   │       │   ├── service/    # 应用服务
│   │       │   └── dto/        # 命令/DTO
│   │       ├── infrastructure/ # 基础设施层
│   │       │   └── config/     # 配置类
│   │       └── interfaces/     # 接口层
│   │           └── web/        # REST 控制器
│   └── src/main/resources/
│       ├── application.yml     # 配置文件
│       ├── schema.sql          # 建表脚本
│       └── mapper/             # MyBatis XML
│
└── frontend/                   # 前端 uni-app 项目
    ├── pages/                  # 页面
    │   ├── index/              # 列表页
    │   ├── detail/             # 详情页
    │   └── create/             # 创建页
    ├── src/
    │   ├── utils/              # 工具类
    │   │   └── api.js          # API 封装
    │   ├── App.vue             # 应用入口
    │   └── main.js             # 入口文件
    ├── pages.json              # 页面配置
    └── package.json            # 依赖配置
```

## 注意事项

1. **并发安全**: 所有更新操作都带版本号检查，避免超卖
2. **事务边界**: 领域事件在事务提交后异步执行
3. **跨域配置**: 后端已配置允许跨域，方便前后端分离开发
4. **小程序适配**: 使用 uni-app 实现一套代码多端运行

## 测试

运行单元测试：
```bash
cd backend
mvn test
```
