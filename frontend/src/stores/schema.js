import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { graphApi } from '../api/graph'

/**
 * Schema管理Store
 * 用于管理数据模型（点类型、边类型）的状态
 */
export const useSchemaStore = defineStore('schema', () => {
  // 当前选中的连接ID
  const selectedConnectionId = ref('')
  
  // 当前选中的图名称
  const selectedGraphName = ref('')
  
  // 图列表
  const graphs = ref([])
  
  // 图列表加载状态
  const graphsLoading = ref(false)
  
  // 点类型列表
  const vertexTypes = ref([])
  
  // 边类型列表
  const edgeTypes = ref([])
  
  // 加载状态
  const vertexLoading = ref(false)
  const edgeLoading = ref(false)
  
  // 错误信息
  const error = ref(null)
  
  // 计算属性：点类型数量
  const vertexTypeCount = computed(() => vertexTypes.value.length)
  
  // 计算属性：边类型数量
  const edgeTypeCount = computed(() => edgeTypes.value.length)
  
  // 计算属性：所有类型数量
  const totalTypeCount = computed(() => vertexTypeCount.value + edgeTypeCount.value)
  
  // 计算属性：根据名称获取点类型
  const getVertexTypeByName = computed(() => (name) => 
    vertexTypes.value.find(type => type.name === name)
  )
  
  // 计算属性：根据名称获取边类型
  const getEdgeTypeByName = computed(() => (name) => 
    edgeTypes.value.find(type => type.name === name)
  )
  
  /**
   * 设置当前选中的连接和图
   * @param {string} connectionId - 连接ID
   * @param {string} graphName - 图名称
   */
  const setSelection = async (connectionId, graphName) => {
    selectedConnectionId.value = connectionId
    selectedGraphName.value = graphName
    // 清空现有数据
    clearData()
    // 加载新连接的图列表
    if (connectionId) {
      await loadGraphs()
    }
  }
  
  /**
   * 清空所有数据
   */
  const clearData = () => {
    graphs.value = []
    vertexTypes.value = []
    edgeTypes.value = []
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
      graphs.value = res.data || []
    } catch (err) {
      error.value = `加载图列表失败: ${err.message}`
      console.error('加载图列表失败:', err)
    } finally {
      graphsLoading.value = false
    }
  }
  
  /**
   * 加载所有Schema数据
   */
  const loadAll = async () => {
    if (!selectedConnectionId.value || !selectedGraphName.value) {
      error.value = '请先选择连接和图'
      return
    }
    
    await Promise.all([loadVertexTypes(), loadEdgeTypes()])
  }
  
  /**
   * 加载点类型列表
   */
  const loadVertexTypes = async () => {
    if (!selectedConnectionId.value || !selectedGraphName.value) return
    
    vertexLoading.value = true
    error.value = null
    
    try {
      const res = await graphApi.getVertexTypes(selectedConnectionId.value, selectedGraphName.value)
      vertexTypes.value = res.data || []
    } catch (err) {
      error.value = `加载点类型失败: ${err.message}`
      console.error('加载点类型失败:', err)
    } finally {
      vertexLoading.value = false
    }
  }
  
  /**
   * 加载边类型列表
   */
  const loadEdgeTypes = async () => {
    if (!selectedConnectionId.value || !selectedGraphName.value) return
    
    edgeLoading.value = true
    error.value = null
    
    try {
      const res = await graphApi.getEdgeTypes(selectedConnectionId.value, selectedGraphName.value)
      edgeTypes.value = res.data || []
    } catch (err) {
      error.value = `加载边类型失败: ${err.message}`
      console.error('加载边类型失败:', err)
    } finally {
      edgeLoading.value = false
    }
  }
  
  /**
   * 创建点类型
   * @param {object} data - 点类型数据
   * @returns {Promise<object>} 创建结果
   */
  const createVertexType = async (data) => {
    if (!selectedConnectionId.value || !selectedGraphName.value) {
      throw new Error('请先选择连接和图')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.createVertexType(selectedConnectionId.value, selectedGraphName.value, data)
      // 重新加载点类型列表
      await loadVertexTypes()
      return res.data
    } catch (err) {
      error.value = `创建点类型失败: ${err.message}`
      console.error('创建点类型失败:', err)
      throw err
    }
  }
  
  /**
   * 创建边类型
   * @param {object} data - 边类型数据
   * @returns {Promise<object>} 创建结果
   */
  const createEdgeType = async (data) => {
    if (!selectedConnectionId.value || !selectedGraphName.value) {
      throw new Error('请先选择连接和图')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.createEdgeType(selectedConnectionId.value, selectedGraphName.value, data)
      // 重新加载边类型列表
      await loadEdgeTypes()
      return res.data
    } catch (err) {
      error.value = `创建边类型失败: ${err.message}`
      console.error('创建边类型失败:', err)
      throw err
    }
  }
  
  /**
   * 删除点类型
   * @param {string} labelName - 点类型名称
   * @returns {Promise<object>} 删除结果
   */
  const deleteVertexType = async (labelName) => {
    if (!selectedConnectionId.value || !selectedGraphName.value) {
      throw new Error('请先选择连接和图')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.deleteVertexType(selectedConnectionId.value, selectedGraphName.value, labelName)
      // 重新加载点类型列表
      await loadVertexTypes()
      return res.data
    } catch (err) {
      error.value = `删除点类型失败: ${err.message}`
      console.error('删除点类型失败:', err)
      throw err
    }
  }
  
  /**
   * 删除边类型
   * @param {string} labelName - 边类型名称
   * @returns {Promise<object>} 删除结果
   */
  const deleteEdgeType = async (labelName) => {
    if (!selectedConnectionId.value || !selectedGraphName.value) {
      throw new Error('请先选择连接和图')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.deleteEdgeType(selectedConnectionId.value, selectedGraphName.value, labelName)
      // 重新加载边类型列表
      await loadEdgeTypes()
      return res.data
    } catch (err) {
      error.value = `删除边类型失败: ${err.message}`
      console.error('删除边类型失败:', err)
      throw err
    }
  }
  
  /**
   * 重置错误状态
   */
  const clearError = () => {
    error.value = null
  }
  
  return {
    // 状态
    selectedConnectionId,
    selectedGraphName,
    graphs,
    graphsLoading,
    vertexTypes,
    edgeTypes,
    vertexLoading,
    edgeLoading,
    error,
    
    // 计算属性
    vertexTypeCount,
    edgeTypeCount,
    totalTypeCount,
    getVertexTypeByName,
    getEdgeTypeByName,
    
    // 方法
    setSelection,
    clearData,
    loadAll,
    loadGraphs,
    loadVertexTypes,
    loadEdgeTypes,
    createVertexType,
    createEdgeType,
    deleteVertexType,
    deleteEdgeType,
    clearError
  }
})