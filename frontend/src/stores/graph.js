import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { graphApi } from '../api/graph'

/**
 * 图可视化管理Store
 * 用于管理图可视化状态，包括路径高亮、子图展示等
 */
export const useGraphStore = defineStore('graph', () => {
  // 当前选中的连接ID
  const selectedConnectionId = ref('')
  
  // 当前选中的图名称
  const selectedGraphName = ref('')
  
  // 当前选中的图对象
  const currentGraph = ref(null)
  
  // 图列表
  const graphs = ref([])
  
  // 查询语言映射表
  const graphLanguageMap = ref({
    '社交网络': 'cypher',
    '推荐系统': 'cypher',
    '知识图谱': 'cypher',
    '金融风控': 'cypher',
    'default': 'cypher'
  })
  
  // 当前查询语言（自动根据选中的图确定）
  const currentQueryLanguage = computed(() => {
    return graphLanguageMap.value[selectedGraphName.value] || graphLanguageMap.value.default
  })
  
  // 高亮的路径（节点ID数组）
  const highlightedPath = ref([])
  
  // 已展开的子图ID集合
  const expandedSubgraphs = ref(new Set())
  
  // 选中的节点ID
  const selectedNode = ref(null)
  
  // 图布局算法
  const layoutAlgorithm = ref('force-directed')
  
  // 加载状态
  const loading = ref(false)
  
  // 错误信息
  const error = ref(null)
  
  // 计算属性：是否有高亮路径
  const hasHighlightedPath = computed(() => highlightedPath.value.length > 0)
  
  // 计算属性：已展开子图数量
  const expandedSubgraphCount = computed(() => expandedSubgraphs.value.size)
  
  // 计算属性：获取当前图名称
  const currentGraphName = computed(() => currentGraph.value?.name || '')
  
  // 计算属性：是否有选中的连接和图
  const hasSelection = computed(() => selectedConnectionId.value && selectedGraphName.value)
  
  /**
   * 设置当前选中的连接和图
   * @param {string} connectionId - 连接ID
   * @param {string} graphName - 图名称
   */
  const setSelection = (connectionId, graphName) => {
    selectedConnectionId.value = connectionId
    selectedGraphName.value = graphName
    // 清空现有数据
    clearData()
  }
  
  /**
   * 清空所有数据
   */
  const clearData = () => {
    graphs.value = []
    currentGraph.value = null
    error.value = null
    resetVisualizationState()
  }
  
  /**
   * 加载图列表
   * @param {string} sourceType 图来源类型（PLATFORM/EXISTING），可选
   */
  const loadGraphs = async (sourceType) => {
    if (!selectedConnectionId.value) {
      error.value = '请先选择连接'
      return
    }
    
    loading.value = true
    error.value = null
    
    try {
      const res = await graphApi.list(selectedConnectionId.value, sourceType)
      graphs.value = res.data || []
    } catch (err) {
      error.value = `加载图列表失败: ${err.message}`
      console.error('加载图列表失败:', err)
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 加载所有连接的图列表
   */
  const loadAllGraphs = async (sourceType) => {
    loading.value = true
    error.value = null
    
    try {
      // 获取所有连接（需要从连接API获取）
      const connectionRes = await graphApi.getConnections()
      const allConnections = connectionRes.data || []
      
      const allGraphs = []
      
      // 遍历所有连接，加载每个连接的图
      for (const conn of allConnections) {
        try {
          const res = await graphApi.list(conn.id, sourceType)
          const connGraphs = (res.data || []).map(graph => ({
            ...graph,
            connectionId: conn.id,
            connectionName: conn.name
          }))
          allGraphs.push(...connGraphs)
        } catch (err) {
          console.error(`加载连接 ${conn.name} 的图失败:`, err)
          // 继续加载其他连接的图
        }
      }
      
      graphs.value = allGraphs
    } catch (err) {
      error.value = `加载所有图列表失败: ${err.message}`
      console.error('加载所有图列表失败:', err)
    } finally {
      loading.value = false
    }
  }
  
  /**
   * 创建图
   * @param {object} data - 图数据
   * @returns {Promise<object>} 创建结果
   */
  const createGraph = async (data) => {
    if (!selectedConnectionId.value) {
      throw new Error('请先选择连接')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.createGraph(selectedConnectionId.value, data)
      // 重新加载图列表
      await loadGraphs()
      return res.data
    } catch (err) {
      error.value = `创建图失败: ${err.message}`
      console.error('创建图失败:', err)
      throw err
    }
  }
  
  /**
   * 删除图
   * @param {string} graphName - 图名称
   * @returns {Promise<object>} 删除结果
   */
  const deleteGraph = async (graphName) => {
    if (!selectedConnectionId.value) {
      throw new Error('请先选择连接')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.deleteGraph(selectedConnectionId.value, graphName)
      // 重新加载图列表
      await loadGraphs()
      return res.data
    } catch (err) {
      error.value = `删除图失败: ${err.message}`
      console.error('删除图失败:', err)
      throw err
    }
  }
  
  /**
   * 更新图
   * @param {object} data - 图数据 { oldGraphName, newGraphName, description }
   * @returns {Promise<object>} 更新结果
   */
  const updateGraph = async (data) => {
    if (!selectedConnectionId.value) {
      throw new Error('请先选择连接')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.updateGraph(selectedConnectionId.value, data)
      // 重新加载图列表
      await loadGraphs()
      return res.data
    } catch (err) {
      error.value = `更新图失败: ${err.message}`
      console.error('更新图失败:', err)
      throw err
    }
  }
  
  /**
   * 加载图Schema
   * @param {string} graphName - 图名称
   * @returns {Promise<object>} Schema数据
   */
  const loadGraphSchema = async (graphName) => {
    if (!selectedConnectionId.value || !graphName) {
      throw new Error('请先选择连接和图')
    }
    
    error.value = null
    
    try {
      const res = await graphApi.getGraphSchema(selectedConnectionId.value, graphName)
      return res.data
    } catch (err) {
      error.value = `加载图Schema失败: ${err.message}`
      console.error('加载图Schema失败:', err)
      throw err
    }
  }
  
  /**
   * 设置当前图
   * @param {object} graph - 图对象
   */
  const setCurrentGraph = (graph) => {
    currentGraph.value = graph
  }
  
  /**
   * 设置图列表
   * @param {Array} graphList - 图列表
   */
  const setGraphs = (graphList) => {
    graphs.value = graphList
  }
  
  /**
   * 设置高亮路径
   * @param {Array} path - 节点ID数组
   */
  const setHighlightedPath = (path) => {
    highlightedPath.value = Array.isArray(path) ? path : []
  }
  
  /**
   * 清除高亮路径
   */
  const clearHighlightedPath = () => {
    highlightedPath.value = []
  }
  
  /**
   * 添加子图到展开集合
   * @param {string} subgraphId - 子图ID
   */
  const expandSubgraph = (subgraphId) => {
    expandedSubgraphs.value.add(subgraphId)
  }
  
  /**
   * 从展开集合中移除子图
   * @param {string} subgraphId - 子图ID
   */
  const collapseSubgraph = (subgraphId) => {
    expandedSubgraphs.value.delete(subgraphId)
  }
  
  /**
   * 切换子图展开状态
   * @param {string} subgraphId - 子图ID
   */
  const toggleSubgraph = (subgraphId) => {
    if (expandedSubgraphs.value.has(subgraphId)) {
      collapseSubgraph(subgraphId)
    } else {
      expandSubgraph(subgraphId)
    }
  }
  
  /**
   * 设置选中的节点
   * @param {string} nodeId - 节点ID
   */
  const setSelectedNode = (nodeId) => {
    selectedNode.value = nodeId
  }
  
  /**
   * 清除选中的节点
   */
  const clearSelectedNode = () => {
    selectedNode.value = null
  }
  
  /**
   * 设置布局算法
   * @param {string} algorithm - 布局算法名称
   */
  const setLayoutAlgorithm = (algorithm) => {
    layoutAlgorithm.value = algorithm
  }
  
  /**
   * 重置所有可视化状态
   */
  const resetVisualizationState = () => {
    highlightedPath.value = []
    expandedSubgraphs.value.clear()
    selectedNode.value = null
    layoutAlgorithm.value = 'force-directed'
    error.value = null
  }
  
  /**
   * 设置错误信息
   * @param {string} message - 错误消息
   */
  const setError = (message) => {
    error.value = message
  }
  
  /**
   * 清除错误信息
   */
  const clearError = () => {
    error.value = null
  }
  
  return {
    // 状态 (直接返回 ref)
    selectedConnectionId,
    selectedGraphName,
    currentGraph,
    graphs,
    highlightedPath,
    expandedSubgraphs,
    selectedNode,
    layoutAlgorithm,
    loading,
    error,
    graphLanguageMap,
    
    // 计算属性
    hasHighlightedPath,
    expandedSubgraphCount,
    currentGraphName,
    hasSelection,
    currentQueryLanguage,
    
    // 方法
    setSelection,
    clearData,
    loadGraphs,
    loadAllGraphs,
    createGraph,
    deleteGraph,
    updateGraph,
    loadGraphSchema,
    setCurrentGraph,
    setGraphs,
    setHighlightedPath,
    clearHighlightedPath,
    expandSubgraph,
    collapseSubgraph,
    toggleSubgraph,
    setSelectedNode,
    clearSelectedNode,
    setLayoutAlgorithm,
    resetVisualizationState,
    setError,
    clearError
  }
})
