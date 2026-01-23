<template>
  <div class="data-export">
    <div class="header">
      <h2 class="page-title">数据导出管理</h2>
      <div class="header-actions">
        <el-select v-model="selectedConnectionId" placeholder="选择连接" style="width: 200px; margin-right: 10px;">
          <el-option v-for="conn in connections" :key="conn.id" :label="conn.name" :value="conn.id">
            <span>{{ conn.name }}</span>
            <el-tag :type="conn.status === 1 ? 'success' : 'danger'" size="small" style="margin-left: 8px;">
              {{ conn.status === 1 ? '正常' : '异常' }}
            </el-tag>
          </el-option>
        </el-select>
        <el-button type="primary" @click="showCreateExportDialog" :disabled="!selectedGraphName.value">
          <el-icon><Plus /></el-icon>
          新建导出任务
        </el-button>
      </div>
    </div>

    <div class="content">
      <!-- 导出任务创建表单 -->
      <div class="export-form-section" v-if="showExportForm">
        <el-card class="export-form-card">
          <template #header>
            <div class="card-header">
              <span>创建导出任务</span>
              <el-button size="small" @click="showExportForm = false">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div class="export-form">
            <el-form :model="exportForm" label-width="120px">
              <el-form-item label="导出类型" required>
                <el-radio-group v-model="exportForm.exportType">
                  <el-radio-button label="vertices">节点数据</el-radio-button>
                  <el-radio-button label="edges">边数据</el-radio-button>
                  <el-radio-button label="graph">全图数据</el-radio-button>
                  <el-radio-button label="visual">可视化视图</el-radio-button>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="导出格式" required>
                <el-radio-group v-model="exportForm.exportFormat">
                  <el-radio-button label="csv" :disabled="exportForm.exportType === 'visual'">CSV</el-radio-button>
                  <el-radio-button label="json" :disabled="exportForm.exportType === 'visual'">JSON</el-radio-button>
                  <el-radio-button label="gexf" :disabled="exportForm.exportType === 'visual'">GEXF (Gephi)</el-radio-button>
                  <el-radio-button label="graphml" :disabled="exportForm.exportType === 'visual'">GraphML</el-radio-button>
                  <el-radio-button label="png" :disabled="exportForm.exportType !== 'visual'">PNG图片</el-radio-button>
                  <el-radio-button label="svg" :disabled="exportForm.exportType !== 'visual'">SVG矢量图</el-radio-button>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="筛选条件" v-if="exportForm.exportType !== 'visual'">
                <div class="filter-fields">
                  <el-form-item label="标签过滤" label-width="80px">
                    <el-select v-model="exportForm.filterLabels" multiple placeholder="选择标签" style="width: 100%">
                      <el-option v-for="label in availableLabels" :key="label" :label="label" :value="label" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="属性选择" label-width="80px">
                    <el-select v-model="exportForm.filterProperties" multiple placeholder="选择属性" style="width: 100%">
                      <el-option v-for="prop in availableProperties" :key="prop" :label="prop" :value="prop" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="数据量限制" label-width="80px">
                    <el-input-number v-model="exportForm.limit" :min="1" :max="1000000" placeholder="不限制" style="width: 200px" />
                    <span class="limit-hint">不填写则导出全部数据</span>
                  </el-form-item>
                </div>
              </el-form-item>
              
              <el-form-item label="导出选项">
                <el-checkbox-group v-model="exportForm.options">
                  <el-checkbox label="includeHeader">包含表头（CSV格式）</el-checkbox>
                  <el-checkbox label="includeMetadata">包含元数据</el-checkbox>
                  <el-checkbox label="compress">压缩为ZIP</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="createExportTask" :loading="creatingTask">创建任务</el-button>
                <el-button @click="showExportForm = false">取消</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </div>
      
      <!-- 导出任务列表 -->
      <div class="task-list-section">
        <el-card class="task-list-card">
          <template #header>
            <div class="card-header">
              <span>导出任务列表</span>
              <el-button type="primary" size="small" @click="refreshTasks">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <el-table :data="filteredTasks" border stripe v-loading="loading">
            <el-table-column prop="id" label="任务ID" width="120" />
            <el-table-column prop="name" label="任务名称" min-width="160">
              <template #default="{ row }">
                <span class="task-name">{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="exportType" label="导出类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getExportTypeTagType(row.exportType)" size="small">
                  {{ getExportTypeLabel(row.exportType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="exportFormat" label="导出格式" width="120">
              <template #default="{ row }">
                <el-tag :type="getExportFormatTagType(row.exportFormat)" size="small">
                  {{ getExportFormatLabel(row.exportFormat) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" size="small">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="progress" label="进度" width="120">
              <template #default="{ row }">
                <el-progress :percentage="row.progress" :status="getProgressStatus(row.status)" />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="finishTime" label="完成时间" width="180">
              <template #default="{ row }">
                {{ row.finishTime ? formatDateTime(row.finishTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button 
                    v-if="row.status === 'completed' && row.downloadUrl" 
                    type="primary" 
                    size="small" 
                    @click="downloadExport(row)"
                  >
                    <el-icon><Download /></el-icon>
                    下载
                  </el-button>
                  <el-button 
                    v-if="row.status === 'running'" 
                    type="warning" 
                    size="small" 
                    @click="cancelExportTask(row)"
                  >
                    <el-icon><Close /></el-icon>
                    取消
                  </el-button>
                  <el-button 
                    v-if="row.status !== 'running'" 
                    type="danger" 
                    size="small" 
                    @click="deleteExportTask(row)"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination" v-if="tasks.length > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="totalTasks"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
          
          <div class="empty-state" v-if="tasks.length === 0 && !loading">
            <el-empty description="暂无导出任务">
              <template #description>
                <p>请先选择连接和图，然后创建导出任务</p>
              </template>
            </el-empty>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 任务详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="导出任务详情" width="700px">
      <div v-if="currentTaskDetail">
        <el-descriptions title="任务信息" border :column="2">
          <el-descriptions-item label="任务ID">{{ currentTaskDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ currentTaskDetail.name }}</el-descriptions-item>
          <el-descriptions-item label="导出类型">
            <el-tag :type="getExportTypeTagType(currentTaskDetail.exportType)" size="small">
              {{ getExportTypeLabel(currentTaskDetail.exportType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="导出格式">
            <el-tag :type="getExportFormatTagType(currentTaskDetail.exportFormat)" size="small">
              {{ getExportFormatLabel(currentTaskDetail.exportFormat) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentTaskDetail.status)" size="small">
              {{ getStatusLabel(currentTaskDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="进度">{{ currentTaskDetail.progress }}%</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentTaskDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ currentTaskDetail.finishTime ? formatDateTime(currentTaskDetail.finishTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="文件大小" v-if="currentTaskDetail.fileSize">
            {{ formatFileSize(currentTaskDetail.fileSize) }}
          </el-descriptions-item>
          <el-descriptions-item label="下载链接" v-if="currentTaskDetail.downloadUrl">
            <el-link type="primary" :href="currentTaskDetail.downloadUrl" target="_blank">点击下载</el-link>
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="currentTaskDetail.errorMessage" class="error-section">
          <h4>错误信息</h4>
          <pre class="error-message">{{ currentTaskDetail.errorMessage }}</pre>
        </div>
        
        <div v-if="currentTaskDetail.statistics" class="statistics-section">
          <h4>统计信息</h4>
          <el-descriptions border :column="2">
            <el-descriptions-item label="总记录数">{{ currentTaskDetail.statistics.totalRecords }}</el-descriptions-item>
            <el-descriptions-item label="导出记录数">{{ currentTaskDetail.statistics.exportedRecords }}</el-descriptions-item>
            <el-descriptions-item label="过滤记录数">{{ currentTaskDetail.statistics.filteredRecords }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ formatDateTime(currentTaskDetail.statistics.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatDateTime(currentTaskDetail.statistics.endTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <div v-else>
        <el-empty description="加载中..." />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Close, Refresh, Download, Delete } from '@element-plus/icons-vue'
import { connectionApi } from '../api/connection'
import { useExportStore } from '../stores/export'
import { useGraphStore } from '@/stores/graph'

// 使用export store
const exportStore = useExportStore()

// 状态管理
const graphStore = useGraphStore()
const { selectedGraphName } = storeToRefs(graphStore)

// 响应式数据
const connections = ref([])

// 从store获取状态
const selectedConnectionId = computed({
  get: () => exportStore.selectedConnectionId,
  set: (value) => {
    exportStore.selectedConnectionId = value
  }
})
const tasks = computed(() => exportStore.tasks)
const loading = computed(() => exportStore.loading)
const currentPage = ref(1)
const pageSize = ref(20)
const totalTasks = computed(() => exportStore.totalTasks)

// 表单相关
const showExportForm = ref(false)
const creatingTask = ref(false)
const exportForm = ref({
  name: '',
  exportType: 'vertices',
  exportFormat: 'csv',
  filterLabels: [],
  filterProperties: [],
  limit: null,
  options: ['includeHeader', 'includeMetadata']
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentTaskDetail = ref(null)

// 可用标签和属性（模拟数据）
const availableLabels = ref(['Person', 'Company', 'Product', 'Order', 'Category', 'Tag'])
const availableProperties = ref(['name', 'age', 'city', 'created_at', 'updated_at', 'type', 'status'])

// 计算属性
const filteredTasks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return tasks.value.slice(start, end)
})

// 获取导出类型标签样式
const getExportTypeTagType = (type) => {
  const map = {
    'vertices': 'success',
    'edges': 'warning',
    'graph': 'info',
    'visual': 'primary'
  }
  return map[type] || ''
}

// 获取导出类型文本
const getExportTypeLabel = (type) => {
  const map = {
    'vertices': '节点数据',
    'edges': '边数据',
    'graph': '全图数据',
    'visual': '可视化视图'
  }
  return map[type] || type
}

// 获取导出格式标签样式
const getExportFormatTagType = (format) => {
  const map = {
    'csv': 'success',
    'json': 'warning',
    'gexf': 'info',
    'graphml': 'primary',
    'png': 'danger',
    'svg': 'danger'
  }
  return map[format] || ''
}

// 获取导出格式文本
const getExportFormatLabel = (format) => {
  const map = {
    'csv': 'CSV',
    'json': 'JSON',
    'gexf': 'GEXF',
    'graphml': 'GraphML',
    'png': 'PNG',
    'svg': 'SVG'
  }
  return map[format] || format
}

// 获取状态标签样式
const getStatusTagType = (status) => {
  const map = {
    'pending': 'info',
    'running': 'primary',
    'completed': 'success',
    'failed': 'danger',
    'cancelled': 'warning'
  }
  return map[status] || ''
}

// 获取状态文本
const getStatusLabel = (status) => {
  const map = {
    'pending': '等待中',
    'running': '进行中',
    'completed': '已完成',
    'failed': '失败',
    'cancelled': '已取消'
  }
  return map[status] || status
}

// 获取进度条状态
const getProgressStatus = (status) => {
  if (status === 'failed') return 'exception'
  if (status === 'cancelled') return 'warning'
  return undefined
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 加载连接列表
const loadConnections = async () => {
  try {
    const res = await connectionApi.list()
    const data = res.data
    connections.value = Array.isArray(data) ? data : []
    if (connections.value.length > 0 && !selectedConnectionId.value) {
      selectedConnectionId.value = connections.value[0].id
    }
  } catch (error) {
    console.error('加载连接列表失败:', error)
    ElMessage.error('加载连接列表失败')
  }
}

// 加载图列表
const loadGraphs = async () => {
  await exportStore.loadGraphs()
  // 图列表现在由全局状态管理
}

// 加载任务列表 - 使用store的方法
const loadTasks = async () => {
  await exportStore.loadTasks()
}

// 显示创建导出任务对话框
const showCreateExportDialog = () => {
  // 生成默认任务名称
  exportForm.value.name = `导出_${selectedGraphName.value}_${new Date().toLocaleString('zh-CN')}`
  showExportForm.value = true
}

// 创建导出任务 - 使用store的方法
const createExportTask = async () => {
  if (!exportForm.value.name.trim()) {
    ElMessage.warning('请输入任务名称')
    return
  }
  
  creatingTask.value = true
  try {
    await exportStore.createTask(exportForm.value)
    ElMessage.success('导出任务创建成功，任务已加入队列')
    showExportForm.value = false
  } catch (error) {
    console.error('创建导出任务失败:', error)
    ElMessage.error('创建导出任务失败')
  } finally {
    creatingTask.value = false
  }
}

// 刷新任务列表
const refreshTasks = () => {
  loadTasks()
}

// 下载导出文件
const downloadExport = (task) => {
  if (task.downloadUrl) {
    window.open(task.downloadUrl, '_blank')
  }
}

// 取消导出任务 - 使用store的方法
const cancelExportTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确定取消导出任务 "${task.name}" 吗？`, '警告', {
      type: 'warning'
    })
    
    await exportStore.cancelTask(task.id)
    ElMessage.success('导出任务已取消')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消导出任务失败:', error)
      ElMessage.error('取消导出任务失败')
    }
  }
}

// 删除导出任务 - 使用store的方法
const deleteExportTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确定删除导出任务 "${task.name}" 吗？`, '警告', {
      type: 'warning'
    })
    
    await exportStore.deleteTask(task.id)
    ElMessage.success('导出任务已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除导出任务失败:', error)
      ElMessage.error('删除导出任务失败')
    }
  }
}

// 显示任务详情
const showTaskDetail = (task) => {
  currentTaskDetail.value = {
    ...task,
    statistics: {
      totalRecords: Math.floor(Math.random() * 100000) + 10000,
      exportedRecords: Math.floor(Math.random() * 100000) + 10000,
      filteredRecords: Math.floor(Math.random() * 1000) + 100,
      startTime: task.createTime,
      endTime: task.finishTime || null
    }
  }
  detailDialogVisible.value = true
}

// 分页大小变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
}

// 当前页变化
const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}

// 监听连接变化
watch(() => selectedConnectionId.value, async () => {
  exportStore.clearTasks()
  if (selectedConnectionId.value) {
    await exportStore.setSelection(selectedConnectionId.value, selectedGraphName.value)
  }
})

// 监听全局图选择变化
watch(() => selectedGraphName.value, async () => {
  if (selectedGraphName.value) {
    // 更新store的选中状态
    await exportStore.setSelection(selectedConnectionId.value, selectedGraphName.value)
    // 加载任务列表
    loadTasks()
    // 开始轮询
    exportStore.startPolling()
  }
})

// 生命周期
onMounted(() => {
  loadConnections()
})

// 组件卸载时清理
onUnmounted(() => {
  exportStore.stopPolling()
  exportStore.clearTasks()
})
</script>

<style scoped>
.data-export {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.export-form-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.export-form-card {
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.export-form {
  padding: 16px;
}

.filter-fields {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 6px;
}

.limit-hint {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.task-list-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.task-list-card {
  border: none;
}

.task-name {
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px 0;
  background: #f5f7fa;
  border-radius: 0 0 8px 8px;
}

.empty-state {
  padding: 40px 0;
}

.error-section {
  margin-top: 20px;
  padding: 16px;
  background: #fef0f0;
  border-radius: 6px;
  border: 1px solid #fde2e2;
}

.error-message {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: #f56c6c;
  white-space: pre-wrap;
  margin: 0;
}

.statistics-section {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}
</style>