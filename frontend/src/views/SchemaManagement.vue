<template>
  <div class="schema-management">
    <div class="header">
      <h2 class="page-title">Schema管理</h2>
      <div class="header-actions">
        <el-select v-model="selectedConnectionId" placeholder="选择连接" style="width: 200px; margin-right: 10px;">
          <el-option v-for="conn in connections" :key="conn.id" :label="conn.name" :value="conn.id">
            <span>{{ conn.name }}</span>
            <el-tag :type="conn.status === 1 ? 'success' : 'danger'" size="small" style="margin-left: 8px;">
              {{ conn.status === 1 ? '正常' : '异常' }}
            </el-tag>
          </el-option>
        </el-select>
        <el-select v-model="selectedGraphName" placeholder="选择图数据库" style="width: 200px; margin-right: 10px;" :disabled="!selectedConnectionId">
          <el-option v-for="graph in graphs" :key="graph.name" :label="graph.name" :value="graph.name" />
        </el-select>
        <el-button type="primary" @click="refreshAll" :disabled="!selectedConnectionId || !selectedGraphName">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="primary" @click="showCreateVertexTypeDialog" :disabled="!selectedConnectionId || !selectedGraphName">
          <el-icon><Plus /></el-icon>
          新建点类型
        </el-button>
        <el-button type="primary" @click="showCreateEdgeTypeDialog" :disabled="!selectedConnectionId || !selectedGraphName">
          <el-icon><Plus /></el-icon>
          新建边类型
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="点类型" name="vertex">
        <el-table :data="vertexTypes" stripe border v-loading="vertexLoading">
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
          <el-table-column prop="description" label="描述" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" @click="editVertexType(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteVertexType(row.name)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="边类型" name="edge">
        <el-table :data="edgeTypes" stripe border v-loading="edgeLoading">
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
          <el-table-column prop="description" label="描述" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" @click="editEdgeType(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteEdgeType(row.name)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="索引管理" name="index">
        <IndexManager
          :selected-connection-id="selectedConnectionId"
          :selected-graph-name="selectedGraphName.value"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 创建/编辑点类型对话框 -->
    <el-dialog
      v-model="createVertexTypeDialogVisible"
      :title="editingVertexType ? '编辑点类型' : '新建点类型'"
      width="800px"
    >
      <el-form ref="vertexTypeFormRef" :model="vertexTypeForm" :rules="vertexTypeRules" label-width="100px">
        <el-form-item label="类型名称" required prop="name">
          <el-input v-model="vertexTypeForm.name" placeholder="请输入点类型名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="vertexTypeForm.description" placeholder="请输入描述" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="属性定义">
          <div class="property-list">
            <div v-for="(prop, index) in vertexTypeForm.properties" :key="index" class="property-item">
              <el-input v-model="prop.name" placeholder="属性名" style="width: 150px; margin-right: 10px;" />
              <el-select v-model="prop.dataType" placeholder="数据类型" style="width: 120px; margin-right: 10px;">
                <el-option label="字符串" value="STRING" />
                <el-option label="整数" value="INTEGER" />
                <el-option label="浮点数" value="FLOAT" />
                <el-option label="布尔值" value="BOOLEAN" />
                <el-option label="日期" value="DATE" />
              </el-select>
              <el-checkbox v-model="prop.required">必填</el-checkbox>
              <el-checkbox v-model="prop.indexed">索引</el-checkbox>
              <el-button type="danger" size="small" @click="removeVertexProperty(index)">删除</el-button>
            </div>
            <el-button type="primary" @click="addVertexProperty">添加属性</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVertexTypeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateVertexType" :loading="creatingVertexType">确定</el-button>
      </template>
    </el-dialog>

    <!-- 创建/编辑边类型对话框 -->
    <el-dialog
      v-model="createEdgeTypeDialogVisible"
      :title="editingEdgeType ? '编辑边类型' : '新建边类型'"
      width="800px"
    >
      <el-form ref="edgeTypeFormRef" :model="edgeTypeForm" :rules="edgeTypeRules" label-width="100px">
        <el-form-item label="类型名称" required prop="name">
          <el-input v-model="edgeTypeForm.name" placeholder="请输入边类型名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="edgeTypeForm.description" placeholder="请输入描述" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="属性定义">
          <div class="property-list">
            <div v-for="(prop, index) in edgeTypeForm.properties" :key="index" class="property-item">
              <el-input v-model="prop.name" placeholder="属性名" style="width: 150px; margin-right: 10px;" />
              <el-select v-model="prop.dataType" placeholder="数据类型" style="width: 120px; margin-right: 10px;">
                <el-option label="字符串" value="STRING" />
                <el-option label="整数" value="INTEGER" />
                <el-option label="浮点数" value="FLOAT" />
                <el-option label="布尔值" value="BOOLEAN" />
                <el-option label="日期" value="DATE" />
              </el-select>
              <el-checkbox v-model="prop.required">必填</el-checkbox>
              <el-checkbox v-model="prop.indexed">索引</el-checkbox>
              <el-button type="danger" size="small" @click="removeEdgeProperty(index)">删除</el-button>
            </div>
            <el-button type="primary" @click="addEdgeProperty">添加属性</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createEdgeTypeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateEdgeType" :loading="creatingEdgeType">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { connectionApi } from '../api/connection'
import { useSchemaStore } from '../stores/schema'
import { useGraphStore } from '@/stores/graph'
import IndexManager from '../components/schema/IndexManager.vue'

// 使用schema store
const schemaStore = useSchemaStore()

// 状态管理
const graphStore = useGraphStore()
const { selectedGraphName } = storeToRefs(graphStore)

// 使用路由
const router = useRouter()
const route = useRoute()

// 组件卸载标志
const isUnmounted = ref(false)

const connections = ref([])

// 从store获取状态，支持双向绑定
const selectedConnectionId = computed({
  get: () => schemaStore.selectedConnectionId,
  set: (value) => {
    schemaStore.selectedConnectionId = value
  }
})

const selectedGraphName = computed({
  get: () => schemaStore.selectedGraphName,
  set: (value) => {
    schemaStore.selectedGraphName = value
  }
})

const graphs = computed(() => schemaStore.graphs)
const vertexTypes = computed(() => schemaStore.vertexTypes)
const edgeTypes = computed(() => schemaStore.edgeTypes)
const vertexLoading = computed(() => schemaStore.vertexLoading)
const edgeLoading = computed(() => schemaStore.edgeLoading)

const activeTab = ref('vertex')

const createVertexTypeDialogVisible = ref(false)
const createEdgeTypeDialogVisible = ref(false)
const creatingVertexType = ref(false)
const creatingEdgeType = ref(false)
const editingVertexType = ref(false)
const editingEdgeType = ref(false)

const vertexTypeForm = reactive({
  name: '',
  type: 'VERTEX',
  description: '',
  properties: []
})

const edgeTypeForm = reactive({
  name: '',
  type: 'EDGE',
  description: '',
  properties: []
})

const vertexTypeFormRef = ref(null)
const edgeTypeFormRef = ref(null)

const vertexTypeRules = {
  name: [
    { required: true, message: '请输入点类型名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ]
}

const edgeTypeRules = {
  name: [
    { required: true, message: '请输入边类型名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ]
}

const loadConnections = async () => {
  try {
    const res = await connectionApi.list()
    connections.value = res.data || []
    if (connections.value.length > 0 && !selectedConnectionId.value) {
      selectedConnectionId.value = connections.value[0].id
    }
  } catch (error) {
    console.error('加载连接列表失败:', error)
    ElMessage.error('加载连接列表失败')
  }
}

const loadGraphs = async () => {
  await schemaStore.loadGraphs()
}

const loadVertexTypes = async () => {
  await schemaStore.loadVertexTypes()
}

const loadEdgeTypes = async () => {
  await schemaStore.loadEdgeTypes()
}

const refreshAll = () => {
  loadVertexTypes()
  loadEdgeTypes()
}

const showCreateVertexTypeDialog = () => {
  editingVertexType.value = false
  vertexTypeForm.name = ''
  vertexTypeForm.description = ''
  vertexTypeForm.properties = []
  createVertexTypeDialogVisible.value = true
}

const showCreateEdgeTypeDialog = () => {
  editingEdgeType.value = false
  edgeTypeForm.name = ''
  edgeTypeForm.description = ''
  edgeTypeForm.properties = []
  createEdgeTypeDialogVisible.value = true
}

const addVertexProperty = () => {
  vertexTypeForm.properties.push({
    name: '',
    dataType: 'STRING',
    required: false,
    indexed: false
  })
}

const removeVertexProperty = (index) => {
  vertexTypeForm.properties.splice(index, 1)
}

const addEdgeProperty = () => {
  edgeTypeForm.properties.push({
    name: '',
    dataType: 'STRING',
    required: false,
    indexed: false
  })
}

const removeEdgeProperty = (index) => {
  edgeTypeForm.properties.splice(index, 1)
}

const handleCreateVertexType = async () => {
  // 表单验证
  try {
    await vertexTypeFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查表单输入')
    return
  }
  
  // 属性验证
  const properties = vertexTypeForm.properties
  const propertyNames = new Set()
  for (let i = 0; i < properties.length; i++) {
    const prop = properties[i]
    if (!prop.name.trim()) {
      ElMessage.warning(`第 ${i + 1} 个属性名称不能为空`)
      return
    }
    if (propertyNames.has(prop.name)) {
      ElMessage.warning(`属性名称 "${prop.name}" 重复`)
      return
    }
    propertyNames.add(prop.name)
  }
  
  creatingVertexType.value = true
  try {
    await schemaStore.createVertexType(vertexTypeForm)
    ElMessage.success(editingVertexType.value ? '点类型更新成功' : '点类型创建成功')
    createVertexTypeDialogVisible.value = false
  } catch (error) {
    console.error('创建点类型失败:', error)
    ElMessage.error('创建点类型失败')
  } finally {
    creatingVertexType.value = false
  }
}

const handleCreateEdgeType = async () => {
  // 表单验证
  try {
    await edgeTypeFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查表单输入')
    return
  }
  
  // 属性验证
  const properties = edgeTypeForm.properties
  const propertyNames = new Set()
  for (let i = 0; i < properties.length; i++) {
    const prop = properties[i]
    if (!prop.name.trim()) {
      ElMessage.warning(`第 ${i + 1} 个属性名称不能为空`)
      return
    }
    if (propertyNames.has(prop.name)) {
      ElMessage.warning(`属性名称 "${prop.name}" 重复`)
      return
    }
    propertyNames.add(prop.name)
  }
  
  creatingEdgeType.value = true
  try {
    await schemaStore.createEdgeType(edgeTypeForm)
    ElMessage.success(editingEdgeType.value ? '边类型更新成功' : '边类型创建成功')
    createEdgeTypeDialogVisible.value = false
  } catch (error) {
    console.error('创建边类型失败:', error)
    ElMessage.error('创建边类型失败')
  } finally {
    creatingEdgeType.value = false
  }
}

const editVertexType = (type) => {
  editingVertexType.value = true
  vertexTypeForm.name = type.name
  vertexTypeForm.description = type.description || ''
  vertexTypeForm.properties = type.properties ? type.properties.map(p => ({
    name: p.name,
    dataType: p.dataType || 'STRING',
    required: p.required || false,
    indexed: p.indexed || false
  })) : []
  createVertexTypeDialogVisible.value = true
}

const editEdgeType = (type) => {
  editingEdgeType.value = true
  edgeTypeForm.name = type.name
  edgeTypeForm.description = type.description || ''
  edgeTypeForm.properties = type.properties ? type.properties.map(p => ({
    name: p.name,
    dataType: p.dataType || 'STRING',
    required: p.required || false,
    indexed: p.indexed || false
  })) : []
  createEdgeTypeDialogVisible.value = true
}

const deleteVertexType = async (labelName) => {
  try {
    await ElMessageBox.confirm(`确定删除点类型 "${labelName}" 吗？`, '警告', {
      type: 'warning'
    })
    
    await schemaStore.deleteVertexType(labelName)
    ElMessage.success('点类型删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除点类型失败:', error)
      ElMessage.error('删除点类型失败')
    }
  }
}

const deleteEdgeType = async (labelName) => {
  try {
    await ElMessageBox.confirm(`确定删除边类型 "${labelName}" 吗？`, '警告', {
      type: 'warning'
    })
    
    await schemaStore.deleteEdgeType(labelName)
    ElMessage.success('边类型删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除边类型失败:', error)
      ElMessage.error('删除边类型失败')
    }
  }
}

const stopConnectionWatch = watch(selectedConnectionId, async () => {
  if (isUnmounted.value) return
  schemaStore.clearData()
  if (selectedConnectionId.value) {
    await schemaStore.setSelection(selectedConnectionId.value, selectedGraphName.value)
  }
})

const stopGraphWatch = watch(selectedGraphName, async () => {
  if (isUnmounted.value) return
  if (selectedGraphName.value) {
    // 更新store的选中状态
    await schemaStore.setSelection(selectedConnectionId.value, selectedGraphName.value)
    // 加载数据
    refreshAll()
  }
})

// 处理路由参数
const handleRouteParams = () => {
  const { connectionId, graphName } = route.query
  
  if (connectionId && graphName) {
    // 如果路由中有参数，优先使用路由参数
    selectedConnectionId.value = connectionId
    selectedGraphName.value = graphName
    
    // 设置store选择状态并加载schema
    schemaStore.setSelection(connectionId, graphName)
    refreshAll()
  } else if (selectedGraphName.value && selectedConnectionId.value) {
    // 如果全局已选择图，则自动加载schema
    schemaStore.setSelection(selectedConnectionId.value, selectedGraphName.value)
    refreshAll()
  }
}

onMounted(async () => {
  await loadConnections()
  
  // 处理路由参数
  handleRouteParams()
})

// 组件卸载时清理
onUnmounted(() => {
  isUnmounted.value = true
  
  // 停止所有 watch
  stopConnectionWatch?.()
  stopGraphWatch?.()
  
  // 清理数据
  schemaStore.clearData()
})
</script>

<style scoped>
.schema-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.property-list {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  margin-top: 10px;
}

.property-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 8px;
  border-bottom: 1px dashed #e4e7ed;
}

.property-item:last-child {
  border-bottom: none;
}
</style>