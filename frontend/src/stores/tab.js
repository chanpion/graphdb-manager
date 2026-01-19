import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

/**
 * 标签页管理Store
 * 用于管理全局多标签页状态
 */
export const useTabStore = defineStore('tab', () => {
  const router = useRouter()
  
  // 标签页列表
  const tabs = ref([])

  // 当前激活的标签页ID
  const activeTabId = ref('')
  
  // 固定标签页ID列表（不能关闭）
  const fixedTabIds = ref([])
  
  // 最大标签页数量
  const maxTabs = 5
  
  // 获取所有标签页
  const allTabs = computed(() => tabs.value)
  
  // 获取当前激活的标签页
  const activeTab = computed(() => tabs.value.find(tab => tab.id === activeTabId.value))
  
  // 获取固定标签页
  const fixedTabs = computed(() => tabs.value.filter(tab => fixedTabIds.value.includes(tab.id)))
  
  // 获取可关闭标签页
  const closableTabs = computed(() => tabs.value.filter(tab => !fixedTabIds.value.includes(tab.id)))
  
  /**
   * 添加或激活标签页
   * @param {Object} tabInfo - 标签页信息
   * @param {string} tabInfo.id - 标签页唯一ID
   * @param {string} tabInfo.title - 标签页标题
   * @param {string} tabInfo.path - 路由路径
   * @param {string} tabInfo.icon - 图标名称
   * @param {boolean} tabInfo.closable - 是否可关闭
   */
  const addOrActivateTab = (tabInfo) => {
    const { id, title, path, icon = '', closable = true } = tabInfo
    
    // 检查标签页是否已存在
    const existingTab = tabs.value.find(tab => tab.id === id)
    
    if (existingTab) {
      // 如果标签页已存在，激活它
      activeTabId.value = id
      // 如果需要，更新标签页信息
      if (title !== existingTab.title || path !== existingTab.path) {
        existingTab.title = title
        existingTab.path = path
        existingTab.icon = icon
      }
      // 跳转到对应路由
      if (router.currentRoute.value.path !== path) {
        router.push(path)
      }
    } else {
      // 创建新标签页
      const newTab = {
        id,
        title,
        path,
        icon,
        closable,
        createTime: Date.now(),
        lastActiveTime: Date.now()
      }
      
      // 添加到标签页列表
      tabs.value.push(newTab)
      
      // 如果超过最大标签页数量，移除最早的可关闭标签页
      if (tabs.value.length > maxTabs) {
        // 找到所有可关闭且非固定的标签页
        const closableTabs = tabs.value.filter(tab => 
          tab.closable && !fixedTabIds.value.includes(tab.id)
        )
        
        if (closableTabs.length > 0) {
          // 按创建时间升序排序，找到最早创建的标签页
          const sortedClosableTabs = closableTabs.sort((a, b) => a.createTime - b.createTime)
          const oldestClosableTab = sortedClosableTabs[0]
          
          const index = tabs.value.findIndex(tab => tab.id === oldestClosableTab.id)
          if (index !== -1) {
            tabs.value.splice(index, 1)
          }
        }
      }
      
      // 激活新标签页
      activeTabId.value = id
      
      // 跳转到对应路由
      router.push(path)
    }
    
    // 保存到sessionStorage
    saveTabsToStorage()
  }
  
  /**
   * 关闭标签页
   * @param {string} tabId - 要关闭的标签页ID
   * @returns {boolean} 是否关闭成功
   */
  const closeTab = (tabId) => {
    // 检查是否为固定标签页
    if (fixedTabIds.value.includes(tabId)) {
      console.warn(`标签页 ${tabId} 是固定标签页，不能关闭`)
      return false
    }
    
    // 查找标签页索引
    const tabIndex = tabs.value.findIndex(tab => tab.id === tabId)
    
    if (tabIndex === -1) {
      console.warn(`标签页 ${tabId} 不存在`)
      return false
    }
    
    // 如果要关闭的是当前激活的标签页，需要激活另一个标签页
    if (activeTabId.value === tabId) {
      // 尝试激活前一个标签页
      if (tabIndex > 0) {
        activeTabId.value = tabs.value[tabIndex - 1].id
        router.push(tabs.value[tabIndex - 1].path)
      }
      // 如果没有前一个标签页，尝试激活后一个标签页
      else if (tabs.value.length > 1) {
        activeTabId.value = tabs.value[1].id
        router.push(tabs.value[1].path)
      }
      // 如果只剩下一个标签页，清空激活状态
      else {
        activeTabId.value = ''
        router.push('/')
      }
    }
    
    // 从标签页列表中移除
    tabs.value.splice(tabIndex, 1)
    
    // 保存到sessionStorage
    saveTabsToStorage()
    
    return true
  }
  
  /**
   * 关闭其他标签页
   * @param {string} tabId - 要保留的标签页ID
   */
  const closeOtherTabs = (tabId) => {
    // 保留固定标签页和指定的标签页
    const tabsToKeep = tabs.value.filter(tab => 
      fixedTabIds.value.includes(tab.id) || tab.id === tabId
    )
    
    tabs.value = tabsToKeep
    
    // 激活指定标签页
    if (tabId && tabs.value.find(tab => tab.id === tabId)) {
      activeTabId.value = tabId
      router.push(tabs.value.find(tab => tab.id === tabId).path)
    }
    
    saveTabsToStorage()
  }
  
  /**
   * 关闭所有标签页
   */
  const closeAllTabs = () => {
    // 只保留固定标签页
    const tabsToKeep = tabs.value.filter(tab => fixedTabIds.value.includes(tab.id))
    
    tabs.value = tabsToKeep
    
    // 如果有固定标签页，激活第一个
    if (tabs.value.length > 0) {
      activeTabId.value = tabs.value[0].id
      router.push(tabs.value[0].path)
    } else {
      activeTabId.value = ''
      router.push('/')
    }
    
    saveTabsToStorage()
  }
  
  /**
   * 激活标签页
   * @param {string} tabId - 要激活的标签页ID
   */
  const activateTab = (tabId) => {
    const tab = tabs.value.find(tab => tab.id === tabId)
    if (tab) {
      activeTabId.value = tabId
      router.push(tab.path)
      
      // 更新最后激活时间
      tab.lastActiveTime = Date.now()
    }
  }
  
  /**
   * 刷新标签页
   * @param {string} tabId - 要刷新的标签页ID
   */
  const refreshTab = (tabId) => {
    const tab = tabs.value.find(tab => tab.id === tabId)
    if (tab) {
      // 重新跳转到当前路由
      router.push(tab.path)
      
      // 触发组件重新加载
      // 这里可以通过事件总线或其他方式通知页面组件刷新
    }
  }
  
  /**
   * 更新标签页标题
   * @param {string} tabId - 标签页ID
   * @param {string} newTitle - 新标题
   */
  const updateTabTitle = (tabId, newTitle) => {
    const tab = tabs.value.find(tab => tab.id === tabId)
    if (tab) {
      tab.title = newTitle
      saveTabsToStorage()
    }
  }
  
  /**
   * 更新标签页路径
   * @param {string} tabId - 标签页ID
   * @param {string} newPath - 新路径
   */
  const updateTabPath = (tabId, newPath) => {
    const tab = tabs.value.find(tab => tab.id === tabId)
    if (tab) {
      tab.path = newPath
      saveTabsToStorage()
    }
  }
  
  /**
   * 根据路由路径获取标签页ID
   * @param {string} path - 路由路径
   * @returns {string} 标签页ID
   */
  const getTabIdByPath = (path) => {
    // 简单的映射逻辑，可以根据需要扩展
    const routeMapping = {
      '/': 'home',
      '/connections': 'connections',
      '/graphs': 'graphs',
      '/data-modeling': 'data-modeling',
      '/data-explorer': 'data-explorer'
    }
    
    return routeMapping[path] || path.replace(/\//g, '-').substring(1) || 'unknown'
  }
  
  /**
   * 根据路由路径获取标签页标题
   * @param {string} path - 路由路径
   * @returns {string} 标签页标题
   */
  const getTabTitleByPath = (path) => {
    const routeNameMap = {
      '/': '首页',
      '/connections': '连接管理',
      '/graphs': '图管理',
      '/data-modeling': '图建模',
      '/data-explorer': '图数据'
    }
    
    return routeNameMap[path] || '未知页面'
  }
  
  /**
   * 保存标签页状态到sessionStorage
   */
  const saveTabsToStorage = () => {
    try {
      sessionStorage.setItem('tab_store', JSON.stringify({
        tabs: tabs.value,
        activeTabId: activeTabId.value,
        fixedTabIds: fixedTabIds.value
      }))
    } catch (error) {
      console.error('保存标签页状态失败:', error)
    }
  }
  
  /**
   * 从sessionStorage加载标签页状态
   */
  const loadTabsFromStorage = () => {
    try {
      const storedData = sessionStorage.getItem('tab_store')
      if (storedData) {
        const { tabs: storedTabs, activeTabId: storedActiveTabId, fixedTabIds: storedFixedTabIds } = JSON.parse(storedData)
        
        if (Array.isArray(storedTabs)) {
          tabs.value = storedTabs
        }
        
        if (storedActiveTabId) {
          activeTabId.value = storedActiveTabId
        }
        
        if (Array.isArray(storedFixedTabIds)) {
          fixedTabIds.value = storedFixedTabIds
        }
      }
    } catch (error) {
      console.error('加载标签页状态失败:', error)
    }
  }
  
  /**
   * 初始化标签页（添加首页标签）
   */
  const initTabs = () => {
    // 确保首页标签存在
    const homeTabExists = tabs.value.some(tab => tab.id === 'home')
    if (!homeTabExists) {
      // 检查是否超过最大标签页数量
      if (tabs.value.length >= maxTabs) {
        // 找到所有可关闭且非固定的标签页
        const closableTabs = tabs.value.filter(tab => 
          tab.closable && !fixedTabIds.value.includes(tab.id)
        )
        
        if (closableTabs.length > 0) {
          // 按创建时间升序排序，找到最早创建的标签页
          const sortedClosableTabs = closableTabs.sort((a, b) => a.createTime - b.createTime)
          const oldestClosableTab = sortedClosableTabs[0]
          
          const index = tabs.value.findIndex(tab => tab.id === oldestClosableTab.id)
          if (index !== -1) {
            tabs.value.splice(index, 1)
          }
        }
      }
      
      tabs.value.push({
        id: 'home',
        title: '首页',
        path: '/',
        icon: 'house',
        closable: true,
        createTime: Date.now(),
        lastActiveTime: Date.now()
      })
    }
    
    // 如果没有激活的标签页，激活首页
    if (!activeTabId.value && tabs.value.length > 0) {
      activeTabId.value = tabs.value[0].id
    }
  }
  
  // 初始化
  loadTabsFromStorage()
  initTabs()
  
  return {
    // 状态
    tabs,
    activeTabId,
    fixedTabIds,
    
    // 计算属性
    allTabs,
    activeTab,
    fixedTabs,
    closableTabs,
    
    // 方法
    addOrActivateTab,
    closeTab,
    closeOtherTabs,
    closeAllTabs,
    activateTab,
    refreshTab,
    updateTabTitle,
    updateTabPath,
    getTabIdByPath,
    getTabTitleByPath,
    saveTabsToStorage,
    loadTabsFromStorage,
    initTabs
  }
})