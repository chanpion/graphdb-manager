import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getApiConfig } from '../config/apiConfig'
import {
  mockListConnections,
  mockCreateConnection,
  mockUpdateConnection,
  mockDeleteConnection,
  mockTestConnection
} from '../mock/connection'
import {
  mockListGraphs,
  mockGetGraphSchema,
  mockCreateGraph,
  mockDeleteGraph,
  mockGetVertexTypes,
  mockCreateVertexType,
  mockDeleteVertexType,
  mockGetEdgeTypes,
  mockCreateEdgeType,
  mockDeleteEdgeType
} from '../mock/graph'
import {
  mockQueryVertices,
  mockGetVertex,
  mockUpdateVertex,
  mockDeleteVertex,
  mockCreateVertex,
  mockQueryEdges,
  mockCreateEdge,
  mockGetEdge,
  mockUpdateEdge,
  mockDeleteEdge,
  mockGetDataStats,
  mockExecuteQuery,
  mockImportCsv,
  mockNativeQuery
} from '../mock/data'

const config = getApiConfig()

// 创建真实请求实例
const axiosRequest = axios.create({
  baseURL: config.realApiUrl,
  timeout: config.realApiTimeout
})

/**
 * Mock 路由处理器
 * 根据 URL 和方法返回对应的 Mock 数据
 */
const mockHandler = async (config) => {
  const { url, method, data, params } = config
  const urlLower = url.toLowerCase()

  // console.log(`[Mock] ${method?.toUpperCase() || 'GET'} ${url}`)

  // 连接管理 API
  if (urlLower.includes('/connections')) {
    if (method === 'get' && urlLower.match(/\/connections$/)) {
      return mockListConnections()
    } else if (method === 'post' && !urlLower.includes('/test')) {
      return mockCreateConnection(data)
    } else if (method === 'put') {
      const id = urlLower.match(/\/connections\/([^/]+)/)?.[1]
      return mockUpdateConnection(id, data)
    } else if (method === 'delete') {
      const id = urlLower.match(/\/connections\/([^/]+)/)?.[1]
      return mockDeleteConnection(id)
    } else if (method === 'post' && urlLower.includes('/test')) {
      const id = urlLower.match(/\/connections\/([^/]+)\/test/)?.[1]
      return mockTestConnection(id)
    }
  }

  // 图管理 API
  if (urlLower.includes('/graphs')) {
    if (method === 'get' && !urlLower.includes('/schema') && !urlLower.includes('/vertex-types') && !urlLower.includes('/edge-types') && !urlLower.includes('/data')) {
      const connectionId = urlLower.match(/\/connections\/([^/]+)/)?.[1]
      return mockListGraphs(connectionId)
    } else if (method === 'get' && urlLower.includes('/schema')) {
      const match = urlLower.match(/\/connections\/([^/]+)\/graphs\/([^/]+)\/schema/)
      if (match) {
        return mockGetGraphSchema(match[1], match[2])
      }
    } else if (method === 'post' && !urlLower.includes('/data') && !urlLower.includes('/types')) {
      const match = urlLower.match(/\/connections\/([^/]+)\/graphs/)
      if (match) {
        return mockCreateGraph(match[1], data)
      }
    } else if (method === 'delete' && !urlLower.includes('/data') && !urlLower.includes('/types')) {
      const match = urlLower.match(/\/connections\/([^/]+)\/graphs\/([^/]+)/)
      if (match) {
        return mockDeleteGraph(match[1], match[2])
      }
    }
  }

  // 点类型管理
  if (urlLower.includes('/vertex-types')) {
    const match = urlLower.match(/\/connections\/([^/]+)\/graphs\/([^/]+)\/vertex-types/)
    if (match) {
      if (method === 'get') {
        return mockGetVertexTypes(match[1], match[2])
      } else if (method === 'post') {
        return mockCreateVertexType(match[1], match[2], data)
      } else if (method === 'delete') {
        const labelName = urlLower.match(/\/vertex-types\/([^/]+)/)?.[1]
        return mockDeleteVertexType(match[1], match[2], labelName)
      }
    }
  }

  // 边类型管理
  if (urlLower.includes('/edge-types')) {
    const match = urlLower.match(/\/connections\/([^/]+)\/graphs\/([^/]+)\/edge-types/)
    if (match) {
      if (method === 'get') {
        return mockGetEdgeTypes(match[1], match[2])
      } else if (method === 'post') {
        return mockCreateEdgeType(match[1], match[2], data)
      } else if (method === 'delete') {
        const labelName = urlLower.match(/\/edge-types\/([^/]+)/)?.[1]
        return mockDeleteEdgeType(match[1], match[2], labelName)
      }
    }
  }

  // 数据查询 API
  if (urlLower.includes('/data/vertices')) {
    const match = urlLower.match(/\/connections\/([^/]+)\/graphs\/([^/]+)\/data\/vertices/)
    if (match) {
      if (method === 'get' && urlLower.endsWith('/vertices')) {
        return mockQueryVertices(match[1], match[2], params)
      } else if (method === 'get' && urlLower.match(/\/vertices\/[^/]+$/)) {
        const uid = urlLower.match(/\/vertices\/([^/]+)/)?.[1]
        return mockGetVertex(match[1], match[2], uid)
      } else if (method === 'put') {
        const uid = urlLower.match(/\/vertices\/([^/]+)/)?.[1]
        return mockUpdateVertex(match[1], match[2], uid, data)
      } else if (method === 'delete') {
        const uid = urlLower.match(/\/vertices\/([^/]+)/)?.[1]
        return mockDeleteVertex(match[1], match[2], uid)
      } else if (method === 'post') {
        return mockCreateVertex(match[1], match[2], data)
      }
    }
  }

  // 边数据 API
  if (urlLower.includes('/data/edges')) {
    const match = urlLower.match(/\/connections\/([^/]+)\/graphs\/([^/]+)\/data\/edges/)
    if (match) {
      if (method === 'get' && urlLower.endsWith('/edges')) {
        return mockQueryEdges(match[1], match[2], params)
      } else if (method === 'get' && urlLower.match(/\/edges\/[^/]+$/)) {
        const uid = urlLower.match(/\/edges\/([^/]+)/)?.[1]
        return mockGetEdge(match[1], match[2], uid)
      } else if (method === 'put') {
        const uid = urlLower.match(/\/edges\/([^/]+)/)?.[1]
        return mockUpdateEdge(match[1], match[2], uid, data)
      } else if (method === 'delete') {
        const uid = urlLower.match(/\/edges\/([^/]+)/)?.[1]
        return mockDeleteEdge(match[1], match[2], uid)
      } else if (method === 'post') {
        return mockCreateEdge(match[1], match[2], data)
      }
    }
  }

  if (urlLower.includes('/stats')) {
    const match = urlLower.match(/\/connections\/([^/]+)\/graphs\/([^/]+)\/stats/)
    if (match) {
      return mockGetDataStats(match[1], match[2])
    }
  }

  if (urlLower.includes('/query')) {
    const match = urlLower.match(/\/connections\/([^/]+)\/graphs\/([^/]+)\/query/)
    if (match) {
      return mockExecuteQuery(match[1], match[2], data?.query || '')
    }
  }

  if (urlLower.includes('/data/native-query')) {
    const match = urlLower.match(/\/connections\/([^/]+)\/graphs\/([^/]+)\/data\/native-query/)
    if (match) {
      if (method === 'post') {
        return mockNativeQuery(match[1], match[2], data)
      }
    }
  }

  if (urlLower.includes('/import-csv')) {
    const match = urlLower.match(/\/connections\/([^/]+)\/graphs\/([^/]+)\/data\/import-csv/)
    if (match) {
      if (method === 'post') {
        return mockImportCsv(match[1], match[2], data?.config || {}, data?.file)
      }
    }
  }

  // 未知路由，返回错误
  return Promise.reject(new Error(`[Mock] 未找到路由: ${url}`))
}

/**
 * 请求拦截器
 */
const requestInterceptors = {
  request: [
    config => {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    error => {
      return Promise.reject(error)
    }
  ],
  response: [
    response => {
      return response.data
    },
    error => {
      const message = error.response?.data?.message || error.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(error)
    }
  ]
}

// 应用拦截器到 axios
requestInterceptors.request.forEach(handler => axiosRequest.interceptors.request.use(handler[0], handler[1]))
requestInterceptors.response.forEach(handler => axiosRequest.interceptors.response.use(handler[0], handler[1]))

/**
 * 创建请求函数
 * 根据配置决定使用 Mock 数据还是真实请求
 */
const request = async (config) => {
  const currentConfig = getApiConfig()

  if (currentConfig.useMock) {
    // Mock 模式：返回 Mock 数据
    return mockHandler(config)
  } else {
    // 真实模式：发送真实请求
    return axiosRequest(config)
  }
}

export default request
