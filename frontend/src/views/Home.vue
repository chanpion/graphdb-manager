<template>
  <div class="home-container">
    <h1 class="title">图数据库管理系统</h1>
    <div class="description">
      <p>欢迎使用图数据库管理系统</p>
      <p>支持 Neo4j、NebulaGraph、JanusGraph 等多种图数据库</p>
      <el-button type="primary" size="large">
        开始使用
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const handleStart = () => {
  router.push('/connections')
}
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40px 20px;
}

.title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #303133;
}

.description {
  text-align: center;
  color: #606266;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 30px;
}

.description p {
  margin: 8px 0;
}
</style>
