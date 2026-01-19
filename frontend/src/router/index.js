import { createRouter, createWebHistory } from 'vue-router'
import { installTabGuard } from './guards/tabGuard'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: '首页',
      icon: 'house',
      closable: true
    }
  },
  {
    path: '/connections',
    name: 'ConnectionManagement',
    component: () => import('../views/ConnectionManagement.vue'),
    meta: {
      title: '连接管理',
      icon: 'connection',
      closable: true
    }
  },
  {
    path: '/graphs',
    name: 'GraphManagement',
    component: () => import('../views/GraphManagement.vue'),
    meta: {
      title: '图管理',
      icon: 'grid',
      closable: true
    }
  },
  {
    path: '/data-modeling',
    name: 'DataModeling',
    component: () => import('../views/DataModeling.vue'),
    meta: {
      title: '图建模',
      icon: 'data-line',
      closable: true
    }
  },
  {
    path: '/data-explorer',
    name: 'DataExplorer',
    component: () => import('../views/DataExplorer.vue'),
    meta: {
      title: '图数据',
      icon: 'search',
      closable: true
    }
  },
  {
    path: '/schema-management',
    name: 'SchemaManagement',
    component: () => import('../views/SchemaManagement.vue'),
    meta: {
      title: 'Schema管理',
      icon: 'document',
      closable: true
    }
  },
  {
    path: '/data-export',
    name: 'DataExport',
    component: () => import('../views/DataExport.vue'),
    meta: {
      title: '数据导出',
      icon: 'download',
      closable: true
    }
  },
  {
    path: '/graph-visualization',
    name: 'GraphVisualization',
    component: () => import('../views/GraphVisualization.vue'),
    meta: {
      title: '图分析',
      icon: 'share',
      closable: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 安装标签页路由守卫
installTabGuard(router)

export default router
