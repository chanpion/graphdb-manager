<template>
  <div class="graph-visualization">
    <el-container>
      <!-- 左侧查询面板 -->
      <el-aside width="300px" class="query-panel">
        <div class="panel-header">
          <h3>图查询</h3>
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
            
            <!-- 自动补全弹出框 -->
            <div v-if="showAutocomplete" class="autocomplete-popup">
              <div 
                v-for="(item, index) in autocompleteItems" 
                :key="index"
                :class="['autocomplete-item', { active: index === autocompleteIndex }]"
                @click="applyAutocomplete(item)"
              >
                {{ item }}
              </div>
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
<!--        <div class="viz-toolbar">-->
<!--          <div class="toolbar-left">-->
<!--            <h2>图分析</h2>-->
<!--            <span class="stats" v-if="graphData.nodes.length > 0">-->
<!--              节点: {{ graphData.nodes.length }} | 边: {{ graphData.edges.length }}-->
<!--            </span>-->
<!--          </div>-->
<!--          <div class="toolbar-right">-->
<!--            <el-button-group size="small">-->
<!--              <el-button @click="zoomIn">-->
<!--                <el-icon><ZoomIn /></el-icon>-->
<!--              </el-button>-->
<!--              <el-button @click="zoomOut">-->
<!--                <el-icon><ZoomOut /></el-icon>-->
<!--              </el-button>-->
<!--              <el-button @click="resetView">-->
<!--                <el-icon><Refresh /></el-icon>-->
<!--                重置视图-->
<!--              </el-button>-->
<!--              <el-button @click="exportVisualization">-->
<!--                <el-icon><Download /></el-icon>-->
<!--                导出图片-->
<!--              </el-button>-->
<!--              <el-button @click="toggleFullscreen">-->
<!--                <el-icon><FullScreen /></el-icon>-->
<!--                全屏-->
<!--              </el-button>-->
<!--            </el-button-group>-->
<!--          </div>-->
<!--        </div>-->

        <!-- 可视化画布 -->
        <div class="viz-canvas-container" ref="vizContainer">
          <div class="graph-container">
            <D3Graph 
              ref="d3GraphRef"
              :data="graphData"
              :width="vizWidth"
              :height="vizHeight"
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
import D3Graph from '@/components/graph/D3Graph.vue'
import { ElMessage } from 'element-plus'
import {
  VideoPlay,
  Refresh,
  Download,
  FullScreen,
  MagicStick,
  Delete,
  Close,
  Expand,
  ZoomIn,
  ZoomOut
} from '@element-plus/icons-vue'

// 状态管理
const graphStore = useGraphStore()
const { selectedGraphName, currentQueryLanguage } = storeToRefs(graphStore)

// 响应式数据
const queryStatement = ref('MATCH (n)-[r]->(m) RETURN n, r, m LIMIT 50')
const queryLoading = ref(false)
const queryHistory = ref([])
const selectedTemplate = ref('')
const graphData = ref({
  nodes: [],
  edges: []
})

const vizContainer = ref(null)
const vizWidth = ref(800)
const vizHeight = ref(600)
const queryEditor = ref(null)
const highlightElement = ref(null)
const d3GraphRef = ref(null)

const detailDrawerVisible = ref(false)
const selectedElement = ref(null)
const statsPanelVisible = ref(true)

// 自动补全相关
const showAutocomplete = ref(false)
const autocompleteItems = ref([])
const autocompleteIndex = ref(0)

// 进度相关
const queryProgress = ref(0)
const estimatedTime = ref(0)

// 计算图统计信息
const graphStats = computed(() => {
  const nodes = graphData.value.nodes
  const edges = graphData.value.edges
  
  // 节点类型统计
  const nodeTypeCount = {}
  nodes.forEach(node => {
    const label = node.label || 'Unknown'
    nodeTypeCount[label] = (nodeTypeCount[label] || 0) + 1
  })
  
  // 边类型统计
  const edgeTypeCount = {}
  edges.forEach(edge => {
    const label = edge.label || 'Unknown'
    edgeTypeCount[label] = (edgeTypeCount[label] || 0) + 1
  })
  
  // 图密度计算
  const n = nodes.length
  const m = edges.length
  const density = n > 0 ? (2 * m) / (n * (n - 1)) : 0
  
  // 平均度数计算
  const degreeSum = nodes.reduce((sum, node) => {
    const degree = edges.filter(edge => 
      edge.source.id === node.id || edge.target.id === node.id
    ).length
    return sum + degree
  }, 0)
  const avgDegree = n > 0 ? degreeSum / n : 0
  
  return {
    nodes: n,
    edges: m,
    density,
    avgDegree,
    nodeTypes: Object.entries(nodeTypeCount).map(([label, count]) => ({
      label,
      count,
      percentage: ((count / n) * 100).toFixed(1)
    })),
    edgeTypes: Object.entries(edgeTypeCount).map(([label, count]) => ({
      label,
      count,
      percentage: ((count / m) * 100).toFixed(1)
    }))
  }
})

// 查询模板定义
const queryTemplates = {
  basic_nodes: 'MATCH (n) RETURN n LIMIT 50',
  basic_edges: 'MATCH ()-[r]->() RETURN r LIMIT 50',
  path_query: 'MATCH p=(a)-[r*1..3]-(b) RETURN p LIMIT 20',
  neighbors_query: 'MATCH (n)-[r]-(m) WHERE n.id = $nodeId RETURN n, r, m',
  property_filter: 'MATCH (n) WHERE n.property = $value RETURN n LIMIT 50',
  aggregation_query: 'MATCH (n) RETURN n.label AS type, COUNT(n) AS count ORDER BY count DESC'
}



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
  estimatedTime.value = 5 // 预估5秒完成

  try {
    // 模拟进度更新
    const progressInterval = setInterval(() => {
      queryProgress.value += 0.1
      if (queryProgress.value >= 1) {
        clearInterval(progressInterval)
        queryProgress.value = 1
      }
    }, 100)

    // TODO: 调用真实API执行查询
    // const response = await graphApi.executeNativeQuery(
    //   graphStore.selectedGraphName,
    //   graphStore.currentQueryLanguage,
    //   queryStatement.value
    // )
    
    // 模拟API响应延迟
    await new Promise(resolve => setTimeout(resolve, 5000))
    
    // 模拟数据 - 后续任务会替换为真实数据
    const mockResponse = {
      nodes: [
        { id: 'node1', label: 'Person', properties: { name: 'Alice', age: 30, city: 'New York' } },
        { id: 'node2', label: 'Person', properties: { name: 'Bob', age: 35, city: 'San Francisco' } },
        { id: 'node3', label: 'Company', properties: { name: 'TechCorp', industry: 'Technology', employees: 1000 } },
        { id: 'node4', label: 'Person', properties: { name: 'Charlie', age: 28, city: 'Boston' } },
        { id: 'node5', label: 'Company', properties: { name: 'DataInc', industry: 'Data Analytics', employees: 500 } }
      ],
      edges: [
        { id: 'edge1', source: 'node1', target: 'node2', label: 'KNOWS', properties: { since: '2020-01-01' } },
        { id: 'edge2', source: 'node1', target: 'node3', label: 'WORKS_FOR', properties: { position: 'Engineer', startDate: '2019-06-01' } },
        { id: 'edge3', source: 'node2', target: 'node4', label: 'KNOWS', properties: { since: '2021-03-15' } },
        { id: 'edge4', source: 'node4', target: 'node5', label: 'WORKS_FOR', properties: { position: 'Data Scientist', startDate: '2022-01-10' } }
      ]
    }

    graphData.value = mockResponse

    // 保存到查询历史
    saveToQueryHistory({
      graphName: selectedGraphName.value,
      language: currentQueryLanguage.value,
      statement: queryStatement.value,
      timestamp: Date.now()
    })

    ElMessage.success('查询执行成功')
  } catch (error) {
    console.error('查询执行失败:', error)
    ElMessage.error('查询执行失败: ' + error.message)
  } finally {
    queryLoading.value = false
    queryProgress.value = 0
  }
}

const saveToQueryHistory = (queryInfo) => {
  queryHistory.value.unshift(queryInfo)
  // 保持最多10条历史记录
  if (queryHistory.value.length > 10) {
    queryHistory.value = queryHistory.value.slice(0, 10)
  }
  localStorage.setItem('graphQueryHistory', JSON.stringify(queryHistory.value))
}

const loadQueryHistory = () => {
  try {
    const saved = localStorage.getItem('graphQueryHistory')
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
  // 简单的格式化逻辑
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

const applyTemplate = (templateKey) => {
  if (templateKey === 'custom') {
    // 自定义模板，不清空当前查询
    return
  }
  
  if (queryTemplates[templateKey]) {
    queryStatement.value = queryTemplates[templateKey]
    ElMessage.success(`已应用 ${templateKey} 模板`)
  }
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
  
  // 高亮关键字
  keywords.forEach(keyword => {
    const regex = new RegExp(`\\b${keyword}\\b`, 'gi')
    highlighted = highlighted.replace(regex, `<span class="keyword">${keyword}</span>`)
  })
  
  // 高亮函数
  functions.forEach(func => {
    const regex = new RegExp(`\\b${func}\\b`, 'gi')
    highlighted = highlighted.replace(regex, `<span class="function">${func}</span>`)
  })
  
  // 高亮字符串
  highlighted = highlighted.replace(/'(.*?)'/g, '<span class="string">\'$1\'</span>')
  
  // 高亮数字
  highlighted = highlighted.replace(/\b\d+\b/g, '<span class="number">$&</span>')
  
  // 高亮变量和节点标识
  highlighted = highlighted.replace(/\$[a-zA-Z_][a-zA-Z0-9_]*/g, '<span class="variable">$&</span>')
  highlighted = highlighted.replace(/\([a-zA-Z_][a-zA-Z0-9_]*\)/g, '<span class="node">$&</span>')
  highlighted = highlighted.replace(/\[[a-zA-Z_][a-zA-Z0-9_]*\]/g, '<span class="relationship">$&</span>')
  
  highlightElement.value.innerHTML = highlighted
}

const handleKeydown = (event) => {
  // Tab键处理
  if (event.key === 'Tab') {
    event.preventDefault()
    if (showAutocomplete.value) {
      // 如果有自动补全，应用当前选中的项
      applyAutocomplete(autocompleteItems.value[autocompleteIndex.value])
      return
    }
    
    const start = event.target.selectionStart
    const end = event.target.selectionEnd
    const value = queryStatement.value
    
    queryStatement.value = value.substring(0, start) + '  ' + value.substring(end)
    event.target.selectionStart = event.target.selectionEnd = start + 2
    
    // 触发语法高亮更新
    nextTick(() => {
      highlightSyntax()
    })
  }
  
  // 自动补全导航
  if (showAutocomplete.value) {
    if (event.key === 'ArrowUp') {
      event.preventDefault()
      autocompleteIndex.value = Math.max(0, autocompleteIndex.value - 1)
    } else if (event.key === 'ArrowDown') {
      event.preventDefault()
      autocompleteIndex.value = Math.min(autocompleteItems.value.length - 1, autocompleteIndex.value + 1)
    } else if (event.key === 'Enter') {
      event.preventDefault()
      applyAutocomplete(autocompleteItems.value[autocompleteIndex.value])
    } else if (event.key === 'Escape') {
      event.preventDefault()
      showAutocomplete.value = false
    }
  }
}

const applyAutocomplete = (item) => {
  if (!queryEditor.value) return
  
  const start = queryEditor.value.selectionStart
  const value = queryStatement.value
  
  // 找到当前单词的起始位置
  let wordStart = start - 1
  while (wordStart >= 0 && /[a-zA-Z_]/.test(value[wordStart])) {
    wordStart--
  }
  wordStart = Math.max(0, wordStart + 1)
  
  // 替换单词
  queryStatement.value = value.substring(0, wordStart) + item + value.substring(start)
  
  // 设置光标位置
  nextTick(() => {
    queryEditor.value.selectionStart = queryEditor.value.selectionEnd = wordStart + item.length
    queryEditor.value.focus()
    showAutocomplete.value = false
    highlightSyntax()
  })
}

// 自动补全数据
const autocompleteKeywords = [
  'MATCH', 'RETURN', 'WHERE', 'WITH', 'ORDER BY', 'LIMIT', 'CREATE', 'DELETE', 'SET', 'REMOVE', 
  'MERGE', 'UNWIND', 'CALL', 'UNION', 'AS', 'AND', 'OR', 'NOT', 'IN', 'IS', 'NULL', 'TRUE', 'FALSE'
]

const autocompleteFunctions = [
  'COUNT', 'SUM', 'AVG', 'MIN', 'MAX', 'COLLECT', 'DISTINCT', 'STARTS WITH', 'ENDS WITH', 'CONTAINS'
]

const autocompletePatterns = [
  '()', '[]', '()-[]->()', '()-[]-()', '()<-[]-()'
]

const resetView = () => {
  if (d3GraphRef.value) {
    d3GraphRef.value.resetView()
  }
  ElMessage.success('视图已重置')
}

const zoomIn = () => {
  if (d3GraphRef.value) {
    d3GraphRef.value.zoomIn()
  }
}

const zoomOut = () => {
  if (d3GraphRef.value) {
    d3GraphRef.value.zoomOut()
  }
}

const exportVisualization = () => {
  // TODO: 导出可视化图片逻辑
  ElMessage.info('导出功能开发中...')
}

const toggleFullscreen = () => {
  // TODO: 全屏切换逻辑
  ElMessage.info('全屏功能开发中...')
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  // 如果是今天内，显示时间
  if (diff < 24 * 60 * 60 * 1000) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  // 否则显示日期
  return date.toLocaleDateString()
}
</script>

<style scoped>
.graph-visualization {
  height: calc(100vh - 64px); /* 减去顶部导航栏高度 */
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

.selection-group {
  margin-bottom: 8px;
}

.editor-section {
  flex: 1;
}

.query-editor-container {
  position: relative;
}

/* 代码编辑器样式 */
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

/* 语法高亮样式 */
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

.comment {
  color: #6a737d;
  font-style: italic;
}

/* 自动补全样式 */
.autocomplete-popup {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
}

.autocomplete-item {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
}

.autocomplete-item:hover,
.autocomplete-item.active {
  background: #f5f7fa;
}

.autocomplete-item:last-child {
  border-bottom: none;
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
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
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
  color: #1f2d3d;
}

.stats {
  font-size: 14px;
  color: #606266;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
}

.toolbar-right {
  display: flex;
  gap: 8px;
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

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  border-radius: 8px;
}

.empty-actions {
  text-align: center;
  margin-top: 16px;
}

.empty-actions p {
  color: #94a3b8;
  margin-bottom: 16px;
  font-size: 14px;
}

.detail-content {
  padding: 16px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.detail-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
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

/* 详情浮动面板样式 */
.detail-panel {
  position: absolute;
  top: 20px;
  right: 0;
  width: 180px;
  max-height: 90%;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 详情浮动面板字体调整 */
.detail-panel h3 {
  font-size: 13px;
}

.detail-panel h4 {
  font-size: 12px;
}

.detail-panel .property-key {
  font-size: 11px;
}

.detail-panel .property-value {
  font-size: 11px;
}

.detail-panel .compact-descriptions :deep(.el-descriptions__label),
.detail-panel .compact-descriptions :deep(.el-descriptions__content) {
  font-size: 11px !important;
  padding: 4px 6px !important;
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

/* 紧凑型描述列表样式 */
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


/* 统计信息面板样式 */
.stats-panel {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 280px;
  max-height: 80%;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stats-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.toggle-btn {
  padding: 4px;
  color: rgba(255, 255, 255, 0.8);
  transition: color 0.2s ease;
}

.toggle-btn:hover {
  color: white;
}

.stats-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.stats-section {
  margin-bottom: 24px;
}

.stats-section:last-child {
  margin-bottom: 0;
}

.stats-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
  border-bottom: 1px solid #e8e8e8;
  padding-bottom: 4px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-size: 13px;
  color: #606266;
}

.stat-value {
  font-size: 13px;
  font-weight: 600;
  color: #409EFF;
}

/* 响应式设计 */
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