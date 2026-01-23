/**
 * Mock 数据入口
 * 统一导出所有 Mock 数据
 */

import { getApiConfig } from '../config/apiConfig'

/**
 * 模拟网络延迟
 * @param {number} delay - 延迟时间（毫秒）
 */
export const mockDelay = (delay = 300) => {
  const config = getApiConfig()
  return new Promise(resolve => {
    setTimeout(() => resolve(), delay)
  })
}

/**
 * 模拟成功响应
 * 与后端真实API格式保持一致
 * @param {*} data - 响应数据
 */
export const mockSuccess = (data) => {
  return {
    code: 0,
    message: '操作成功',
    data,
    timestamp: Date.now(),
    traceId: 'mock_' + Math.random().toString(36).substr(2, 16)
  }
}

/**
 * 模拟错误响应
 * @param {string} message - 错误信息
 * @param {number} code - 错误码
 */
export const mockError = (message = '请求失败', code = 500) => {
  return {
    code,
    message,
    data: null
  }
}
