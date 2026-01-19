<template>
  <div class="graph-management">
    <div class="header">
      <h2 class="page-title">图管理</h2>
      <div class="header-actions">
        <el-select v-model="selectedConnectionId" placeholder="选择连接" style="width: 200px; margin-right: 10px;" clearable>
          <el-option
            label="全部连接"
            value=""
          >
            <div class="option-content">
              <span class="option-label">全部连接</span>
              <el-tag type="info" size="small" style="margin-left: 8px;">
                全部
              </el-tag>
            </div>
          </el-option>
          <el-option
            v-for="conn in connections"
            :key="conn.id"
            :label="conn.name"
            :value="conn.id"
          >
            <span>{{ conn.name }}</span>
            <el-tag :type="conn.status === 1 ? 'success' : 'danger'" size="small" style="margin-left: 8px;">
              {{ conn.status === 1 ? '正常' : '异常' }}
            </el-tag>
          </el-option>
        </el-select>
        <el-select v-model="selectedSourceType" placeholder="图来源" style="width: 150px; margin-right: 10px;" clearable @change="resetPagination">
          <el-option
            v-for="type in sourceTypes"
            :key="type.value"
            :label="type.label"
            :value="type.value"
          >
            <el-tag :type="type.tagType" size="small">
              {{ type.label }}
            </el-tag>
          </el-option>
        </el-select>
        <el-button type="primary" @click="loadGraphs" :disabled="connections.length === 0">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="showCreateDialog" :disabled="!selectedConnectionId">
          <el-icon><Plus /></el-icon>
          新建图
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="graph-grid">
      <GraphCard
        v-for="graph in displayGraphs"
        :key="graph.name"
        :graph="graph"
        @open="openGraph"
        @edit="handleEdit"
        @detail="showDetail"
        @browse="browseGraph"
        @delete="deleteGraph"
      />
    </div>

    <div v-if="loadingMore" class="loading-more">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="!hasMore && displayGraphs.length > 0" class="no-more">
      <span>没有更多了</span>
    </div>

    <el-empty v-if="!loading && !loadingMore && displayGraphs.length === 0" description="暂无图数据，请选择连接后点击刷新" />

    <el-dialog
      v-model="createDialogVisible"
      title="新建图"
      width="500px"
    >
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="图名称" required>
          <el-input v-model="createForm.graphName" placeholder="请输入图名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑图信息"
      width="500px"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="图名称" required>
          <el-input v-model="editForm.graphName" placeholder="请输入图名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入图描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateEdit" :loading="editing">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="schemaDialogVisible"
      title="图Schema"
      width="800px"
    >
      <div v-if="currentSchema">
        <el-descriptions title="基本信息" border :column="2">
          <el-descriptions-item label="图名称">{{ currentSchema.graphName }}</el-descriptions-item>
          <el-descriptions-item label="数据库类型">{{ currentSchema.databaseType }}</el-descriptions-item>
        </el-descriptions>
        
        <div v-if="currentSchema.vertexLabels && currentSchema.vertexLabels.length > 0">
          <h3 style="margin-top: 20px;">点类型</h3>
          <el-table :data="currentSchema.vertexLabels" stripe border>
            <el-table-column prop="name" label="类型名称" width="150" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column label="属性">
              <template #default="{ row }">
                <div v-if="row.properties && row.properties.length > 0">
                  <el-tag v-for="prop in row.properties" :key="prop.name" size="small" style="margin-right: 5px;">
                    {{ prop.name }}:{{ prop.dataType }}
                  </el-tag>
                </div>
                <span v-else>无属性</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div v-if="currentSchema.edgeLabels && currentSchema.edgeLabels.length > 0">
          <h3 style="margin-top: 20px;">边类型</h3>
          <el-table :data="currentSchema.edgeLabels" stripe border>
            <el-table-column prop="name" label="类型名称" width="150" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column label="属性">
              <template #default="{ row }">
                <div v-if="row.properties && row.properties.length > 0">
                  <el-tag v-for="prop in row.properties" :key="prop.name" size="small" style="margin-right: 5px;">
                    {{ prop.name }}:{{ prop.dataType }}
                  </el-tag>
                </div>
                <span v-else>无属性</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div v-if="(!currentSchema.vertexLabels || currentSchema.vertexLabels.length === 0) && 
                  (!currentSchema.edgeLabels || currentSchema.edgeLabels.length === 0)">
          <el-empty description="暂无Schema信息" />
        </div>
      </div>
      <div v-else>
        <el-empty description="加载中..." />
      </div>
    </el-dialog>

    <el-dialog
      v-model="detailDialogVisible"
      title="图详情"
      width="600px"
    >
      <div v-if="currentGraphDetail">
        <el-descriptions title="图信息" border :column="1">
          <el-descriptions-item label="图名称">{{ currentGraphDetail.name }}</el-descriptions-item>
          <el-descriptions-item label="数据库类型">{{ currentGraphDetail.databaseType || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="节点数">{{ currentGraphDetail.vertexCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="边数">{{ currentGraphDetail.edgeCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentGraphDetail.status === 'NORMAL' ? 'success' : 'warning'">
              {{ currentGraphDetail.status === 'NORMAL' ? '正常' : '归档' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentGraphDetail.createdAt ? new Date(currentGraphDetail.createdAt).toLocaleString() : '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else>
        <el-empty description="加载中..." />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, Loading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import GraphCard from '../components/graph/GraphCard.vue'
import { connectionApi } from '../api/connection'
import { useGraphStore } from '../stores/graph'

// 使用路由
const router = useRouter()

// 使用graph store
const graphStore = useGraphStore()

// 本地状态
const connections = ref([])
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const schemaDialogVisible = ref(false)
const creating = ref(false)
const editing = ref(false)
const currentSchema = ref(null)
const detailDialogVisible = ref(false)
const currentGraphDetail = ref(null)
const selectedSourceType = ref('')

// 分页状态
const pageSize = computed(() => {
  // 根据屏幕宽度计算每页显示2行的数量
  const width = window.innerWidth
  if (width >= 1400) return 8 // 4列 x 2行 = 8
  if (width >= 1024) return 6 // 3列 x 2行 = 6
  if (width >= 768) return 4 // 2列 x 2行 = 4
  return 2 // 1列 x 2行 = 2
})
const currentPage = ref(1)
const hasMore = ref(true)
const loadingMore = ref(false)

// 防抖函数
const debounce = (fn, delay) => {
  let timer = null
  return (...args) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

// 图来源类型配置
const sourceTypes = [
  { label: '平台创建', value: 'PLATFORM', tagType: 'success' },
  { label: '图数据库已有', value: 'EXISTING', tagType: 'info' }
]

const createForm = reactive({
  graphName: ''
})

const editForm = reactive({
  graphName: '',
  description: '',
  originalGraphName: ''
})

// 使用storeToRefs正确解构store
const { selectedConnectionId, selectedGraphName, graphs, loading } = storeToRefs(graphStore)

// 根据图来源筛选图表
const filteredGraphs = computed(() => {
  const graphsList = graphs.value || []
  if (!selectedSourceType.value) {
    return graphsList
  }
  return graphsList.filter(graph => graph.sourceType === selectedSourceType.value)
})

// 分页显示的图表
const displayGraphs = computed(() => {
  const start = 0
  const end = currentPage.value * pageSize.value
  const filteredList = filteredGraphs.value || []
  const paginated = filteredList.slice(start, end)
  hasMore.value = end < filteredList.length
  return paginated
})

// 滚动加载更多（带防抖）
const handleScroll = debounce(() => {
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = window.innerHeight
  const threshold = 100 // 距离底部100px时触发

  if (!loadingMore.value && hasMore.value && (scrollHeight - scrollTop - clientHeight < threshold)) {
    loadMoreGraphs()
  }
}, 100)

const loadMoreGraphs = () => {
  if (loadingMore.value || !hasMore.value) {
    return
  }

  loadingMore.value = true
  setTimeout(() => {
    currentPage.value++
    loadingMore.value = false
  }, 300)
}

// 重置分页
const resetPagination = () => {
  currentPage.value = 1
  hasMore.value = true
  loadingMore.value = false
}

const loadConnections = async () => {
  try {
    const res = await connectionApi.list()
    connections.value = res.data || []
    // 默认不选择任何连接，显示全部连接的图
    if (connections.value.length > 0 && selectedConnectionId.value === undefined) {
      selectedConnectionId.value = ''
    }
  } catch (error) {
    console.error('加载连接列表失败:', error)
    ElMessage.error('加载连接列表失败')
  }
}

const loadGraphs = async () => {
  if (selectedConnectionId.value) {
    // 加载特定连接的图
    await graphStore.loadGraphs(selectedSourceType.value)
  } else {
    // 加载所有连接的图
    await graphStore.loadAllGraphs(selectedSourceType.value)
  }
}

const showCreateDialog = () => {
  createForm.graphName = ''
  createDialogVisible.value = true
}

const handleCreate = async () => {
  if (!createForm.graphName.trim()) {
    ElMessage.warning('请输入图名称')
    return
  }
  
  creating.value = true
  try {
    await graphStore.createGraph({ graphName: createForm.graphName })
    ElMessage.success('图创建成功')
    createDialogVisible.value = false
  } catch (error) {
    console.error('创建图失败:', error)
    ElMessage.error('创建图失败')
  } finally {
    creating.value = false
  }
}

const handleUpdateEdit = async () => {
  if (!editForm.graphName.trim()) {
    ElMessage.warning('请输入图名称')
    return
  }
  
  editing.value = true
  try {
    // 这里需要调用更新图的API，传递图名称和描述
    // 暂时使用模拟方式
    await graphStore.updateGraph({
      oldGraphName: editForm.originalGraphName,
      newGraphName: editForm.graphName,
      description: editForm.description
    })
    ElMessage.success('图信息更新成功')
    editDialogVisible.value = false
    // 刷新图列表
    await loadGraphs()
  } catch (error) {
    console.error('更新图失败:', error)
    ElMessage.error('更新图失败')
  } finally {
    editing.value = false
  }
}

const showDetail = (graph) => {
  // 设置当前选中的连接和图
  // 检查graph是否为对象，防止在字符串上使用可选链
  if (typeof graph !== 'object' || graph === null) {
    console.warn('showDetail received non-object parameter:', graph)
    return
  }
  
  selectedConnectionId.value = graph?.connectionId || selectedConnectionId.value
  selectedGraphName.value = graph?.name

  currentGraphDetail.value = {
    ...graph,
    connectionId: graph?.connectionId || selectedConnectionId.value,
    databaseType: graph?.databaseType || '未知'
  }
  detailDialogVisible.value = true
}

const openGraph = (graph) => {
  // 设置当前选中的连接和图
  selectedConnectionId.value = graph.connectionId || selectedConnectionId.value
  selectedGraphName.value = graph.name

  // 跳转到图建模页面
  router.push({
    name: 'DataModeling',
    query: {
      connectionId: graph.connectionId || selectedConnectionId.value,
      graphName: graph.name
    }
  })
}

const browseGraph = (graph) => {
  // 设置当前选中的连接和图
  selectedConnectionId.value = graph.connectionId || selectedConnectionId.value
  selectedGraphName.value = graph.name

  // 跳转到图数据页面
  router.push({
    name: 'DataExplorer',
    query: {
      connectionId: graph.connectionId || selectedConnectionId.value,
      graphName: graph.name
    }
  })
}

const handleEdit = (graphData) => {
  // 支持传入图对象或图名称
  const graph = typeof graphData === 'string' ? { name: graphData } : graphData
  const graphName = graph?.name
  const description = graph?.description || ''
  
  // 设置编辑表单的值
  editForm.graphName = graphName || ''
  editForm.description = description
  editForm.originalGraphName = graphName || ''
  
  // 打开编辑对话框
  editDialogVisible.value = true
}

const deleteGraph = async (graph) => {
  // 支持传入图对象或图名称
  const graphName = typeof graph === 'string' ? graph : graph?.name

  try {
    await ElMessageBox.confirm(`确定删除图 "${graphName}" 吗？`, '警告', {
      type: 'warning'
    })

    await graphStore.deleteGraph(graphName)
    ElMessage.success('图删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除图失败:', error)
      ElMessage.error('删除图失败')
    }
  }
}

// 监听连接变化，加载对应的图
watch(selectedConnectionId, async (newVal, oldVal) => {
  if (newVal !== oldVal) {
    graphStore.clearData()
    resetPagination()
    // 无论是选择特定连接还是全部连接（空值），都加载图
    await loadGraphs()
  }
})

// 监听窗口大小变化，重置分页
watch(() => window.innerWidth, () => {
  resetPagination()
})

onMounted(async () => {
  await loadConnections()
  // 组件挂载后加载所有图（因为默认selectedConnectionId为空）
  await loadGraphs()
  // 等待DOM更新后添加滚动监听
  await nextTick()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  // 移除window滚动监听
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.graph-management {
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

.graph-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.loading-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.loading-more .el-icon {
  font-size: 20px;
}

.no-more::before,
.no-more::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e8e8e8;
}

.no-more span {
  padding: 0 20px;
}

@media (max-width: 1400px) {
  .graph-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 18px;
  }
}

@media (max-width: 1024px) {
  .graph-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .graph-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>