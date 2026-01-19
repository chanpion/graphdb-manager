<template>
  <div class="data-modeling">
    <div class="header">
      <h2 class="page-title">图建模</h2>
      <div class="header-actions">
        <el-button-group>
          <el-button
            :type="viewMode === 'list' ? 'primary' : 'default'"
            @click="viewMode = 'list'"
            title="列表视图"
          >
            <el-icon><List /></el-icon>
            列表
          </el-button>
          <el-button
            :type="viewMode === 'graph' ? 'primary' : 'default'"
            @click="viewMode = 'graph'"
            title="图析视图"
          >
            <el-icon><Grid /></el-icon>
            图析
          </el-button>
        </el-button-group>

      </div>
    </div>

    <!-- 图析视图 -->
    <div v-if="viewMode === 'graph'" class="graph-view-container">
      <GraphView
        :vertex-types="vertexTypes"
        :edge-types="edgeTypes"
        @node-click="handleNodeClick"
        @edge-click="handleEdgeClick"
      />
    </div>

    <!-- 列表视图 -->
    <el-tabs v-else v-model="activeTab">
      <el-tab-pane label="点类型" name="vertex">
        <div class="tab-header-actions">
          <el-button type="primary" @click="loadVertexTypes" :disabled="!selectedGraphName">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button type="primary" @click="showCreateVertexTypeDialog">
            <el-icon><Plus /></el-icon>
            新建点类型
          </el-button>
        </div>
        <el-table :data="vertexTypes" stripe border v-loading="vertexLoading">
          <el-table-column prop="name" label="类型名称" width="150" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column label="属性">
            <template #default="{ row }">
              <div>
                <!-- 内置属性 -->
                <el-tag v-for="prop in builtInProperties" :key="prop.name" size="small" type="warning" style="margin-right: 5px;">
                  {{ prop.name }}:{{ prop.dataType }}
                </el-tag>
                <!-- 自定义属性 -->
                <el-tag v-for="prop in (row.properties || [])" :key="prop.name" size="small" style="margin-right: 5px;">
                  {{ prop.name }}:{{ prop.dataType }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" />
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <div class="operation-buttons">
                <el-button size="small" @click="editVertexType(row)" title="编辑">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button size="small" type="danger" @click="deleteVertexType(row.name)" title="删除">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="边类型" name="edge">
        <div class="tab-header-actions">
          <el-button type="primary" @click="loadEdgeTypes" :disabled="!selectedGraphName">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button type="primary" @click="showCreateEdgeTypeDialog">
            <el-icon><Plus /></el-icon>
            新建边类型
          </el-button>
        </div>
        <el-table :data="edgeTypes" stripe border v-loading="edgeLoading">
          <el-table-column prop="name" label="类型名称" width="150" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="sourceVertexType" label="起点类型" width="120" />
          <el-table-column prop="targetVertexType" label="终点类型" width="120" />
          <el-table-column label="属性">
            <template #default="{ row }">
              <div>
                <!-- 内置属性 -->
                <el-tag v-for="prop in builtInProperties" :key="prop.name" size="small" type="warning" style="margin-right: 5px;">
                  {{ prop.name }}:{{ prop.dataType }}
                </el-tag>
                <!-- 自定义属性 -->
                <el-tag v-for="prop in (row.properties || [])" :key="prop.name" size="small" style="margin-right: 5px;">
                  {{ prop.name }}:{{ prop.dataType }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" />
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <div class="operation-buttons">
                <el-button size="small" @click="editEdgeType(row)" title="编辑">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button size="small" type="danger" @click="deleteEdgeType(row.name)" title="删除">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="索引" name="index">
        <div class="tab-header-actions">
          <el-button type="primary" @click="loadIndexes" :disabled="!selectedGraphName">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button type="primary" @click="showCreateIndexDialog">
            <el-icon><Plus /></el-icon>
            新建索引
          </el-button>
        </div>
        <el-table :data="indexes" stripe border v-loading="indexLoading">
          <el-table-column prop="name" label="索引名称" width="200" />
          <el-table-column prop="type" label="索引类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getIndexTypeTag(row.type)">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="字段" width="250">
            <template #default="{ row }">
              <el-tag v-for="field in (row.fields || [])" :key="field" size="small" style="margin-right: 5px;">
                {{ field }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTag(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="description" label="描述" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <div class="operation-buttons">
                <el-button size="small" @click="rebuildIndex(row.name)" :disabled="row.status !== 'ACTIVE'" title="重建">
                  <el-icon><Tools /></el-icon>
                </el-button>
                <el-button size="small" type="danger" @click="deleteIndex(row.name)" :disabled="row.status === 'BUILDING'" title="删除">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

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

    <el-dialog
      v-model="createEdgeTypeDialogVisible"
      :title="editingEdgeType ? '编辑边类型' : '新建边类型'"
      width="800px"
    >
      <el-form ref="edgeTypeFormRef" :model="edgeTypeForm" :rules="edgeTypeRules" label-width="100px">
        <el-form-item label="类型名称" required prop="name">
          <el-input v-model="edgeTypeForm.name" placeholder="请输入边类型名称" />
        </el-form-item>
        <el-form-item label="起点类型" required>
          <el-select v-model="edgeTypeForm.sourceVertexType" placeholder="请选择起点类型" style="width: 100%;">
            <el-option
              v-for="vertex in vertexTypes"
              :key="vertex.name"
              :label="vertex.name"
              :value="vertex.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="终点类型" required>
          <el-select v-model="edgeTypeForm.targetVertexType" placeholder="请选择终点类型" style="width: 100%;">
            <el-option
              v-for="vertex in vertexTypes"
              :key="vertex.name"
              :label="vertex.name"
              :value="vertex.name"
            />
          </el-select>
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

    <el-dialog
      v-model="createIndexDialogVisible"
      title="新建索引"
      width="600px"
    >
      <el-form ref="indexFormRef" :model="indexForm" :rules="indexRules" label-width="100px">
        <el-form-item label="索引名称" required prop="name">
          <el-input v-model="indexForm.name" placeholder="请输入索引名称" />
        </el-form-item>
        <el-form-item label="索引类型" required prop="type">
          <el-select v-model="indexForm.type" placeholder="请选择索引类型" style="width: 100%;">
            <el-option label="唯一索引" value="UNIQUE" />
            <el-option label="复合索引" value="COMPOUND" />
            <el-option label="全文索引" value="FULLTEXT" />
            <el-option label="空间索引" value="SPATIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="索引字段" required>
          <div class="field-list">
            <div v-for="(field, index) in indexForm.fields" :key="index" class="field-item">
              <el-input v-model="indexForm.fields[index]" placeholder="字段名" style="width: 200px; margin-right: 10px;" />
              <el-button type="danger" size="small" @click="removeIndexField(index)">删除</el-button>
            </div>
            <el-button type="primary" @click="addIndexField">添加字段</el-button>
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="indexForm.description" placeholder="请输入描述" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createIndexDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateIndex" :loading="creatingIndex">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus, List, Grid, Edit, Delete, Tools } from '@element-plus/icons-vue'
import { connectionApi } from '../api/connection'
import { storeToRefs } from 'pinia'
import { graphApi } from '../api/graph'
import { useGraphStore } from '@/stores/graph'
import GraphView from '../components/modeling/GraphView.vue'

// 获取路由
const route = useRoute()
const router = useRouter()

// 组件卸载标志
const isUnmounted = ref(false)

// 状态管理
const graphStore = useGraphStore()

const connections = ref([])
// 使用全局状态管理连接ID
const { selectedConnectionId, selectedGraphName } = storeToRefs(graphStore)
const activeTab = ref('vertex')
const viewMode = ref('list')

const vertexTypes = ref([])
const edgeTypes = ref([])
const vertexLoading = ref(false)
const edgeLoading = ref(false)
const indexes = ref([])
const indexLoading = ref(false)

const createVertexTypeDialogVisible = ref(false)
const createEdgeTypeDialogVisible = ref(false)
const createIndexDialogVisible = ref(false)
const creatingVertexType = ref(false)
const creatingEdgeType = ref(false)
const creatingIndex = ref(false)
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
  properties: [],
  sourceVertexType: '',
  targetVertexType: ''
})

const indexForm = reactive({
  name: '',
  type: 'UNIQUE',
  fields: [],
  description: ''
})

const vertexTypeFormRef = ref(null)
const edgeTypeFormRef = ref(null)
const indexFormRef = ref(null)

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

const indexRules = {
  name: [
    { required: true, message: '请输入索引名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择索引类型', trigger: 'change' }
  ]
}

// 内置属性
const builtInProperties = [
  {
    name: 'uid',
    dataType: 'STRING',
    required: true,
    indexed: true,
    unique: true,
    description: '唯一标识符'
  }
]

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

const loadVertexTypes = async () => {
  if (!selectedConnectionId.value || !selectedGraphName.value) return

  vertexLoading.value = true
  try {
    const res = await graphApi.getVertexTypes(selectedConnectionId.value, selectedGraphName.value)
    vertexTypes.value = res.data || []
  } catch (error) {
    console.error('加载点类型列表失败:', error)
    ElMessage.error('加载点类型列表失败')
  } finally {
    vertexLoading.value = false
  }
}

const loadEdgeTypes = async () => {
  if (!selectedConnectionId.value || !selectedGraphName.value) return

  edgeLoading.value = true
  try {
    const res = await graphApi.getEdgeTypes(selectedConnectionId.value, selectedGraphName.value)
    edgeTypes.value = res.data || []
  } catch (error) {
    console.error('加载边类型列表失败:', error)
    ElMessage.error('加载边类型列表失败')
  } finally {
    edgeLoading.value = false
  }
}

const loadIndexes = async () => {
  if (!selectedConnectionId.value || !selectedGraphName.value) return

  indexLoading.value = true
  try {
    const res = await graphApi.listIndexes(selectedConnectionId.value, selectedGraphName.value)
    indexes.value = res.data || []
  } catch (error) {
    console.error('加载索引列表失败:', error)
    ElMessage.error('加载索引列表失败')
  } finally {
    indexLoading.value = false
  }
}

const showCreateIndexDialog = () => {
  indexForm.name = ''
  indexForm.type = 'UNIQUE'
  indexForm.fields = []
  indexForm.description = ''
  createIndexDialogVisible.value = true
}

const handleCreateIndex = async () => {
  if (!indexForm.name || indexForm.fields.length === 0) {
    ElMessage.warning('请填写索引名称和字段')
    return
  }

  creatingIndex.value = true
  try {
    await graphApi.createIndex(selectedConnectionId.value, selectedGraphName.value, indexForm)
    ElMessage.success('索引创建成功')
    createIndexDialogVisible.value = false
    loadIndexes()
  } catch (error) {
    console.error('创建索引失败:', error)
    ElMessage.error('创建索引失败')
  } finally {
    creatingIndex.value = false
  }
}

const rebuildIndex = async (indexName) => {
  try {
    await ElMessageBox.confirm('确定要重建此索引吗？', '提示', {
      type: 'warning'
    })
    await graphApi.rebuildIndex(selectedConnectionId.value, selectedGraphName.value, indexName)
    ElMessage.success('索引重建已开始')
    loadIndexes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重建索引失败:', error)
      ElMessage.error('重建索引失败')
    }
  }
}

const deleteIndex = async (indexName) => {
  try {
    await ElMessageBox.confirm('确定要删除此索引吗？', '提示', {
      type: 'warning'
    })
    await graphApi.deleteIndex(selectedConnectionId.value, selectedGraphName.value, indexName)
    ElMessage.success('索引删除成功')
    loadIndexes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除索引失败:', error)
      ElMessage.error('删除索引失败')
    }
  }
}

const getIndexTypeTag = (type) => {
  const tagMap = {
    'UNIQUE': 'success',
    'COMPOUND': 'primary',
    'FULLTEXT': 'warning',
    'SPATIAL': 'info'
  }
  return tagMap[type] || ''
}

const getStatusTag = (status) => {
  const tagMap = {
    'ACTIVE': 'success',
    'BUILDING': 'warning',
    'FAILED': 'danger'
  }
  return tagMap[status] || ''
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
  edgeTypeForm.sourceVertexType = ''
  edgeTypeForm.targetVertexType = ''
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

const addIndexField = () => {
  indexForm.fields.push('')
}

const removeIndexField = (index) => {
  indexForm.fields.splice(index, 1)
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
    await graphApi.createVertexType(selectedConnectionId.value, selectedGraphName.value, vertexTypeForm)
    ElMessage.success(editingVertexType.value ? '点类型更新成功' : '点类型创建成功')
    createVertexTypeDialogVisible.value = false
    loadVertexTypes()
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
    await graphApi.createEdgeType(selectedConnectionId.value, selectedGraphName.value, edgeTypeForm)
    ElMessage.success(editingEdgeType.value ? '边类型更新成功' : '边类型创建成功')
    createEdgeTypeDialogVisible.value = false
    loadEdgeTypes()
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

    await graphApi.deleteVertexType(selectedConnectionId.value, selectedGraphName.value, labelName)
    ElMessage.success('点类型删除成功')
    loadVertexTypes()
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

    await graphApi.deleteEdgeType(selectedConnectionId.value, selectedGraphName.value, labelName)
    ElMessage.success('边类型删除成功')
    loadEdgeTypes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除边类型失败:', error)
      ElMessage.error('删除边类型失败')
    }
  }
}

// 图析视图节点点击处理
const handleNodeClick = (node) => {
  console.log('节点点击:', node)
  // 可以在这里显示节点详情或执行其他操作
}

// 图析视图边点击处理
const handleEdgeClick = (edge) => {
  console.log('边点击:', edge)
  // 可以在这里显示边详情或执行其他操作
}

const stopConnectionWatch = watch(selectedConnectionId, () => {
  if (isUnmounted.value) return
  vertexTypes.value = []
  edgeTypes.value = []
})

const stopGraphWatch = watch(selectedGraphName, () => {
  if (isUnmounted.value) return
  vertexTypes.value = []
  edgeTypes.value = []
  indexes.value = []
  if (selectedGraphName.value) {
    loadVertexTypes()
    loadEdgeTypes()
    loadIndexes()
  }
})

onMounted(async () => {
  await loadConnections()

  // 从路由参数中获取连接ID和图名称
  const { connectionId, graphName } = route.query

  if (connectionId) {
    selectedConnectionId.value = connectionId
  }

  if (graphName) {
    // 设置全局状态
    selectedGraphName.value = graphName
    // 加载该图的类型数据
    await Promise.all([loadVertexTypes(), loadEdgeTypes(), loadIndexes()])
  }
})

// 组件卸载时清理
onUnmounted(() => {
  isUnmounted.value = true
  
  // 停止所有 watch
  stopConnectionWatch?.()
  stopGraphWatch?.()
  
  // 清理数据
  vertexTypes.value = []
  edgeTypes.value = []
  indexes.value = []
})
</script>

<style scoped>
.data-modeling {
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 40px);
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-shrink: 0;
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

.graph-view-container {
  flex: 1;
  min-height: 0;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
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

.tab-header-actions {
  margin-bottom: 15px;
  display: flex;
  justify-content: flex-end;
}

.field-list {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  margin-top: 10px;
}

.field-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 8px;
  border-bottom: 1px dashed #e4e7ed;
}

.field-item:last-child {
  border-bottom: none;
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
</style>