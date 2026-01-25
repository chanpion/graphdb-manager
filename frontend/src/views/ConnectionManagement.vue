<template>
  <div class="connection-management">
    <div class="header">
      <h2 class="page-title">连接管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        新增连接
      </el-button>
    </div>

    <div class="connection-grid">
      <ConnectionCard
        v-for="connection in connections"
        :key="connection.id"
        :connection="connection"
        @test="testConnection"
        @edit="editConnection"
        @delete="deleteConnection"
      />
    </div>

    <el-empty v-if="connections.length === 0" description="暂无连接，点击右上角创建新连接" />

    <el-dialog
      v-model="createDialogVisible"
      :title="isEditMode ? '编辑连接' : '新增连接'"
      width="700px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="连接名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入连接名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="数据库类型" prop="databaseType">
          <el-select v-model="form.databaseType" placeholder="请选择数据库类型" @change="handleTypeChange">
            <el-option label="Neo4j" value="NEO4J" />
            <el-option label="NebulaGraph" value="NEBULA" />
            <el-option label="JanusGraph" value="JANUS" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="form.host" placeholder="请输入主机地址，如 localhost 或 127.0.0.1" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入连接描述（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- JSON参数配置 -->
        <el-form-item label="高级参数">
          <div class="json-params-section">
            <el-alert
              :title="jsonParamsTip"
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 12px;"
            />
            <el-input
              v-model="form.jsonParams"
              type="textarea"
              :rows="6"
              placeholder='请输入JSON格式的额外参数，例如：{"timeout": 30000, "poolSize": 10}'
              resize="vertical"
              class="json-input"
            />
            <div class="json-actions">
              <el-button size="small" @click="formatJson" type="primary" plain>
                格式化JSON
              </el-button>
              <el-button size="small" @click="validateJson" type="success" plain>
                验证JSON
              </el-button>
              <el-button size="small" @click="loadDefaultParams" type="info" plain>
                加载默认参数
              </el-button>
            </div>
            <div v-if="jsonError" class="json-error">
              <el-icon color="#F56C6C"><Warning /></el-icon>
              <span style="color: #F56C6C; margin-left: 6px;">{{ jsonError }}</span>
            </div>
          </div>
        </el-form-item>

        <!-- JanusGraph 特有字段 -->
        <template v-if="form.databaseType === 'JANUS'">
          <el-divider content-position="left">JanusGraph 配置</el-divider>
          <el-alert
            title="JanusGraph仅支持Cassandra和HBase作为存储后端，索引功能通过JSON参数配置"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px;"
          />
          <el-form-item label="存储后端" prop="storageBackend">
            <el-select v-model="form.storageBackend" placeholder="请选择存储后端">
              <el-option label="CQL (Cassandra)" value="cql" />
              <el-option label="HBase" value="hbase" />
            </el-select>
          </el-form-item>
          <el-form-item label="存储主机">
            <el-input v-model="form.storageHost" placeholder="请输入存储主机地址" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Warning } from '@element-plus/icons-vue'
import ConnectionCard from '../components/connection/ConnectionCard.vue'
import { connectionApi } from '../api/connection'

const connections = ref([])
const createDialogVisible = ref(false)
const isEditMode = ref(false)
const editingId = ref('')
const formRef = ref(null)

// 默认端口配置
const defaultPorts = {
  'NEO4J': 7687,
  'NEBULA': 9669,
  'JANUS': 8182
}

const form = reactive({
  name: '',
  databaseType: 'NEO4J',
  host: '',
  port: 7687,
  username: '',
  password: '',
  description: '',
  jsonParams: '', // JSON格式参数
  // JanusGraph 特有字段
  storageBackend: 'cql',
  storageHost: ''
})

const jsonError = ref('')

// 表单验证规则
const formRules = computed(() => {
  const baseRules = {
    name: [
      { required: true, message: '请输入连接名称', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    databaseType: [
      { required: true, message: '请选择数据库类型', trigger: 'change' }
    ],
    host: [
      { required: true, message: '请输入主机地址', trigger: 'blur' },
      { pattern: /^[\w.-]+$/, message: '请输入有效的主机地址', trigger: 'blur' }
    ],
    port: [
      { required: true, message: '请输入端口号', trigger: 'blur' },
      { type: 'number', min: 1, max: 65535, message: '端口范围在 1-65535', trigger: 'blur' }
    ],
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' }
    ]
  }

  // JanusGraph 需要额外验证（仅存储后端）
  if (form.databaseType === 'JANUS') {
    baseRules.storageBackend = [
      { required: true, message: '请选择存储后端', trigger: 'change' }
    ]
  }

  return baseRules
})

// 数据库字段标签和占位符已统一为"数据库名称"

// JSON参数提示信息
const jsonParamsTip = computed(() => {
  const tips = {
    'NEO4J': 'Neo4j数据库额外参数，如加密设置、连接池配置等',
    'NEBULA': 'NebulaGraph数据库额外参数，如超时设置、重试策略等',
    'JANUS': 'JanusGraph数据库额外参数，如存储配置、性能优化等'
  }
  return tips[form.databaseType] || '数据库额外参数配置'
})

// 数据库类型变化处理
const handleTypeChange = (type) => {
  form.port = defaultPorts[type] || 7687
  // 清空 JanusGraph 特有字段
  if (type !== 'JANUS') {
    form.storageBackend = 'cql'
    form.storageHost = ''
  }
}

// JSON参数处理方法
const formatJson = () => {
  if (!form.jsonParams.trim()) {
    ElMessage.warning('请输入JSON内容')
    return
  }
  
  try {
    const parsed = JSON.parse(form.jsonParams)
    form.jsonParams = JSON.stringify(parsed, null, 2)
    jsonError.value = ''
    ElMessage.success('JSON格式化成功')
  } catch (e) {
    jsonError.value = 'JSON格式错误：' + e.message
    ElMessage.error('JSON格式错误，请检查')
  }
}

const validateJson = () => {
  if (!form.jsonParams.trim()) {
    ElMessage.info('JSON内容为空')
    return
  }
  
  try {
    JSON.parse(form.jsonParams)
    jsonError.value = ''
    ElMessage.success('JSON验证通过')
  } catch (e) {
    jsonError.value = 'JSON验证失败：' + e.message
    ElMessage.error('JSON验证失败')
  }
}

const loadDefaultParams = () => {
  const defaults = {
    'NEO4J': {
      encryption: false,
      maxConnectionPoolSize: 100,
      connectionTimeout: 30000,
      maxTransactionRetryTime: 15000
    },
    'NEBULA': {
      timeout: 30000,
      retry: 3,
      enableSSL: false,
      minConnectionPoolSize: 0,
      maxConnectionPoolSize: 10
    },
    'JANUS': {
      storage: {
        hostname: 'localhost',
        port: 9042
      }
    }
  }
  
  form.jsonParams = JSON.stringify(defaults[form.databaseType] || {}, null, 2)
  jsonError.value = ''
  ElMessage.success('已加载默认参数')
}

const loadConnections = async () => {
  try {
    const res = await connectionApi.list()
    // 响应拦截器已自动提取 Result.data，res 直接是数组
    connections.value = Array.isArray(res) ? res : []
  } catch (error) {
    console.error('加载连接列表失败:', error)
  }
}

const showCreateDialog = () => {
  isEditMode.value = false
  editingId.value = ''
  resetForm()
  createDialogVisible.value = true
}

const handleCreate = async () => {
  if (!formRef.value) return

  try {
    // 表单验证
    await formRef.value.validate()
    
    // JSON参数验证
    if (form.jsonParams && form.jsonParams.trim()) {
      try {
        JSON.parse(form.jsonParams)
      } catch (e) {
        ElMessage.error('JSON参数格式错误，请检查')
        return
      }
    }

    if (isEditMode.value && editingId.value) {
      await connectionApi.update(editingId.value, form)
      ElMessage.success('连接编辑成功')
    } else {
      await connectionApi.create(form)
      ElMessage.success('连接创建成功')
    }
    createDialogVisible.value = false
    await loadConnections()
    resetForm()
    isEditMode.value = false
    editingId.value = ''
  } catch (error) {
    if (error && typeof error === 'boolean') {
      // 表单验证失败
      ElMessage.warning('请检查表单填写是否正确')
    } else {
      ElMessage.error(isEditMode.value ? '连接编辑失败' : '连接创建失败')
    }
  }
}

const testConnection = async (row) => {
  try {
    await connectionApi.testConnection(row.id)
    ElMessage.success('连接测试成功')
    await loadConnections() // 刷新状态
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

const editConnection = (row) => {
  isEditMode.value = true
  editingId.value = row.id
  form.name = row.name
  form.databaseType = row.databaseType
  form.host = row.host
  form.port = row.port
  form.username = row.username || ''
  form.password = '' // 密码不填充，用户需要重新输入
  form.description = row.description || ''
  form.jsonParams = row.jsonParams || '' // JSON参数
  // JanusGraph 特有字段
  form.storageBackend = row.storageBackend || 'cql'
  form.storageHost = row.storageHost || ''
  createDialogVisible.value = true
}

const deleteConnection = async (id) => {
  try {
    await connectionApi.delete(id)
    ElMessage.success('连接删除成功')
    await loadConnections()
  } catch (error) {
    ElMessage.error('连接删除失败')
  }
}

const resetForm = () => {
  form.name = ''
  form.databaseType = 'NEO4J'
  form.host = ''
  form.port = defaultPorts['NEO4J']
  form.username = ''
  form.password = ''
  form.description = ''
  form.jsonParams = '' // 清空JSON参数
  // JanusGraph 特有字段
  form.storageBackend = 'cql'
  form.storageHost = ''
}

loadConnections()
</script>

<style scoped>
.connection-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.connection-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 1400px) {
  .connection-grid {
    grid-template-columns: repeat(3, minmax(280px, 1fr));
  }
}

@media (max-width: 1200px) {
  .connection-grid {
    grid-template-columns: repeat(2, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .connection-grid {
    grid-template-columns: 1fr;
  }
}

.json-params-section {
  width: 100%;
}

.json-input {
  width: 100%;
}

.json-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.json-error {
  margin-top: 8px;
  display: flex;
  align-items: center;
}

</style>
