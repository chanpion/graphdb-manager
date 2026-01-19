<template>
  <div class="status-bar">
    <div class="status-left">
      <div class="status-item">
        <el-icon class="status-icon" :color="connectionStatus === 'connected' ? '#52c41a' : '#ff4d4f'">
          <component :is="connectionIcon" />
        </el-icon>
        <span>{{ connectionText }}</span>
      </div>
      <div class="status-item">
        <el-icon class="status-icon"><DataLine /></el-icon>
        <span>{{ currentGraph || '未选择' }}</span>
      </div>
    </div>

    <div class="status-center">
      <span class="status-message">{{ message }}</span>
    </div>

    <div class="status-right">
      <div class="status-item">
        <el-icon class="status-icon"><Clock /></el-icon>
        <span>{{ currentTime }}</span>
      </div>
      <div class="status-item">
        <el-icon class="status-icon"><InfoFilled /></el-icon>
        <span>v1.0.0</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Connection, Close, DataLine, Clock, InfoFilled } from '@element-plus/icons-vue'

const props = defineProps({
  connectionStatus: {
    type: String,
    default: 'disconnected' // connected, disconnected
  },
  currentGraph: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    default: '就绪'
  }
})

const currentTime = ref('')
let timer = null

const connectionIcon = computed(() => {
  return props.connectionStatus === 'connected' ? Connection : Close
})

const connectionText = computed(() => {
  return props.connectionStatus === 'connected' ? '已连接' : '未连接'
})

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.status-bar {
  height: 32px;
  background: #001529;
  color: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  font-size: 13px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  user-select: none;
}

.status-left,
.status-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.status-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.status-message {
  color: rgba(255, 255, 255, 0.65);
}

.status-item {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: opacity 0.3s;
}

.status-item:hover {
  opacity: 0.8;
}

.status-icon {
  font-size: 14px;
}
</style>
