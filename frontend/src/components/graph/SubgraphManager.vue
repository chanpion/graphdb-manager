<template>
  <div class="subgraph-manager">
    <div class="header">
      <h3 class="title">子图管理</h3>
      <el-button type="primary" size="small" @click="extractSubgraph" :disabled="!selectedNodeId || !graphData">
        <el-icon><Share /></el-icon>
        提取子图
      </el-button>
    </div>

    <div class="controls">
      <div class="control-item">
        <label>选择度数 (k):</label>
        <el-input-number 
          v-model="degreeK" 
          :min="1" 
          :max="5" 
          size="small" 
          :disabled="!selectedNodeId || !graphData"
          style="width: 100px;"
        />
      </div>
      <div class="control-item">
        <el-checkbox v-model="includeEdges">包含边</el-checkbox>
        <el-checkbox v-model="limitNodes" v-if="includeEdges">限制节点数</el-checkbox>
      </div>
      <div class="control-item" v-if="limitNodes">
        <label>最大节点数:</label>
        <el-input-number v-model="maxNodes" :min="10" :max="500" size="small" style="width: 100px;" />
      </div>
    </div>

    <div class="subgraph-info" v-if="subgraph.nodes.length > 0">
      <div class="info-section">
        <h4>子图信息</h4>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">中心节点:</span>
            <span class="value">{{ selectedNodeLabel || selectedNodeId }}</span>
          </div>
          <div class="info-item">
            <span class="label">度数 k:</span>
            <span class="value">{{ degreeK }}</span>
          </div>
          <div class="info-item">
            <span class="label">节点数:</span>
            <span class="value">{{ subgraph.nodes.length }}</span>
          </div>
          <div class="info-item">
            <span class="label">边数:</span>
            <span class="value">{{ subgraph.edges.length }}</span>
          </div>
        </div>
      </div>

      <div class="node-list">
        <h4>子图节点 ({{ subgraph.nodes.length }})</h4>
        <div class="list-container">
          <div 
            v-for="node in subgraph.nodes" 
            :key="node.id" 
            class="node-item"
            :class="{ 'center-node': node.id === selectedNodeId }"
          >
            <div class="node-color" :style="{ backgroundColor: getNodeColor(node.label) }"></div>
            <div class="node-details">
              <span class="node-label">{{ node.label || node.id.substring(0, 12) }}</span>
              <span class="node-id">{{ node.id.substring(0, 8) }}...</span>
            </div>
          </div>
        </div>
      </div>

      <div class="actions">
        <el-button type="success" size="small" @click="exportSubgraph('json')">
          <el-icon><Download /></el-icon> 导出 JSON
        </el-button>
        <el-button type="info" size="small" @click="exportSubgraph('csv')">
          <el-icon><Document /></el-icon> 导出 CSV
        </el-button>
        <el-button type="warning" size="small" @click="resetSubgraph">
          <el-icon><Refresh /></el-icon> 重置
        </el-button>
      </div>
    </div>

    <div class="empty-state" v-else>
      <el-empty description="请选择一个节点并提取子图" :image-size="80">
        <template #description>
          <p>选择一个节点，设置度数 k，然后点击"提取子图"按钮</p>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Share, Download, Document, Refresh } from '@element-plus/icons-vue'

const props = defineProps({
  selectedNodeId: {
    type: String,
    default: ''
  },
  selectedNodeLabel: {
    type: String,
    default: ''
  },
  graphData: {
    type: Object,
    default: () => ({ nodes: [], edges: [] })
  }
})

const emit = defineEmits(['subgraph-extracted', 'highlight-subgraph'])

// 状态
const degreeK = ref(2)
const includeEdges = ref(true)
const limitNodes = ref(false)
const maxNodes = ref(100)
const subgraph = ref({ nodes: [], edges: [] })

// 提取子图
const extractSubgraph = () => {
  if (!props.selectedNodeId || !props.graphData) {
    ElMessage.warning('请先选择节点')
    return
  }

  const { nodes, edges } = props.graphData
  
  // 使用 BFS 查找 k 度邻居
  const visited = new Set([props.selectedNodeId])
  const queue = [{ nodeId: props.selectedNodeId, depth: 0 }]
  const subgraphNodes = []
  const subgraphEdges = []

  while (queue.length > 0) {
    const { nodeId, depth } = queue.shift()
    
    if (depth >= degreeK.value) continue

    // 找到当前节点对象
    const currentNode = nodes.find(n => n.id === nodeId)
    if (!currentNode) continue

    // 添加节点到子图
    subgraphNodes.push(currentNode)

    // 查找相连的边
    edges.forEach(edge => {
      if (edge.source.id === nodeId || edge.target.id === nodeId) {
        // 添加边到子图
        if (includeEdges.value) {
          subgraphEdges.push(edge)
        }

        // 将邻居节点加入队列
        const neighborId = edge.source.id === nodeId ? edge.target.id : edge.source.id
        if (!visited.has(neighborId)) {
          visited.add(neighbor)
          queue.push({ nodeId: neighborId, depth: depth + 1 })
        }
      }
    })

    // 如果限制了节点数，达到后停止
    if (limitNodes.value && subgraphNodes.length >= maxNodes.value) {
      break
    }
  }

  // 构建子图
  subgraph.value = {
    nodes: subgraphNodes,
    edges: includeEdges.value ? subgraphEdges : []
  }

  // 通知父组件
  emit('subgraph-extracted', subgraph.value)
  emit('highlight-subgraph', subgraph.value.nodes.map(n => n.id))

  ElMessage.success(`提取了 ${subgraphNodes.length} 个节点和 ${subgraphEdges.length} 条边`)
}

// 获取节点颜色
const getNodeColor = (label) => {
  const colors = {
    'Person': '#409EFF',
    'Company': '#67C23A',
    'Product': '#E6A23C',
    'default': '#909399'
  }
  
  return colors[label] || colors.default
}

// 导出子图
const exportSubgraph = (format) => {
  if (subgraph.value.nodes.length === 0) {
    ElMessage.warning('没有子图数据可导出')
    return
  }

  switch (format) {
    case 'json':
      exportAsJSON()
      break
    case 'csv':
      exportAsCSV()
      break
  }
}

// 导出为 JSON
const exportAsJSON = () => {
  const jsonData = JSON.stringify(subgraph.value, null, 2)
  const blob = new Blob([jsonData], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.download = `subgraph-k${degreeK.value}-${Date.now()}.json`
  link.href = url
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('子图 JSON 导出成功')
}

// 导出为 CSV
const exportAsCSV = () => {
  // 节点 CSV
  const nodeHeaders = ['ID', 'Label', 'Properties']
  const nodeRows = subgraph.value.nodes.map(node => {
    const props = node.properties ? JSON.stringify(node.properties) : ''
    return [node.id, node.label || '', props]
  })
  
  const nodeCSV = [nodeHeaders, ...nodeRows].map(row => 
    row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(',')
  ).join('\n')

  // 边 CSV
  const edgeHeaders = ['Source', 'Target', 'Type', 'Properties']
  const edgeRows = subgraph.value.edges.map(edge => {
    const props = edge.properties ? JSON.stringify(edge.properties) : ''
    return [edge.source.id, edge.target.id, edge.type || '', props]
  })
  
  const edgeCSV = [edgeHeaders, ...edgeRows].map(row => 
    row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(',')
  ).join('\n')

  // 合并 CSV
  const fullCSV = `# 子图节点数据\n${nodeCSV}\n\n# 子图边数据\n${edgeCSV}`
  
  const blob = new Blob([fullCSV], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.download = `subgraph-k${degreeK.value}-${Date.now()}.csv`
  link.href = url
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('子图 CSV 导出成功')
}

// 重置子图
const resetSubgraph = () => {
  subgraph.value = { nodes: [], edges: [] }
  emit('subgraph-extracted', subgraph.value)
  ElMessage.info('子图已重置')
}

// 监听选定节点变化
watch(() => props.selectedNodeId, () => {
  if (props.selectedNodeId) {
    subgraph.value = { nodes: [], edges: [] }
  }
})

// 监听图数据变化
watch(() => props.graphData, () => {
  if (props.graphData.nodes.length === 0) {
    subgraph.value = { nodes: [], edges: [] }
  }
}, { deep: true })
</script>

<style scoped>
.subgraph-manager {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header .title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.controls {
  background: #f9f9f9;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 20px;
}

.control-item {
  margin-bottom: 10px;
}

.control-item:last-child {
  margin-bottom: 0;
}

.control-item label {
  display: block;
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  font-weight: 500;
}

.subgraph-info {
  flex: 1;
  overflow-y: auto;
}

.info-section {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
}

.info-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item .label {
  font-size: 11px;
  color: #909399;
  margin-bottom: 2px;
}

.info-item .value {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.node-list {
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
}

.node-list h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  font-weight: 600;
}

.list-container {
  max-height: 300px;
  overflow-y: auto;
  padding-right: 4px;
}

.node-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.node-item:hover {
  background-color: #f5f7fa;
}

.node-item:last-child {
  border-bottom: none;
}

.node-item.center-node {
  background-color: rgba(255, 107, 107, 0.1);
  border-left: 3px solid #FF6B6B;
}

.node-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 10px;
  flex-shrink: 0;
}

.node-details {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.node-label {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.node-id {
  font-size: 10px;
  color: #909399;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.actions {
  display: flex;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  text-align: center;
}

.empty-state p {
  margin-top: 8px;
  color: #606266;
}
</style>