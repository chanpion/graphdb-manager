import request from './request'

export const graphApi = {
  list(connectionId, sourceType) {
    return request({
      method: 'get',
      url: `/connections/${connectionId}/graphs`,
      params: { sourceType }
    })
  },

  getConnections() {
    return request({
      method: 'get',
      url: '/connections'
    })
  },

  getGraphSchema(connectionId, graphName) {
    return request({
      method: 'get',
      url: `/connections/${connectionId}/graphs/${graphName}/schema`
    })
  },

  createGraph(connectionId, data) {
    return request({
      method: 'post',
      url: `/connections/${connectionId}/graphs`,
      data
    })
  },

  deleteGraph(connectionId, graphName) {
    return request({
      method: 'delete',
      url: `/connections/${connectionId}/graphs/${graphName}`
    })
  },

  updateGraph(connectionId, data) {
    return request({
      method: 'put',
      url: `/connections/${connectionId}/graphs/${data.oldGraphName}`,
      data: {
        newGraphName: data.newGraphName,
        description: data.description
      }
    })
  },

  // 点类型管理
  getVertexTypes(connectionId, graphName) {
    return request({
      method: 'get',
      url: `/connections/${connectionId}/graphs/${graphName}/vertex-types`
    })
  },

  createVertexType(connectionId, graphName, data) {
    return request({
      method: 'post',
      url: `/connections/${connectionId}/graphs/${graphName}/vertex-types`,
      data
    })
  },

  deleteVertexType(connectionId, graphName, labelName) {
    return request({
      method: 'delete',
      url: `/connections/${connectionId}/graphs/${graphName}/vertex-types/${labelName}`
    })
  },

  // 边类型管理
  getEdgeTypes(connectionId, graphName) {
    return request({
      method: 'get',
      url: `/connections/${connectionId}/graphs/${graphName}/edge-types`
    })
  },

  createEdgeType(connectionId, graphName, data) {
    return request({
      method: 'post',
      url: `/connections/${connectionId}/graphs/${graphName}/edge-types`,
      data
    })
  },

  deleteEdgeType(connectionId, graphName, labelName) {
    return request({
      method: 'delete',
      url: `/connections/${connectionId}/graphs/${graphName}/edge-types/${labelName}`
    })
  },

  // 索引管理
  listIndexes(connectionId, graphName) {
    // TODO: 调用真实API
    // return request({
    //   method: 'get',
    //   url: `/connections/${connectionId}/graphs/${graphName}/indexes`
    // })
    
    // 临时模拟数据
    return new Promise((resolve) => {
      setTimeout(() => {
        const mockData = [
          {
            name: 'idx_user_email',
            type: 'UNIQUE',
            fields: ['email'],
            status: 'ACTIVE',
            createTime: '2024-01-15 10:30:00',
            description: '用户邮箱唯一索引'
          },
          {
            name: 'idx_user_name_age',
            type: 'COMPOUND',
            fields: ['name', 'age'],
            status: 'ACTIVE',
            createTime: '2024-01-16 14:20:00',
            description: '用户名和年龄复合索引'
          },
          {
            name: 'idx_article_content',
            type: 'FULLTEXT',
            fields: ['content'],
            status: 'BUILDING',
            createTime: '2024-01-17 09:15:00',
            description: '文章内容全文索引'
          },
          {
            name: 'idx_location',
            type: 'SPATIAL',
            fields: ['latitude', 'longitude'],
            status: 'ACTIVE',
            createTime: '2024-01-18 16:45:00',
            description: '地理位置空间索引'
          }
        ]
        resolve({ data: mockData })
      }, 500)
    })
  },

  createIndex(connectionId, graphName, data) {
    // TODO: 调用真实API
    // return request({
    //   method: 'post',
    //   url: `/connections/${connectionId}/graphs/${graphName}/indexes`,
    //   data
    // })
    
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ data: { success: true, message: '索引创建成功' } })
      }, 800)
    })
  },

  deleteIndex(connectionId, graphName, indexName) {
    // TODO: 调用真实API
    // return request({
    //   method: 'delete',
    //   url: `/connections/${connectionId}/graphs/${graphName}/indexes/${indexName}`
    // })
    
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ data: { success: true, message: '索引删除成功' } })
      }, 500)
    })
  },

  rebuildIndex(connectionId, graphName, indexName) {
    // TODO: 调用真实API
    // return request({
    //   method: 'post',
    //   url: `/connections/${connectionId}/graphs/${graphName}/indexes/${indexName}/rebuild`
    // })
    
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ data: { success: true, message: '索引重建已开始' } })
      }, 800)
    })
  },

  // 导出任务管理
  listExportTasks(connectionId, graphName) {
    // TODO: 调用真实API
    // return request({
    //   method: 'get',
    //   url: `/connections/${connectionId}/graphs/${graphName}/export-tasks`
    // })
    
    return new Promise((resolve) => {
      setTimeout(() => {
        const mockData = [
          {
            id: 'exp_001',
            name: '用户节点导出',
            exportType: 'vertices',
            exportFormat: 'csv',
            status: 'completed',
            progress: 100,
            createTime: '2024-01-15T10:30:00Z',
            finishTime: '2024-01-15T10:32:15Z',
            fileSize: 2457600,
            downloadUrl: 'https://example.com/exports/user-vertices.csv'
          },
          {
            id: 'exp_002',
            name: '全图数据导出',
            exportType: 'graph',
            exportFormat: 'json',
            status: 'running',
            progress: 65,
            createTime: '2024-01-16T14:20:00Z',
            finishTime: null,
            fileSize: null,
            downloadUrl: null
          },
          {
            id: 'exp_003',
            name: '可视化截图',
            exportType: 'visual',
            exportFormat: 'png',
            status: 'failed',
            progress: 45,
            createTime: '2024-01-17T09:15:00Z',
            finishTime: '2024-01-17T09:20:30Z',
            fileSize: null,
            downloadUrl: null,
            errorMessage: '渲染超时，请尝试简化图数据'
          },
          {
            id: 'exp_004',
            name: '边数据导出',
            exportType: 'edges',
            exportFormat: 'gexf',
            status: 'completed',
            progress: 100,
            createTime: '2024-01-18T16:45:00Z',
            finishTime: '2024-01-18T16:48:22Z',
            fileSize: 3850240,
            downloadUrl: 'https://example.com/exports/edges.gexf'
          },
          {
            id: 'exp_005',
            name: '产品节点导出',
            exportType: 'vertices',
            exportFormat: 'csv',
            status: 'cancelled',
            progress: 30,
            createTime: '2024-01-19T11:10:00Z',
            finishTime: '2024-01-19T11:12:45Z',
            fileSize: null,
            downloadUrl: null
          }
        ]
        resolve({ data: mockData })
      }, 500)
    })
  },

  createExportTask(connectionId, graphName, data) {
    // TODO: 调用真实API
    // return request({
    //   method: 'post',
    //   url: `/connections/${connectionId}/graphs/${graphName}/export-tasks`,
    //   data
    // })
    
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ 
          data: { 
            success: true, 
            message: '导出任务创建成功',
            taskId: `exp_${Date.now()}`
          } 
        })
      }, 800)
    })
  },

  deleteExportTask(connectionId, graphName, taskId) {
    // TODO: 调用真实API
    // return request({
    //   method: 'delete',
    //   url: `/connections/${connectionId}/graphs/${graphName}/export-tasks/${taskId}`
    // })
    
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ data: { success: true, message: '导出任务删除成功' } })
      }, 500)
    })
  },

  cancelExportTask(connectionId, graphName, taskId) {
    // TODO: 调用真实API
    // return request({
    //   method: 'post',
    //   url: `/connections/${connectionId}/graphs/${graphName}/export-tasks/${taskId}/cancel`
    // })
    
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ data: { success: true, message: '导出任务取消成功' } })
      }, 500)
    })
  },

  // 查询执行
  executeQuery(connectionId, graphName, language, query) {
    // TODO: 调用真实API
    // return request({
    //   method: 'post',
    //   url: `/connections/${connectionId}/graphs/${graphName}/query`,
    //   data: {
    //     language,
    //     query
    //   }
    // })

    // 临时模拟数据
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        try {
          // 根据查询语句生成不同的模拟数据
          const mockData = {
            nodes: [
              { id: 'node1', label: 'Person', properties: { name: 'Alice', age: 30, city: 'New York' } },
              { id: 'node2', label: 'Person', properties: { name: 'Bob', age: 35, city: 'San Francisco' } },
              { id: 'node3', label: 'Company', properties: { name: 'TechCorp', industry: 'Technology', employees: 1000 } },
              { id: 'node4', label: 'Person', properties: { name: 'Charlie', age: 28, city: 'Boston' } },
              { id: 'node5', label: 'Company', properties: { name: 'DataInc', industry: 'Data Analytics', employees: 500 } },
              { id: 'node6', label: 'Person', properties: { name: 'Diana', age: 32, city: 'Seattle' } },
              { id: 'node7', label: 'Person', properties: { name: 'Eve', age: 29, city: 'Austin' } },
              { id: 'node8', label: 'Company', properties: { name: 'CloudTech', industry: 'Cloud Computing', employees: 2500 } }
            ],
            edges: [
              { id: 'edge1', source: 'node1', target: 'node2', label: 'KNOWS', properties: { since: '2020-01-01' } },
              { id: 'edge2', source: 'node1', target: 'node3', label: 'WORKS_FOR', properties: { position: 'Engineer', startDate: '2019-06-01' } },
              { id: 'edge3', source: 'node2', target: 'node4', label: 'KNOWS', properties: { since: '2021-03-15' } },
              { id: 'edge4', source: 'node4', target: 'node5', label: 'WORKS_FOR', properties: { position: 'Data Scientist', startDate: '2022-01-10' } },
              { id: 'edge5', source: 'node6', target: 'node3', label: 'WORKS_FOR', properties: { position: 'Manager', startDate: '2018-05-20' } },
              { id: 'edge6', source: 'node7', target: 'node8', label: 'WORKS_FOR', properties: { position: 'DevOps Engineer', startDate: '2021-09-15' } },
              { id: 'edge7', source: 'node1', target: 'node6', label: 'KNOWS', properties: { since: '2019-11-20' } },
              { id: 'edge8', source: 'node2', target: 'node7', label: 'KNOWS', properties: { since: '2020-07-30' } },
              { id: 'edge9', source: 'node3', target: 'node8', label: 'PARTNERS_WITH', properties: { since: '2020-02-01' } },
              { id: 'edge10', source: 'node5', target: 'node8', label: 'PARTNERS_WITH', properties: { since: '2021-06-15' } }
            ]
          }
          resolve({ data: mockData })
        } catch (error) {
          reject(error)
        }
      }, 1000 + Math.random() * 2000)
    })
  }
}
