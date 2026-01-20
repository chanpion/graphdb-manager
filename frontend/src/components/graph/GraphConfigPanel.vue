<template>
  <div class="graph-config-panel">
    <div class="panel-header">
      <h3>可视化配置</h3>
      <el-button 
        text 
        @click="toggleCollapse"
        class="toggle-btn"
      >
        <el-icon>
          <component :is="collapsed ? 'ArrowRight' : 'ArrowLeft'" />
        </el-icon>
      </el-button>
    </div>

    <div class="panel-content" v-show="!collapsed">
      <!-- 布局设置 -->
      <div class="config-section">
        <h4>布局设置</h4>
        <el-form label-position="top" size="small">
          <el-form-item label="布局类型">
            <el-select v-model="config.layoutType" @change="handleConfigChange">
              <el-option label="力导向" value="force" />
              <el-option label="环形" value="circular" />
              <el-option label="无" value="none" />
            </el-select>
          </el-form-item>

          <el-form-item label="斥力大小" v-if="config.layoutType === 'force'">
            <el-slider 
              v-model="config.repulsion" 
              :min="100" 
              :max="2000" 
              :step="100"
              @change="handleConfigChange"
            />
            <span class="value-label">{{ config.repulsion }}</span>
          </el-form-item>

          <el-form-item label="边长度" v-if="config.layoutType === 'force'">
            <el-slider 
              v-model="config.edgeLength" 
              :min="30" 
              :max="300" 
              :step="10"
              @change="handleConfigChange"
            />
            <span class="value-label">{{ config.edgeLength }}</span>
          </el-form-item>

          <el-form-item label="引力" v-if="config.layoutType === 'force'">
            <el-slider 
              v-model="config.gravity" 
              :min="0" 
              :max="1" 
              :step="0.05"
              @change="handleConfigChange"
            />
            <span class="value-label">{{ config.gravity }}</span>
          </el-form-item>
        </el-form>
      </div>

      <!-- 节点样式 -->
      <div class="config-section">
        <h4>节点样式</h4>
        <el-form label-position="top" size="small">
          <el-form-item label="节点大小">
            <el-slider 
              v-model="config.nodeSize" 
              :min="10" 
              :max="60" 
              :step="5"
              @change="handleConfigChange"
            />
            <span class="value-label">{{ config.nodeSize }}</span>
          </el-form-item>

          <el-form-item label="标签显示">
            <el-switch 
              v-model="config.showNodeLabel"
              @change="handleConfigChange"
            />
          </el-form-item>

          <el-form-item label="边框宽度">
            <el-slider 
              v-model="config.borderWidth" 
              :min="0" 
              :max="5" 
              :step="1"
              @change="handleConfigChange"
            />
            <span class="value-label">{{ config.borderWidth }}</span>
          </el-form-item>
        </el-form>
      </div>

      <!-- 边样式 -->
      <div class="config-section">
        <h4>边样式</h4>
        <el-form label-position="top" size="small">
          <el-form-item label="线条粗细">
            <el-slider 
              v-model="config.edgeWidth" 
              :min="1" 
              :max="10" 
              :step="1"
              @change="handleConfigChange"
            />
            <span class="value-label">{{ config.edgeWidth }}</span>
          </el-form-item>

          <el-form-item label="曲线度">
            <el-slider 
              v-model="config.curveness" 
              :min="0" 
              :max="1" 
              :step="0.1"
              @change="handleConfigChange"
            />
            <span class="value-label">{{ config.curveness }}</span>
          </el-form-item>

          <el-form-item label="标签显示">
            <el-switch 
              v-model="config.showEdgeLabel"
              @change="handleConfigChange"
            />
          </el-form-item>

          <el-form-item label="箭头显示">
            <el-switch 
              v-model="config.showArrow"
              @change="handleConfigChange"
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 交互设置 -->
      <div class="config-section">
        <h4>交互设置</h4>
        <el-form label-position="top" size="small">
          <el-form-item label="启用缩放">
            <el-switch 
              v-model="config.enableZoom"
              @change="handleConfigChange"
            />
          </el-form-item>

          <el-form-item label="启用拖拽">
            <el-switch 
              v-model="config.enableDrag"
              @change="handleConfigChange"
            />
          </el-form-item>

          <el-form-item label="缩放限制">
            <div class="zoom-limit">
              <el-input-number 
                v-model="config.minScale" 
                :min="0.1" 
                :max="1" 
                :step="0.1"
                size="small"
                @change="handleConfigChange"
              />
              <span>至</span>
              <el-input-number 
                v-model="config.maxScale" 
                :min="1" 
                :max="5" 
                :step="0.5"
                size="small"
                @change="handleConfigChange"
              />
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 导出功能 -->
      <div class="config-section">
        <h4>导出</h4>
        <el-space direction="vertical" :size="8" style="width: 100%;">
          <el-button size="small" @click="exportImage" style="width: 100%;">
            <el-icon><Download /></el-icon>
            导出图片
          </el-button>
          <el-button size="small" @click="exportData" style="width: 100%;">
            <el-icon><Document /></el-icon>
            导出数据
          </el-button>
        </el-space>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Document } from '@element-plus/icons-vue'

const emit = defineEmits(['config-change', 'export-image', 'export-data'])

const collapsed = ref(false)

const config = reactive({
  // 布局设置
  layoutType: 'force',
  repulsion: 1000,
  edgeLength: 100,
  gravity: 0.1,

  // 节点样式
  nodeSize: 30,
  showNodeLabel: true,
  borderWidth: 2,

  // 边样式
  edgeWidth: 2,
  curveness: 0.2,
  showEdgeLabel: true,
  showArrow: true,

  // 交互设置
  enableZoom: true,
  enableDrag: true,
  minScale: 0.5,
  maxScale: 3
})

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

const handleConfigChange = () => {
  emit('config-change', { ...config })
}

const exportImage = () => {
  emit('export-image')
  ElMessage.success('已导出图片')
}

const exportData = () => {
  emit('export-data')
  ElMessage.success('已导出数据')
}

// 暴露配置对象
defineExpose({
  config
})
</script>

<style scoped>
.graph-config-panel {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 280px;
  max-height: calc(100% - 40px);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.panel-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.toggle-btn {
  color: rgba(255, 255, 255, 0.8);
  transition: color 0.2s ease;
}

.toggle-btn:hover {
  color: white;
}

.panel-content {
  overflow-y: auto;
  max-height: calc(100vh - 200px);
  padding: 16px;
}

.config-section {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.config-section:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.config-section h4 {
  margin: 0 0 12px 0;
  font-size: 13px;
  font-weight: 600;
  color: #1f2d3d;
}

.value-label {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
  min-width: 40px;
  display: inline-block;
}

.zoom-limit {
  display: flex;
  align-items: center;
  gap: 8px;
}

.zoom-limit span {
  color: #606266;
  font-size: 13px;
}

:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-form-item__label) {
  font-size: 13px;
  color: #606266;
  padding: 0;
}

:deep(.el-select),
:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-slider) {
  margin-right: 60px;
}

.panel-content::-webkit-scrollbar {
  width: 6px;
}

.panel-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.panel-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.panel-content::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
