<template>
  <div class="data-exporter">
    <el-card class="export-card">
      <template #header>
        <div class="card-header">
          <span>数据导出</span>
          <el-button size="small" @click="$emit('close')">
            <el-icon><close /></el-icon>
          </el-button>
        </div>
      </template>
      
      <!-- 导出类型选择 -->
      <div class="export-section">
        <h4>1. 选择导出类型</h4>
        <el-radio-group v-model="exportType" class="type-selector">
          <el-radio-button label="vertices">节点数据</el-radio-button>
          <el-radio-button label="edges">边数据</el-radio-button>
          <el-radio-button label="graph">全图数据</el-radio-button>
          <el-radio-button label="visual">可视化视图</el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 导出格式选择 -->
      <div class="export-section">
        <h4>2. 选择导出格式</h4>
        <el-radio-group v-model="exportFormat" class="format-selector">
          <el-radio-button label="csv" :disabled="exportType === 'visual'">CSV</el-radio-button>
          <el-radio-button label="json" :disabled="exportType === 'visual'">JSON</el-radio-button>
          <el-radio-button label="gexf" :disabled="exportType === 'visual'">GEXF (Gephi)</el-radio-button>
          <el-radio-button label="graphml" :disabled="exportType === 'visual'">GraphML</el-radio-button>
          <el-radio-button label="png" :disabled="exportType !== 'visual'">PNG图片</el-radio-button>
          <el-radio-button label="svg" :disabled="exportType !== 'visual'">SVG矢量图</el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 筛选条件 -->
      <div class="export-section" v-if="exportType !== 'visual'">
        <h4>3. 设置筛选条件（可选）</h4>
        <el-form :model="filterForm" label-width="100px" size="small">
          <el-form-item label="标签过滤">
            <el-select 
              v-model="filterForm.labels" 
              multiple 
              placeholder="选择要导出的标签"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="label in availableLabels"
                :key="label"
                :label="label"
                :value="label"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="属性选择">
            <el-select 
              v-model="filterForm.properties" 
              multiple 
              placeholder="选择要导出的属性"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="prop in availableProperties"
                :key="prop"
                :label="prop"
                :value="prop"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="数据量限制">
            <el-input-number 
              v-model="filterForm.limit" 
              :min="1" 
              :max="1000000"
              placeholder="不限制"
              style="width: 200px"
            />
            <span style="margin-left: 10px; color: #909399;">不填写则导出全部数据</span>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 导出选项 -->
      <div class="export-section">
        <h4>4. 导出选项</h4>
        <el-checkbox-group v-model="exportOptions">
          <el-checkbox label="includeHeader">包含表头（CSV格式）</el-checkbox>
          <el-checkbox label="includeMetadata">包含元数据</el-checkbox>
          <el-checkbox label="compress">压缩为ZIP</el-checkbox>
        </el-checkbox-group>
      </div>
      
      <!-- 预览信息 -->
      <div class="export-section preview-section">
        <h4>导出预览</h4>
        <div class="preview-info">
          <div class="info-item">
            <span class="label">导出类型:</span>
            <span class="value">{{ exportTypeText }}</span>
          </div>
          <div class="info-item">
            <span class="label">导出格式:</span>
            <span class="value">{{ exportFormatText }}</span>
          </div>
          <div class="info-item">
            <span class="label">预估记录数:</span>
            <span class="value">{{ estimatedRecords }}</span>
          </div>
          <div class="info-item">
            <span class="label">预估文件大小:</span>
            <span class="value">{{ estimatedFileSize }}</span>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="export-actions">
        <el-button @click="previewData">
          <el-icon><view /></el-icon> 预览数据
        </el-button>
        <el-button type="primary" @click="exportData" :loading="exporting">
          <el-icon><download /></el-icon> 立即导出
        </el-button>
      </div>
    </el-card>
    
    <!-- 预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="数据预览" width="800px">
      <el-table :data="previewData" border stripe max-height="400">
        <el-table-column
          v-for="column in previewColumns"
          :key="column"
          :prop="column"
          :label="column"
          min-width="120"
        />
      </el-table>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, View, Download } from '@element-plus/icons-vue'

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
const emit = defineEmits(['close', 'export'])

// 状态
const exportType = ref('vertices')
const exportFormat = ref('csv')
const exporting = ref(false)
const previewDialogVisible = ref(false)
const previewData = ref([])

// 筛选表单
const filterForm = ref({
  labels: [],
  properties: [],
  limit: null
})

// 导出选项
const exportOptions = ref(['includeHeader', 'includeMetadata'])

// 可用标签（模拟数据）
const availableLabels = ref(['Person', 'Company', 'Product', 'Order'])

// 可用属性（模拟数据）
const availableProperties = ref(['name', 'age', 'city', 'created_at', 'updated_at'])

// 导出类型文本
const exportTypeText = computed(() => {
  const map = {
    vertices: '节点数据',
    edges: '边数据',
    graph: '全图数据',
    visual: '可视化视图'
  }
  return map[exportType.value]
})

// 导出格式文本
const exportFormatText = computed(() => {
  const map = {
    csv: 'CSV (逗号分隔值)',
    json: 'JSON (JavaScript对象表示法)',
    gexf: 'GEXF (Gephi格式)',
    graphml: 'GraphML (图建模语言)',
    png: 'PNG (位图图片)',
    svg: 'SVG (可缩放矢量图)'
  }
  return map[exportFormat.value]
})

// 预估记录数
const estimatedRecords = computed(() => {
  // 模拟计算
  const baseMap = {
    vertices: 1250,
    edges: 3200,
    graph: 4450,
    visual: 1
  }
  return baseMap[exportType.value].toLocaleString()
})

// 预估文件大小
const estimatedFileSize = computed(() => {
  // 模拟计算
  const sizeMap = {
    csv: '2.5 MB',
    json: '3.8 MB',
    gexf: '4.2 MB',
    graphml: '3.5 MB',
    png: '1.8 MB',
    svg: '850 KB'
  }
  return sizeMap[exportFormat.value]
})

// 预览列
const previewColumns = computed(() => {
  if (previewData.value.length === 0) return []
  return Object.keys(previewData.value[0])
})

// 预览数据
function previewData() {
  // 模拟预览数据
  previewData.value = [
    { uid: 'node_001', label: 'Person', name: 'Alice', age: 30, city: 'Beijing' },
    { uid: 'node_002', label: 'Person', name: 'Bob', age: 25, city: 'Shanghai' },
    { uid: 'node_003', label: 'Company', name: 'Tech Corp', founded: 2010 }
  ]
  previewDialogVisible.value = true
}

// 导出数据
async function exportData() {
  exporting.value = true
  
  try {
    // 模拟导出延迟
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 生成文件内容
    let content = ''
    let filename = ''
    let type = ''
    
    switch (exportFormat.value) {
      case 'csv':
        content = generateCSV()
        filename = `${exportType.value}-export.csv`
        type = 'text/csv'
        break
      case 'json':
        content = generateJSON()
        filename = `${exportType.value}-export.json`
        type = 'application/json'
        break
      case 'gexf':
        content = generateGEXF()
        filename = `${exportType.value}-export.gexf`
        type = 'application/xml'
        break
      case 'graphml':
        content = generateGraphML()
        filename = `${exportType.value}-export.graphml`
        type = 'application/xml'
        break
      case 'png':
      case 'svg':
        ElMessage.info('可视化导出功能已触发')
        exporting.value = false
        return
    }
    
    // 下载文件
    downloadFile(content, filename, type)
    
    ElMessage.success('导出成功')
    emit('export', { type: exportType.value, format: exportFormat.value })
  } catch (error) {
    ElMessage.error('导出失败: ' + error.message)
  } finally {
    exporting.value = false
  }
}

// 生成CSV
function generateCSV() {
  const headers = exportOptions.value.includes('includeHeader') 
    ? ['UID', 'Label', 'Name', 'Properties'] 
    : []
  
  const rows = [
    ...headers,
    'node_001,Person,Alice,"{""age"":30,""city"":""Beijing""}"',
    'node_002,Person,Bob,"{""age"":25,""city"":""Shanghai""}"',
    'node_003,Company,Tech Corp,"{""founded"":2010}"'
  ]
  
  return rows.join('\n')
}

// 生成JSON
function generateJSON() {
  const data = {
    exportType: exportType.value,
    exportFormat: exportFormat.value,
    exportTime: new Date().toISOString(),
    connectionId: props.connectionId,
    graphName: props.graphName,
    includeMetadata: exportOptions.value.includes('includeMetadata'),
    data: [
      {
        uid: 'node_001',
        label: 'Person',
        properties: { name: 'Alice', age: 30, city: 'Beijing' }
      },
      {
        uid: 'node_002',
        label: 'Person',
        properties: { name: 'Bob', age: 25, city: 'Shanghai' }
      },
      {
        uid: 'node_003',
        label: 'Company',
        properties: { name: 'Tech Corp', founded: 2010 }
      }
    ]
  }
  
  return JSON.stringify(data, null, 2)
}

// 生成GEXF
function generateGEXF() {
  return `<?xml version="1.0" encoding="UTF-8"?>
<gexf xmlns="http://www.gexf.net/1.2draft" version="1.2">
  <graph mode="static" defaultedgetype="directed">
    <nodes>
      <node id="node_001" label="Person">
        <attvalues>
          <attvalue for="name" value="Alice" />
          <attvalue for="age" value="30" />
        </attvalues>
      </node>
      <node id="node_002" label="Person">
        <attvalues>
          <attvalue for="name" value="Bob" />
          <attvalue for="age" value="25" />
        </attvalues>
      </node>
    </nodes>
    <edges>
      <edge id="edge_001" source="node_001" target="node_002" label="KNOWS" />
    </edges>
  </graph>
</gexf>`
}

// 生成GraphML
function generateGraphML() {
  return `<?xml version="1.0" encoding="UTF-8"?>
<graphml xmlns="http://graphml.graphdrawing.org/xmlns"  
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns  
     http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
  <key id="d1" for="node" attr.name="name" attr.type="string"/>
  <key id="d2" for="node" attr.name="age" attr.type="int"/>
  <graph id="G" edgedefault="directed">
    <node id="node_001">
      <data key="d1">Alice</data>
      <data key="d2">30</data>
    </node>
    <node id="node_002">
      <data key="d1">Bob</data>
      <data key="d2">25</data>
    </node>
    <edge id="e1" source="node_001" target="node_002" label="KNOWS"/>
  </graph>
</graphml>`
}

// 下载文件
function downloadFile(content, filename, type) {
  const blob = new Blob([content], { type })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.data-exporter {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.export-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.export-section {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.export-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.export-section h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.type-selector,
.format-selector {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.preview-section {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
}

.preview-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.info-item .value {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}

.export-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
</style>
