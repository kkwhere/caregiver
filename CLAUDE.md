# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 项目概述

这是一个全栈护工管理应用：
- **后端**：`caregiver/` 目录中的 Spring Boot 3.2.8 (Java 17) REST API
- **前端**：`caregiver_app/` 目录中的 uni-app (Vue 3) 微信小程序
- **数据库**：MySQL 8.0

## 后端架构 (caregiver/)

### 技术栈
- Spring Boot 3.2.8 with Java 17
- MyBatis-Plus 3.5.9 用于 ORM
- MySQL with HikariCP 连接池
- Lombok 用于减少样板代码
- Maven 用于构建管理

### 包结构
```
com.ck0731.caregiver/
├── controller/     # REST API 端点
├── service/        # 业务逻辑
├── mapper/         # MyBatis-Plus 数据访问
├── entity/         # 领域模型 (Caregiver, Review, User)
└── config/         # 配置 (CORS 等)
```

### 关键配置
- 端口：8080
- 数据库：`caregiver` (在 `application.yaml` 中配置)
- MyBatis-Plus 特性：
  - 自动 ID 生成 (`id-type: auto`)
  - 逻辑删除支持 (`deleted` 字段)
  - 驼峰命名到下划线映射
- HikariCP 连接池，最大生命周期 10 分钟

### 构建和运行命令
```bash
# 构建项目
cd caregiver
./mvnw clean package

# 本地运行
./mvnw spring-boot:run

# 运行测试
./mvnw test

# 运行特定测试
./mvnw test -Dtest=ClassName#methodName
```

### Docker 部署
```bash
# 启动 MySQL 和后端服务
docker-compose up -d

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down
```

**重要**：`application.yaml` 包含远程数据库 URL (116.62.237.84:3306)。使用 Docker Compose 时，后端会通过环境变量自动使用容器化的 MySQL 服务。

## 前端架构 (caregiver_app/)

### 技术栈
- uni-app 框架 (Vue 3)
- 主要目标：微信小程序 (mp-weixin)
- 也支持 Android/iOS 应用构建

### 项目结构
```
caregiver_app/
├── pages/          # 应用页面
│   ├── list/       # 启动页（护工列表）
│   ├── detail/     # 护工详情页
│   └── index/      # 次要页面
├── utils/          # 工具类
│   └── request.js  # API 客户端封装
├── static/         # 静态资源
├── App.vue         # 根组件
├── main.js         # 入口文件
├── pages.json      # 页面路由配置
└── manifest.json   # 应用配置
```

### API 配置
前端通过 `utils/request.js` 连接后端：
- 生产环境：`https://www.ck0731.com`
- 本地开发：`http://127.0.0.1:8080`

根据环境修改 `request.js` 中的 `BASE_URL`：
- H5 浏览器：使用 `localhost` 或局域网 IP (如 `192.168.x.x`)
- 微信小程序真机：必须使用 HTTPS 或禁用 URL 校验

### 开发
uni-app 项目通常使用以下方式开发：
- **HBuilderX IDE**：官方 IDE，内置开发服务器和编译功能
- **uni-app CLI**：命令行工具用于构建和运行

常见工作流程：
1. 在 HBuilderX 中打开 `caregiver_app/`
2. 运行 > 运行到微信小程序（需要微信开发者工具）
3. H5 版本：运行 > 运行到浏览器

## 数据库结构

三个主要实体：
- **User**：用户账户
- **Caregiver**：护工档案和信息
- **Review**：护工的评价/评分

所有实体都使用自增 ID，并通过 `deleted` 字段支持逻辑删除。

## 开发工作流

1. **后端修改**：
   - 修改 `caregiver/src/main/java/com/ck0731/caregiver/` 中的代码
   - 运行 `./mvnw spring-boot:run` 进行本地测试
   - 提交前运行 `./mvnw test`

2. **前端修改**：
   - 修改 `caregiver_app/` 中的代码
   - 如需要，更新 `utils/request.js` 中的 `BASE_URL`
   - 在 HBuilderX 或微信开发者工具中测试

3. **数据库修改**：
   - 更新 `caregiver/src/main/java/com/ck0731/caregiver/entity/` 中的实体类
   - MyBatis-Plus 自动处理基本 CRUD 操作
   - 自定义查询放在 `caregiver/src/main/resources/mapper/` 的 mapper XML 文件中

## 重要说明

- 后端使用 **MyBatis-Plus**，而非普通 MyBatis。它通过 `BaseMapper` 提供自动 CRUD 操作。
- CORS 在 `CorsConfig.java` 中配置，允许前端跨域请求。
- 微信小程序 appid 是 `wxdb63c6dd26c5c6a6`（在 `manifest.json` 中配置）。
- 修改数据库连接设置时，需要同时更新 `application.yaml` 和 `docker-compose.yml`。
