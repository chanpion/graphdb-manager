<template>
  <div class="tab-system" v-if="showTabs">
    <div class="tabs-container" ref="tabsContainer">
      <div class="tabs-scroll">
        <!-- 标签页列表 -->
        <div 
          v-for="tab in allTabs" 
          :key="tab.id"
          :class="[
            'tab-item',
            { 
              'active': tab.id === activeTabId,
              'fixed': fixedTabIds.includes(tab.id)
            }
          ]"
          @click="handleTabClick(tab.id)"
          @contextmenu.prevent="handleTabContextMenu($event, tab.id)"
          draggable="true"
          @dragstart="handleDragStart($event, tab.id)"
          @dragover.prevent="handleDragOver($event, tab.id)"
          @drop="handleDrop($event, tab.id)"
        >
          <!-- 标签页图标 -->
          <el-icon v-if="tab.icon" class="tab-icon">
            <component :is="getIconComponent(tab.icon)" />
          </el-icon>
          
          <!-- 标签页标题 -->
          <span class="tab-title">{{ tab.title }}</span>
          
          <!-- 关闭按钮（只有可关闭的标签页显示） -->
          <el-icon 
            v-if="tab.closable && !fixedTabIds.includes(tab.id)" 
            class="close-icon"
            @click.stop="handleCloseTab(tab.id)"
          >
            <Close />
          </el-icon>
        </div>
      </div>
    </div>
    
    <!-- 标签页操作按钮 -->
    <div class="tab-actions">
      <el-dropdown 
        trigger="click" 
        placement="bottom-end"
        @command="handleTabAction"
      >
        <el-button link class="more-actions-btn">
          <el-icon><MoreFilled /></el-icon>
        </el-button>
        
        <template #dropdown>
          <el-dropdown-menu class="tab-actions-menu">
            <el-dropdown-item command="refresh-current">
              <el-icon><Refresh /></el-icon>
              <span>刷新当前标签页</span>
            </el-dropdown-item>
            
            <el-dropdown-item command="close-other">
              <el-icon><Remove /></el-icon>
              <span>关闭其他标签页</span>
            </el-dropdown-item>
            
            <el-dropdown-item command="close-all">
              <el-icon><Delete /></el-icon>
              <span>关闭所有标签页</span>
            </el-dropdown-item>

            <el-dropdown-item command="show-all">
              <el-icon><Menu /></el-icon>
              <span>显示所有标签页</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <!-- 右键菜单 -->
    <div 
      v-if="contextMenu.visible" 
      class="tab-context-menu"
      :style="{ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }"
      @click.stop
    >
      <div class="context-menu-item" @click="handleContextMenuAction('refresh')">
        <el-icon><Refresh /></el-icon>
        <span>刷新</span>
      </div>
      
      <div class="context-menu-item" @click="handleContextMenuAction('close')">
        <el-icon><Close /></el-icon>
        <span>关闭</span>
      </div>
      
      <div class="context-menu-item" @click="handleContextMenuAction('close-other')">
        <el-icon><Remove /></el-icon>
        <span>关闭其他</span>
      </div>
      
      <div class="context-menu-item" @click="handleContextMenuAction('close-all')">
        <el-icon><Delete /></el-icon>
        <span>关闭所有</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useTabStore } from '@/stores/tab'
import { 
  Close, 
  MoreFilled, 
  Refresh,
  Remove,
  Delete,
  Menu,
  House,
  Connection,
  Grid,
  DataLine,
  Search,
  Upload
} from '@element-plus/icons-vue'

const props = defineProps({
  // 是否显示标签页系统
  showTabs: {
    type: Boolean,
    default: true
  },
  // 最小标签页数量（小于此数量时不显示关闭按钮）
  minTabCount: {
    type: Number,
    default: 1
  }
})

const emit = defineEmits(['tab-click', 'tab-close', 'tab-refresh'])

const tabStore = useTabStore()

// 右键菜单状态
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  tabId: ''
})

// 拖拽状态
const dragState = ref({
  dragging: false,
  sourceTabId: '',
  targetTabId: ''
})

// 计算属性
const allTabs = computed(() => tabStore.allTabs)
const activeTabId = computed(() => tabStore.activeTabId)
const fixedTabIds = computed(() => tabStore.fixedTabIds)

// 图标组件映射
const iconMap = {
  'house': House,
  'connection': Connection,
  'grid': Grid,
  'data-line': DataLine,
  'search': Search,
  'upload': Upload
}

// 获取图标组件
const getIconComponent = (iconName) => {
  return iconMap[iconName] || House
}

// 处理标签页点击
const handleTabClick = (tabId) => {
  tabStore.activateTab(tabId)
  emit('tab-click', tabId)
}

// 处理关闭标签页
const handleCloseTab = (tabId) => {
  // 允许关闭所有标签页（包括最后一个）
  const success = tabStore.closeTab(tabId)
  if (success) {
    emit('tab-close', tabId)
  }
}

// 处理右键菜单
const handleTabContextMenu = (event, tabId) => {
  event.preventDefault()
  
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    tabId
  }
  
  // 点击其他地方关闭右键菜单
  const closeMenu = (e) => {
    if (!e.target.closest('.tab-context-menu')) {
      contextMenu.value.visible = false
      document.removeEventListener('click', closeMenu)
    }
  }
  
  setTimeout(() => {
    document.addEventListener('click', closeMenu)
  }, 10)
}

// 处理右键菜单操作
const handleContextMenuAction = (action) => {
  const { tabId } = contextMenu.value
  
  switch (action) {
    case 'refresh':
      tabStore.refreshTab(tabId)
      emit('tab-refresh', tabId)
      break
      
    case 'close':
      handleCloseTab(tabId)
      break
      
    case 'close-other':
      tabStore.closeOtherTabs(tabId)
      break
      
    case 'close-all':
      tabStore.closeAllTabs()
      break
  }
  
  contextMenu.value.visible = false
}

// 处理标签页操作
const handleTabAction = (command) => {
  switch (command) {
    case 'refresh-current':
      if (activeTabId.value) {
        tabStore.refreshTab(activeTabId.value)
        emit('tab-refresh', activeTabId.value)
      }
      break
      
    case 'close-other':
      if (activeTabId.value) {
        tabStore.closeOtherTabs(activeTabId.value)
      }
      break
      
    case 'close-all':
      tabStore.closeAllTabs()
      break
      
    case 'show-all':
      // 可以在这里实现显示所有标签页的预览功能
      console.log('显示所有标签页')
      break
  }
}

// 拖拽相关处理
const handleDragStart = (event, tabId) => {
  dragState.value = {
    dragging: true,
    sourceTabId: tabId,
    targetTabId: ''
  }
  
  event.dataTransfer.setData('text/plain', tabId)
  event.dataTransfer.effectAllowed = 'move'
}

const handleDragOver = (event, tabId) => {
  event.preventDefault()
  if (dragState.value.dragging && dragState.value.sourceTabId !== tabId) {
    dragState.value.targetTabId = tabId
  }
}

const handleDrop = (event, tabId) => {
  event.preventDefault()
  
  if (dragState.value.dragging && dragState.value.sourceTabId && dragState.value.sourceTabId !== tabId) {
    // 重新排序标签页
    const sourceIndex = allTabs.value.findIndex(tab => tab.id === dragState.value.sourceTabId)
    const targetIndex = allTabs.value.findIndex(tab => tab.id === tabId)
    
    if (sourceIndex !== -1 && targetIndex !== -1) {
      const [removed] = allTabs.value.splice(sourceIndex, 1)
      allTabs.value.splice(targetIndex, 0, removed)
      
      // 保存到store
      tabStore.tabs = [...allTabs.value]
      tabStore.saveTabsToStorage()
    }
  }
  
  // 重置拖拽状态
  dragState.value = {
    dragging: false,
    sourceTabId: '',
    targetTabId: ''
  }
}

// 处理文档点击，关闭右键菜单
const handleDocumentClick = () => {
  if (contextMenu.value.visible) {
    contextMenu.value.visible = false
  }
}

// 初始化
onMounted(() => {
  document.addEventListener('click', handleDocumentClick)
  
  // 确保首页标签存在
  tabStore.initTabs()
})

onUnmounted(() => {
  document.removeEventListener('click', handleDocumentClick)
})

// 暴露方法
defineExpose({
  refreshCurrentTab: () => {
    if (activeTabId.value) {
      tabStore.refreshTab(activeTabId.value)
    }
  },
  closeCurrentTab: () => {
    if (activeTabId.value) {
      handleCloseTab(activeTabId.value)
    }
  },
  addNewTab: (tabInfo) => {
    tabStore.addOrActivateTab(tabInfo)
  }
})
</script>

<style scoped>
.tab-system {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  height: 48px;
  user-select: none;
  position: relative;
  z-index: 100;
}

.tabs-container {
  flex: 1;
  height: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}

.tabs-container::-webkit-scrollbar {
  display: none;
}

.tabs-container {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.tabs-scroll {
  display: flex;
  height: 100%;
  align-items: center;
  padding: 0 8px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  height: 36px;
  min-width: 80px;
  max-width: 200px;
  border: 1px solid #e8e8e8;
  border-radius: 6px 6px 0 0;
  margin-right: 4px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tab-item:hover {
  background: #f0f7ff;
  border-color: #1890ff;
}

.tab-item.active {
  background: #ffffff;
  border-bottom-color: transparent;
  border-color: #1890ff;
  color: #1890ff;
  font-weight: 500;
  z-index: 10;
}

.tab-item.fixed {
  background: linear-gradient(135deg, #f8f9fa 0%, #e8e8e8 100%);
}

.tab-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.tab-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.close-icon {
  font-size: 12px;
  color: #999;
  opacity: 0.6;
  transition: all 0.2s ease;
  flex-shrink: 0;
  padding: 2px;
  border-radius: 3px;
}

.close-icon:hover {
  color: #ff4d4f;
  opacity: 1;
  background-color: rgba(255, 77, 79, 0.1);
}

.tab-actions {
  padding: 0 12px;
  display: flex;
  align-items: center;
  height: 100%;
  border-left: 1px solid #e8e8e8;
  background: #ffffff;
}

.more-actions-btn {
  padding: 6px;
}

.more-actions-btn:hover {
  background-color: #f5f7fa;
}

/* 右键菜单样式 */
.tab-context-menu {
  position: fixed;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 160px;
  z-index: 1000;
  padding: 6px 0;
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.context-menu-item:hover {
  background-color: #f5f7fa;
  color: #1890ff;
}

.context-menu-item .el-icon {
  font-size: 16px;
  width: 20px;
}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .tab-system {
    background: #1a1a1a;
    border-bottom-color: #404040;
  }
  
  .tabs-container {
    scrollbar-color: #4a4a4a #2a2a2a;
  }
  
  .tabs-container::-webkit-scrollbar {
    display: none;
  }
  
  .tabs-container {
    -ms-overflow-style: none;
    scrollbar-width: none;
  }
  
  .tab-item {
    background: #2a2a2a;
    border-color: #404040;
    color: #d9d9d9;
  }
  
  .tab-item:hover {
    background: rgba(24, 144, 255, 0.1);
    border-color: #1890ff;
  }
  
  .tab-item.active {
    background: #1a1a1a;
    color: #1890ff;
  }
  
  .tab-item.fixed {
    background: linear-gradient(135deg, #2a2a2a 0%, #404040 100%);
  }
  
  .close-icon {
    color: #8c8c8c;
  }
  
  .close-icon:hover {
    color: #ff4d4f;
    background-color: rgba(255, 77, 79, 0.2);
  }
  
  .tab-actions {
    background: #1a1a1a;
    border-left-color: #404040;
  }
  
  .more-actions-btn:hover {
    background-color: #2a2a2a;
  }
  
  .tab-context-menu {
    background: #2a2a2a;
    border-color: #404040;
  }
  
  .context-menu-item {
    color: #d9d9d9;
  }
  
  .context-menu-item:hover {
    background-color: #3a3a3a;
    color: #1890ff;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tab-system {
    height: 40px;
  }
  
  .tab-item {
    padding: 0 12px;
    min-width: 60px;
    max-width: 150px;
    height: 32px;
  }
  
  .tab-title {
    font-size: 12px;
  }
  
  .tab-icon {
    font-size: 12px;
  }
  
  .close-icon {
    font-size: 10px;
  }
}
</style>