<template>
  <div class="query-editor">
    <!-- 顶部工具栏 -->
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <el-select v-model="queryLanguage" placeholder="选择查询语言" size="small" style="width: 150px;">
          <el-option label="Cypher" value="CYPHER" />
          <el-option label="nGQL" value="NEBULA" />
          <el-option label="Gremlin" value="GREMLIN" />
        </el-select>
        <el-select 
          v-model="selectedTemplate" 
          placeholder="查询模板" 
          size="small" 
          style="width: 180px; margin-left: 10px;"
          clearable
          @change="loadTemplate"
        >
          <el-option
            v-for="template in queryTemplates"
            :key="template.id"
            :label="template.name"
            :value="template.id"
          />
        </el-select>
      </div>
      
      <div class="toolbar-right">
        <el-button size="small" @click="showHistory">
          <el-icon><clock /></el-icon> 历史记录
        </el-button>
        <el-button size="small" @click="formatQuery">
          <el-icon><magic-stick /></el-icon> 格式化
        </el-button>
        <el-button type="primary" size="small" @click="executeQuery" :loading="executing">
          <el-icon><video-play /></el-icon> 执行查询
        </el-button>
      </div>
    </div>
    
    <!-- 代码编辑器 -->
    <div class="editor-container">
      <div class="editor-wrapper">
        <div ref="editor" class="code-editor" contenteditable="true" @input="onEditorInput" @keydown="onEditorKeydown">
          {{ queryStatement }}
        </div>
        <div class="editor-placeholder" v-if="!queryStatement">
          请输入查询语句，例如：MATCH (n:Person) RETURN n LIMIT 10
        </div>
      </div>
      
      <!-- 查询建议面板 -->
      <div v-if="showSuggestions && suggestions.length > 0" class="suggestions-panel">
        <div 
          v-for="(suggestion, index) in suggestions" 
          :key="index"
          class="suggestion-item"
          :class="{ 'active': suggestionIndex === index }"
          @click="applySuggestion(suggestion)"
        >
          <span class="suggestion-type">{{ suggestion.type }}</span>
          <span class="suggestion-text">{{ suggestion.text }}</span>
          <span class="suggestion-desc">{{ suggestion.description }}</span>
        </div>
      </div>
    </div>
    
    <!-- 结果展示 -->
    <div class="result-container" v-if="queryResult">
      <div class="result-header">
        <h4>查询结果</h4>
        <div class="result-tabs">
          <el-button-group size="small">
            <el-button 
              :type="resultView === 'table' ? 'primary' : ''" 
              @click="resultView = 'table'"
            >
              <el-icon><grid /></el-icon> 表格
            </el-button>
            <el-button 
              :type="resultView === 'json' ? 'primary' : ''" 
              @click="resultView = 'json'"
            >
              <el-icon><document /></el-icon> JSON
            </el-button>
            <el-button 
              :type="resultView === 'graph' ? 'primary' : ''" 
              @click="resultView = 'graph'"
            >
              <el-icon><share /></el-icon> 图视图
            </el-button>
          </el-button-group>
        </div>
        <div class="result-actions">
          <el-button size="small" @click="exportResult">
            <el-icon><download /></el-icon> 导出
          </el-button>
        </div>
      </div>
      
      <!-- 表格视图 -->
      <div v-if="resultView === 'table'" class="table-view">
        <el-table :data="tableData" border stripe style="width: 100%" max-height="400">
          <el-table-column
            v-for="column in tableColumns"
            :key="column"
            :prop="column"
            :label="column"
            min-width="120"
          />
        </el-table>
      </div>
      
      <!-- JSON视图 -->
      <div v-if="resultView === 'json'" class="json-view">
        <pre class="json-content">{{ formattedJson }}</pre>
      </div>
      
      <!-- 图视图 -->
      <div v-if="resultView === 'graph'" class="graph-view">
        <D3Graph 
          :data="graphData" 
          :width="800" 
          :height="400"
          @export="handleGraphExport"
        />
      </div>
    </div>
    
    <!-- 历史记录对话框 -->
    <el-dialog v-model="historyDialogVisible" title="查询历史" width="600px">
      <el-table :data="queryHistory" border style="width: 100%">
        <el-table-column prop="time" label="执行时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.time) }}
          </template>
        </el-table-column>
        <el-table-column prop="language" label="语言" width="100" />
        <el-table-column prop="query" label="查询语句" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="useHistory(row)">
              使用
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, MagicStick, VideoPlay, Grid, Document, Share, Download } from '@element-plus/icons-vue'
import D3Graph from '../graph/D3Graph.vue'

// Props
const props = defineProps({
  connectionId: {
    type: Number,
    required: true
  },
  graphName: {
    type: String,
    required: true
  }
})

// Emits
const emit = defineEmits(['query-executed'])

// 状态
const queryLanguage = ref('CYPHER')
const queryStatement = ref('')
const executing = ref(false)
const queryResult = ref(null)
const resultView = ref('table')
const selectedTemplate = ref('')
const historyDialogVisible = ref(false)
const showSuggestions = ref(false)
const suggestions = ref([])
const suggestionIndex = ref(0)

// Refs
const editor = ref(null)

// 查询模板
const queryTemplates = ref([
  {
    id: '1',
    name: '查询所有节点',
    language: 'CYPHER',
    code: 'MATCH (n) RETURN n LIMIT 100'
  },
  {
    id: '2',
    name: '查询特定标签节点',
    language: 'CYPHER',
    code: 'MATCH (n:Person) RETURN n LIMIT 100'
  },
  {
    id: '3',
    name: '查询节点关系',
    language: 'CYPHER',
    code: 'MATCH (p:Person)-[r:KNOWS]->(o:Person) RETURN p, r, o LIMIT 100'
  },
  {
    id: '4',
    name: '最短路径查询',
    language: 'CYPHER',
    code: 'MATCH (start:Person {name: "Alice"}), (end:Person {name: "Bob"})\nMATCH path = shortestPath((start)-[*..5]-(end))\nRETURN path'
  },
  {
    id: '5',
    name: '聚合统计',
    language: 'CYPHER',
    code: 'MATCH (n:Person)\nRETURN n.name, count(*) as count\nORDER BY count DESC\nLIMIT 10'
  }
])

// 查询历史
const queryHistory = ref([])

// 表格数据
const tableData = computed(() => {
  if (!queryResult.value || !Array.isArray(queryResult.value)) return []
  
  return queryResult.value.map(row => {
    const obj = {}
    Object.keys(row).forEach(key => {
      obj[key] = formatValue(row[key])
    })
    return obj
  })
})

// 表格列
const tableColumns = computed(() => {
  if (!queryResult.value || !Array.isArray(queryResult.value) || queryResult.value.length === 0) return []
  return Object.keys(queryResult.value[0])
})

// 格式化JSON
const formattedJson = computed(() => {
  if (!queryResult.value) return ''
  return JSON.stringify(queryResult.value, null, 2)
})

// 图数据
const graphData = computed(() => {
  if (!queryResult.value || !Array.isArray(queryResult.value)) {
    return { nodes: [], edges: [] }
  }
  
  const nodes = []
  const edges = []
  const nodeMap = new Map()
  
  queryResult.value.forEach((row, index) => {
    // 处理节点
    Object.values(row).forEach(value => {
      if (typeof value === 'object' && value !== null) {
        if (value.id && !nodeMap.has(value.id)) {
          nodeMap.set(value.id, value)
          nodes.push({
            id: value.id,
            label: value.label || value.id,
            properties: value
          })
        }
      }
    })
  })
  
  return { nodes, edges }
})

// 加载模板
function loadTemplate(templateId) {
  const template = queryTemplates.value.find(t => t.id === templateId)
  if (template) {
    queryLanguage.value = template.language
    queryStatement.value = template.code
  }
}

// 格式化查询
function formatQuery() {
  if (!queryStatement.value.trim()) {
    ElMessage.warning('请输入查询语句')
    return
  }
  
  // 简单格式化：添加适当的换行和缩进
  let formatted = queryStatement.value
    .replace(/\b(MATCH|WHERE|RETURN|ORDER BY|LIMIT|SKIP|WITH|CREATE|SET|DELETE|MERGE|OPTIONAL)\b/gi, '\n$1')
    .replace(/\b(AND|OR|XOR)\b/gi, '\n  $1')
    .replace(/\n\s*\n/g, '\n')
    .trim()
  
  queryStatement.value = formatted
  ElMessage.success('格式化完成')
}

// 执行查询
async function executeQuery() {
  if (!queryStatement.value.trim()) {
    ElMessage.warning('请输入查询语句')
    return
  }
  
  executing.value = true
  
  try {
    // 调用API执行查询（这里需要集成实际的API）
    // const res = await dataApi.executeNativeQuery(props.connectionId, props.graphName, {
    //   queryLanguage: queryLanguage.value,
    //   queryStatement: queryStatement.value
    // })
    
    // 模拟结果
    queryResult.value = [
      { id: '1', name: 'Alice', age: 30, city: 'Beijing' },
      { id: '2', name: 'Bob', age: 25, city: 'Shanghai' },
      { id: '3', name: 'Charlie', age: 35, city: 'Guangzhou' }
    ]
    
    // 保存到历史
    saveToHistory()
    
    ElMessage.success('查询执行成功')
    emit('query-executed', queryResult.value)
  } catch (error) {
    ElMessage.error('查询失败: ' + error.message)
  } finally {
    executing.value = false
  }
}

// 保存到历史
function saveToHistory() {
  queryHistory.value.unshift({
    time: Date.now(),
    language: queryLanguage.value,
    query: queryStatement.value
  })
  
  // 限制历史记录数量
  if (queryHistory.value.length > 50) {
    queryHistory.value = queryHistory.value.slice(0, 50)
  }
}

// 显示历史
function showHistory() {
  historyDialogVisible.value = true
}

// 使用历史记录
function useHistory(historyItem) {
  queryLanguage.value = historyItem.language
  queryStatement.value = historyItem.query
  historyDialogVisible.value = false
}

// 格式化时间
function formatTime(timestamp) {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

// 格式化值
function formatValue(value) {
  if (value === null || value === undefined) return ''
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

// 导出结果
function exportResult() {
  if (!queryResult.value) return
  
  let content = ''
  let filename = ''
  let type = ''
  
  if (resultView.value === 'json') {
    content = formattedJson.value
    filename = 'query-result.json'
    type = 'application/json'
  } else {
    // CSV格式
    const headers = tableColumns.value.join(',')
    const rows = tableData.value.map(row => 
      tableColumns.value.map(col => `"${row[col]}"`).join(',')
    )
    content = [headers, ...rows].join('\n')
    filename = 'query-result.csv'
    type = 'text/csv'
  }
  
  const blob = new Blob([content], { type })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// 处理图导出
function handleGraphExport({ format }) {
  ElMessage.info(`导出图数据为 ${format}`)
}

// 编辑器输入
function onEditorInput(event) {
  queryStatement.value = event.target.innerText
}

// 编辑器按键
function onEditorKeydown(event) {
  if (event.key === 'Tab') {
    event.preventDefault()
    const selection = window.getSelection()
    const range = selection.getRangeAt(0)
    const textNode = document.createTextNode('  ')
    range.insertNode(textNode)
    range.setStartAfter(textNode)
    range.collapse(true)
    selection.removeAllRanges()
    selection.addRange(range)
  }
}

// 生命周期
onMounted(() => {
  // 加载保存的历史记录
  const saved = localStorage.getItem('queryHistory')
  if (saved) {
    try {
      queryHistory.value = JSON.parse(saved)
    } catch (e) {
      console.error('Failed to load query history:', e)
    }
  }
})

// 监听历史变化
queryHistory.value && watch(queryHistory, (newHistory) => {
  localStorage.setItem('queryHistory', JSON.stringify(newHistory))
}, { deep: true })
</script>

<style scoped>
.query-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: 1px solid #ebeef5;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  padding: 16px;
  background: #f5f7fa;
}

.editor-wrapper {
  position: relative;
  flex: 1;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.code-editor {
  width: 100%;
  height: 100%;
  min-height: 200px;
  padding: 16px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  outline: none;
  overflow: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.editor-placeholder {
  position: absolute;
  top: 16px;
  left: 16px;
  color: #c0c4cc;
  font-size: 14px;
  pointer-events: none;
}

.suggestions-panel {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
  margin-top: 4px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.suggestion-item:hover,
.suggestion-item.active {
  background-color: #f5f7fa;
}

.suggestion-type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  margin-right: 10px;
}

.suggestion-item:nth-child(odd) .suggestion-type {
  background-color: #ecf5ff;
  color: #409EFF;
}

.suggestion-item:nth-child(even) .suggestion-type {
  background-color: #f0f9ff;
  color: #67C23A;
}

.suggestion-text {
  flex: 1;
  font-weight: 500;
  margin-right: 10px;
}

.suggestion-desc {
  color: #909399;
  font-size: 12px;
}

.result-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-top: 1px solid #ebeef5;
  overflow: hidden;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

.result-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.table-view {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.json-view {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.json-content {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #606266;
  background: #f9f9f9;
  border-radius: 4px;
  padding: 16px;
}

.graph-view {
  flex: 1;
  overflow: hidden;
}
</style>
