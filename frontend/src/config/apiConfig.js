/**
 * API 配置管理
 * 用于控制 Mock 模式和真实 API 模式的切换
 */

const CONFIG_KEY = 'graphdb_api_config'

// 默认配置
const defaultConfig = {
  useMock: true,           // 是否使用 Mock 模式（默认 true，使用 Mock 数据）
  mockDelay: 300,           // Mock 响应延迟（毫秒）
  realApiUrl: 'http://localhost:8080/api',  // 真实 API 基础 URL
  realApiTimeout: 30000     // 真实 API 请求超时时间（毫秒）
}

/**
 * 获取 API 配置
 * @returns {Object} 配置对象
 */
export function getApiConfig() {
  try {
    const savedConfig = localStorage.getItem(CONFIG_KEY)
    if (savedConfig) {
      return { ...defaultConfig, ...JSON.parse(savedConfig) }
    }
  } catch (error) {
    console.warn('读取 API 配置失败，使用默认配置:', error)
  }
  return { ...defaultConfig }
}

/**
 * 设置 API 配置
 * @param {Object} config - 配置对象
 */
export function setApiConfig(config) {
  const newConfig = { ...getApiConfig(), ...config }
  localStorage.setItem(CONFIG_KEY, JSON.stringify(newConfig))
  console.log('API 配置已更新:', newConfig)
}

/**
 * 切换 Mock/真实模式
 * @param {boolean} useMock - 是否使用 Mock 模式
 */
export function toggleApiMode(useMock) {
  setApiConfig({ useMock })
  return getApiConfig()
}

/**
 * 重置为默认配置
 */
export function resetApiConfig() {
  localStorage.removeItem(CONFIG_KEY)
  console.log('API 配置已重置为默认值')
}

/**
 * 获取当前模式标签
 * @returns {string} 'Mock' 或 'Real'
 */
export function getApiModeLabel() {
  return getApiConfig().useMock ? 'Mock' : 'Real'
}

/**
 * 获取 API 基础 URL
 * @returns {string} API URL
 */
export function getApiBaseUrl() {
  const config = getApiConfig()
  return config.useMock ? '' : config.realApiUrl
}

/**
 * 是否为 Mock 模式
 * @returns {boolean}
 */
export function isMockMode() {
  return getApiConfig().useMock
}

// 导出默认配置（供外部引用）
export { defaultConfig }