<template>
  <div class="breadcrumb-nav">
    <el-breadcrumb separator="/" class="breadcrumb-container">
      <el-breadcrumb-item 
        v-for="(node, index) in breadcrumbNodes" 
        :key="index"
        :to="node.isLast ? undefined : node.path"
        :class="{ 'last-item': node.isLast }"
      >
        <span class="breadcrumb-text">
          <el-icon v-if="index === 0" class="home-icon">
            <House />
          </el-icon>
          <template v-else-if="node.title === '...'">
            <el-icon><MoreFilled /></el-icon>
          </template>
          <template v-else>
            {{ node.title }}
          </template>
        </span>
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { generateBreadcrumbNodes } from '@/utils/breadcrumbUtils'
import { House, MoreFilled } from '@element-plus/icons-vue'

const route = useRoute()
const breadcrumbNodes = ref([])

const updateBreadcrumbNodes = () => {
  breadcrumbNodes.value = generateBreadcrumbNodes(route.path)
}

// 监听路由变化
watch(() => route.path, (newPath, oldPath) => {
  if (newPath !== oldPath) {
    updateBreadcrumbNodes()
  }
}, { immediate: true })

defineExpose({
  updateBreadcrumbNodes
})
</script>

<style scoped>
.breadcrumb-nav {
  padding: 0 16px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  height: 48px;
  display: flex;
  align-items: center;
}

.breadcrumb-container {
  font-size: 14px;
}

.breadcrumb-container :deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-weight: 400;
  transition: color 0.2s ease;
  padding: 4px 8px;
  border-radius: 4px;
}

.breadcrumb-container :deep(.el-breadcrumb__inner:hover) {
  color: #1890ff;
  background-color: #f0f7ff;
}

.breadcrumb-container :deep(.el-breadcrumb__separator) {
  color: #c0c4cc;
  margin: 0 8px;
}

.breadcrumb-container :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #1890ff;
  font-weight: 500;
}

.breadcrumb-container :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner:hover) {
  background-color: transparent;
  cursor: default;
}

.home-icon {
  font-size: 16px;
  color: #1890ff;
}

.breadcrumb-text {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .breadcrumb-nav {
    padding: 0 12px;
    height: 40px;
  }
  
  .breadcrumb-container {
    font-size: 13px;
  }
  
  .breadcrumb-container :deep(.el-breadcrumb__separator) {
    margin: 0 6px;
  }
}
</style>