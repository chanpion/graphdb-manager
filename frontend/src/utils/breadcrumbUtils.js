/**
 * 面包屑导航工具函数
 * 用于生成和解析三层深度的面包屑路径
 */

/**
 * 路由配置映射表
 * 包含路由路径到显示名称的映射
 */
const routeNameMap = {
  '/': '首页',
  '/connections': '连接管理',
  '/graphs': '图管理',
  '/data-modeling': '图建模',
  '/data-explorer': '图数据'
}

/**
 * 获取路由对应的显示名称
 * @param {string} path - 路由路径
 * @returns {string} 显示名称
 */
export const getRouteDisplayName = (path) => {
  if (routeNameMap[path]) {
    return routeNameMap[path]
  }
  
  // 处理动态路由参数
  if (path.startsWith('/connections/')) {
    return '连接详情'
  }
  if (path.startsWith('/graphs/')) {
    return '图详情'
  }
  if (path.startsWith('/data-explorer/')) {
    return '数据详情'
  }
  
  // 默认返回路径的最后一部分
  const parts = path.split('/').filter(p => p)
  return parts.length > 0 ? decodeURIComponent(parts[parts.length - 1]) : '未知页面'
}

/**
 * 生成三层深度的面包屑节点
 * @param {string} currentPath - 当前路由路径
 * @param {Array} routeHistory - 路由历史记录（可选）
 * @returns {Array} 面包屑节点数组，最多3个节点
 */
export const generateBreadcrumbNodes = (currentPath, routeHistory = []) => {
  const nodes = []
  
  // 总是包含首页作为第一级
  nodes.push({
    title: '首页',
    path: '/',
    isLast: currentPath === '/'
  })
  
  if (currentPath === '/') {
    return nodes
  }
  
  // 解析当前路径的各个部分
  const pathParts = currentPath.split('/').filter(p => p)
  
  // 根据路径深度生成面包屑节点
  if (pathParts.length === 1) {
    // 一级路径：如 /connections
    const firstLevelPath = `/${pathParts[0]}`
    nodes.push({
      title: getRouteDisplayName(firstLevelPath),
      path: firstLevelPath,
      isLast: true
    })
  } else if (pathParts.length === 2) {
    // 二级路径：如 /connections/details
    const firstLevelPath = `/${pathParts[0]}`
    const secondLevelPath = `/${pathParts[0]}/${pathParts[1]}`
    
    nodes.push({
      title: getRouteDisplayName(firstLevelPath),
      path: firstLevelPath,
      isLast: false
    })
    
    nodes.push({
      title: getRouteDisplayName(secondLevelPath),
      path: secondLevelPath,
      isLast: true
    })
  } else if (pathParts.length >= 3) {
    // 三级或更多路径：只显示三层，第一层用省略号表示
    const firstLevelPath = `/${pathParts[0]}`
    const secondLevelPath = `/${pathParts[0]}/${pathParts[1]}`
    const thirdLevelPath = `/${pathParts.slice(0, 3).join('/')}`
    
    nodes.push({
      title: '...',
      path: firstLevelPath,
      isLast: false
    })
    
    nodes.push({
      title: getRouteDisplayName(secondLevelPath),
      path: secondLevelPath,
      isLast: false
    })
    
    nodes.push({
      title: getRouteDisplayName(thirdLevelPath),
      path: thirdLevelPath,
      isLast: true
    })
  }
  
  // 如果节点数量超过3个，则截取最后3个
  if (nodes.length > 3) {
    const lastThreeNodes = nodes.slice(-3)
    // 确保第一个节点不是省略号时才设置为省略号
    if (lastThreeNodes[0].title !== '...' && nodes.length > 3) {
      lastThreeNodes[0] = {
        ...lastThreeNodes[0],
        title: '...',
        isLast: false
      }
    }
    lastThreeNodes[lastThreeNodes.length - 1].isLast = true
    return lastThreeNodes
  }
  
  return nodes
}

/**
 * 根据路由历史智能生成面包屑路径
 * @param {Array} routePaths - 路由历史路径数组
 * @returns {Array} 优化后的面包屑节点数组
 */
export const generateSmartBreadcrumb = (routePaths) => {
  if (!routePaths || routePaths.length === 0) {
    return generateBreadcrumbNodes('/')
  }
  
  const currentPath = routePaths[routePaths.length - 1]
  const nodes = generateBreadcrumbNodes(currentPath, routePaths)
  
  // 如果当前是详情页，尝试从历史中获取父级信息
  if (currentPath.includes('/') && currentPath.split('/').filter(p => p).length > 2) {
    const parentPaths = routePaths.filter(path => 
      path !== currentPath && currentPath.startsWith(path)
    )
    
    if (parentPaths.length > 0) {
      const parentPath = parentPaths[parentPaths.length - 1]
      const parentNode = {
        title: getRouteDisplayName(parentPath),
        path: parentPath,
        isLast: false
      }
      
      // 尝试用父级节点替换省略号节点
      const updatedNodes = nodes.map(node => {
        if (node.title === '...' && node.path === parentPath) {
          return parentNode
        }
        return node
      })
      
      return updatedNodes
    }
  }
  
  return nodes
}

/**
 * 获取面包屑层级信息
 * @param {string} currentPath - 当前路径
 * @returns {Object} 层级信息
 */
export const getBreadcrumbLevelInfo = (currentPath) => {
  const pathParts = currentPath.split('/').filter(p => p)
  return {
    level: pathParts.length,
    isRoot: pathParts.length === 0,
    isFirstLevel: pathParts.length === 1,
    isSecondLevel: pathParts.length === 2,
    isThirdLevel: pathParts.length >= 3
  }
}

export default {
  getRouteDisplayName,
  generateBreadcrumbNodes,
  generateSmartBreadcrumb,
  getBreadcrumbLevelInfo
}