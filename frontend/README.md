# 图数据库管理系统 - 前端

## 项目简介

基于 Vue 3 + JavaScript + Element Plus + D3.js 的图数据库管理系统前端项目。

## 技术栈

- Vue 3.4.0
- Vue Router 4.2.0
- Pinia 2.1.0
- Element Plus 2.4.0
- Axios 1.6.0
- D3.js 7.8.5
- Vite 5.0.0
- Tailwind CSS 3.4.0

## 目录结构

```
frontend/
├── public/
├── src/
│   ├── api/              # API接口封装
│   │   ├── request.js
│   │   ├── connection.js
│   │   └── graph.js
│   ├── components/       # 通用组件
│   ├── views/           # 页面组件
│   │   ├── Home.vue
│   │   └── ConnectionManagement.vue
│   ├── stores/          # Pinia状态管理
│   │   ├── connection.js
│   │   └── graph.js
│   ├── utils/           # 工具函数
│   │   └── common.js
│   ├── router/          # Vue Router路由配置
│   │   └── index.js
│   ├── App.vue          # 根组件
│   └── main.js         # 入口文件
├── index.css          # Tailwind CSS入口
├── vite.config.js     # Vite配置
└── package.json        # 项目依赖
```

## 开发命令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 功能模块

### 已完成
- [x] 前端项目结构搭建
- [x] 路由配置
- [ ] 连接管理功能
- [ ] 图管理功能
- [ ] 数据建模功能
- [ ] 图可视化功能
- [ ] CSV导入功能
- [ ] 原生查询功能

## 后续计划

1. 完善连接管理页面的增删改查功能
2. 添加表单验证
3. 实现连接状态管理
4. 创建图管理页面
5. 创建数据建模页面
6. 集成D3.js可视化
7. 实现原生查询编辑器
8. 添加权限控制
9. 完善响应式设计
