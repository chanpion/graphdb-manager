<template>
  <div class="property-editor">
    <div class="editor-header">
      <h3>属性编辑器</h3>
      <div class="editor-actions">
        <el-button type="primary" size="small" @click="handleSave" :loading="saving">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
        <el-button size="small" @click="handleReset">
          <el-icon><RefreshLeft /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <el-scrollbar class="editor-content">
      <div v-if="selectedType" class="editor-form">
        <!-- 基本信息 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon v-if="selectedType.type === 'vertex'" class="type-icon vertex">
                <Circle />
              </el-icon>
              <el-icon v-else class="type-icon edge">
                <Top />
              </el-icon>
              <span class="type-name">{{ selectedType.name }}</span>
              <el-tag size="small" type="info">
                {{ selectedType.type === 'vertex' ? '点类型' : '边类型' }}
              </el-tag>
            </div>
          </template>

          <el-form :model="formData" label-width="80px" size="small">
            <el-form-item label="类型名称">
              <el-input v-model="formData.name" placeholder="请输入类型名称" />
            </el-form-item>
            <el-form-item label="描述">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="3"
                placeholder="请输入类型描述"
              />
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 属性列表 -->
        <el-card class="properties-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>属性定义</span>
              <el-button link @click="addProperty">
                <el-icon><Plus /></el-icon>
                添加属性
              </el-button>
            </div>
          </template>

          <div v-if="formData.properties.length > 0" class="property-list">
            <div
              v-for="(prop, index) in formData.properties"
              :key="index"
              class="property-item"
            >
              <div class="property-field">
                <el-input
                  v-model="prop.name"
                  placeholder="属性名"
                  size="small"
                />
              </div>
              <div class="property-field">
                <el-select
                  v-model="prop.dataType"
                  placeholder="数据类型"
                  size="small"
                >
                  <el-option label="字符串" value="STRING" />
                  <el-option label="整数" value="INTEGER" />
                  <el-option label="浮点数" value="FLOAT" />
                  <el-option label="布尔值" value="BOOLEAN" />
                  <el-option label="日期" value="DATE" />
                </el-select>
              </div>
              <div class="property-field checkboxes">
                <el-checkbox v-model="prop.required" size="small">必填</el-checkbox>
                <el-checkbox v-model="prop.indexed" size="small">索引</el-checkbox>
                <el-checkbox v-model="prop.unique" size="small">唯一</el-checkbox>
              </div>
              <div class="property-field actions">
                <el-button
                  link
                  size="small"
                  @click="moveProperty(index, -1)"
                  :disabled="index === 0"
                >
                  <el-icon><ArrowUp /></el-icon>
                </el-button>
                <el-button
                  link
                  size="small"
                  @click="moveProperty(index, 1)"
                  :disabled="index === formData.properties.length - 1"
                >
                  <el-icon><ArrowDown /></el-icon>
                </el-button>
                <el-button
                  link
                  size="small"
                  @click="removeProperty(index)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>

          <el-empty v-else description="暂无属性，点击上方按钮添加" :image-size="80" />
        </el-card>

        <!-- 统计信息 -->
        <el-card class="stats-card" shadow="never">
          <template #header>
            <span>统计信息</span>
          </template>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="属性总数">
              {{ formData.properties.length }}
            </el-descriptions-item>
            <el-descriptions-item label="必填属性">
              {{ requiredCount }}
            </el-descriptions-item>
            <el-descriptions-item label="索引属性">
              {{ indexedCount }}
            </el-descriptions-item>
            <el-descriptions-item label="唯一属性">
              {{ uniqueCount }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>

      <el-empty v-else description="请从左侧树形菜单中选择一个类型进行编辑" />
    </el-scrollbar>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Check,
  RefreshLeft,
  Circle,
  Top,
  Plus,
  ArrowUp,
  ArrowDown,
  Delete
} from '@element-plus/icons-vue'

const props = defineProps({
  selectedType: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['save', 'reset'])

const saving = ref(false)

// 表单数据
const formData = ref({
  name: '',
  type: 'VERTEX',
  description: '',
  properties: []
})

// 计算统计数据
const requiredCount = computed(() => {
  return formData.value.properties.filter(p => p.required).length
})

const indexedCount = computed(() => {
  return formData.value.properties.filter(p => p.indexed).length
})

const uniqueCount = computed(() => {
  return formData.value.properties.filter(p => p.unique).length
})

// 监听选中的类型变化
watch(() => props.selectedType, (newVal) => {
  if (newVal) {
    // 深拷贝数据，避免直接修改原对象
    formData.value = JSON.parse(JSON.stringify(newVal))
  } else {
    formData.value = {
      name: '',
      type: 'VERTEX',
      description: '',
      properties: []
    }
  }
}, { immediate: true, deep: true })

// 添加属性
const addProperty = () => {
  formData.value.properties.push({
    name: '',
    dataType: 'STRING',
    required: false,
    indexed: false,
    unique: false
  })
}

// 删除属性
const removeProperty = (index) => {
  formData.value.properties.splice(index, 1)
}

// 移动属性
const moveProperty = (index, direction) => {
  const newIndex = index + direction
  if (newIndex >= 0 && newIndex < formData.value.properties.length) {
    const temp = formData.value.properties[index]
    formData.value.properties[index] = formData.value.properties[newIndex]
    formData.value.properties[newIndex] = temp
  }
}

// 保存
const handleSave = async () => {
  // 验证
  if (!formData.value.name || !formData.value.name.trim()) {
    ElMessage.warning('请输入类型名称')
    return
  }

  if (formData.value.properties.length === 0) {
    ElMessage.warning('请至少添加一个属性')
    return
  }

  // 验证属性
  for (let i = 0; i < formData.value.properties.length; i++) {
    const prop = formData.value.properties[i]
    if (!prop.name || !prop.name.trim()) {
      ElMessage.warning(`第 ${i + 1} 个属性名称不能为空`)
      return
    }
    if (!prop.dataType) {
      ElMessage.warning(`第 ${i + 1} 个属性必须选择数据类型`)
      return
    }
  }

  // 检查属性名重复
  const names = formData.value.properties.map(p => p.name.trim())
  const duplicates = names.filter((name, index) => names.indexOf(name) !== index)
  if (duplicates.length > 0) {
    ElMessage.warning(`属性名称重复: ${duplicates.join(', ')}`)
    return
  }

  saving.value = true
  try {
    emit('save', formData.value)
  } finally {
    saving.value = false
  }
}

// 重置
const handleReset = () => {
  if (props.selectedType) {
    formData.value = JSON.parse(JSON.stringify(props.selectedType))
    ElMessage.info('已重置为原始数据')
  }
}
</script>

<style scoped>
.property-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-left: 1px solid #e8e8e8;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.editor-header h3 {
  margin: 0;
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.editor-actions .el-button {
  color: white;
}

.editor-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.2);
}

.editor-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.info-card,
.properties-card,
.stats-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.type-icon {
  font-size: 18px;
}

.type-icon.vertex {
  color: #52c41a;
}

.type-icon.edge {
  color: #1890ff;
}

.type-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.property-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.property-item {
  display: grid;
  grid-template-columns: 1fr 120px auto auto;
  gap: 8px;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  transition: all 0.3s;
}

.property-item:hover {
  background: #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.property-field {
  display: flex;
  align-items: center;
}

.property-field.checkboxes {
  display: flex;
  gap: 12px;
}

.property-field.actions {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
}

.property-field.actions .el-button {
  padding: 4px;
}

.property-field.actions .el-button:hover {
  background: rgba(0, 0, 0, 0.04);
}

/* 响应式调整 */
@media (max-width: 1400px) {
  .property-item {
    grid-template-columns: 1fr;
  }

  .property-field {
    width: 100%;
  }
}

/* 滚动条样式 */
.editor-content::-webkit-scrollbar {
  width: 6px;
}

.editor-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.editor-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.editor-content::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
