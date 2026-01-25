import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { graphApi } from '../api/graph'

/**
 * 数据导出管理Store
 * 用于管理导出任务的状态和生命周期
 */
export const useExportStore = defineStore('export', () => {
  // 当前选中的连接ID
  const selectedConnectionId = ref('')
  
  // 当前选中的图名称
  const selectedGraphName = ref('')
  
  // 图列表
  const graphs = ref([])
  
  // 图列表加载状态
  const graphsLoading = ref(false)
  
  // 导出任务列表
  const tasks = ref([])
  
  // 加载状态
  const loading = ref(false)
  
  // 错误信息
  const error = ref(null)
  
  // 任务轮询间隔（毫秒）
  const pollingInterval = ref(5000)
  
  // 轮询定时器ID
  let pollingTimer = null
  
  // 计算属性：任务总数
  const totalTasks = computed(() => tasks.value.length)
  
  // 计算属性：进行中的任务
  const runningTasks = computed(() => 
    tasks.value.filter(task => task.status === 'running')
  )
  
  // 计算属性：已完成的任务
  const completedTasks = computed(() => 
    tasks.value.filter(task => task.status === 'completed')
  )
  
  // 计算属性：失败的任务
  const failedTasks = computed(() => 
    tasks.value.filter(task => task.status === 'failed')
  )
  
  // 计算属性：已取消的任务
  const cancelledTasks = computed(() => 
    tasks.value.filter(task => task.status === 'cancelled')
  )
  
  // 计算属性：根据ID获取任务
  const getTaskById = computed(() => (taskId) => 
    tasks.value.find(task => task.id === taskId)
  )
  
  /**
   * 设置当前选中的连接和图
   * @param {string} connectionId - 连接ID
   * @param {string} graphName - 图名称
   */
  const setSelection = async (connectionId, graphName) => {
    selectedConnectionId.value = connectionId
    selectedGraphName.value = graphName
    // 清空现有任务
    clearTasks()
    // 加载新连接的图列表
    if (connectionId) {
      await loadGraphs()
    }
  }
  
  /**
   * 清空所有任务
   */
  const clearTasks = () => {
    tasks.value = []
    error.value = null
  }
  
  /**
   * 加载图列表
   */
  const loadGraphs = async () => {
    if (!selectedConnectionId.value) {
      error.value = '请先选择连接'
      return
    }
    
    graphsLoading.value = true
    error.value = null
    
    try {
      const res = await graphApi.list(selectedConnectionId.value)
      graphs.value = res || []
    } catch (err) {
      error.value = `加载图列表失败: ${err.message}`
      console.error('加载图列表失败:', err)
    } finally {
      graphsLoading.value = false
    }
  }
  
  /**
   * 加载导出任务列表
   */
  const loadTasks = async () => {
    if (!selectedConnectionId.value || !selectedGraphName.value) {
      error.value = '请先选择连接和图'
      return
    }
    
    loading.value = true
    error.value = null
    
    try {
      const res = await graphApi.listExportTasks(selectedConnectionId.value, selectedGraphName.value)
      tasks.value = res || []
    } catch (err) {
      error.value = `加载导出任务失败: ${err.message}`
      console.error('加载导出任务失败:', err)
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 创建导出任务
   * @param {object} data - 导出任务配置
   * @returns {Promise<object>} 创建结果
   */
  const createTask = async (data) => {
    if (!selectedConnectionId.value || !selectedGraphName.value) {
      throw new Error('请先选择连接和图')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.createExportTask(selectedConnectionId.value, selectedGraphName.value, data)
      // 重新加载任务列表
      await loadTasks()
      return res
    } catch (err) {
      error.value = `创建导出任务失败: ${err.message}`
      console.error('创建导出任务失败:', err)
      throw err
    }
  }
  
  /**
   * 删除导出任务
   * @param {string} taskId - 任务ID
   * @returns {Promise<object>} 删除结果
   */
  const deleteTask = async (taskId) => {
    if (!selectedConnectionId.value || !selectedGraphName.value) {
      throw new Error('请先选择连接和图')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.deleteExportTask(selectedConnectionId.value, selectedGraphName.value, taskId)
      // 重新加载任务列表
      await loadTasks()
      return res
    } catch (err) {
      error.value = `删除导出任务失败: ${err.message}`
      console.error('删除导出任务失败:', err)
      throw err
    }
  }
  
  /**
   * 取消导出任务
   * @param {string} taskId - 任务ID
   * @returns {Promise<object>} 取消结果
   */
  const cancelTask = async (taskId) => {
    if (!selectedConnectionId.value || !selectedGraphName.value) {
      throw new Error('请先选择连接和图')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.cancelExportTask(selectedConnectionId.value, selectedGraphName.value, taskId)
      // 重新加载任务列表
      await loadTasks()
      return res
    } catch (err) {
      error.value = `取消导出任务失败: ${err.message}`
      console.error('取消导出任务失败:', err)
      throw err
    }
  }
  
  /**
   * 开始轮询任务状态
   */
  const startPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer)
    }
    
    pollingTimer = setInterval(async () => {
      // 只有在有进行中任务时才轮询
      if (runningTasks.value.length > 0) {
        await loadTasks()
      }
    }, pollingInterval.value)
  }
  
  /**
   * 停止轮询任务状态
   */
  const stopPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
  }
  
  /**
   * 设置轮询间隔
   * @param {number} interval - 轮询间隔（毫秒）
   */
  const setPollingInterval = (interval) => {
    pollingInterval.value = interval
    // 如果正在轮询，重启轮询
    if (pollingTimer) {
      stopPolling()
      startPolling()
    }
  }
  
  /**
   * 重置错误状态
   */
  const clearError = () => {
    error.value = null
  }
  
  // 在store销毁时停止轮询
  const onUnmounted = () => {
    stopPolling()
  }
  
  return {
    // 状态
    selectedConnectionId,
    selectedGraphName,
    graphs,
    graphsLoading,
    tasks,
    loading,
    error,
    pollingInterval,
    
    // 计算属性
    totalTasks,
    runningTasks,
    completedTasks,
    failedTasks,
    cancelledTasks,
    getTaskById,
    
    // 方法
    setSelection,
    clearTasks,
    loadGraphs,
    loadTasks,
    createTask,
    deleteTask,
    cancelTask,
    startPolling,
    stopPolling,
    setPollingInterval,
    clearError,
    onUnmounted
  }
})