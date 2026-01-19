<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <span class="api-switcher">
      <el-icon class="mode-icon" :color="isMockMode ? '#52c41a' : '#1890ff'">
        <component :is="isMockMode ? Files : Connection" />
      </el-icon>
      <span class="mode-text">{{ modeLabel }}模式</span>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="toggle">
          <div class="menu-item">
            <el-icon><component :is="isMockMode ? Connection : Files" /></el-icon>
            <span>切换到{{ isMockMode ? '真实' : 'Mock' }} API</span>
          </div>
        </el-dropdown-item>
        <el-dropdown-item divided command="config">
          <div class="menu-item">
            <el-icon><Setting /></el-icon>
            <span>API 配置</span>
          </div>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>

  <el-dialog
    v-model="configDialogVisible"
    title="API 配置"
    width="500px"
  >
    <el-form :model="form" label-width="120px">
      <el-form-item label="API 模式">
        <el-switch
          v-model="form.useMock"
          active-text="Mock"
          inactive-text="Real"
        />
      </el-form-item>
      <el-form-item label="Mock 延迟">
        <el-input-number
          v-model="form.mockDelay"
          :min="0"
          :max="2000"
          :disabled="!form.useMock"
        />
        <span class="unit">ms</span>
      </el-form-item>
      <el-form-item label="API 地址">
        <el-input
          v-model="form.realApiUrl"
          :disabled="form.useMock"
          placeholder="http://localhost:8080/api"
        />
      </el-form-item>
      <el-form-item label="请求超时">
        <el-input-number
          v-model="form.realApiTimeout"
          :min="1000"
          :max="60000"
          :step="1000"
          :disabled="form.useMock"
        />
        <span class="unit">ms</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="configDialogVisible = false">取消</el-button>
      <el-button @click="handleReset">重置默认</el-button>
      <el-button type="primary" @click="handleSaveConfig">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Files,
  Connection,
  Setting
} from '@element-plus/icons-vue'
import {
  getApiConfig,
  setApiConfig,
  toggleApiMode,
  resetApiConfig
} from '../config/apiConfig'

defineOptions({
  inheritAttrs: false
})

const configDialogVisible = ref(false)

const form = ref({
  useMock: true,
  mockDelay: 300,
  realApiUrl: 'http://localhost:8080/api',
  realApiTimeout: 30000
})

const modeLabel = computed(() => {
  return form.value.useMock ? 'Mock' : '真实'
})

const isMockMode = computed(() => form.value.useMock)

// 加载配置
const loadConfig = () => {
  const config = getApiConfig()
  form.value = { ...config }
}

// 切换模式
const handleToggle = () => {
  toggleApiMode(!form.value.useMock)
  loadConfig()
  ElMessage.success(`已切换到${modeLabel.value} API 模式`)
  // 重新加载页面
  setTimeout(() => {
    window.location.reload()
  }, 500)
}

// 打开配置对话框
const handleConfig = () => {
  loadConfig()
  configDialogVisible.value = true
}

// 保存配置
const handleSaveConfig = () => {
  setApiConfig(form.value)
  configDialogVisible.value = false
  ElMessage.success('API 配置已保存')
  // 重新加载页面
  setTimeout(() => {
    window.location.reload()
  }, 500)
}

// 重置配置
const handleReset = () => {
  resetApiConfig()
  loadConfig()
  ElMessage.info('配置已重置为默认值')
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'toggle') {
    handleToggle()
  } else if (command === 'config') {
    handleConfig()
  }
}

// 初始化加载配置
loadConfig()
</script>

<style scoped>
.api-switcher {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.api-switcher:hover {
  background: rgba(255, 255, 255, 0.15);
}

.mode-icon {
  font-size: 16px;
  transition: color 0.3s;
}

.mode-text {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 150px;
}

.unit {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.api-switcher:hover .mode-icon {
  transform: scale(1.1);
}
</style>
