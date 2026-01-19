<template>
  <header class="header-navigation">
    <!-- 顶部主导航栏 -->
    <div class="top-navbar">
      <!-- 左侧：品牌Logo -->
      <div class="nav-left">
        <!-- 品牌Logo -->
        <router-link to="/" class="brand-logo">
          <el-icon class="logo-icon">
            <DataLine />
          </el-icon>
          <span class="brand-text">图数据库管理系统</span>
        </router-link>
      </div>
      
      <!-- 中间：标签页区域 -->
      <div class="tabs-section-inline" v-if="showTabs">
        <TabSystem
          :show-tabs="true"
          :min-tab-count="1"
          @tab-click="handleTabClick"
          @tab-close="handleTabClose"
          @tab-refresh="handleTabRefresh"
        />
      </div>

      <!-- 右侧：功能区域 -->
      <div class="nav-right">
        <!-- 图选择器 -->
        <div class="graph-selector">
          <el-select
            v-model="selectedGraph"
            placeholder="选择图"
            size="default"
            clearable
            style="width: 200px;"
            @change="handleGraphChange"
            value-key="name"
          >
            <el-option
              v-for="graph in graphs"
              :key="graph.name"
              :label="graph.name"
              :value="graph"
            />
          </el-select>
        </div>

        <!-- API切换器 -->
        <APISwitcher class="api-switcher" />
        
        <!-- 用户信息 -->
        <UserProfileMenu 
          :user="userProfile"
          :show-user-name="showUserName"
          @command="handleUserCommand"
          @theme-change="handleThemeChange"
          class="user-profile"
        />
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useTabStore } from '@/stores/tab'
import { useGraphStore } from '@/stores/graph'
import { 
  DataLine,
  Search,
  Sunny,
  Moon
} from '@element-plus/icons-vue'

// 组件导入
import APISwitcher from '@/components/APISwitcher.vue'
import UserProfileMenu from './UserProfileMenu.vue'
import TabSystem from './TabSystem.vue'

const props = defineProps({
  // 显示选项
  showTabs: {
    type: Boolean,
    default: true
  },
  showSearch: {
    type: Boolean,
    default: true
  },
  showNotifications: {
    type: Boolean,
    default: false
  },
  showUserName: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits([
  'search',
  'user-command',
  'theme-change',
  'graph-change'
])

const tabStore = useTabStore()
const graphStore = useGraphStore()
const { selectedConnectionId, selectedGraphName, graphs } = storeToRefs(graphStore)

// 搜索关键词
const searchKeyword = ref('')

// 主题状态
const isDarkTheme = ref(false)

// 选中的图
const selectedGraph = computed({
  get: () => {
    // 从 graphs 中找到当前选中的图对象
    if (!selectedGraphName.value) return ''
    return graphs.value.find(g => g.name === selectedGraphName.value) || selectedGraphName.value
  },
  set: (value) => {
    if (value && typeof value === 'object') {
      // 如果是对象，设置 connectionId 和 graphName
      selectedConnectionId.value = value.connectionId || ''
      selectedGraphName.value = value.name || ''
    } else {
      // 如果是字符串（兼容旧值）
      selectedGraphName.value = value
    }
  }
})



// 用户信息
const userProfile = ref({
  username: '管理员',
  avatar: '',
  email: 'admin@graphdb.com',
  role: '系统管理员',
  department: '技术部'
})

// 处理搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    emit('search', searchKeyword.value)
    
    // 这里可以实现全局搜索功能
    console.log('执行全局搜索:', searchKeyword.value)
    
    // 清空搜索框
    searchKeyword.value = ''
  }
}

// 切换主题
const toggleTheme = () => {
  isDarkTheme.value = !isDarkTheme.value
  handleThemeChange(isDarkTheme.value)
}

// 处理主题变更
const handleThemeChange = (isDark) => {
  emit('theme-change', isDark)
  
  // 应用主题样式
  if (isDark) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
  
  // 保存主题偏好
  localStorage.setItem('theme', isDark ? 'dark' : 'light')
}









// 处理用户命令
const handleUserCommand = (command) => {
  emit('user-command', command)
}

// 处理标签页点击
const handleTabClick = (tabId) => {
  console.log('标签页点击:', tabId)
}

// 处理标签页关闭
const handleTabClose = (tabId) => {
  console.log('标签页关闭:', tabId)
}

// 处理标签页刷新
const handleTabRefresh = (tabId) => {
  console.log('标签页刷新:', tabId)
}

// 处理图变更
const handleGraphChange = (graph) => {
  console.log('图选择变更:', graph)
  
  if (graph && typeof graph === 'object') {
    // 设置连接ID和图名称
    selectedConnectionId.value = graph.connectionId || ''
    selectedGraphName.value = graph.name || ''
    
    // 加载图数据
    if (graph.name && graph.connectionId) {
      graphStore.loadGraphSchema(graph.name).catch(err => {
        console.error('加载图Schema失败:', err)
      })
    }
  } else if (typeof graph === 'string') {
    // 兼容字符串值（如果还有遗留）
    selectedGraphName.value = graph
  }
}



// 初始化
onMounted(async () => {
  // 加载主题偏好
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    isDarkTheme.value = true
    handleThemeChange(true)
  }

  // 初始化标签页
  tabStore.initTabs()

  // 加载所有连接的图列表
  await graphStore.loadAllGraphs()
})
</script>

<style scoped>
.header-navigation {
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.top-navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  border-bottom: 1px solid #e8e8e8;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: #1890ff;
  font-weight: 600;
  font-size: 18px;
  transition: opacity 0.2s ease;
  flex-shrink: 0;
}

.brand-logo:hover {
  opacity: 0.8;
}

.logo-icon {
  font-size: 24px;
  color: #1890ff;
}

.brand-text {
  white-space: nowrap;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
  min-width: 350px;
  justify-content: flex-end;
}

.search-container {
  width: 200px;
}

.global-search :deep(.el-input__wrapper) {
  border-radius: 16px;
  background-color: #f8f9fa;
  border: 1px solid #e8e8e8;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.global-search :deep(.el-input__wrapper:hover) {
  background-color: #f0f4ff;
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
  transform: translateY(-1px);
}

.global-search :deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff;
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1), 0 2px 12px rgba(24, 144, 255, 0.2);
  transform: translateY(-1px);
}

.api-switcher {
  margin-right: 8px;
}

.theme-toggle-btn {
  padding: 8px;
  color: #606266;
}

.theme-toggle-btn:hover {
  background-color: #f5f7fa;
  color: #1890ff;
}

.theme-icon {
  font-size: 18px;
}



.user-profile {
  margin-left: 8px;
}

.nav-center {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  min-width: 0;
  max-width: calc(100% - 400px);
  gap: 12px;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.nav-center::-webkit-scrollbar {
  display: none;
}

.tabs-section-inline {
  flex: 1;
  display: flex;
  align-items: center;
  min-height: 48px;
  min-width: 0;
  overflow: visible;
  justify-content: flex-start;
  margin-left: 0;
}

/* 内联标签页样式覆盖 */
.tabs-section-inline :deep(.tab-system) {
  background: transparent;
  border-bottom: none;
  height: auto;
  padding: 0;
}

/* 移除滚动条隐藏样式，允许水平滚动 */

.tabs-section-inline :deep(.tab-item) {
  border-radius: 4px;
  margin-right: 6px;
  background: #f0f0f0;
  border: 1px solid #e8e8e8;
  padding: 0 12px;
  height: 36px;
  max-width: 200px;
}

.tabs-section-inline :deep(.tab-item:hover) {
  background: #e6f7ff;
  border-color: #1890ff;
}

.tabs-section-inline :deep(.tab-item.active) {
  background: #ffffff;
  border-color: #1890ff;
  color: #1890ff;
  font-weight: 500;
}

.tabs-section-inline :deep(.tab-actions) {
  border-left: none;
  padding: 0 8px;
}





.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.header-title {
  font-weight: 600;
  color: #1f2d3d;
  font-size: 14px;
}







/* 响应式设计 */
@media (max-width: 992px) {
  .nav-left {
    gap: 16px;
  }
  
  .search-container {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .top-navbar {
    padding: 0 16px;
    height: 56px;
  }
  
  .nav-left {
    gap: 12px;
  }
  
  .brand-text {
    display: none;
  }
  
  .tabs-section-inline {
    display: none;
  }
  
  .search-container {
    display: none;
  }
  

}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .header-navigation {
    background: #1a1a1a;
  }
  
  .top-navbar {
    border-bottom-color: #404040;
  }
  
  .brand-logo {
    color: #1890ff;
  }
  
  .global-search :deep(.el-input__wrapper) {
    background-color: #2a2a2a;
    border-color: #404040;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  }

  .global-search :deep(.el-input__wrapper:hover) {
    background-color: #343a40;
    border-color: #1890ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.25);
    transform: translateY(-1px);
  }

  .global-search :deep(.el-input__wrapper.is-focus) {
    background-color: #2a2a2a;
    border-color: #1890ff;
    box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.15), 0 2px 12px rgba(24, 144, 255, 0.3);
    transform: translateY(-1px);
  }
  
  .theme-toggle-btn:hover {
    background-color: #2a2a2a;
  }
  

  

}

.graph-selector :deep(.el-select) {
  width: 180px;
}

.graph-selector :deep(.el-input__wrapper) {
  border-radius: 6px;
  background: #f8f9fa;
  border: 1px solid #e8e8e8;
  transition: all 0.2s ease;
}

.graph-selector :deep(.el-input__wrapper:hover) {
  background: #f0f4ff;
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.graph-selector :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  border-color: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1), 0 2px 12px rgba(24, 144, 255, 0.2);
}
</style>