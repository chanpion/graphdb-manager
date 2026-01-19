<template>
  <div class="sidebar-navigation" :class="{ 'collapsed': collapsed }">
    <div class="sidebar-header">
      <div class="brand-section" v-if="!collapsed">
        <h3><el-icon><Menu /></el-icon></h3>
      </div>
      <el-button
        link
        @click="toggleCollapse"
        class="collapse-btn"
        :title="collapsed ? '展开菜单' : '折叠菜单'"
      >
        <el-icon v-if="!collapsed"><ArrowLeft /></el-icon>
        <el-icon v-else><ArrowRight /></el-icon>
      </el-button>
    </div>

    <div class="sidebar-content" v-if="!collapsed">
      <el-menu
        :default-active="activeMenu"
        class="nav-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-menu-item index="/connections">
          <el-icon><Connection /></el-icon>
          <span>连接管理</span>
        </el-menu-item>
        
        <el-menu-item index="/graphs">
          <el-icon><Grid /></el-icon>
          <span>图管理</span>
        </el-menu-item>
        
        <el-menu-item index="/data-modeling">
          <el-icon><DataLine /></el-icon>
          <span>图建模</span>
        </el-menu-item>
        
        <el-menu-item index="/data-explorer">
          <el-icon><Search /></el-icon>
          <span>图数据</span>
        </el-menu-item>

        <el-menu-item index="/graph-visualization">
          <el-icon><Share /></el-icon>
          <span>图分析</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="sidebar-collapsed" v-else>
      <el-menu
        :default-active="activeMenu"
        class="collapsed-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/" :title="'首页'">
          <el-icon><House /></el-icon>
        </el-menu-item>
        
        <el-menu-item index="/connections" :title="'连接管理'">
          <el-icon><Connection /></el-icon>
        </el-menu-item>
        
        <el-menu-item index="/graphs" :title="'图管理'">
          <el-icon><Grid /></el-icon>
        </el-menu-item>
        
        <el-menu-item index="/data-modeling" :title="'图建模'">
          <el-icon><DataLine /></el-icon>
        </el-menu-item>
        
        <el-menu-item index="/data-explorer" :title="'图数据'">
          <el-icon><Search /></el-icon>
        </el-menu-item>

        <el-menu-item index="/graph-visualization" :title="'图分析'">
          <el-icon><Share /></el-icon>
        </el-menu-item>
      </el-menu>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowLeft,
  ArrowRight,
  House,
  Connection,
  Grid,
  DataLine,
  Search,
  Share,
  Menu
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const collapsed = ref(false)

const activeMenu = computed(() => {
  // 确保路径匹配更加精确
  return route.path;
})

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
  // 可以在这里触发全局事件，让其他组件知道侧边栏状态变化
  emit('toggle', collapsed.value)
}

const handleMenuSelect = (index) => {
  router.push(index)
}

const emit = defineEmits(['toggle'])
</script>

<style scoped>
.sidebar-navigation {
  width: 180px;
  height: 100%;
  background: white;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.sidebar-navigation.collapsed {
  width: 64px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #e8e8e8;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 40px;
}

.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.brand-section h3 {
  margin: 0;
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.collapse-btn {
  color: white;
  padding: 4px;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.nav-menu {
  border-right: none;
  background-color: white;
}

.nav-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s ease;
}

.nav-menu .el-menu-item:hover {
  background-color: #f5f7fa;
}

.nav-menu .el-menu-item.is-active {
  background-color: #ecf5ff;
  color: #409eff;
}

.nav-menu .el-menu-item.is-active .el-icon {
  color: #409eff;
}

.sidebar-collapsed {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
}

.collapsed-menu {
  border-right: none;
  background-color: white;
  width: 100%;
}

.collapsed-menu .el-menu-item {
  height: 48px;
  width: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 4px 0;
  border-radius: 6px;
}

.collapsed-menu .el-menu-item:hover {
  background-color: #f5f7fa;
}

.collapsed-menu .el-menu-item.is-active {
  background-color: #ecf5ff;
  color: #409eff;
}

.collapsed-menu .el-menu-item.is-active .el-icon {
  color: #409eff;
}

/* 滚动条样式 */
.sidebar-content::-webkit-scrollbar {
  width: 6px;
}

.sidebar-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.sidebar-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.sidebar-content::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>