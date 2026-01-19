<script setup>
import { RouterView } from 'vue-router'
import HeaderNavigation from './components/layout/HeaderNavigation.vue'
import SidebarNavigation from './components/layout/SidebarNavigation.vue'
</script>

<template>
  <div class="app-container">
    <!-- 顶部导航栏（包含面包屑、用户信息、搜索等） -->
    <HeaderNavigation :show-tabs="false" />
    
    <div class="main-layout">
      <!-- 左侧导航栏 -->
      <SidebarNavigation class="sidebar" />
      
      <!-- 内容区域 -->
      <div class="content-area">
        <div class="main-content">
          <RouterView />
        </div>
      </div>
    </div>
    
  </div>
</template>

<style scoped>
.app-container {
  height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
}

/* 原来的顶部导航栏样式已移除，由HeaderNavigation组件替代 */

.main-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  flex-shrink: 0;
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.main-content {
  flex: 1;
  padding: 24px;
  background-color: #f5f7fa;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-layout {
    position: relative;
  }
  
  .sidebar {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    z-index: 150;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
  }
  
  .content-area {
    padding-left: 0;
  }
}
</style>
