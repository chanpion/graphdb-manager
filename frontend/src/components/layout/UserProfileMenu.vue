<template>
  <div class="user-profile-menu">
    <el-dropdown 
      :hide-on-click="false" 
      trigger="click" 
      placement="bottom-end"
      class="user-dropdown"
      @command="handleCommand"
    >
      <div class="user-trigger">
        <el-avatar :size="32" :src="userInfo.avatar" class="user-avatar">
          {{ userInfo.username ? userInfo.username.charAt(0).toUpperCase() : 'U' }}
        </el-avatar>
        <div class="user-info" v-if="showUserName">
          <div class="user-name">{{ userInfo.username }}</div>
          <div class="user-role" v-if="userInfo.role">{{ userInfo.role }}</div>
        </div>
        <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
      </div>
      
      <template #dropdown>
        <el-dropdown-menu class="user-dropdown-menu">
          <!-- 用户信息摘要 -->
          <el-dropdown-item class="user-summary" disabled>
            <div class="summary-content">
              <el-avatar :size="40" :src="userInfo.avatar" class="summary-avatar">
                {{ userInfo.username ? userInfo.username.charAt(0).toUpperCase() : 'U' }}
              </el-avatar>
              <div class="summary-info">
                <div class="summary-name">{{ userInfo.username }}</div>
                <div class="summary-email" v-if="userInfo.email">{{ userInfo.email }}</div>
                <div class="summary-role" v-if="userInfo.role">{{ userInfo.role }}</div>
              </div>
            </div>
          </el-dropdown-item>
          
          <!-- 功能菜单 -->
          <el-dropdown-item command="profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-dropdown-item>
          
          <el-dropdown-item command="settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-dropdown-item>
          
          <el-dropdown-item command="theme">
            <el-icon><Moon /></el-icon>
            <span>主题切换</span>
            <el-switch
              v-model="isDarkTheme"
              size="small"
              @click.stop
              @change="handleThemeChange"
              class="theme-switch"
            />
          </el-dropdown-item>
          
          <!-- 退出登录 -->
          <el-dropdown-item command="logout" class="logout-item">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowDown, 
  User, 
  Setting, 
  Moon,
  QuestionFilled,
  ChatDotRound,
  InfoFilled,
  SwitchButton
} from '@element-plus/icons-vue'

const props = defineProps({
  // 是否显示用户名
  showUserName: {
    type: Boolean,
    default: true
  },
  // 用户信息
  user: {
    type: Object,
    default: () => ({
      username: '管理员',
      avatar: '',
      email: 'admin@graphdb.com',
      role: '系统管理员',
      department: '技术部'
    })
  }
})

const emit = defineEmits(['command', 'theme-change'])

const router = useRouter()

// 主题状态
const isDarkTheme = ref(false)

// 用户信息
const userInfo = computed(() => {
  // 安全的用户数据，确保始终有值
  const user = props.user || {
    username: '管理员',
    avatar: '',
    email: 'admin@graphdb.com',
    role: '系统管理员',
    department: '技术部'
  }
  
  try {
    return {
      ...user,
      avatar: user.avatar || getDefaultAvatar()
    }
  } catch (error) {
    console.error('生成用户信息失败:', error)
    // 返回一个安全的fallback对象
    return {
      username: user.username || '管理员',
      avatar: '',
      email: user.email || '',
      role: user.role || '',
      department: user.department || ''
    }
  }
})

// 获取默认头像
const getDefaultAvatar = () => {
  // 基于用户名的确定性颜色生成
  const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe']
  const username = props.user?.username || '管理员'
  
  // 基于用户名生成确定性颜色索引
  let hash = 0
  for (let i = 0; i < username.length; i++) {
    hash = username.charCodeAt(i) + ((hash << 5) - hash)
  }
  const colorIndex = Math.abs(hash) % colors.length
  const color = colors[colorIndex]
  
  // 获取首字母并确保是ASCII可打印字符
  const firstChar = username.charAt(0).toUpperCase()
  
  // 构建SVG字符串
  const svgString = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">
  <rect width="100" height="100" fill="${color}" rx="50"/>
  <text x="50" y="55" font-size="40" text-anchor="middle" fill="white" font-family="Arial, sans-serif">
    ${firstChar}
  </text>
</svg>`
  
  // 使用TextEncoder将字符串转换为UTF-8 Uint8Array
  const encoder = new TextEncoder()
  const utf8Bytes = encoder.encode(svgString)
  
  // 将Uint8Array转换为二进制字符串（btoa兼容）
  let binaryString = ''
  for (let i = 0; i < utf8Bytes.length; i++) {
    binaryString += String.fromCharCode(utf8Bytes[i])
  }
  
  // 使用btoa进行base64编码
  const base64 = btoa(binaryString)
  
  return `data:image/svg+xml;base64,${base64}`
}

// 处理菜单命令
const handleCommand = (command) => {
  emit('command', command)
  
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理主题切换
const handleThemeChange = (value) => {
  emit('theme-change', value)
  
  // 触发全局主题变更
  if (value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

// 处理退出登录
const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    // 这里应该调用退出登录的API
    localStorage.removeItem('user_token')
    router.push('/login')
  }
}

defineExpose({
  toggleTheme: () => {
    isDarkTheme.value = !isDarkTheme.value
    handleThemeChange(isDarkTheme.value)
  }
})
</script>

<style scoped>
.user-profile-menu {
  display: inline-block;
}

.user-dropdown {
  cursor: pointer;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.user-trigger:hover {
  background-color: rgba(24, 144, 255, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
}

.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2d3d;
}

.user-role {
  font-size: 12px;
  color: #8c8c8c;
}

.dropdown-icon {
  font-size: 12px;
  color: #8c8c8c;
  transition: transform 0.2s ease;
}

.user-dropdown:focus .dropdown-icon,
.user-dropdown:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.user-dropdown-menu {
  min-width: 200px;
  padding: 8px 0;
}

.user-summary {
  opacity: 1 !important;
  cursor: default;
}

.summary-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
}

.summary-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
  flex-shrink: 0;
}

.summary-info {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
  overflow: hidden;
}

.summary-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.summary-email {
  font-size: 12px;
  color: #8c8c8c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.summary-role {
  font-size: 11px;
  color: #1890ff;
  background-color: rgba(24, 144, 255, 0.1);
  padding: 2px 6px;
  border-radius: 3px;
  display: inline-block;
  margin-top: 4px;
}

.user-dropdown-menu :deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  font-size: 14px;
  color: #606266;
  transition: background-color 0.2s ease;
}

.user-dropdown-menu :deep(.el-dropdown-menu__item:hover) {
  background-color: #f5f7fa;
  color: #1890ff;
}

.user-dropdown-menu :deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
  width: 20px;
}

/* 主题切换开关 */
.theme-switch {
  margin-left: auto;
}

/* 退出登录项特殊样式 */
.logout-item :deep(.el-icon) {
  color: #ff4d4f;
}

.logout-item :deep(span) {
  color: #ff4d4f;
  font-weight: 500;
}

/* 暗色主题适配 */
@media (prefers-color-scheme: dark) {
  .user-name {
    color: #d9d9d9;
  }
  
  .user-role {
    color: #8c8c8c;
  }
  
  .user-trigger:hover {
    background-color: rgba(24, 144, 255, 0.2);
  }
  
  .summary-name {
    color: #d9d9d9;
  }
  
  .summary-email {
    color: #8c8c8c;
  }
  
  .user-dropdown-menu :deep(.el-dropdown-menu__item) {
    color: #d9d9d9;
  }
  
  .user-dropdown-menu :deep(.el-dropdown-menu__item:hover) {
    background-color: #2a2a2a;
    color: #1890ff;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info {
    display: none;
  }
  
  .user-trigger {
    padding: 6px;
    gap: 6px;
  }
  
  .user-dropdown-menu {
    min-width: 180px;
  }
}
</style>