import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 全局搜索Store
 * 用于管理全局搜索功能的状态和逻辑
 */
export const useSearchStore = defineStore('search', () => {
  // 搜索关键词
  const keyword = ref('')
  
  // 搜索历史
  const searchHistory = ref([])
  
  // 搜索结果
  const searchResults = ref([])
  
  // 搜索状态
  const isLoading = ref(false)
  
  // 是否显示搜索结果
  const showResults = ref(false)
  
  // 最大历史记录数量
  const MAX_HISTORY = 10
  
  // 计算属性：是否有搜索关键词
  const hasKeyword = computed(() => keyword.value.trim().length > 0)
  
  // 计算属性：最近搜索
  const recentSearches = computed(() => 
    searchHistory.value.slice(0, 5).map(item => item.keyword)
  )
  
  /**
   * 执行搜索
   * @param {string} searchKeyword - 搜索关键词
   */
  const performSearch = async (searchKeyword) => {
    if (!searchKeyword.trim()) {
      return
    }
    
    keyword.value = searchKeyword.trim()
    isLoading.value = true
    showResults.value = true
    
    try {
      // 这里模拟搜索过程，实际应该调用搜索API
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 模拟搜索结果
      searchResults.value = generateMockResults(searchKeyword)
      
      // 添加到搜索历史
      addToHistory(searchKeyword)
    } catch (error) {
      console.error('搜索失败:', error)
      searchResults.value = []
    } finally {
      isLoading.value = false
    }
  }
  
  /**
   * 添加到搜索历史
   * @param {string} searchKeyword - 搜索关键词
   */
  const addToHistory = (searchKeyword) => {
    const trimmedKeyword = searchKeyword.trim()
    
    // 移除重复的历史记录
    searchHistory.value = searchHistory.value.filter(
      item => item.keyword.toLowerCase() !== trimmedKeyword.toLowerCase()
    )
    
    // 添加到历史记录开头
    searchHistory.value.unshift({
      keyword: trimmedKeyword,
      timestamp: Date.now(),
      resultCount: searchResults.value.length
    })
    
    // 限制历史记录数量
    if (searchHistory.value.length > MAX_HISTORY) {
      searchHistory.value = searchHistory.value.slice(0, MAX_HISTORY)
    }
    
    // 保存到localStorage
    saveHistoryToStorage()
  }
  
  /**
   * 清除搜索历史
   */
  const clearHistory = () => {
    searchHistory.value = []
    saveHistoryToStorage()
  }
  
  /**
   * 移除单个历史记录
   * @param {number} index - 历史记录索引
   */
  const removeHistoryItem = (index) => {
    if (index >= 0 && index < searchHistory.value.length) {
      searchResults.value.splice(index, 1)
      saveHistoryToStorage()
    }
  }
  
  /**
   * 隐藏搜索结果
   */
  const hideResults = () => {
    showResults.value = false
  }
  
  /**
   * 清除搜索
   */
  const clearSearch = () => {
    keyword.value = ''
    searchResults.value = []
    showResults.value = false
  }
  
  /**
   * 保存搜索历史到localStorage
   */
  const saveHistoryToStorage = () => {
    try {
      localStorage.setItem('search_history', JSON.stringify(searchHistory.value))
    } catch (error) {
      console.error('保存搜索历史失败:', error)
    }
  }
  
  /**
   * 从localStorage加载搜索历史
   */
  const loadHistoryFromStorage = () => {
    try {
      const storedHistory = localStorage.getItem('search_history')
      if (storedHistory) {
        searchHistory.value = JSON.parse(storedHistory)
      }
    } catch (error) {
      console.error('加载搜索历史失败:', error)
    }
  }
  
  /**
   * 生成模拟搜索结果
   * @param {string} keyword - 搜索关键词
   * @returns {Array} 搜索结果数组
   */
  const generateMockResults = (keyword) => {
    const lowerKeyword = keyword.toLowerCase()
    const mockData = [
      {
        id: 'conn-1',
        type: 'connection',
        title: '生产环境Neo4j',
        description: '生产环境的Neo4j图数据库连接',
        relevance: 0.95,
        tags: ['neo4j', '生产环境', '图数据库']
      },
      {
        id: 'graph-1',
        type: 'graph',
        title: '社交网络图',
        description: '包含用户、关系和社交数据的图实例',
        relevance: 0.88,
        tags: ['社交网络', '用户关系', '数据图']
      },
      {
        id: 'data-1',
        type: 'data',
        title: '用户节点数据',
        description: '包含用户基本信息的节点数据',
        relevance: 0.82,
        tags: ['用户', '节点', '个人信息']
      },
      {
        id: 'model-1',
        type: 'model',
        title: '电商数据模型',
        description: '电商平台的数据模型设计',
        relevance: 0.76,
        tags: ['电商', '数据模型', '设计']
      },
      {
        id: 'query-1',
        type: 'query',
        title: '热门商品查询',
        description: '查询热门商品的Cypher语句',
        relevance: 0.71,
        tags: ['cypher', '查询', '商品']
      }
    ]
    
    // 根据关键词过滤和排序
    return mockData
      .filter(item => 
        item.title.toLowerCase().includes(lowerKeyword) ||
        item.description.toLowerCase().includes(lowerKeyword) ||
        item.tags.some(tag => tag.toLowerCase().includes(lowerKeyword))
      )
      .sort((a, b) => b.relevance - a.relevance)
  }
  
  // 初始化时加载搜索历史
  loadHistoryFromStorage()
  
  return {
    // 状态
    keyword,
    searchHistory,
    searchResults,
    isLoading,
    showResults,
    
    // 计算属性
    hasKeyword,
    recentSearches,
    
    // 方法
    performSearch,
    addToHistory,
    clearHistory,
    removeHistoryItem,
    hideResults,
    clearSearch,
    saveHistoryToStorage,
    loadHistoryFromStorage
  }
})