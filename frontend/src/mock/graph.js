/**
 * 图管理 Mock 数据
 */

import { mockDelay, mockSuccess, mockError } from './index'

// 模拟图列表数据
const mockGraphs = [
  {
    name: 'social_network',
    databaseType: 'NEO4J',
    vertexCount: 12580,
    edgeCount: 34256,
    status: 'NORMAL',
    createdAt: new Date('2024-01-15T10:30:00').toISOString(),
    description: '社交网络图'
  },
  {
    name: 'product_graph',
    databaseType: 'NEBULA',
    vertexCount: 8432,
    edgeCount: 15678,
    status: 'NORMAL',
    createdAt: new Date('2024-02-20T14:20:00').toISOString(),
    description: '产品关系图'
  },
  {
    name: 'knowledge_base',
    databaseType: 'NEO4J',
    vertexCount: 45210,
    edgeCount: 78932,
    status: 'NORMAL',
    createdAt: new Date('2024-03-10T09:15:00').toISOString(),
    description: '知识库图'
  },
  {
    name: 'archived_graph',
    databaseType: 'JANUS',
    vertexCount: 5620,
    edgeCount: 9843,
    status: 'ARCHIVED',
    createdAt: new Date('2023-12-05T16:45:00').toISOString(),
    description: '已归档的图'
  }
]

// 模拟 Schema 数据
const mockSchemas = {
  social_network: {
    graphName: 'social_network',
    databaseType: 'NEO4J',
    vertexLabels: [
      {
        name: 'Person',
        type: 'VERTEX',
        properties: [
          { name: 'name', dataType: 'STRING', required: true, indexed: true, unique: false },
          { name: 'age', dataType: 'INTEGER', required: false, indexed: false, unique: false },
          { name: 'email', dataType: 'STRING', required: true, indexed: true, unique: true }
        ],
        description: '人员节点'
      },
      {
        name: 'Company',
        type: 'VERTEX',
        properties: [
          { name: 'name', dataType: 'STRING', required: true, indexed: true, unique: false },
          { name: 'industry', dataType: 'STRING', required: false, indexed: false, unique: false },
          { name: 'founded', dataType: 'DATE', required: false, indexed: false, unique: false }
        ],
        description: '公司节点'
      }
    ],
    edgeLabels: [
      {
        name: 'WORKS_AT',
        type: 'EDGE',
        properties: [
          { name: 'since', dataType: 'DATE', required: false, indexed: false, unique: false },
          { name: 'position', dataType: 'STRING', required: false, indexed: false, unique: false }
        ],
        description: '工作关系'
      },
      {
        name: 'KNOWS',
        type: 'EDGE',
        properties: [],
        description: '认识关系'
      }
    ]
  }
}

/**
 * Mock: 获取图列表
 */
export const mockListGraphs = async (connectionId) => {
  await mockDelay()
  console.log(`Mock: 获取连接 ${connectionId} 的图列表`)
  return mockSuccess([...mockGraphs])
}

/**
 * Mock: 获取图 Schema
 */
export const mockGetGraphSchema = async (connectionId, graphName) => {
  await mockDelay()
  console.log(`Mock: 获取图 ${graphName} 的 Schema`)
  const schema = mockSchemas[graphName] || {
    graphName,
    databaseType: 'NEO4J',
    vertexLabels: [],
    edgeLabels: []
  }
  return mockSuccess(schema)
}

/**
 * Mock: 创建图
 */
export const mockCreateGraph = async (connectionId, data) => {
  await mockDelay()
  console.log(`Mock: 在连接 ${connectionId} 创建图`, data)
  const newGraph = {
    graphName: data.graphName || `graph_${Date.now()}`,
    databaseType: 'NEO4J',
    vertexCount: 0,
    edgeCount: 0,
    status: 'NORMAL',
    createdAt: new Date().toISOString(),
    description: data.description || '新建图'
  }
  mockGraphs.unshift(newGraph)
  return mockSuccess(newGraph)
}

/**
 * Mock: 删除图
 */
export const mockDeleteGraph = async (connectionId, graphName) => {
  await mockDelay()
  console.log(`Mock: 删除图 ${graphName}`)
  const index = mockGraphs.findIndex(g => g.graphName === graphName)
  if (index === -1) {
    return mockError('图不存在', 404)
  }
  mockGraphs.splice(index, 1)
  return mockSuccess({ message: '删除成功' })
}

/**
 * Mock: 点类型管理
 */
export const mockGetVertexTypes = async (connectionId, graphName) => {
  await mockDelay()
  const schema = mockSchemas[graphName]
  return mockSuccess(schema?.vertexLabels || [])
}

export const mockCreateVertexType = async (connectionId, graphName, data) => {
  await mockDelay()
  const schema = mockSchemas[graphName]
  if (!schema) {
    return mockError('图不存在', 404)
  }
  schema.vertexLabels.push({
    ...data,
    properties: data.properties || []
  })
  return mockSuccess({ message: '创建成功' })
}

export const mockDeleteVertexType = async (connectionId, graphName, labelName) => {
  await mockDelay()
  const schema = mockSchemas[graphName]
  if (!schema) {
    return mockError('图不存在', 404)
  }
  schema.vertexLabels = schema.vertexLabels.filter(l => l.name !== labelName)
  return mockSuccess({ message: '删除成功' })
}

/**
 * Mock: 边类型管理
 */
export const mockGetEdgeTypes = async (connectionId, graphName) => {
  await mockDelay()
  const schema = mockSchemas[graphName]
  return mockSuccess(schema?.edgeLabels || [])
}

export const mockCreateEdgeType = async (connectionId, graphName, data) => {
  await mockDelay()
  const schema = mockSchemas[graphName]
  if (!schema) {
    return mockError('图不存在', 404)
  }
  schema.edgeLabels.push({
    ...data,
    properties: data.properties || []
  })
  return mockSuccess({ message: '创建成功' })
}

export const mockDeleteEdgeType = async (connectionId, graphName, labelName) => {
  await mockDelay()
  const schema = mockSchemas[graphName]
  if (!schema) {
    return mockError('图不存在', 404)
  }
  schema.edgeLabels = schema.edgeLabels.filter(l => l.name !== labelName)
  return mockSuccess({ message: '删除成功' })
}
