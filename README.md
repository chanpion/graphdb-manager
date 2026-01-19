# 图数据库管理系统

一个功能完整的图数据库管理系统，提供统一的管理界面和强大的数据处理能力，支持多种主流图数据库的连接、管理和可视化操作。

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术架构](#技术架构)
- [快速开始](#快速开始)
  - [环境要求](#环境要求)
  - [安装步骤](#安装步骤)
  - [启动应用](#启动应用)
- [配置说明](#配置说明)
- [使用指南](#使用指南)
- [项目结构](#项目结构)
- [开发指南](#开发指南)
- [常见问题](#常见问题)

## 项目简介

图数据库管理系统是一个基于前后端分离架构的Web应用，旨在为图数据库（Neo4j、NebulaGraph、JanusGraph等）提供统一的管理平台。系统集成了连接管理、数据建模、数据浏览、CSV导入等功能，帮助用户高效管理和分析图数据。

## 功能特性

### 已实现功能

#### 连接管理
- ✅ 支持多种图数据库类型（Neo4j、NebulaGraph、JanusGraph）
- ✅ 创建、编辑、删除数据库连接
- ✅ 连接测试功能，实时验证连接状态
- ✅ 连接状态监控和管理

#### 图管理
- ✅ 图列表查看和管理
- ✅ 创建新图
- ✅ 查看图详细信息（节点数、边数、状态等）
- ✅ 删除图操作
- ✅ 图Schema查看

#### 数据建模
- ✅ 可视化定义顶点类型和属性
- ✅ 可视化定义边类型和属性
- ✅ 属性配置（数据类型、必填、唯一性等）
- ✅ 表单验证和数据校验
- ✅ 模型保存和更新

#### 数据浏览器
- ✅ 执行原生查询（Cypher、Gremlin、nGQL）
- ✅ 查询结果表格展示
- ✅ 查询结果图形可视化（D3.js）
- ✅ 查询历史记录
- ✅ 查询模板和代码格式化
- ✅ 查询结果导出

#### CSV导入
- ✅ CSV文件上传和解析
- ✅ 字段映射配置
- ✅ 导入模式选择（仅节点、仅边、节点和边）
- ✅ 批量导入进度跟踪
- ✅ 导入日志实时显示
- ✅ 错误处理和错误日志下载
- ✅ 导入结果统计报告
- ✅ 剩余时间估算

## 技术架构

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.0 | 渐进式JavaScript框架 |
| Vue Router | 4.2.0 | 官方路由管理器 |
| Pinia | 2.1.0 | 状态管理库 |
| Element Plus | 2.4.0 | Vue 3组件库 |
| Axios | 1.6.0 | HTTP客户端 |
| D3.js | 7.8.5 | 数据可视化库 |
| Vite | 7.2.4 | 下一代前端构建工具 |
| Tailwind CSS | 3.4.0 | 原子化CSS框架 |
| PapaParse | 5.5.3 | CSV解析库 |

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.0 | 应用框架 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0.33 | 关系型数据库 |
| Neo4j Driver | 5.12.0 | Neo4j数据库驱动 |
| Nebula Client | 3.6.0 | NebulaGraph客户端 |
| JanusGraph | 1.0.1 | JanusGraph图数据库 |

### 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      用户界面 (Vue 3)                    │
│  连接管理 │ 图管理 │ 数据建模 │ 数据浏览器 │ CSV导入  │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP/REST API
┌────────────────────┴────────────────────────────────────────┐
│                后端服务 (Spring Boot)                      │
│  ┌──────────────┬──────────────┬──────────────┐        │
│  │ graphdb-api  │graphdb-service│ graphdb-core│        │
│  └──────────────┴──────────────┴──────────────┘        │
│         ┌──────────────────────────────────┐              │
│         │    图数据库适配器层             │              │
│  ┌──────┴──────┬────────┬─────────┐  │              │
│  │ Neo4j       │Nebula  │ Janus   │  │              │
│  │ Adapter     │Adapter  │ Adapter  │  │              │
│  └─────────────┴────────┴─────────┘  │              │
└─────────────────────────────────────────┘              │
         ┌──────────────┬──────────────┐                   │
         │  MySQL       │  Graph DBs   │                   │
         │  (元数据)   │ (Neo4j/...) │                   │
         └─────────────┴──────────────┘                   │
```

## 快速开始

### 环境要求

#### 前端开发环境
- Node.js >= 16.0.0
- npm >= 8.0.0 或 yarn >= 1.22.0

#### 后端开发环境
- Java JDK >= 17
- Maven >= 3.6.0
- MySQL >= 8.0.0

#### 可选的图数据库
- Neo4j >= 4.0.0
- NebulaGraph >= 3.0.0
- JanusGraph >= 0.6.0

### 安装步骤

#### 1. 克隆项目

```bash
git clone <repository-url>
cd graphdb-manager
```

#### 2. 后端安装和配置

```bash
# 进入后端目录
cd backend

# 安装依赖（Maven会自动下载）
mvn clean install

# 配置数据库
# 编辑 backend/graphdb-api/src/main/resources/application.yml
# 配置MySQL连接信息、图数据库连接信息等
```

#### 3. 前端安装

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install
# 或使用 yarn
yarn install
```

### 启动应用

#### 启动后端服务

```bash
# 方式1: 使用Maven命令
cd backend/graphdb-api
mvn spring-boot:run

# 方式2: 先打包再运行
cd backend
mvn clean package
java -jar graphdb-api/target/graphdb-api-1.0.0.jar
```

后端服务默认运行在 `http://localhost:8080`

#### 启动前端开发服务器

```bash
cd frontend

# 启动开发服务器
npm run dev

# 或使用 yarn
yarn dev
```

前端开发服务器默认运行在 `http://localhost:3000`，启动后会自动打开浏览器。

#### 构建生产版本

```bash
# 前端构建
cd frontend
npm run build

# 构建产物位于 frontend/dist 目录
```

## 配置说明

### 后端配置

#### MySQL数据库配置

编辑 `backend/graphdb-api/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/graphdb_manager?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
```

#### Neo4j连接配置

```yaml
graphdb:
  neo4j:
    uri: bolt://localhost:7687
    username: neo4j
    password: your_password
```

#### NebulaGraph连接配置

```yaml
graphdb:
  nebula:
    addresses:
      - 127.0.0.1:9669
    username: root
    password: your_password
    space: graphdb_space
```

#### JanusGraph连接配置

```yaml
graphdb:
  janus:
    config:
      storage.backend: cql
      storage.hostname: 127.0.0.1
      storage.port: 9042
      index.search.backend: elasticsearch
      index.search.hostname: 127.0.0.1
      index.search.port: 9200
```

### 前端配置

#### API地址配置

编辑 `frontend/src/api/request.js`：

```javascript
const baseURL = 'http://localhost:8080/api' // 修改为后端实际地址
```

#### 开发服务器配置

编辑 `frontend/vite.config.js`：

```javascript
export default defineConfig({
  server: {
    port: 3000,        // 修改端口
    host: '0.0.0.0',  // 修改监听地址
    open: true          // 自动打开浏览器
  }
})
```

## 使用指南

### 连接数据库

1. 访问 `http://localhost:3000`
2. 进入"连接管理"页面
3. 点击"新增连接"按钮
4. 填写连接信息：
   - 连接名称：自定义名称
   - 数据库类型：选择Neo4j、NebulaGraph或JanusGraph
   - 主机地址：数据库服务器地址
   - 端口：数据库服务端口
   - 用户名/密码：数据库认证信息
5. 点击"测试连接"验证配置
6. 保存连接

### 管理图数据

1. 在连接管理中选择一个连接
2. 进入"图管理"页面查看该连接下的图列表
3. 可以创建新图、查看图详情或删除图

### 数据建模

1. 选择连接和图
2. 进入"数据建模"页面
3. 点击"创建点类型"或"创建边类型"
4. 定义类型名称和属性
5. 保存模型

### 执行查询

1. 进入"数据浏览器"页面
2. 选择连接、图和节点/边类型
3. 在查询编辑器中输入查询语句（Cypher/Gremlin/nGQL）
4. 点击"执行查询"
5. 查看结果表格或图形可视化

### CSV导入

1. 进入"CSV导入"页面
2. 上传CSV文件
3. 配置导入参数：
   - 选择导入模式（节点/边/两者）
   - 配置字段映射
   - 设置批量大小等
4. 预览数据
5. 点击"开始导入"
6. 监控导入进度和日志
7. 查看导入结果报告

## 项目结构

```
graphdb-manager/
├── frontend/                 # 前端项目
│   ├── public/              # 静态资源
│   ├── src/
│   │   ├── api/           # API接口封装
│   │   │   ├── request.js
│   │   │   ├── connection.js
│   │   │   ├── graph.js
│   │   │   └── data.js
│   │   ├── components/    # 通用组件
│   │   │   ├── data/     # 数据相关组件
│   │   │   ├── graph/    # 图形组件
│   │   │   └── query/    # 查询组件
│   │   ├── views/        # 页面组件
│   │   │   ├── Home.vue
│   │   │   ├── ConnectionManagement.vue
│   │   │   ├── GraphManagement.vue
│   │   │   ├── DataModeling.vue
│   │   │   ├── DataExplorer.vue
│   │   │   ├── ImportCSV.vue
│   │   │   └── ImportCSVEnhanced.vue
│   │   ├── stores/       # Pinia状态管理
│   │   │   ├── connection.js
│   │   │   └── graph.js
│   │   ├── router/       # 路由配置
│   │   │   └── index.js
│   │   ├── utils/        # 工具函数
│   │   │   └── apiSimulator.js
│   │   ├── App.vue       # 根组件
│   │   └── main.js      # 入口文件
│   ├── index.css         # Tailwind CSS入口
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── backend/               # 后端项目
│   ├── graphdb-api/      # API接口层
│   │   ├── src/main/java/com/graphdb/api/
│   │   │   ├── controller/      # 控制器
│   │   │   ├── dto/           # 数据传输对象
│   │   │   ├── service/        # 服务接口
│   │   │   └── GraphdbApiApplication.java
│   │   └── pom.xml
│   ├── graphdb-service/  # 业务逻辑层
│   │   ├── src/main/java/com/graphdb/service/
│   │   │   └── GraphService.java
│   │   └── pom.xml
│   ├── graphdb-core/     # 核心层
│   │   ├── src/main/java/com/graphdb/core/
│   │   │   ├── interfaces/      # 接口定义
│   │   │   ├── model/          # 数据模型
│   │   │   └── constant/       # 常量定义
│   │   └── pom.xml
│   ├── graphdb-storage/  # 数据访问层
│   │   ├── src/main/java/com/graphdb/storage/
│   │   │   ├── entity/         # 实体类
│   │   │   └── mapper/         # MyBatis映射
│   │   └── pom.xml
│   ├── graphdb-adapter-neo4j/   # Neo4j适配器
│   ├── graphdb-adapter-nebula/  # NebulaGraph适配器
│   ├── graphdb-adapter-janus/    # JanusGraph适配器
│   └── pom.xml          # Maven父POM
└── docs/               # 技术文档
    ├── 需求设计.md
    ├── 技术方案设计.md
    ├── 技术架构设计.md
    ├── API接口设计.md
    └── ...
```

## 开发指南

### 前端开发

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器（热更新）
npm run dev

# 代码格式检查
npm run lint

# 构建生产版本
npm run build
```

### 后端开发

```bash
# 进入后端目录
cd backend

# 清理并编译
mvn clean compile

# 运行单元测试
mvn test

# 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests
```

### 代码规范

#### 前端
- 使用Vue 3 Composition API
- 遵循ESLint代码规范
- 组件命名使用PascalCase
- 工具函数命名使用camelCase
- 常量命名使用UPPER_SNAKE_CASE

#### 后端
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- Controller层负责请求接收和响应
- Service层负责业务逻辑
- Mapper层负责数据访问

## 常见问题

### Q: 前端启动失败，提示端口被占用？

A: 修改 `frontend/vite.config.js` 中的端口号：

```javascript
server: {
  port: 3001  // 改为其他端口
}
```

### Q: 后端连接MySQL失败？

A: 检查以下几点：
1. MySQL服务是否启动
2. `application.yml` 中的连接配置是否正确
3. MySQL用户是否有权限访问 `graphdb_manager` 数据库
4. 防火墙是否允许端口3306的连接

### Q: Neo4j连接测试失败？

A: 确认：
1. Neo4j服务是否运行
2. 使用正确的协议（bolt:// 而非 http://）
3. 端口是否正确（默认7687）
4. 用户名密码是否正确（默认 neo4j/neo4j）

### Q: CSV导入过程中出现错误？

A: 常见原因：
1. CSV文件编码不是UTF-8
2. 字段映射配置错误
3. 数据类型不匹配
4. 必填字段为空
5. 错误日志会显示具体问题，请根据错误信息调整配置

### Q: 查询结果图形不显示？

A: 检查：
1. 查询结果中是否包含节点和边数据
2. 切换到"图视图"标签页
3. 检查浏览器控制台是否有错误信息

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件

## 致谢

感谢以下开源项目：
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [D3.js](https://d3js.org/)
