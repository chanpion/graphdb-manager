<template>
  <div class="index-manager">
    <div class="header-actions">
      <el-button type="primary" @click="showCreateIndexDialog" :disabled="!selectedConnectionId || !selectedGraphName">
        <el-icon><Plus /></el-icon>
        新建索引
      </el-button>
      <el-button @click="loadIndexes" :disabled="!selectedConnectionId || !selectedGraphName">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <el-table :data="indexes" stripe border v-loading="loading" style="margin-top: 20px;">
      <el-table-column prop="name" label="索引名称" width="200" />
      <el-table-column prop="type" label="索引类型" width="150">
        <template #default="{ row }">
          <el-tag :type="getIndexTypeTagType(row.type)" size="small">
            {{ getIndexTypeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="字段" width="250">
        <template #default="{ row }">
          <div v-if="row.fields && row.fields.length > 0">
            <el-tag v-for="field in row.fields" :key="field" size="small" style="margin-right: 5px;">
              {{ field }}
        </el-tag>
          </div>
          <span v-else>无字段</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'warning'" size="small">
            {{ row.status === 'ACTIVE' ? '活跃' : '构建中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="editIndex(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteIndex(row.name)" :disabled="row.status === 'BUILDING'">删除</el-button>
          <el-button size="small" type="info" @click="rebuildIndex(row.name)" v-if="row.status === 'ACTIVE'">重建</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑索引对话框 -->
    <el-dialog
      v-model="createIndexDialogVisible"
      :title="editingIndex ? '编辑索引' : '新建索引'"
      width="600px"
    >
      <el-form ref="indexFormRef" :model="indexForm" :rules="indexFormRules" label-width="100px">
        <el-form-item label="索引名称" required prop="name">
          <el-input v-model="indexForm.name" placeholder="请输入索引名称" :disabled="editingIndex" />
        </el-form-item>
        <el-form-item label="索引类型" required prop="type">
          <el-select v-model="indexForm.type" placeholder="请选择索引类型" style="width: 100%;">
            <el-option label="唯一索引" value="UNIQUE" />
            <el-option label="复合索引" value="COMPOUND" />
            <el-option label="全文索引" value="FULLTEXT" />
            <el-option label="空间索引" value="SPATIAL" />
            <el-option label="普通索引" value="NORMAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="字段" required prop="fields">
          <div class="field-list">
            <div v-for="(field, index) in indexForm.fields" :key="index" class="field-item">
              <el-input v-model="field.name" placeholder="字段名" style="width: 200px; margin-right: 10px;" />
              <el-select v-model="field.order" placeholder="排序" style="width: 100px; margin-right: 10px;">
                <el-option label="升序" value="ASC" />
                <el-option label="降序" value="DESC" />
              </el-select>
              <el-button type="danger" size="small" @click="removeField(index)">删除</el-button>
            </div>
            <el-button type="primary" @click="addField">添加字段</el-button>
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="indexForm.description" placeholder="请输入索引描述" type="textarea" :rows="2" />
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
import { ref, reactive, watch, defineProps, defineEmits } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { graphApi } from '../../api/graph'

const props = defineProps({
  selectedConnectionId: {
    type: String,
    default: ''
  },
  selectedGraphName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:selectedConnectionId', 'update:selectedGraphName'])

const indexes = ref([])
const loading = ref(false)

const createIndexDialogVisible = ref(false)
const creatingIndex = ref(false)
const editingIndex = ref(false)

const indexForm = reactive({
  name: '',
  type: 'UNIQUE',
  fields: [],
  description: ''
})

const indexFormRef = ref(null)

const indexFormRules = {
  name: [
    { required: true, message: '请输入索引名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择索引类型', trigger: 'change' }
  ],
  fields: [
    { 
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('至少需要一个字段'))
        } else {
          const invalidFields = value.filter(f => !f.name.trim())
          if (invalidFields.length > 0) {
            callback(new Error('字段名不能为空'))
          } else {
            const fieldNames = new Set()
            for (const field of value) {
              if (fieldNames.has(field.name)) {
                callback(new Error(`字段名 "${field.name}" 重复`))
                return
              }
              fieldNames.add(field.name)
            }
            callback()
          }
        }
      },
      trigger: 'blur'
    }
  ]
}

const getIndexTypeTagType = (type) => {
  const map = {
    'UNIQUE': 'success',
    'COMPOUND': 'warning',
    'FULLTEXT': 'info',
    'SPATIAL': 'primary',
    'NORMAL': ''
  }
  return map[type] || ''
}

const getIndexTypeLabel = (type) => {
  const map = {
    'UNIQUE': '唯一索引',
    'COMPOUND': '复合索引',
    'FULLTEXT': '全文索引',
    'SPATIAL': '空间索引',
    'NORMAL': '普通索引'
  }
  return map[type] || type
}

const loadIndexes = async () => {
  if (!props.selectedConnectionId || !props.selectedGraphName) {
    indexes.value = []
    return
  }

  loading.value = true
  try {
    const res = await graphApi.listIndexes(props.selectedConnectionId, props.selectedGraphName)
    indexes.value = res.data || []
  } catch (error) {
    console.error('加载索引列表失败:', error)
    ElMessage.error('加载索引列表失败')
    indexes.value = []
  } finally {
    loading.value = false
  }
}

const showCreateIndexDialog = () => {
  editingIndex.value = false
  indexForm.name = ''
  indexForm.type = 'UNIQUE'
  indexForm.fields = []
  indexForm.description = ''
  createIndexDialogVisible.value = true
}

const addField = () => {
  indexForm.fields.push({
    name: '',
    order: 'ASC'
  })
}

const removeField = (index) => {
  indexForm.fields.splice(index, 1)
}

const handleCreateIndex = async () => {
  // 表单验证
  try {
    await indexFormRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查表单输入')
    return
  }

  creatingIndex.value = true
  try {
    await graphApi.createIndex(props.selectedConnectionId, props.selectedGraphName, indexForm)
    
    ElMessage.success(editingIndex.value ? '索引更新成功' : '索引创建成功')
    createIndexDialogVisible.value = false
    loadIndexes()
  } catch (error) {
    console.error('索引操作失败:', error)
    ElMessage.error(editingIndex.value ? '索引更新失败' : '索引创建失败')
  } finally {
    creatingIndex.value = false
  }
}

const editIndex = (index) => {
  editingIndex.value = true
  indexForm.name = index.name
  indexForm.type = index.type
  indexForm.fields = index.fields.map(f => ({
    name: f,
    order: 'ASC'
  }))
  indexForm.description = index.description || ''
  createIndexDialogVisible.value = true
}

const deleteIndex = async (indexName) => {
  try {
    await ElMessageBox.confirm(`确定删除索引 "${indexName}" 吗？`, '警告', {
      type: 'warning'
    })
    
    await graphApi.deleteIndex(props.selectedConnectionId, props.selectedGraphName, indexName)
    
    ElMessage.success('索引删除成功')
    loadIndexes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除索引失败:', error)
      ElMessage.error('删除索引失败')
    }
  }
}

const rebuildIndex = async (indexName) => {
  try {
    await ElMessageBox.confirm(`确定重建索引 "${indexName}" 吗？重建期间索引将不可用。`, '警告', {
      type: 'warning'
    })
    
    await graphApi.rebuildIndex(props.selectedConnectionId, props.selectedGraphName, indexName)
    
    ElMessage.success('索引重建已开始')
    loadIndexes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重建索引失败:', error)
      ElMessage.error('重建索引失败')
    }
  }
}

// 监听连接和图的变化
watch(() => props.selectedConnectionId, () => {
  indexes.value = []
  if (props.selectedConnectionId && props.selectedGraphName) {
    loadIndexes()
  }
})

watch(() => props.selectedGraphName, () => {
  indexes.value = []
  if (props.selectedConnectionId && props.selectedGraphName) {
    loadIndexes()
  }
})
</script>

<style scoped>
.index-manager {
  padding: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
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
</style>