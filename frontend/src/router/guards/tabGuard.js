import { useTabStore } from '@/stores/tab'

/**
 * 标签页路由守卫
 * 用于在路由变化时自动管理标签页状态
 */
export const tabGuard = {
  /**
   * 路由后置守卫
   * 在路由跳转后执行
   */
  afterRouteChange: (to, _from) => {
    const tabStore = useTabStore()
    
    // 避免重复添加相同的标签页
    const existingTab = tabStore.tabs.find(tab => tab.path === to.path)
    
    if (existingTab) {
      // 如果标签页已存在，直接激活它（不调用 activateTab 方法避免路由循环）
      // 只有当当前激活的标签页不是目标标签页时才更新
      if (tabStore.activeTabId !== existingTab.id) {
        tabStore.activeTabId = existingTab.id
        // 更新最后激活时间
        existingTab.lastActiveTime = Date.now()
        // 保存到sessionStorage
        tabStore.saveTabsToStorage()
      }
      return
    }
    
    // 根据路由信息生成标签页ID和标题
    const tabId = tabStore.getTabIdByPath(to.path)
    const tabTitle = tabStore.getTabTitleByPath(to.path)
    
    // 只添加标签页到列表，不触发路由跳转（避免循环）
    const newTab = {
      id: tabId,
      title: tabTitle,
      path: to.path,
      icon: getIconByPath(to.path),
      closable: true,
      createTime: Date.now(),
      lastActiveTime: Date.now()
    }
    
    // 添加到标签页列表
    tabStore.tabs.push(newTab)
    
    // 激活新标签页
    tabStore.activeTabId = tabId
    
    // 保存到sessionStorage
    tabStore.saveTabsToStorage()
  }
}

/**
 * 根据路由路径获取对应的图标名称
 * @param {string} path - 路由路径
 * @returns {string} 图标名称
 */
function getIconByPath(path) {
  const iconMapping = {
    '/': 'house',
    '/connections': 'connection',
    '/graphs': 'grid',
    '/data-modeling': 'data-line',
    '/data-explorer': 'search',
    '/graph-visualization': 'share'
  }
  
  // 检查完全匹配
  if (iconMapping[path]) {
    return iconMapping[path]
  }
  
  // 检查路径前缀匹配
  for (const [routePath, icon] of Object.entries(iconMapping)) {
    if (path.startsWith(routePath) && routePath !== '/') {
      return icon
    }
  }
  
  return 'house' // 默认图标
}

/**
 * 安装路由守卫
 * @param {Object} router - Vue Router实例
 */
export const installTabGuard = (router) => {
  // 在每次路由跳转后执行标签页管理逻辑
  router.afterEach((to, from) => {
    // 延迟执行，确保组件已经挂载
    setTimeout(() => {
      tabGuard.afterRouteChange(to, from)
    }, 100)
  })
  
  console.log('标签页路由守卫已安装')
}

export default {
  tabGuard,
  installTabGuard
}