<template>
  <el-card class="connection-card" :class="cardClass" @click="$emit('click', connection)">
    <div class="card-header">
      <div class="icon-wrapper" :class="`icon-${connection.type.toLowerCase()}`">
        <el-icon :size="24" :color="iconColor">
          <component :is="iconComponent" />
        </el-icon>
      </div>
      <div class="connection-info">
        <h4 class="connection-name">{{ connection.name }}</h4>
        <div class="connection-meta">
          <el-tag size="small" :type="tagType">{{ dbTypeLabel }}</el-tag>
          <span class="separator">|</span>
          <span class="connection-host">{{ connection.host }}:{{ connection.port }}</span>
        </div>
      </div>
    </div>

    <div class="card-body">
      <div class="info-row">
        <el-icon class="info-icon"><User /></el-icon>
        <span>{{ connection.username || '未设置' }}</span>
      </div>
      <div v-if="connection.description" class="info-row description">
        <el-icon class="info-icon"><Document /></el-icon>
        <span>{{ connection.description }}</span>
      </div>
    </div>

    <div class="card-footer">
      <div class="status-indicator">
        <span class="status-dot" :class="statusClass"></span>
        <span class="status-text">{{ statusText }}</span>
      </div>
      <div class="action-buttons">
        <el-tooltip content="测试连接" placement="top">
          <el-button size="small" circle @click.stop="handleTest">
            <el-icon><Connection /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="编辑连接" placement="top">
          <el-button size="small" circle @click.stop="handleEdit">
            <el-icon><Edit /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="删除连接" placement="top">
          <el-button size="small" circle type="danger" @click.stop="handleDelete">
            <el-icon><Delete /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import {
  Connection,
  User,
  Document,
  Edit,
  Delete,
  DataLine,
  Share,
  Aim
} from '@element-plus/icons-vue'

const props = defineProps({
  connection: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click', 'test', 'edit', 'delete'])

const iconComponent = computed(() => {
  const iconMap = {
    'NEO4J': DataLine,
    'NEBULA': Share,
    'JANUS': Aim
  }
  return iconMap[props.connection.type] || DataLine
})

const iconColor = computed(() => {
  const colorMap = {
    'NEO4J': '#F5A623',
    'NEBULA': '#00D6A9',
    'JANUS': '#6C5CE7'
  }
  return colorMap[props.connection.type] || '#F5A623'
})

const tagType = computed(() => {
  const typeMap = {
    'NEO4J': 'warning',
    'NEBULA': 'success',
    'JANUS': 'info'
  }
  return typeMap[props.connection.type] || 'info'
})

const dbTypeLabel = computed(() => {
  const labelMap = {
    'NEO4J': 'Neo4j',
    'NEBULA': 'NebulaGraph',
    'JANUS': 'JanusGraph'
  }
  return labelMap[props.connection.type] || '未知'
})

const cardClass = computed(() => {
  return `card-${props.connection.type.toLowerCase()}`
})

const statusClass = computed(() => {
  return props.connection.status === 1 ? 'status-normal' : 'status-error'
})

const statusText = computed(() => {
  return props.connection.status === 1 ? '正常' : '异常'
})

const handleTest = () => {
  emit('test', props.connection)
}

const handleEdit = () => {
  emit('edit', props.connection)
}

const handleDelete = () => {
  emit('delete', props.connection)
}
</script>

<style scoped>
.connection-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e8e8e8;
  height: 100%;
}

.connection-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #1890ff;
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s;
}

.icon-neo4j {
  background: linear-gradient(135deg, #FFB347 0%, #F5A623 100%);
}

.icon-nebula {
  background: linear-gradient(135deg, #00F5A9 0%, #00D6A9 100%);
}

.icon-janus {
  background: linear-gradient(135deg, #A29BFE 0%, #6C5CE7 100%);
}

.connection-card:hover .icon-wrapper {
  transform: scale(1.1);
}

.connection-info {
  flex: 1;
  min-width: 0;
}

.connection-name {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.connection-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
  flex-wrap: wrap;
}

.separator {
  color: #dcdfe6;
}

.connection-host {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-body {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row.description {
  color: #909399;
  font-style: italic;
}

.info-icon {
  font-size: 14px;
  color: #909399;
  flex-shrink: 0;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-normal {
  background-color: #52c41a;
  box-shadow: 0 0 8px rgba(82, 196, 26, 0.3);
}

.status-error {
  background-color: #ff4d4f;
  box-shadow: 0 0 8px rgba(255, 77, 79, 0.3);
}

.status-text {
  font-size: 13px;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.action-buttons :deep(.el-button) {
  width: 28px;
  height: 28px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-buttons :deep(.el-button .el-icon) {
  font-size: 14px;
}
</style>
