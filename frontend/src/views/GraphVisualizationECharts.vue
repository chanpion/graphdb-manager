<template>
  <div class="graph-visualization">
    <el-container>
      <!-- 左侧查询面板 -->
      <el-aside width="300px" class="query-panel">
        <div class="panel-header">
          <h3>ECharts 图查询</h3>
        </div>

        <div class="section">
          <el-button type="primary" @click="loadSampleData">加载示例数据</el-button>
        </div>
        
        <!-- 查询编辑器 -->
        <div class="section editor-section">
          <h4>查询语句</h4>
          <div class="query-editor-container">
            <div class="code-editor-wrapper">
              <textarea
                v-model="queryStatement"
                ref="queryEditor"
                class="code-editor"
                placeholder="输入图数据库查询语句，例如：MATCH (n)-[r]->(m) RETURN n, r, m LIMIT 50"
                @input="highlightSyntax"
                @keydown="handleKeydown"
              ></textarea>
              <div class="code-highlight" ref="highlightElement"></div>
            </div>
            <div class="editor-actions">
              <el-button size="small" @click="formatQuery">
                <el-icon><MagicStick /></el-icon>
                格式化
              </el-button>
              <el-button size="small" @click="clearQuery">
                <el-icon><Delete /></el-icon>
                清空
              </el-button>
              <el-button type="primary" size="small" @click="executeQuery" :loading="queryLoading">
                <el-icon><VideoPlay /></el-icon>
                {{ queryLoading ? `执行中... (${Math.round(queryProgress * 100)}%)` : '执行查询' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 查询历史 -->
        <div class="section" v-if="queryHistory.length > 0">
          <h4>查询历史</h4>
          <div class="history-list">
            <div 
              v-for="(item, index) in queryHistory.slice(0, 5)" 
              :key="index"
              class="history-item"
              @click="loadHistoryQuery(item)"
            >
              <div class="history-content">{{ item.statement.substring(0, 60) }}{{ item.statement.length > 60 ? '...' : '' }}</div>
              <div class="history-time">{{ formatTime(item.timestamp) }}</div>
            </div>
          </div>
        </div>
      </el-aside>

      <!-- 主内容区：可视化展示 -->
      <el-main class="visualization-area">
        <!-- 工具栏 -->
        <div class="viz-toolbar">
          <div class="toolbar-left">
            <h2>ECharts 图分析</h2>
            <span class="stats" v-if="graphData.nodes.length > 0">
              节点: {{ graphData.nodes.length }} | 边: {{ graphData.edges.length }}
            </span>
          </div>
          <div class="toolbar-right">
            <el-button-group size="small">
              <el-button @click="handleZoomOut" :disabled="currentZoom <= 0.5">
                <el-icon><ZoomOut /></el-icon>
              </el-button>
              <el-button @click="handleZoomIn" :disabled="currentZoom >= 3">
                <el-icon><ZoomIn /></el-icon>
              </el-button>
              <el-dropdown trigger="click" @command="handleLayoutTypeChange">
                <el-button>
                  {{ currentLayoutLabel }}
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="option in graphLayoutOptions"
                      :key="option.value"
                      :command="option.value"
                    >
                      {{ option.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button @click="resetView">
                <el-icon><Refresh /></el-icon>
              </el-button>
              <el-button @click="exportVisualization">
                <el-icon><Download /></el-icon>
              </el-button>
            </el-button-group>
          </div>
        </div>

        <!-- 可视化画布 -->
        <div class="viz-canvas-container" ref="vizContainer">
          <div class="graph-container">
            <EChartsGraph 
              ref="echartsGraphRef"
              :data="graphData"
              :width="vizWidth"
              :height="vizHeight"
              :layoutType="graphLayoutType"
              @node-click="onNodeClick"
              @edge-click="onEdgeClick"
              class="viz-graph"
            />
            
            <!-- 节点/边详情浮动面板 -->
            <div v-if="detailDrawerVisible && selectedElement" class="detail-panel">
              <div class="detail-header">
                <h3>{{ selectedElement.type === 'node' ? '节点详情' : '边详情' }}</h3>
                <el-button 
                  type="text" 
                  size="small" 
                  @click="closeDetailPanel"
                  class="close-btn"
                >
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
              
              <div class="detail-content">
                <div class="detail-section">
                  <h4>基础信息</h4>
                  <el-descriptions :column="1" border size="small" class="compact-descriptions">
                    <el-descriptions-item label="ID">{{ selectedElement.id }}</el-descriptions-item>
                    <el-descriptions-item label="标签" v-if="selectedElement.type === 'node'">{{ selectedElement.label }}</el-descriptions-item>
                    <el-descriptions-item label="类型" v-if="selectedElement.type === 'edge'">{{ selectedElement.label }}</el-descriptions-item>
                  </el-descriptions>
                </div>

                <div class="detail-section" v-if="selectedElement.properties && Object.keys(selectedElement.properties).length > 0">
                  <h4>属性</h4>
                  <div class="property-list">
                    <div
                      v-for="(value, key) in selectedElement.properties"
                      :key="key"
                      class="property-item"
                    >
                      <span class="property-key">{{ key }}:</span>
                      <span class="property-value">{{ value }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { storeToRefs } from 'pinia'
import { useGraphStore } from '@/stores/graph'
import { graphApi } from '@/api/graph'
import EChartsGraph from '@/components/graph/EChartsGraph.vue'
import { ElMessage } from 'element-plus'
import {
  VideoPlay,
  Refresh,
  Download,
  MagicStick,
  Delete,
  Close,
  ZoomIn,
  ZoomOut,
  ArrowDown
} from '@element-plus/icons-vue'

// 状态管理
const graphStore = useGraphStore()
const { selectedGraphName, currentQueryLanguage } = storeToRefs(graphStore)

// 响应式数据
const queryStatement = ref('MATCH (n)-[r]->(m) RETURN n, r, m LIMIT 50')
const queryLoading = ref(false)
const queryHistory = ref([])
const graphData = ref({
  nodes: [],
  edges: []
})

const vizContainer = ref(null)
const vizWidth = ref(800)
const vizHeight = ref(600)
const queryEditor = ref(null)
const highlightElement = ref(null)
const echartsGraphRef = ref(null)

const detailDrawerVisible = ref(false)
const selectedElement = ref(null)

// 缩放状态
const currentZoom = ref(1)

// 图表类型
const graphLayoutType = ref('force')
const graphLayoutOptions = [
  { label: '力导向图', value: 'force' },
  { label: '环形布局', value: 'circular' },
  { label: '无布局', value: 'none' }
]

// 当前布局类型的标签
const currentLayoutLabel = computed(() => {
  const current = graphLayoutOptions.find(opt => opt.value === graphLayoutType.value)
  return current ? current.label : graphLayoutOptions[0].label
})

// 进度相关
const queryProgress = ref(0)
const estimatedTime = ref(5)

// 生命周期
onMounted(() => {
  updateVizDimensions()
  window.addEventListener('resize', updateVizDimensions)
  loadQueryHistory()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateVizDimensions)
})

// 方法
const updateVizDimensions = () => {
  if (vizContainer.value) {
    const containerRect = vizContainer.value.getBoundingClientRect()
    vizWidth.value = containerRect.width
    vizHeight.value = containerRect.height
  }
}

const executeQuery = async () => {
  if (!selectedGraphName.value) {
    ElMessage.warning('请先选择图')
    return
  }

  if (!queryStatement.value.trim()) {
    ElMessage.warning('请输入查询语句')
    return
  }

  queryLoading.value = true
  queryProgress.value = 0
  estimatedTime.value = 5

  try {
    const progressInterval = setInterval(() => {
      queryProgress.value += 0.1
      if (queryProgress.value >= 1) {
        clearInterval(progressInterval)
        queryProgress.value = 1
      }
    }, 100)

    // 调用 API 执行查询
    const response = await graphApi.executeQuery(
      graphStore.selectedConnectionId,
      selectedGraphName.value,
      currentQueryLanguage.value,
      queryStatement.value
    )

    graphData.value = response.data

    saveToQueryHistory({
      graphName: selectedGraphName.value,
      language: currentQueryLanguage.value,
      statement: queryStatement.value,
      timestamp: Date.now()
    })

    ElMessage.success(`查询执行成功，返回 ${response.data.nodes.length} 个节点，${response.data.edges.length} 条边`)
  } catch (error) {
    console.error('查询执行失败:', error)
    ElMessage.error('查询执行失败: ' + (error.message || '未知错误'))
  } finally {
    queryLoading.value = false
    queryProgress.value = 0
    if (typeof progressInterval !== 'undefined') {
      clearInterval(progressInterval)
    }
  }
}

const saveToQueryHistory = (queryInfo) => {
  queryHistory.value.unshift(queryInfo)
  if (queryHistory.value.length > 10) {
    queryHistory.value = queryHistory.value.slice(0, 10)
  }
  localStorage.setItem('graphQueryHistoryEcharts', JSON.stringify(queryHistory.value))
}

const loadQueryHistory = () => {
  try {
    const saved = localStorage.getItem('graphQueryHistoryEcharts')
    if (saved) {
      queryHistory.value = JSON.parse(saved)
    }
  } catch (error) {
    console.error('加载查询历史失败:', error)
  }
}

const loadHistoryQuery = (historyItem) => {
  selectedGraphName.value = historyItem.graphName
  queryStatement.value = historyItem.statement
}

const formatQuery = () => {
  const formatted = queryStatement.value
    .replace(/\s+/g, ' ')
    .replace(/;\s*/g, ';\n')
    .replace(/\b(MATCH|RETURN|WHERE|WITH|ORDER BY|LIMIT)\b/g, '\n$1')
    .trim()
  queryStatement.value = formatted
  ElMessage.success('查询已格式化')
}

const clearQuery = () => {
  queryStatement.value = ''
}

const loadSampleData = () => {
  graphData.value = {
    nodes: [
      { id: 's1', label: 'Person', properties: { name: 'John', age: 32 } },
      { id: 's2', label: 'Person', properties: { name: 'Mary', age: 28 } },
      { id: 's3', label: 'Company', properties: { name: 'Acme Corp', industry: 'Manufacturing' } }
    ],
    edges: [
      { id: 'e1', source: 's1', target: 's2', label: 'KNOWS', properties: { since: '2018' } },
      { id: 'e2', source: 's1', target: 's3', label: 'WORKS_FOR', properties: { position: 'Manager' } }
    ]
  }
  ElMessage.success('已加载示例数据')
}

const onNodeClick = (node) => {
  selectedElement.value = {
    type: 'node',
    id: node.id,
    label: node.label,
    properties: node.properties
  }
  detailDrawerVisible.value = true
}

const onEdgeClick = (edge) => {
  selectedElement.value = {
    type: 'edge',
    id: edge.id,
    label: edge.label,
    properties: edge.properties,
    source: edge.source,
    target: edge.target
  }
  detailDrawerVisible.value = true
}

const closeDetailPanel = () => {
  detailDrawerVisible.value = false
  selectedElement.value = null
}

const highlightSyntax = () => {
  if (!highlightElement.value) return
  
  const text = queryStatement.value
  const keywords = ['MATCH', 'RETURN', 'WHERE', 'WITH', 'ORDER BY', 'LIMIT', 'CREATE', 'DELETE', 'SET', 'REMOVE', 'MERGE', 'UNWIND', 'CALL', 'UNION', 'AS', 'AND', 'OR', 'NOT', 'IN', 'IS', 'NULL', 'TRUE', 'FALSE']
  const functions = ['COUNT', 'SUM', 'AVG', 'MIN', 'MAX', 'COLLECT', 'DISTINCT', 'STARTS WITH', 'ENDS WITH', 'CONTAINS']
  
  let highlighted = text
  
  keywords.forEach(keyword => {
    const regex = new RegExp(`\\b${keyword}\\b`, 'gi')
    highlighted = highlighted.replace(regex, `<span class="keyword">${keyword}</span>`)
  })
  
  functions.forEach(func => {
    const regex = new RegExp(`\\b${func}\\b`, 'gi')
    highlighted = highlighted.replace(regex, `<span class="function">${func}</span>`)
  })
  
  highlighted = highlighted.replace(/'(.*?)'/g, '<span class="string">\'$1\'</span>')
  highlighted = highlighted.replace(/\b\d+\b/g, '<span class="number">$&</span>')
  highlighted = highlighted.replace(/\$[a-zA-Z_][a-zA-Z0-9_]*/g, '<span class="variable">$&</span>')
  highlighted = highlighted.replace(/\([a-zA-Z_][a-zA-Z0-9_]*\)/g, '<span class="node">$&</span>')
  highlighted = highlighted.replace(/\[[a-zA-Z_][a-zA-Z0-9_]*\]/g, '<span class="relationship">$&</span>')
  
  highlightElement.value.innerHTML = highlighted
}

const handleKeydown = (event) => {
  if (event.key === 'Tab') {
    event.preventDefault()
    const start = event.target.selectionStart
    const end = event.target.selectionEnd
    const value = queryStatement.value
    queryStatement.value = value.substring(0, start) + '  ' + value.substring(end)
    event.target.selectionStart = event.target.selectionEnd = start + 2
    nextTick(() => {
      highlightSyntax()
    })
  }
}

const resetView = () => {
  currentZoom.value = 1
  if (echartsGraphRef.value) {
    echartsGraphRef.value.resetView()
  }
  ElMessage.success('视图已重置')
}

const handleZoomIn = () => {
  if (currentZoom.value < 3) {
    currentZoom.value = parseFloat((currentZoom.value + 0.2).toFixed(1))
    if (echartsGraphRef.value) {
      echartsGraphRef.value.zoomIn(currentZoom.value)
    }
  }
}

const handleZoomOut = () => {
  if (currentZoom.value > 0.5) {
    currentZoom.value = parseFloat((currentZoom.value - 0.2).toFixed(1))
    if (echartsGraphRef.value) {
      echartsGraphRef.value.zoomOut(currentZoom.value)
    }
  }
}

const handleLayoutTypeChange = (newType) => {
  if (echartsGraphRef.value) {
    echartsGraphRef.value.setLayoutType(newType)
  }
  ElMessage.success(`已切换到 ${graphLayoutOptions.find(opt => opt.value === newType)?.label}`)
}

const exportVisualization = () => {
  ElMessage.info('导出功能开发中...')
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 24 * 60 * 60 * 1000) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleDateString()
}
</script>

<style scoped>
.graph-visualization {
  height: calc(100vh - 64px);
  overflow: hidden;
  display: flex;
}

.graph-visualization :deep(.el-container) {
  height: 100%;
  display: flex;
}

.graph-visualization :deep(.el-aside) {
  height: 100%;
  overflow-y: auto;
}

:deep(.el-main) {
  padding: 0;
  height: 100%;
}

.query-panel {
  background: #f8f9fa;
  border-right: 1px solid #e8e8e8;
  padding: 16px;
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.section {
  margin-bottom: 24px;
}

.section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.editor-section {
  flex: 1;
}

.query-editor-container {
  position: relative;
}

.code-editor-wrapper {
  position: relative;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.code-editor-wrapper:focus-within {
  border-color: #409EFF;
  outline: 0;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.code-editor {
  width: 100%;
  height: 200px;
  padding: 8px 12px;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  color: transparent;
  caret-color: #333;
  font-family: inherit;
  font-size: inherit;
  line-height: inherit;
  z-index: 2;
  position: relative;
}

.code-highlight {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 8px 12px;
  white-space: pre-wrap;
  word-wrap: break-word;
  pointer-events: none;
  z-index: 1;
}

.keyword {
  color: #d73a49;
  font-weight: bold;
}

.function {
  color: #6f42c1;
  font-weight: bold;
}

.string {
  color: #032f62;
}

.number {
  color: #005cc5;
}

.variable {
  color: #e36209;
  font-weight: bold;
}

.node {
  color: #22863a;
}

.relationship {
  color: #735c0f;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.history-list {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: hidden;
}

.history-item {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.history-item:hover {
  background-color: #f5f7fa;
}

.history-item:last-child {
  border-bottom: none;
}

.history-content {
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.4;
  word-break: break-word;
}

.history-time {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.visualization-area {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.viz-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  z-index: 10;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #f1f5f9;
}

.stats {
  font-size: 14px;
  color: #94a3b8;
  background: rgba(100, 116, 139, 0.2);
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.toolbar-right {
  display: flex;
  gap: 0;
}

.toolbar-right :deep(.el-button) {
  padding: 5px 8px;
}

.toolbar-right :deep(.el-icon--right) {
  margin-left: 4px;
}

.viz-canvas-container {
  flex: 1;
  padding: 0;
  overflow: hidden;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  position: relative;
}

.graph-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.viz-graph {
  width: 100%;
  height: 100%;
}

.detail-panel {
  position: absolute;
  top: 20px;
  right: 0;
  width: 220px;
  max-height: 90%;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.detail-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}

.close-btn {
  padding: 2px;
  color: #909399;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: #f56c6c;
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px 16px 16px;
}

.detail-section {
  margin-top: 16px;
}

.detail-section:first-child {
  margin-top: 0;
}

.detail-section h4 {
  margin: 0 0 6px 0;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.property-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.property-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 10px;
  background: #f8f9fa;
  border-radius: 3px;
  border-left: 3px solid #6366F1;
}

.property-key {
  font-weight: 500;
  color: #333;
  font-size: 12px;
}

.property-value {
  color: #666;
  max-width: 90px;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.compact-descriptions :deep(.el-descriptions__label) {
  font-size: 12px !important;
  padding: 6px 8px !important;
}

.compact-descriptions :deep(.el-descriptions__content) {
  font-size: 12px !important;
  padding: 6px 8px !important;
}

.compact-descriptions :deep(.el-descriptions__cell) {
  padding: 0 !important;
}

.compact-descriptions :deep(.el-descriptions__label.el-descriptions__cell.is-bordered-label) {
  padding: 6px 8px !important;
}

.compact-descriptions :deep(.el-descriptions__body .el-descriptions__table .el-descriptions__cell) {
  padding: 6px 8px !important;
}

@media (max-width: 1200px) {
  .query-panel {
    width: 280px;
  }
}

@media (max-width: 768px) {
  .query-panel {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
  }
  
  .graph-visualization {
    flex-direction: column;
  }
}
</style>
