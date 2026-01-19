<template>
  <div class="right-sidebar" :class="{ 'collapsed': collapsed }">
    <div class="sidebar-header">
      <h3>属性面板</h3>
      <el-button
        link
        :icon="collapsed ? 'ArrowRight' : 'ArrowLeft'"
        @click="toggleCollapse"
        class="collapse-btn"
      >
        <el-icon v-if="!collapsed"><ArrowLeft /></el-icon>
        <el-icon v-else><ArrowRight /></el-icon>
      </el-button>
    </div>

    <div v-if="!collapsed" class="sidebar-content">
      <!-- 图形属性 -->
      <el-card v-if="selectedItem" class="property-card">
        <template #header>
          <div class="card-header">
            <span>{{ selectedItem.type }}</span>
            <el-tag size="small" :type="getItemStatusType(selectedItem.status)">
              {{ selectedItem.status }}
            </el-tag>
          </div>
        </template>

        <el-form label-width="80px" size="small">
          <el-form-item label="ID">
            <el-input v-model="selectedItem.id" readonly />
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model="selectedItem.label" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="selectedItem.nodeType" placeholder="选择类型">
              <el-option label="节点" value="node" />
              <el-option label="边" value="edge" />
            </el-select>
          </el-form-item>
        </el-form>

        <el-divider />

        <div v-if="selectedItem.properties && Object.keys(selectedItem.properties).length > 0">
          <h4>属性</h4>
          <div v-for="(value, key) in selectedItem.properties" :key="key" class="property-item">
            <span class="property-key">{{ key }}:</span>
            <el-input v-model="selectedItem.properties[key]" size="small" />
          </div>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-empty v-else description="请选择节点或边查看属性" />
    </div>

    <div v-else class="sidebar-collapsed">
      <el-tooltip content="展开属性面板" placement="left">
        <el-icon class="expand-icon"><ArrowRight /></el-icon>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const props = defineProps({
  selectedItem: {
    type: Object,
    default: null
  },
  visible: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:selectedItem', 'toggle'])

const collapsed = ref(false)

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
  emit('toggle', collapsed.value)
}

const getItemStatusType = (status) => {
  const statusMap = {
    '正常': 'success',
    '已归档': 'info',
    '草稿': 'warning',
    '错误': 'danger'
  }
  return statusMap[status] || 'info'
}
</script>

<style scoped>
.right-sidebar {
  width: 320px;
  background: white;
  border-left: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.right-sidebar.collapsed {
  width: 48px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.sidebar-header h3 {
  margin: 0;
  color: white;
  font-size: 16px;
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
  padding: 16px;
}

.sidebar-collapsed {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
}

.expand-icon {
  color: white;
  font-size: 24px;
}

.property-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.property-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  gap: 8px;
}

.property-key {
  min-width: 80px;
  color: #666;
  font-size: 14px;
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
