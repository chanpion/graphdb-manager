/**
 * 数据浏览器API模拟器
 * 用于在开发或演示环境中提供模拟的查询结果数据
 */

import { nanoid } from 'nanoid'

// 模拟图数据库结果数据
const mockGraphData = {
  nodes: [
    { id: 'person_1', label: 'Person', name: 'Alice', age: 30, city: 'Beijing', occupation: 'Engineer' },
    { id: 'person_2', label: 'Person', name: 'Bob', age: 25, city: 'Shanghai', occupation: 'Designer' },
    { id: 'person_3', label: 'Person', name: 'Charlie', age: 35, city: 'Guangzhou', occupation: 'Manager' },
    { id: 'company_1', label: 'Company', name: 'TechCorp', industry: 'Technology', employees: 1000, founded: 2010 },
    { id: 'company_2', label: 'Company', name: 'DesignStudio', industry: 'Creative', employees: 200, founded: 2015 },
    { id: 'product_1', label: 'Product', name: 'AppSuite', category: 'Software', price: 299.99, version: '2.0' },
    { id: 'product_2', label: 'Product', name: 'DesignTool', category: 'Tool', price: 149.99, version: '1.5' }
  ],
  edges: [
    { id: 'edge_1', label: 'WORKS_AT', source: 'person_1', target: 'company_1', since: 2018, position: 'Senior Engineer' },
    { id: 'edge_2', label: 'WORKS_AT', source: 'person_2', target: 'company_2', since: 2020, position: 'Lead Designer' },
    { id: 'edge_3', label: 'WORKS_AT', source: 'person_3', target: 'company_1', since: 2015, position: 'Director' },
    { id: 'edge_4', label: 'KNOWS', source: 'person_1', target: 'person_2', since: 2019, relationship: 'Colleague' },
    { id: 'edge_5', label: 'KNOWS', source: 'person_2', target: 'person_3', since: 2021, relationship: 'Friend' },
    { id: 'edge_6', label: 'PURCHASED', source: 'person_1', target: 'product_1', date: '2023-01-15', quantity: 1 },
    { id: 'edge_7', label: 'PURCHASED', source: 'company_2', target: 'product_2', date: '2023-02-20', quantity: 10 }
  ]
}

// 模拟查询结果数据
const mockQueryResults = {
  // Cypher查询结果
  CYPHER: [
    [
      { id: 'person_1', label: 'Person', name: 'Alice', age: 30, city: 'Beijing' },
      { id: 'person_2', label: 'Person', name: 'Bob', age: 25, city: 'Shanghai' },
      { id: 'person_3', label: 'Person', name: 'Charlie', age: 35, city: 'Guangzhou' }
    ],
    [
      { id: 'company_1', label: 'Company', name: 'TechCorp', industry: 'Technology' },
      { id: 'company_2', label: 'Company', name: 'DesignStudio', industry: 'Creative' }
    ]
  ],
  // Gremlin查询结果
  GREMLIN: [
    [
      { id: 'v[1]', label: 'person', name: 'Alice', properties: { age: 30, city: 'Beijing' } },
      { id: 'v[2]', label: 'person', name: 'Bob', properties: { age: 25, city: 'Shanghai' } }
    ],
    [
      { id: 'e[1]', label: 'knows', properties: { since: 2019 } },
      { id: 'e[2]', label: 'works_at', properties: { since: 2020 } }
    ]
  ],
  // nGQL查询结果
  NEBULA: [
    [
      { vid: 'player100', tag: 'player', name: 'Tim Duncan', age: 42 },
      { vid: 'player101', tag: 'player', name: 'Tony Parker', age: 36 },
      { vid: 'player102', tag: 'player', name: 'LaMarcus Aldridge', age: 33 }
    ],
    [
      { src: 'player100', dst: 'player101', type: 'serve', properties: { start_year: 1997, end_year: 2016 } }
    ]
  ]
}

// 模拟延迟函数
const simulateDelay = (min = 300, max = 800) => {
  return new Promise(resolve => {
    const delay = Math.floor(Math.random() * (max - min + 1)) + min
    setTimeout(resolve, delay)
  })
}

/**
 * 获取模拟的图数据
 * @param {string} graphName - 图名称
 * @returns {Promise<{nodes: Array, edges: Array}>} 图数据
 */
export async function getMockGraphData(graphName = 'default') {
  await simulateDelay()
  
  return {
    code: 200,
    message: 'success',
    data: {
      nodes: mockGraphData.nodes,
      edges: mockGraphData.edges
    }
  }
}

/**
 * 执行模拟的查询
 * @param {string} language - 查询语言 (CYPHER, GREMLIN, NEBULA)
 * @param {string} query - 查询语句
 * @param {string} graphName - 图名称
 * @returns {Promise<{data: Array, executionTime: number, success: boolean}>} 查询结果
 */
export async function executeMockQuery(language, query, graphName = 'default') {
  await simulateDelay(500, 1500)
  
  // 根据查询语言选择不同的模拟数据
  const languageKey = language.toUpperCase()
  const languageResults = mockQueryResults[languageKey] || mockQueryResults.CYPHER
  
  // 根据查询内容选择不同的结果集（简单模拟）
  let resultSet = languageResults[0]
  if (query.includes('Company') || query.includes('company')) {
    resultSet = languageResults[1] || languageResults[0]
  }
  
  // 如果是Gremlin查询，调整数据结构
  if (languageKey === 'GREMLIN') {
    resultSet = resultSet.map(item => ({
      ...item,
      properties: item.properties || {}
    }))
  }
  
  // 如果是nGQL查询，调整数据结构
  if (languageKey === 'NEBULA') {
    resultSet = resultSet.map(item => ({
      ...item,
      tag: item.tag || 'unknown'
    }))
  }
  
  const executionTime = Math.floor(Math.random() * 300) + 100 // 100-400ms
  
  return {
    code: 200,
    message: 'success',
    data: resultSet,
    executionTime,
    success: true
  }
}

/**
 * 获取模拟的查询模板
 * @returns {Promise<Array>} 查询模板列表
 */
export async function getMockQueryTemplates() {
  await simulateDelay(200, 500)
  
  return {
    code: 200,
    message: 'success',
    data: [
      {
        id: '1',
        name: '查询所有节点',
        language: 'CYPHER',
        code: 'MATCH (n) RETURN n LIMIT 100',
        description: '返回图中所有节点，限制100个'
      },
      {
        id: '2',
        name: '查询特定标签节点',
        language: 'CYPHER',
        code: 'MATCH (n:Person) RETURN n LIMIT 100',
        description: '返回所有Person标签的节点'
      },
      {
        id: '3',
        name: '查询节点关系',
        language: 'CYPHER',
        code: 'MATCH (p:Person)-[r:KNOWS]->(o:Person) RETURN p, r, o LIMIT 100',
        description: '返回Person节点之间的KNOWS关系'
      },
      {
        id: '4',
        name: '最短路径查询',
        language: 'CYPHER',
        code: 'MATCH (start:Person {name: "Alice"}), (end:Person {name: "Bob"})\nMATCH path = shortestPath((start)-[*..5]-(end))\nRETURN path',
        description: '查找Alice到Bob的最短路径'
      },
      {
        id: '5',
        name: '聚合统计',
        language: 'CYPHER',
        code: 'MATCH (n:Person)\nRETURN n.name, count(*) as count\nORDER BY count DESC\nLIMIT 10',
        description: '按名称聚合统计Person节点'
      },
      {
        id: '6',
        name: 'Gremlin - 查询所有顶点',
        language: 'GREMLIN',
        code: 'g.V().limit(100)',
        description: '返回所有顶点，限制100个'
      },
      {
        id: '7',
        name: 'Gremlin - 查询特定标签顶点',
        language: 'GREMLIN',
        code: 'g.V().hasLabel("Person").limit(100)',
        description: '返回Person标签的顶点'
      },
      {
        id: '8',
        name: 'nGQL - 查询所有标签',
        language: 'NEBULA',
        code: 'SHOW TAGS',
        description: '显示所有标签'
      },
      {
        id: '9',
        name: 'nGQL - 查询点数据',
        language: 'NEBULA',
        code: 'FETCH PROP ON player "player100"',
        description: '获取player100点的属性'
      }
    ]
  }
}

/**
 * 获取模拟的查询历史
 * @returns {Promise<Array>} 查询历史列表
 */
export async function getMockQueryHistory() {
  await simulateDelay(100, 300)
  
  const now = Date.now()
  const oneDay = 24 * 60 * 60 * 1000
  
  return {
    code: 200,
    message: 'success',
    data: [
      {
        id: '1',
        time: now - oneDay * 2,
        language: 'CYPHER',
        query: 'MATCH (n:Person) RETURN n.name, n.age LIMIT 10',
        executionTime: 123,
        success: true
      },
      {
        id: '2',
        time: now - oneDay,
        language: 'GREMLIN',
        query: 'g.V().hasLabel("Person").limit(5)',
        executionTime: 87,
        success: true
      },
      {
        id: '3',
        time: now - 2 * 60 * 60 * 1000, // 2小时前
        language: 'NEBULA',
        query: 'FETCH PROP ON player "player100"',
        executionTime: 156,
        success: true
      },
      {
        id: '4',
        time: now - 30 * 60 * 1000, // 30分钟前
        language: 'CYPHER',
        query: 'MATCH (c:Company) RETURN c.name, c.industry, c.employees LIMIT 5',
        executionTime: 189,
        success: true
      }
    ]
  }
}

/**
 * 转换模拟结果为表格数据
 * @param {Array} resultData - 查询结果数据
 * @returns {Object} {columns: Array, rows: Array} 表格数据
 */
export function convertToTableData(resultData) {
  if (!Array.isArray(resultData) || resultData.length === 0) {
    return { columns: [], rows: [] }
  }
  
  // 获取所有可能的列名（基于第一个对象）
  const firstItem = resultData[0]
  const columns = Object.keys(firstItem).map(key => ({
    key,
    label: key.toUpperCase(),
    sortable: true
  }))
  
  // 格式化行数据
  const rows = resultData.map((item, index) => {
    const row = { ...item }
    // 添加序号
    row._index = index + 1
    return row
  })
  
  return { columns, rows }
}

/**
 * 转换模拟结果为图数据
 * @param {Array} resultData - 查询结果数据
 * @returns {Object} {nodes: Array, edges: Array} 图数据
 */
export function convertToGraphData(resultData) {
  if (!Array.isArray(resultData) || resultData.length === 0) {
    return { nodes: [], edges: [] }
  }
  
  const nodes = []
  const edges = []
  const nodeMap = new Map()
  let edgeId = 1
  
  resultData.forEach(item => {
    // 处理节点类型数据
    if (item.id || item.vid) {
      const nodeId = item.id || item.vid
      const nodeLabel = item.label || item.tag || 'unknown'
      
      if (!nodeMap.has(nodeId)) {
        nodeMap.set(nodeId, true)
        nodes.push({
          id: nodeId,
          label: nodeLabel,
          properties: item
        })
      }
    }
    
    // 处理边类型数据
    if (item.source && item.target) {
      edges.push({
        id: `edge_${edgeId++}`,
        label: item.label || 'relation',
        source: item.source,
        target: item.target,
        properties: item
      })
    }
  })
  
  return { nodes, edges }
}

/**
 * 模拟API服务
 * 可以集成到现有的API模块中
 */
export const mockApiService = {
  // 查询相关
  executeNativeQuery: (connectionId, graphName, data) => 
    executeMockQuery(data.queryLanguage, data.queryStatement, graphName),
  
  queryVertices: async (connectionId, graphName, params = {}) => {
    await simulateDelay()
    const label = params.label || 'Person'
    const filteredNodes = mockGraphData.nodes.filter(node => node.label === label)
    
    return {
      code: 200,
      message: 'success',
      data: filteredNodes.slice(0, params.pageSize || 20),
      total: filteredNodes.length,
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 20
    }
  },
  
  queryEdges: async (connectionId, graphName, params = {}) => {
    await simulateDelay()
    const label = params.label || 'WORKS_AT'
    const filteredEdges = mockGraphData.edges.filter(edge => edge.label === label)
    
    return {
      code: 200,
      message: 'success',
      data: filteredEdges.slice(0, params.pageSize || 20),
      total: filteredEdges.length,
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 20
    }
  },
  
  // 模板和历史
  getQueryTemplates: getMockQueryTemplates,
  getQueryHistory: getMockQueryHistory
}

export default {
  getMockGraphData,
  executeMockQuery,
  getMockQueryTemplates,
  getMockQueryHistory,
  convertToTableData,
  convertToGraphData,
  mockApiService
}