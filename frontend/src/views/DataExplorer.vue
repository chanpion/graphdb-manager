<template>
  <div class="data-explorer">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="220px">
        <div class="sidebar-header">
          <h3>图数据</h3>
          <el-button type="primary" size="small" @click="refreshData">刷新</el-button>
        </div>

        <div class="sidebar-content">
          <!-- 节点类型 -->
          <div class="section">
            <div class="section-header">
              <h4>节点类型</h4>
            </div>
            <div class="label-tags-container">
              <div
                v-for="label in vertexLabels"
                :key="label.name"
                class="label-tag"
                :class="{ 'active': selectedVertexLabel === label.name }"
                @click="selectVertexLabel(label.name)"
              >
                <span class="label-name">{{ label.name }}</span>
              </div>
            </div>
          </div>
          
          <!-- 边类型 -->
          <div class="section">
            <div class="section-header">
              <h4>边类型</h4>
            </div>
            <div class="label-tags-container">
              <div
                v-for="label in edgeLabels"
                :key="label.name"
                class="label-tag edge-tag"
                :class="{ 'active': selectedEdgeLabel === label.name }"
                @click="selectEdgeLabel(label.name)"
              >
                <span class="label-name">{{ label.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-main>
        <!-- 数据展示区 -->
        <div class="data-content">
          <!-- 顶部操作栏 -->
          <div class="data-header">
            <div class="header-left">
              <h3>{{ dataType === 'vertices' ? '节点数据' : '边数据' }}</h3>
              <span class="data-count">共 {{ dataType === 'vertices' ? vertexTotal : edgeTotal }} 条</span>
            </div>
            <div class="header-actions">
              <el-input
                v-model="searchText"
                :placeholder="dataType === 'vertices' ? '搜索节点...' : '搜索边...'"
                size="small"
                style="width: 200px; margin-right: 10px;"
                clearable
              />
              <el-button type="primary" size="small" @click="loadData">
                <el-icon><Refresh /></el-icon> 刷新
              </el-button>
              <el-button type="success" size="small" @click="exportData">
                <el-icon><Download /></el-icon> 导出
              </el-button>
              <el-button type="warning" size="small" @click="showImportDialog(dataType === 'vertices' ? 'vertex' : 'edge')">
                <el-icon><Upload /></el-icon> 导入
              </el-button>
              <el-button type="primary" size="small" @click="showCreateDialog">
                <el-icon><Plus /></el-icon> {{ dataType === 'vertices' ? '创建节点' : '创建边' }}
              </el-button>
            </div>
          </div>
          
          <!-- 数据表格 -->
          <el-table
            v-if="dataType === 'vertices'"
            :data="filteredVertices"
            stripe
            border
            height="calc(100vh - 220px)"
            v-loading="vertexLoading"
          >
            <el-table-column prop="uid" label="UID" width="150" />
            <el-table-column prop="label" label="标签" width="100" />
            <el-table-column prop="properties" label="属性" min-width="200">
              <template #default="{ row }">
                <div class="properties-display">
                  <span v-for="(value, key) in row.properties" :key="key" class="property-item">
                    <span class="property-key">{{ key }}:</span>
                    <span class="property-value">{{ value }}</span>
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <div class="operation-buttons">
                  <el-button type="primary" size="small" @click="showEditVertexDialog(row)" title="编辑">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button type="danger" size="small" @click="deleteVertex(row.uid)" title="删除">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <el-table
            v-if="dataType === 'edges'"
            :data="filteredEdges"
            stripe
            border
            height="calc(100vh - 220px)"
            v-loading="edgeLoading"
          >
            <el-table-column prop="uid" label="UID" width="150" />
            <el-table-column prop="label" label="标签" width="100" />
            <el-table-column prop="sourceUid" label="源节点" width="150" />
            <el-table-column prop="targetUid" label="目标节点" width="150" />
            <el-table-column prop="properties" label="属性" min-width="200">
              <template #default="{ row }">
                <div class="properties-display">
                  <span v-for="(value, key) in row.properties" :key="key" class="property-item">
                    <span class="property-key">{{ key }}:</span>
                    <span class="property-value">{{ value }}</span>
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <div class="operation-buttons">
                  <el-button type="primary" size="small" @click="showEditEdgeDialog(row)" title="编辑">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button type="danger" size="small" @click="deleteEdge(row.uid)" title="删除">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-if="dataType === 'vertices'"
              v-model:current-page="vertexPageNum"
              v-model:page-size="vertexPageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="vertexTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleVertexSizeChange"
              @current-change="handleVertexPageChange"
            />
            <el-pagination
              v-if="dataType === 'edges'"
              v-model:current-page="edgePageNum"
              v-model:page-size="edgePageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="edgeTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleEdgeSizeChange"
              @current-change="handleEdgePageChange"
            />
          </div>
        </div>
      </el-main>
    </el-container>
    
    <!-- 创建/编辑节点对话框 -->
    <el-dialog
      v-model="vertexDialogVisible"
      :title="vertexDialogTitle"
      width="600px"
    >
      <el-form :model="vertexForm" label-width="100px">
        <el-form-item label="标签" required>
          <el-select v-model="vertexForm.label" placeholder="选择节点标签" style="width: 100%">
            <el-option
              v-for="label in vertexLabels"
              :key="label.name"
              :label="label.name"
              :value="label.name"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="属性">
          <div class="properties-editor">
            <div v-for="(value, key, index) in vertexForm.properties" :key="index" class="property-row">
              <el-input v-model="propertyKeys[index]" placeholder="属性名" style="width: 150px; margin-right: 10px;" />
              <el-input v-model="propertyValues[index]" placeholder="属性值" style="width: 150px; margin-right: 10px;" />
              <el-button type="danger" size="small" @click="removeProperty(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button type="primary" size="small" @click="addProperty">
              <el-icon><Plus /></el-icon> 添加属性
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="vertexDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveVertex">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 创建/编辑边对话框 -->
    <el-dialog
      v-model="edgeDialogVisible"
      :title="edgeDialogTitle"
      width="600px"
    >
      <el-form :model="edgeForm" label-width="100px">
        <el-form-item label="标签" required>
          <el-select v-model="edgeForm.label" placeholder="选择边标签" style="width: 100%">
            <el-option
              v-for="label in edgeLabels"
              :key="label.name"
              :label="label.name"
              :value="label.name"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="源节点UID" required>
          <el-input v-model="edgeForm.sourceUid" placeholder="输入源节点UID" />
        </el-form-item>
        
        <el-form-item label="目标节点UID" required>
          <el-input v-model="edgeForm.targetUid" placeholder="输入目标节点UID" />
        </el-form-item>
        
        <el-form-item label="属性">
          <div class="properties-editor">
            <div v-for="(value, key, index) in edgeForm.properties" :key="index" class="property-row">
              <el-input v-model="edgePropertyKeys[index]" placeholder="属性名" style="width: 150px; margin-right: 10px;" />
              <el-input v-model="edgePropertyValues[index]" placeholder="属性值" style="width: 150px; margin-right: 10px;" />
              <el-button type="danger" size="small" @click="removeEdgeProperty(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button type="primary" size="small" @click="addEdgeProperty">
              <el-icon><Plus /></el-icon> 添加属性
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="edgeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEdge">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 导入数据对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      :title="importDialogTitle"
      width="800px"
    >
      <el-form :model="importForm" label-width="120px">
        <el-form-item label="数据类型">
          <el-tag :type="importForm.dataType === 'vertex' ? 'primary' : 'success'">
            {{ importForm.dataType === 'vertex' ? '节点' : '边' }}
          </el-tag>
        </el-form-item>
        
        <el-form-item label="标签" v-if="importForm.label">
          <el-tag type="info">{{ importForm.label }}</el-tag>
        </el-form-item>
        
        <el-form-item label="模板下载">
          <el-button type="success" size="small" @click="downloadTemplate" :disabled="!importForm.label">
            <el-icon><Download /></el-icon> 下载模板
          </el-button>
          <div class="form-tip">根据当前选择的标签生成CSV导入模板</div>
        </el-form-item>
        
        <el-form-item label="CSV文件" required>
          <el-upload
            ref="uploadRef"
            class="upload-demo"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            accept=".csv"
          >
            <template #trigger>
              <el-button type="primary" size="small">
                <el-icon><FolderOpened /></el-icon> 选择文件
              </el-button>
            </template>
            <div class="el-upload__tip" style="margin-top: 8px;">
              请上传CSV格式的文件
            </div>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="文件信息" v-if="selectedFile">
          <div class="file-info">
            <div>文件名: {{ selectedFile.name }}</div>
            <div>大小: {{ formatFileSize(selectedFile.size) }}</div>
            <div>类型: {{ selectedFile.type || 'text/csv' }}</div>
          </div>
        </el-form-item>
        
        <!-- 数据预览区域 -->
        <el-form-item label="数据预览" v-if="csvPreviewData.length > 0">
          <div class="preview-container">
            <div class="preview-header">
              <span>前 {{ csvPreviewData.length }} 行数据预览</span>
              <el-button size="small" @click="previewAllData" v-if="csvPreviewData.length > 5">
                查看全部
              </el-button>
            </div>
            <el-table 
              :data="csvPreviewData" 
              border 
              stripe 
              size="small" 
              :max-height="200"
              style="width: 100%;"
              :scroll="{ x: csvHeaders.length > 6 ? '600px' : 'auto', y: '200px' }"
            >
              <el-table-column 
                v-for="(header, index) in csvHeaders" 
                :key="index" 
                :prop="header" 
                :label="header" 
                :min-width="Math.max(80, 120 / csvHeaders.length)"
                :show-overflow-tooltip="true"
              >
                <template #default="{ row }">
                  <span class="cell-text">{{ row[header] || '-' }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
        
        <!-- 字段映射配置 -->
        <el-form-item label="字段映射" v-if="csvHeaders.length > 0">
          <div class="field-mapping">
            <div class="mapping-row header">
              <span>点边属性</span>
              <span>映射到</span>
              <span>CSV字段</span>
              <span>说明</span>
            </div>
            <div class="mapping-container" :style="{ maxHeight: getRequiredFields().length > 5 ? '200px' : 'none', overflowY: getRequiredFields().length > 5 ? 'auto' : 'visible' }">
              <div v-for="(field, index) in getRequiredFields()" :key="index" class="mapping-row">
                <span class="target-field">
                  <span class="field-name">{{ field.name }}</span>
                  <span class="field-required" v-if="field.required">*</span>
                </span>
                <span class="mapping-arrow">←</span>
                <el-select 
                  v-model="fieldMapping[field.name]" 
                  size="small" 
                  style="width: 180px;"
                  :placeholder="'选择CSV字段'"
                  :clearable="!field.required"
                >
                  <el-option 
                    v-for="header in csvHeaders" 
                    :key="header" 
                    :value="header" 
                    :label="header"
                  />
                  <el-option value="" label="不映射" />
                </el-select>
                <span class="mapping-tip">
                  {{ field.description }}
                </span>
              </div>
            </div>
            <div class="mapping-footer">
              <div class="remaining-fields">
                <span>剩余CSV字段: </span>
                <el-tag 
                  v-for="header in getUnmappedHeaders()" 
                  :key="header" 
                  size="small" 
                  type="info"
                  style="margin-left: 5px;"
                >
                  {{ header }}
                </el-tag>
                <span v-if="getUnmappedHeaders().length === 0" class="no-remaining">无剩余字段</span>
              </div>
              <el-button 
                type="primary" 
                size="small" 
                @click="autoMapRemainingFields"
                :disabled="getUnmappedHeaders().length === 0"
              >
                自动映射剩余字段为属性
              </el-button>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="导入配置">
          <el-space direction="vertical">
            <div>
              <el-checkbox v-model="importForm.overwrite">覆盖现有数据</el-checkbox>
              <div class="form-tip">如果开启，将删除现有数据后导入</div>
            </div>
            <div>
              <el-checkbox v-model="importForm.hasHeader">包含表头</el-checkbox>
              <div class="form-tip">CSV文件是否包含表头行</div>
            </div>
            <div>
              <el-input v-model="importForm.delimiter" size="small" style="width: 80px;" placeholder=",">
                <template #prepend>分隔符</template>
              </el-input>
              <div class="form-tip">CSV文件使用的分隔符，默认为逗号</div>
            </div>
          </el-space>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="importData" :disabled="!selectedFile || csvPreviewData.length === 0">
            导入数据
          </el-button>
        </span>
      </template>
    </el-dialog>
    
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Link, Refresh, Edit, Delete, Download, Upload, FolderOpened } from '@element-plus/icons-vue'
import { dataApi } from '../api/data'
import { graphApi } from '../api/graph'
import { useGraphStore } from '@/stores/graph'

// 状态管理
const graphStore = useGraphStore()
const { selectedConnectionId, selectedGraphName } = storeToRefs(graphStore)

// 响应式数据
const vertexLabels = ref([])
const edgeLabels = ref([])
const selectedVertexLabel = ref('')
const selectedEdgeLabel = ref('')

// 数据类型：vertices 或 edges
const dataType = ref('vertices')

// 搜索文本
const searchText = ref('')

// 节点数据
const vertices = ref([])
const vertexLoading = ref(false)
const vertexPageNum = ref(1)
const vertexPageSize = ref(20)
const vertexTotal = ref(0)

// 边数据
const edges = ref([])
const edgeLoading = ref(false)
const edgePageNum = ref(1)
const edgePageSize = ref(20)
const edgeTotal = ref(0)

// 对话框状态
const vertexDialogVisible = ref(false)
const edgeDialogVisible = ref(false)
const queryDialogVisible = ref(false)
const importDialogVisible = ref(false)

// 导入表单数据
const importForm = ref({
  dataType: 'vertex', // vertex 或 edge
  label: '',
  overwrite: false,
  hasHeader: true,
  delimiter: ','
})

const importDialogTitle = ref('导入数据')
const selectedFile = ref(null)
const uploadRef = ref(null)

// CSV预览数据
const csvPreviewData = ref([])
const csvHeaders = ref([])
const fieldMapping = ref({})

// 表单数据
const vertexForm = ref({
  uid: '',
  label: '',
  properties: {}
})
const vertexDialogTitle = ref('创建节点')

const edgeForm = ref({
  uid: '',
  label: '',
  sourceUid: '',
  targetUid: '',
  properties: {}
})
const edgeDialogTitle = ref('创建边')

// 辅助数组用于属性编辑
const propertyKeys = ref([])
const propertyValues = ref([])
const edgePropertyKeys = ref([])
const edgePropertyValues = ref([])

// 计算属性
const filteredVertices = computed(() => {
  if (!searchText.value) return vertices.value
  
  const search = searchText.value.toLowerCase()
  return vertices.value.filter(vertex => {
    return (
      vertex.uid?.toLowerCase().includes(search) ||
      vertex.label?.toLowerCase().includes(search) ||
      JSON.stringify(vertex.properties).toLowerCase().includes(search)
    )
  })
})

const filteredEdges = computed(() => {
  if (!searchText.value) return edges.value
  
  const search = searchText.value.toLowerCase()
  return edges.value.filter(edge => {
    return (
      edge.uid?.toLowerCase().includes(search) ||
      edge.label?.toLowerCase().includes(search) ||
      edge.sourceUid?.toLowerCase().includes(search) ||
      edge.targetUid?.toLowerCase().includes(search) ||
      JSON.stringify(edge.properties).toLowerCase().includes(search)
    )
  })
})

// 生命周期钩子
const isMounted = ref(false)

onMounted(() => {
  isMounted.value = true
  loadConnections()
  // 如果已选择图，加载数据
  if (selectedGraphName.value) {
    onGraphChange()
  }
})

onUnmounted(() => {
  isMounted.value = false
})

// 监听全局图选择变化
watch(() => selectedGraphName.value, () => {
  if (isMounted.value) {
    onGraphChange()
  }
})

// 方法
async function loadConnections() {
  // 加载连接和图列表
  try {
    await graphStore.loadAllGraphs()
    console.log('加载连接和图列表成功')
  } catch (error) {
    console.error('加载连接失败:', error)
  }
}

async function onGraphChange() {
  if (!selectedGraphName.value || !isMounted.value) return

  // 加载节点和边类型
  if (isMounted.value) {
    await loadVertexLabels()
  }
  if (isMounted.value) {
    await loadEdgeLabels()
  }

  // 加载数据
  if (isMounted.value) {
    loadVertices()
  }
  if (isMounted.value) {
    loadEdges()
  }
}

async function loadVertexLabels() {
  if (!isMounted.value || !selectedGraphName.value || !selectedConnectionId.value) return
  try {
    const res = await graphApi.getVertexTypes(selectedConnectionId.value, selectedGraphName.value)
    if (isMounted.value) {
      vertexLabels.value = res || []
    }
  } catch (error) {
    if (isMounted.value) {
      ElMessage.error('加载节点类型失败: ' + error.message)
    }
  }
}

async function loadEdgeLabels() {
  if (!isMounted.value || !selectedGraphName.value || !selectedConnectionId.value) return
  try {
    const res = await graphApi.getEdgeTypes(selectedConnectionId.value, selectedGraphName.value)
    if (isMounted.value) {
      edgeLabels.value = res || []
    }
  } catch (error) {
    if (isMounted.value) {
      ElMessage.error('加载边类型失败: ' + error.message)
    }
  }
}

async function loadVertices() {
  if (!selectedGraphName.value || !isMounted.value || !selectedConnectionId.value) return

  vertexLoading.value = true
  try {
    const res = await dataApi.queryVertices(
      selectedConnectionId.value,
      selectedGraphName.value,
      {
        label: selectedVertexLabel.value || undefined,
        pageNum: vertexPageNum.value,
        pageSize: vertexPageSize.value
      }
    )

    if (isMounted.value) {
      vertices.value = res?.list || []
      vertexTotal.value = res?.total || vertices.value.length
    }
  } catch (error) {
    if (isMounted.value) {
      ElMessage.error('查询节点失败: ' + error.message)
    }
  } finally {
    if (isMounted.value) {
      vertexLoading.value = false
    }
  }
}

async function loadEdges() {
  if (!selectedGraphName.value || !isMounted.value || !selectedConnectionId.value) return

  edgeLoading.value = true
  try {
    const res = await dataApi.queryEdges(
      selectedConnectionId.value,
      selectedGraphName.value,
      {
        label: selectedEdgeLabel.value || undefined,
        pageNum: edgePageNum.value,
        pageSize: edgePageSize.value
      }
    )

    if (isMounted.value) {
      edges.value = res?.list || []
      edgeTotal.value = res?.total || edges.value.length
    }
  } catch (error) {
    if (isMounted.value) {
      ElMessage.error('查询边失败: ' + error.message)
    }
  } finally {
    if (isMounted.value) {
      edgeLoading.value = false
    }
  }
}

function onLabelChange() {
  if (dataType.value === 'vertices') {
    loadVertices()
  } else {
    loadEdges()
  }
}

// 选择节点标签
function selectVertexLabel(labelName) {
  if (selectedVertexLabel.value === labelName) {
    selectedVertexLabel.value = ''
  } else {
    selectedVertexLabel.value = labelName
  }
  dataType.value = 'vertices'
  selectedEdgeLabel.value = ''
  loadVertices()
}

// 选择边标签
function selectEdgeLabel(labelName) {
  if (selectedEdgeLabel.value === labelName) {
    selectedEdgeLabel.value = ''
  } else {
    selectedEdgeLabel.value = labelName
  }
  dataType.value = 'edges'
  selectedVertexLabel.value = ''
  loadEdges()
}

function showCreateVertexDialog() {
  vertexForm.value = {
    uid: '',
    label: '',
    properties: {}
  }
  vertexDialogTitle.value = '创建节点'
  vertexDialogVisible.value = true
  
  // 初始化属性编辑器
  propertyKeys.value = []
  propertyValues.value = []
}

function showEditVertexDialog(vertex) {
  vertexForm.value = {
    uid: vertex.uid,
    label: vertex.label,
    properties: { ...vertex.properties }
  }
  vertexDialogTitle.value = '编辑节点'
  vertexDialogVisible.value = true
  
  // 初始化属性编辑器
  propertyKeys.value = Object.keys(vertex.properties)
  propertyValues.value = Object.values(vertex.properties)
}

async function saveVertex() {
  if (!selectedConnectionId.value) {
    ElMessage.error('请先选择连接')
    return
  }

  // 构建属性对象
  const properties = {}
  propertyKeys.value.forEach((key, index) => {
    if (key && propertyValues.value[index] !== undefined) {
      properties[key] = propertyValues.value[index]
    }
  })

  vertexForm.value.properties = properties

  try {
    let res
    if (vertexForm.value.uid) {
      // 更新现有节点
      res = await dataApi.updateVertex(
        selectedConnectionId.value,
        selectedGraphName.value,
        vertexForm.value.uid,
        {
          properties: vertexForm.value.properties
        }
      )
    } else {
      // 创建新节点
      res = await dataApi.createVertex(
        selectedConnectionId.value,
        selectedGraphName.value,
        {
          label: vertexForm.value.label,
          properties: vertexForm.value.properties
        }
      )
    }

    ElMessage.success(vertexForm.value.uid ? '节点更新成功' : '节点创建成功')
    vertexDialogVisible.value = false
    loadVertices()
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

async function deleteVertex(uid) {
  if (!selectedConnectionId.value) {
    ElMessage.error('请先选择连接')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除这个节点吗？', '警告', {
      type: 'warning'
    })

    const res = await dataApi.deleteVertex(selectedConnectionId.value, selectedGraphName.value, uid)
    ElMessage.success('节点删除成功')
    loadVertices()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

function showCreateEdgeDialog() {
  edgeForm.value = {
    uid: '',
    label: '',
    sourceUid: '',
    targetUid: '',
    properties: {}
  }
  edgeDialogTitle.value = '创建边'
  edgeDialogVisible.value = true
  
  // 初始化属性编辑器
  edgePropertyKeys.value = []
  edgePropertyValues.value = []
}

function showEditEdgeDialog(edge) {
  edgeForm.value = {
    uid: edge.uid,
    label: edge.label,
    sourceUid: edge.sourceUid,
    targetUid: edge.targetUid,
    properties: { ...edge.properties }
  }
  edgeDialogTitle.value = '编辑边'
  edgeDialogVisible.value = true
  
  // 初始化属性编辑器
  edgePropertyKeys.value = Object.keys(edge.properties)
  edgePropertyValues.value = Object.values(edge.properties)
}

async function saveEdge() {
  if (!selectedConnectionId.value) {
    ElMessage.error('请先选择连接')
    return
  }

  // 构建属性对象
  const properties = {}
  edgePropertyKeys.value.forEach((key, index) => {
    if (key && edgePropertyValues.value[index] !== undefined) {
      properties[key] = edgePropertyValues.value[index]
    }
  })

  edgeForm.value.properties = properties

  try {
    let res
    if (edgeForm.value.uid) {
      // 更新现有边
      res = await dataApi.updateEdge(
        selectedConnectionId.value,
        selectedGraphName.value,
        edgeForm.value.uid,
        {
          properties: edgeForm.value.properties
        }
      )
    } else {
      // 创建新边
      res = await dataApi.createEdge(
        selectedConnectionId.value,
        selectedGraphName.value,
        {
          label: edgeForm.value.label,
          sourceUid: edgeForm.value.sourceUid,
          targetUid: edgeForm.value.targetUid,
          properties: edgeForm.value.properties
        }
      )
    }

    ElMessage.success(edgeForm.value.uid ? '边更新成功' : '边创建成功')
    edgeDialogVisible.value = false
    loadEdges()
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

async function deleteEdge(uid) {
  if (!selectedConnectionId.value) {
    ElMessage.error('请先选择连接')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除这条边吗？', '警告', {
      type: 'warning'
    })

    const res = await dataApi.deleteEdge(selectedConnectionId.value, selectedGraphName.value, uid)
    ElMessage.success('边删除成功')
    loadEdges()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

function showQueryDialog() {
  queryLanguage.value = 'CYPHER'
  queryStatement.value = ''
  queryResult.value = null
  queryDialogVisible.value = true
}

// 加载数据
function loadData() {
  if (dataType.value === 'vertices') {
    loadVertices()
  } else {
    loadEdges()
  }
}

// 导出数据
function exportData() {
  if (dataType.value === 'vertices') {
    exportVertices()
  } else {
    exportEdges()
  }
}

// 显示创建对话框
function showCreateDialog() {
  if (dataType.value === 'vertices') {
    showCreateVertexDialog()
  } else {
    showCreateEdgeDialog()
  }
}

function refreshData() {
  loadVertices()
  loadEdges()
}

function handleVertexSizeChange(size) {
  vertexPageSize.value = size
  loadVertices()
}

function handleVertexPageChange(page) {
  vertexPageNum.value = page
  loadVertices()
}

function handleEdgeSizeChange(size) {
  edgePageSize.value = size
  loadEdges()
}

function handleEdgePageChange(page) {
  edgePageNum.value = page
  loadEdges()
}

function formatTimestamp(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

function addProperty() {
  propertyKeys.value.push('')
  propertyValues.value.push('')
}

function removeProperty(index) {
  propertyKeys.value.splice(index, 1)
  propertyValues.value.splice(index, 1)
}

function addEdgeProperty() {
  edgePropertyKeys.value.push('')
  edgePropertyValues.value.push('')
}

function removeEdgeProperty(index) {
  edgePropertyKeys.value.splice(index, 1)
  edgePropertyValues.value.splice(index, 1)
}

// 导出节点数据
async function exportVertices() {
  if (vertices.value.length === 0) {
    ElMessage.warning('没有节点数据可导出')
    return
  }

  const data = vertices.value.map(v => ({
    uid: v.uid,
    label: v.label,
    ...v.properties
  }))

  downloadFile(data, `vertices_${selectedGraphName.value}_${Date.now()}.json`)
}

// 导出边数据
async function exportEdges() {
  if (edges.value.length === 0) {
    ElMessage.warning('没有边数据可导出')
    return
  }

  const data = edges.value.map(e => ({
    uid: e.uid,
    label: e.label,
    sourceUid: e.sourceUid,
    targetUid: e.targetUid,
    ...e.properties
  }))

  downloadFile(data, `edges_${selectedGraphName.value}_${Date.now()}.json`)
}

// 下载文件
function downloadFile(data, filename) {
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出: ${filename}`)
}

// 显示导入对话框
function showImportDialog(dataType) {
  if (!selectedGraphName.value) {
    ElMessage.error('请先选择图数据库')
    return
  }

  importForm.value.dataType = dataType
  importForm.value.label = dataType === 'vertex' ? selectedVertexLabel.value : selectedEdgeLabel.value
  importForm.value.overwrite = false
  
  importDialogTitle.value = `导入${dataType === 'vertex' ? '节点' : '边'}数据`
  selectedFile.value = null
  importDialogVisible.value = true
  
  // 重置上传组件
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 处理文件选择
function handleFileChange(file) {
  selectedFile.value = file.raw
  
  // 清空预览数据
  csvPreviewData.value = []
  csvHeaders.value = []
  fieldMapping.value = {}
  
  // 预览CSV文件
  if (file.raw) {
    previewCsvFile(file.raw)
  }
}

// 上传前验证
function beforeUpload(file) {
  const isCSV = file.type === 'text/csv' || file.name.endsWith('.csv')
  if (!isCSV) {
    ElMessage.error('只能上传CSV文件!')
    return false
  }
  return true
}

// 格式化文件大小
function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 导入数据
async function importData() {
  if (!selectedFile.value) {
    ElMessage.error('请先选择CSV文件')
    return
  }

  if (!selectedConnectionId.value || !selectedGraphName.value) {
    ElMessage.error('请先选择连接和图数据库')
    return
  }

  // 构建导入配置，包含字段映射信息
  const config = {
    dataType: importForm.value.dataType,
    overwrite: importForm.value.overwrite,
    hasHeader: importForm.value.hasHeader,
    delimiter: importForm.value.delimiter
  }
  
  // 添加字段映射配置
  if (Object.keys(fieldMapping.value).length > 0) {
    config.fieldMapping = fieldMapping.value
    
    // 设置映射的字段
    if (fieldMapping.value.uid) {
      config.idField = fieldMapping.value.uid
    }
    if (fieldMapping.value.label) {
      config.labelField = fieldMapping.value.label
    }
    if (fieldMapping.value.sourceUid) {
      config.sourceField = fieldMapping.value.sourceUid
    }
    if (fieldMapping.value.targetUid) {
      config.targetField = fieldMapping.value.targetUid
    }
    
    // 处理属性字段映射
    const propertyMappings = Object.keys(fieldMapping.value).filter(key => 
      key.startsWith('property_') && fieldMapping.value[key]
    )
    if (propertyMappings.length > 0) {
      config.propertyMappings = {}
      propertyMappings.forEach(propKey => {
        config.propertyMappings[propKey.replace('property_', '')] = fieldMapping.value[propKey]
      })
    }
  }
  
  if (importForm.value.label) {
    config.label = importForm.value.label
  }

  try {
    const res = await dataApi.importCsv(
      selectedConnectionId.value,
      selectedGraphName.value,
      config,
      selectedFile.value
    )

    ElMessage.success(`数据导入成功! 共导入 ${res?.total || 0} 条记录`)
    importDialogVisible.value = false

    // 刷新当前数据
    if (importForm.value.dataType === 'vertex') {
      loadVertices()
    } else {
      loadEdges()
    }
  } catch (error) {
    ElMessage.error('导入失败: ' + error.message)
  }
}

// 预览CSV文件
function previewCsvFile(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const csvContent = e.target.result
      const lines = csvContent.split('\n').filter(line => line.trim())
      
      if (lines.length === 0) {
        ElMessage.warning('CSV文件为空')
        return
      }
      
      // 解析CSV内容
      const delimiter = importForm.value.delimiter || ','
      const headers = parseCsvLine(lines[0], delimiter)
      csvHeaders.value = headers
      
      // 解析数据行（最多显示10行）
      const previewRows = []
      const startRow = importForm.value.hasHeader ? 1 : 0
      const maxRows = Math.min(lines.length - startRow, 10)
      
      for (let i = startRow; i < startRow + maxRows; i++) {
        if (lines[i]) {
          const rowData = parseCsvLine(lines[i], delimiter)
          const rowObj = {}
          headers.forEach((header, index) => {
            rowObj[header] = rowData[index] || ''
          })
          previewRows.push(rowObj)
        }
      }
      
      csvPreviewData.value = previewRows
      
      // 自动初始化字段映射
      initializeFieldMapping(headers)
      
      ElMessage.success(`成功解析CSV文件，共${lines.length - (importForm.value.hasHeader ? 1 : 0)}行数据`)
    } catch (error) {
      ElMessage.error('CSV文件解析失败: ' + error.message)
    }
  }
  reader.readAsText(file)
}

// 解析CSV单行
function parseCsvLine(line, delimiter) {
  const result = []
  let current = ''
  let inQuotes = false
  
  for (let i = 0; i < line.length; i++) {
    const char = line[i]
    
    if (char === '"') {
      inQuotes = !inQuotes
    } else if (char === delimiter && !inQuotes) {
      result.push(current.trim())
      current = ''
    } else {
      current += char
    }
  }
  
  result.push(current.trim())
  return result
}

// 初始化字段映射
function initializeFieldMapping(headers) {
  const mapping = {}
  
  // 首先尝试自动识别必需字段
  headers.forEach(header => {
    const detectedType = autoDetectField(header)
    if (detectedType === 'uid' && !mapping.uid) {
      mapping.uid = header
    } else if (detectedType === 'label' && !mapping.label) {
      mapping.label = header
    } else if (detectedType === 'sourceUid' && !mapping.sourceUid && importForm.value.dataType === 'edge') {
      mapping.sourceUid = header
    } else if (detectedType === 'targetUid' && !mapping.targetUid && importForm.value.dataType === 'edge') {
      mapping.targetUid = header
    }
  })
  
  fieldMapping.value = mapping
}

// 获取必需映射的字段列表
function getRequiredFields() {
  const baseFields = [
    {
      name: 'uid',
      displayName: 'UID',
      required: true,
      description: '唯一标识符，不能为空'
    },
    {
      name: 'label', 
      displayName: '标签',
      required: false,
      description: '节点/边类型，如果为空则使用默认标签'
    }
  ]
  
  // 如果是边类型，添加源节点和目标节点字段
  if (importForm.value.dataType === 'edge') {
    baseFields.push(
      {
        name: 'sourceUid',
        displayName: '源节点UID',
        required: true,
        description: '源节点的唯一标识'
      },
      {
        name: 'targetUid',
        displayName: '目标节点UID', 
        required: true,
        description: '目标节点的唯一标识'
      }
    )
  }
  
  return baseFields
}

// 获取未映射的CSV字段
function getUnmappedHeaders() {
  const mappedHeaders = Object.values(fieldMapping.value).filter(header => header && header !== '')
  return csvHeaders.value.filter(header => !mappedHeaders.includes(header))
}

// 自动映射剩余字段为属性
function autoMapRemainingFields() {
  const unmappedHeaders = getUnmappedHeaders()
  unmappedHeaders.forEach(header => {
    // 检查是否已经有其他属性字段映射
    const propertyMappings = Object.keys(fieldMapping.value).filter(key => 
      !['uid', 'label', 'sourceUid', 'targetUid'].includes(key) && fieldMapping.value[key]
    )
    
    // 为这个属性字段创建映射
    const propertyField = `property_${propertyMappings.length + 1}`
    fieldMapping.value[propertyField] = header
  })
  ElMessage.success(`已自动映射 ${unmappedHeaders.length} 个字段为属性`)
}

// 自动检测字段类型
function autoDetectField(header) {
  const headerLower = header.toLowerCase()
  
  if (headerLower.includes('id') || headerLower.includes('uid')) {
    return 'uid'
  } else if (headerLower.includes('label') || headerLower.includes('类型') || headerLower.includes('category')) {
    return 'label'
  } else if (headerLower.includes('source') || headerLower.includes('起始') || headerLower.includes('from')) {
    return 'sourceUid'
  } else if (headerLower.includes('target') || headerLower.includes('目标') || headerLower.includes('to')) {
    return 'targetUid'
  } else {
    return 'property'
  }
}

// 获取字段映射提示
function getFieldMappingTip(mapping) {
  switch (mapping) {
    case 'uid': return '作为唯一标识符'
    case 'label': return '作为标签类型'
    case 'sourceUid': return '作为源节点标识'
    case 'targetUid': return '作为目标节点标识'
    case 'property': return '作为属性字段'
    case 'ignore': return '忽略此字段'
    default: return ''
  }
}

// 预览全部数据
function previewAllData() {
  if (selectedFile.value) {
    previewCsvFile(selectedFile.value)
  }
}

// 下载模板
function downloadTemplate() {
  if (!importForm.value.label) {
    ElMessage.error('请先选择要导入的标签类型')
    return
  }
  
  const dataType = importForm.value.dataType
  const label = importForm.value.label
  
  // 生成模板数据
  let templateData = []
  let headers = []
  
  if (dataType === 'vertex') {
    // 节点模板
    headers = ['uid', 'label']
    templateData = [{
      uid: 'node_001',
      label: label
    }]
  } else {
    // 边模板
    headers = ['uid', 'label', 'sourceUid', 'targetUid']
    templateData = [{
      uid: 'edge_001',
      label: label,
      sourceUid: 'node_001',
      targetUid: 'node_002'
    }]
  }
  
  // 添加示例属性
  headers.push('property1', 'property2', 'property3')
  templateData[0].property1 = '示例值1'
  templateData[0].property2 = '示例值2'
  templateData[0].property3 = '示例值3'
  
  // 转换为CSV格式
  const csvContent = [
    headers.join(','),
    ...templateData.map(row => headers.map(header => `"${row[header] || ''}"`).join(','))
  ].join('\n')
  
  // 下载文件
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `${dataType}_${label}_template.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success(`模板文件已生成: ${dataType}_${label}_template.csv`)
}

</script>

<style scoped>
.data-explorer {
  height: calc(100vh - 60px);
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.sidebar-content {
  overflow-y: auto;
  flex: 1;
  padding: 0;
}

.section {
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section h4 {
  margin: 0;
  color: #606266;
  font-size: 14px;
  font-weight: 600;
}

.data-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.data-count {
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.properties-display {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.property-item {
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}

.property-key {
  font-weight: bold;
  color: #409eff;
  margin-right: 4px;
}

.property-value {
  color: #606266;
}

.label-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 200px;
  overflow-y: auto;
}

.label-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e4e7ed;
  background: #f5f7fa;
  font-size: 12px;
  min-width: 60px;
  text-align: center;
}

.label-tag:hover {
  border-color: #409eff;
  background: #ecf5ff;
  transform: translateY(-1px);
}

.label-tag.active {
  border-color: #409eff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.label-name {
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

.edge-tag {
  border-color: #67c23a;
}

.edge-tag:hover {
  border-color: #67c23a;
  background: #f0f9ff;
}

.edge-tag.active {
  border-color: #67c23a;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.properties-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
}

.property-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.operation-buttons {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.operation-buttons .el-button {
  padding: 5px 8px;
  margin: 0;
}

.pagination {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

.file-info {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 预览容器样式 */
.preview-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  background-color: #fafafa;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

/* 字段映射样式 */
.field-mapping {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.mapping-container {
  max-height: 200px;
  overflow-y: auto;
}

.mapping-row {
  display: grid;
  grid-template-columns: 120px 40px 180px 1fr;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  min-height: 36px;
  gap: 8px;
}

.mapping-row.header {
  background-color: #f5f7fa;
  font-weight: 600;
  color: #606266;
  position: sticky;
  top: 0;
  z-index: 10;
}

.mapping-row:last-child {
  border-bottom: none;
}

.target-field {
  display: flex;
  align-items: center;
  min-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.field-name {
  font-weight: 600;
  color: #409eff;
}

.field-required {
  color: #f56c6c;
  margin-left: 4px;
}

.mapping-arrow {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-weight: bold;
  font-size: 16px;
}

.mapping-tip {
  font-size: 12px;
  color: #909399;
  font-style: italic;
}

.mapping-footer {
  padding: 8px 12px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.remaining-fields {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 5px;
}

.no-remaining {
  color: #909399;
  font-style: italic;
}

/* 表格样式优化 */
:deep(.el-table) {
  font-size: 12px;
}

:deep(.el-table .cell) {
  padding: 4px 8px;
  line-height: 1.2;
  word-break: break-word;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

/* 单元格文本样式 */
.cell-text {
  display: inline-block;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 预览表格滚动优化 */
:deep(.preview-container .el-table__body-wrapper) {
  max-height: 200px;
  overflow-y: auto;
}

:deep(.preview-container .el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 10;
}

/* 字段映射表格优化 */
:deep(.field-mapping .el-table) {
  max-height: 150px;
}
</style>