/**
 * 连接管理 Mock 数据
 */

import { mockDelay, mockSuccess, mockError } from './index'

// 模拟连接列表数据
let mockConnections = [
  {
    id: '1',
    name: 'Neo4j 本地测试',
    type: 'NEO4J',
    host: 'localhost',
    port: 7687,
    username: 'neo4j',
    database: 'neo4j',
    description: '本地 Neo4j 数据库连接',
    status: 1
  },
  {
    id: '2',
    name: 'NebulaGraph 测试环境',
    type: 'NEBULA',
    host: '192.168.1.100',
    port: 9669,
    username: 'root',
    database: 'test_graph',
    description: 'NebulaGraph 测试环境',
    status: 1
  },
  {
    id: '3',
    name: 'JanusGraph 集群',
    type: 'JANUS',
    host: '10.0.0.50',
    port: 8182,
    username: 'admin',
    database: 'social_graph',
    description: 'JanusGraph 分布式集群',
    status: 1,
    storageBackend: 'cql',
    indexBackend: 'elasticsearch',
    storageHost: 'cassandra.example.com',
    indexHost: 'elasticsearch.example.com'
  },
  {
    id: '4',
    name: '开发数据库',
    type: 'NEO4J',
    host: '127.0.0.1',
    port: 7687,
    username: 'developer',
    database: 'dev',
    description: '开发环境数据库',
    status: 0
  }
]

/**
 * Mock: 获取连接列表
 */
export const mockListConnections = async () => {
  await mockDelay()
  return mockSuccess([...mockConnections])
}

/**
 * Mock: 创建连接
 */
export const mockCreateConnection = async (data) => {
  await mockDelay()
  const newConnection = {
    id: Date.now().toString(),
    ...data,
    status: 1
  }
  mockConnections.push(newConnection)
  return mockSuccess(newConnection)
}

/**
 * Mock: 更新连接
 */
export const mockUpdateConnection = async (id, data) => {
  await mockDelay()
  const index = mockConnections.findIndex(c => c.id === id)
  if (index === -1) {
    return mockError('连接不存在', 404)
  }
  mockConnections[index] = { ...mockConnections[index], ...data }
  return mockSuccess(mockConnections[index])
}

/**
 * Mock: 删除连接
 */
export const mockDeleteConnection = async (id) => {
  await mockDelay()
  const index = mockConnections.findIndex(c => c.id === id)
  if (index === -1) {
    return mockError('连接不存在', 404)
  }
  mockConnections.splice(index, 1)
  return mockSuccess({ message: '删除成功' })
}

/**
 * Mock: 测试连接
 */
export const mockTestConnection = async (id) => {
  await mockDelay(500)  // 测试连接延迟更长
  const connection = mockConnections.find(c => c.id === id)
  if (!connection) {
    return mockError('连接不存在', 404)
  }
  // 随机测试成功
  const success = Math.random() > 0.2  // 80% 成功率
  connection.status = success ? 1 : 0
  return success
    ? mockSuccess({ message: '连接测试成功' })
    : mockError('连接测试失败：无法连接到服务器', 500)
}
