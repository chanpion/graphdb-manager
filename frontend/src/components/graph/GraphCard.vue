<template>
  <el-card class="graph-card" @click="$emit('click', graph)">
    <div class="card-header">
      <div class="icon-wrapper" :class="`icon-${graph.databaseType?.toLowerCase() || 'default'}`">
        <el-icon :size="24" color="#fff">
          <Share />
        </el-icon>
      </div>
      <div class="graph-info">
        <h4 class="graph-name">{{ graph.name }}</h4>
        <p class="graph-description" v-if="graph.description">{{ graph.description }}</p>
        <div class="graph-meta-row">
          <span class="database-type">{{ graph.databaseType || '未知' }}</span>
          <div class="graph-tags">
            <el-tag size="small" :type="statusTagType">{{ statusText }}</el-tag>
            <el-tag size="small" :type="sourceTypeTagType" v-if="graph.sourceType">{{ sourceTypeText }}</el-tag>
          </div>
        </div>
      </div>
    </div>

    <div class="card-stats">
      <div class="stat-item">
        <div class="stat-value">{{ formatNumber(graph.vertexCount || 0) }}</div>
        <div class="stat-label">节点数</div>
      </div>
      <el-divider direction="vertical" />
      <div class="stat-item">
        <div class="stat-value">{{ formatNumber(graph.edgeCount || 0) }}</div>
        <div class="stat-label">边数</div>
      </div>
    </div>

    <div class="card-meta">
      <div class="meta-item">
        <el-icon class="meta-icon"><Clock /></el-icon>
        <span>{{ formatTime(graph.createdAt) }}</span>
      </div>
    </div>

    <div class="card-footer">
      <button class="icon-btn primary" @click.stop="$emit('open', graph)" title="打开">
        <el-icon><Position /></el-icon>
      </button>
      <button class="icon-btn secondary" @click.stop="$emit('edit', graph)" title="编辑">
        <el-icon><Edit /></el-icon>
      </button>
      <button class="icon-btn secondary" @click.stop="$emit('detail', graph)" title="详情">
        <el-icon><Document /></el-icon>
      </button>
      <button class="icon-btn secondary" @click.stop="$emit('browse', graph)" title="数据浏览">
        <el-icon><DataLine /></el-icon>
      </button>
      <button class="icon-btn danger" @click.stop="$emit('delete', graph)" title="删除">
        <el-icon><Delete /></el-icon>
      </button>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import {
  Share,
  DataLine,
  Clock,
  Position,
  List,
  Edit,
  Document,
  Delete
} from '@element-plus/icons-vue'

const props = defineProps({
  graph: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click', 'open', 'edit', 'detail', 'delete', 'browse'])

const statusTagType = computed(() => {
  return props.graph.status === 'NORMAL' ? 'success' : 'info'
})

const statusText = computed(() => {
  return props.graph.status === 'NORMAL' ? '正常' : '归档'
})

const sourceTypeTagType = computed(() => {
  return props.graph.sourceType === 'PLATFORM' ? 'success' : 'info'
})

const sourceTypeText = computed(() => {
  return props.graph.sourceType === 'PLATFORM' ? '平台创建' : '图数据库已有'
})

const formatNumber = (num) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (days > 7) {
    return date.toLocaleDateString('zh-CN')
  } else if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else if (minutes > 0) {
    return `${minutes}分钟前`
  } else {
    return '刚刚'
  }
}
</script>

<style scoped>
.graph-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e8e8e8;
  height: 280px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
}
.graph-card :deep(.el-card__body) {
  padding: 12px; /* 减小卡片内部内边距 */
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.graph-card::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.graph-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #1890ff;
}

.card-header {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 8px;
  flex-shrink: 0;
}

.icon-wrapper {
  width: 42px;
  height: 42px;
  border-radius: 8px;
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

.icon-default {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.graph-card:hover .icon-wrapper {
  transform: scale(1.1) rotate(5deg);
}

.graph-info {
  flex: 1;
  min-width: 0;
}

.graph-name {
  margin: 0 0 6px 0; /* 减小间距 */
  font-size: 15px; /* 减小字体大小 */
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.graph-description {
  margin: 0 0 6px 0; /* 减小间距 */
  font-size: 12px; /* 减小字体大小 */
  color: #606266;
  line-height: 1.4; /* 减小行高 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1; /* 限制为1行 */
  -webkit-box-orient: vertical;
}

.graph-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.database-type {
  font-size: 11px;
  color: #606266;
  font-weight: 500;
  padding: 2px 6px;
  background: #f0f2f5;
  border-radius: 3px;
}

.graph-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.card-stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 8px 10px;
  background: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 8px;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 20px; /* 减小字体大小 */
  font-weight: 700;
  color: #1890ff;
  margin-bottom: 2px; /* 减小间距 */
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 8px;
  padding: 8px;
  background: #fafafa;
  border-radius: 6px;
  flex-shrink: 0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
}

.meta-icon {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.card-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  margin-top: auto;
  flex-shrink: 0;
  min-height: 54px;
}

/* 纯图标按钮样式 */
.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  position: relative;
  overflow: hidden;
}

.icon-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  transition: all 0.3s ease;
  transform: translate(-50%, -50%);
}

.icon-btn:active::before {
  width: 100%;
  height: 100%;
}

.icon-btn.primary {
  background: linear-gradient(135deg, #3B82F6 0%, #8B5CF6 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.icon-btn.primary:hover {
  transform: translateY(-3px) scale(1.1);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.4);
  filter: brightness(1.1);
}

.icon-btn.primary:active {
  transform: translateY(0) scale(0.95);
  filter: brightness(0.95);
}

.icon-btn.secondary {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  color: #606266;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.icon-btn.secondary:hover {
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
  transform: translateY(-3px) scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  border-color: rgba(59, 130, 246, 0.3);
  color: #3B82F6;
}

.icon-btn.secondary:active {
  transform: translateY(0) scale(0.95);
}

.icon-btn.danger {
  background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.icon-btn.danger:hover {
  transform: translateY(-3px) scale(1.1);
  box-shadow: 0 8px 20px rgba(239, 68, 68, 0.4);
  filter: brightness(1.1);
}

.icon-btn.danger:active {
  transform: translateY(0) scale(0.95);
  filter: brightness(0.95);
}

.icon-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

.icon-btn .el-icon {
  font-size: 16px;
  transition: transform 0.2s ease;
}

.icon-btn:hover .el-icon {
  transform: scale(1.15);
}
</style>
