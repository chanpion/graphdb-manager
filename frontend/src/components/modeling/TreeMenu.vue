<template>
  <div class="tree-menu">
    <div class="menu-header">
      <h3>模型导航</h3>
      <el-button link @click="refreshData">
        <el-icon><Refresh /></el-icon>
      </el-button>
    </div>

    <el-input
      v-model="searchText"
      placeholder="搜索类型..."
      prefix-icon="Search"
      class="search-input"
      clearable
    />

    <el-scrollbar class="menu-content">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="treeProps"
        :filter-node-method="filterNode"
        :expand-on-click-node="false"
        :default-expanded-keys="defaultExpandedKeys"
        node-key="id"
        class="model-tree"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="node-content">
              <el-icon v-if="data.type === 'vertex'" class="node-icon vertex-icon">
                <Circle />
              </el-icon>
              <el-icon v-else class="node-icon edge-icon">
                <Top />
              </el-icon>
              <span class="node-label">{{ node.label }}</span>
              <el-badge
                v-if="data.propertyCount"
                :value="data.propertyCount"
                class="property-badge"
                type="info"
              />
            </div>
            <div v-if="data.type !== 'folder'" class="node-actions">
              <el-button
                link
                size="small"
                @click.stop="handleEdit(data)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button
                link
                size="small"
                @click.stop="handleDelete(data)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-scrollbar>

    <div class="menu-footer">
      <el-dropdown split-button type="primary" @click="handleAddVertex">
        <el-icon><Plus /></el-icon>
        新建类型
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleAddVertex">
              <el-icon><Circle /></el-icon>
              点类型
            </el-dropdown-item>
            <el-dropdown-item @click="handleAddEdge">
              <el-icon><Top /></el-icon>
              边类型
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  Refresh,
  Search,
  Circle,
  Top,
  Edit,
  Delete,
  Plus
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  vertexTypes: {
    type: Array,
    default: () => []
  },
  edgeTypes: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['refresh', 'add-vertex', 'add-edge', 'edit', 'delete', 'select'])

const treeRef = ref(null)
const searchText = ref('')
const defaultExpandedKeys = ref(['vertex', 'edge'])

const treeProps = {
  children: 'children',
  label: 'label'
}

// 构建树形数据
const treeData = computed(() => {
  return [
    {
      id: 'vertex',
      label: '点类型',
      type: 'folder',
      children: props.vertexTypes.map(vt => ({
        id: vt.name,
        label: vt.name,
        type: 'vertex',
        propertyCount: (vt.properties && vt.properties.length) || 0,
        originalData: vt
      }))
    },
    {
      id: 'edge',
      label: '边类型',
      type: 'folder',
      children: props.edgeTypes.map(et => ({
        id: et.name,
        label: et.name,
        type: 'edge',
        propertyCount: (et.properties && et.properties.length) || 0,
        originalData: et
      }))
    }
  ]
})

// 节点过滤
const filterNode = (value, data) => {
  if (!value) return true
  return data.label.includes(value)
}

// 监听搜索文本变化
watch(searchText, (val) => {
  treeRef.value?.filter(val)
})

// 刷新数据
const refreshData = () => {
  emit('refresh')
}

// 节点点击处理
const handleNodeClick = (data) => {
  if (data.type !== 'folder') {
    emit('select', {
      type: data.type,
      data: data.originalData
    })
  }
}

// 编辑节点
const handleEdit = (data) => {
  emit('edit', {
    type: data.type,
    data: data.originalData
  })
}

// 删除节点
const handleDelete = (data) => {
  ElMessageBox.confirm(
    `确定删除${data.type === 'vertex' ? '点类型' : '边类型'} "${data.label}" 吗？`,
    '警告',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  ).then(() => {
    emit('delete', {
      type: data.type,
      name: data.label
    })
  }).catch(() => {
    // 用户取消
  })
}

// 添加点类型
const handleAddVertex = () => {
  emit('add-vertex')
}

// 添加边类型
const handleAddEdge = () => {
  emit('add-edge')
}
</script>

<style scoped>
.tree-menu {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-right: 1px solid #e8e8e8;
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.menu-header h3 {
  margin: 0;
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.menu-header .el-button {
  color: white;
}

.search-input {
  padding: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.menu-content {
  flex: 1;
  overflow: hidden;
}

.model-tree {
  background: transparent;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.node-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.vertex-icon {
  color: #52c41a;
}

.edge-icon {
  color: #1890ff;
}

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: #303133;
}

.property-badge {
  flex-shrink: 0;
}

.property-badge :deep(.el-badge__content) {
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 5px;
}

.node-actions {
  display: none;
  gap: 4px;
}

.tree-node:hover .node-actions {
  display: flex;
}

.node-actions .el-button {
  padding: 4px;
}

.node-actions .el-button:hover {
  background: rgba(0, 0, 0, 0.04);
}

.menu-footer {
  padding: 12px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
}

.menu-footer .el-dropdown {
  width: 100%;
}

.menu-footer .el-dropdown .el-button {
  width: 100%;
}

/* 覆盖 Element Plus 树形组件样式 */
.model-tree :deep(.el-tree-node__content) {
  height: 36px;
  padding-right: 8px;
}

.model-tree :deep(.el-tree-node__content:hover) {
  background: rgba(0, 0, 0, 0.02);
}

.model-tree :deep(.el-tree-node__expand-icon) {
  padding: 6px;
}

.model-tree :deep(.el-tree-node__expand-icon.is-leaf) {
  display: none;
}
</style>
