/**
 * 连接管理 Mock 数据
 */

import { mockDelay, mockSuccess, mockError } from './index'

// 模拟连接列表数据 - 与真实API返回格式保持一致
let mockConnections = [
  {
    id: 1,
    name: 'Neo4j 本地测试',
    databaseType: 'NEO4J',
    host: 'localhost',
    port: 7687,
    username: 'neo4j',
    password: 'password',
    databaseName: 'neo4j',
    description: '本地 Neo4j 数据库连接',
    status: 1,
    priority: 5,
    createdBy: 'system',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 2,
    name: 'NebulaGraph 测试环境',
    databaseType: 'NEBULA',
    host: '192.168.1.100',
    port: 9669,
    username: 'root',
    password: 'password',
    databaseName: 'test_graph',
    description: 'NebulaGraph 测试环境',
    status: 1,
    priority: 5,
    createdBy: 'system',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 3,
    name: 'JanusGraph 集群',
    databaseType: 'JANUS',
    host: '10.0.0.50',
    port: 8182,
    username: 'admin',
    password: 'password',
    databaseName: 'social_graph',
    description: 'JanusGraph 分布式集群',
    status: 1,
    storageBackend: 'cql',
    storageType: 'CASSANDRA',
    storageHost: 'cassandra.example.com',
    storageConfig: '{"keyspace":"janusgraph"}',
    extraParams: '{}',
    priority: 5,
    createdBy: 'system',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  },
  {
    id: 4,
    name: '开发数据库',
    databaseType: 'NEO4J',
    host: '127.0.0.1',
    port: 7687,
    username: 'developer',
    password: 'dev123',
    databaseName: 'dev',
    description: '开发环境数据库',
    status: 0,
    priority: 5,
    createdBy: 'system',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
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
    id: mockConnections.length > 0 ? Math.max(...mockConnections.map(c => c.id)) + 1 : 1,
    ...data,
    status: 1,
    priority: data.priority || 5,
    createdBy: 'system',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
  mockConnections.push(newConnection)
  return mockSuccess(newConnection)
}

/**
 * Mock: 更新连接
 */
export const mockUpdateConnection = async (id, data) => {
  await mockDelay()
  const index = mockConnections.findIndex(c => c.id === parseInt(id))
  if (index === -1) {
    return mockError('连接不存在', 404)
  }
  mockConnections[index] = { ...mockConnections[index], ...data, updatedAt: new Date().toISOString() }
  return mockSuccess(mockConnections[index])
}

/**
 * Mock: 删除连接
 */
export const mockDeleteConnection = async (id) => {
  await mockDelay()
  const index = mockConnections.findIndex(c => c.id === parseInt(id))
  if (index === -1) {
    return mockError('连接不存在', 404)
  }
  mockConnections.splice(index, 1)
  return mockSuccess(null)
}

/**
 * Mock: 测试连接
 */
export const mockTestConnection = async (id) => {
  await mockDelay(500)  // 测试连接延迟更长
  const connection = mockConnections.find(c => c.id === parseInt(id))
  if (!connection) {
    return mockError('连接不存在', 404)
  }
  // 随机测试成功
  const success = Math.random() > 0.2  // 80% 成功率
  connection.status = success ? 1 : 0
  return success
    ? mockSuccess(true)
    : mockError('连接测试失败：无法连接到服务器', 500)
}
