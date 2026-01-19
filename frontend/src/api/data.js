import request from './request'

export const dataApi = {
  // ========== 节点操作 ==========

  /**
   * 创建节点
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {object} data - 节点数据 {label, properties}
   */
  createVertex(connectionId, graphName, data) {
    return request({
      method: 'post',
      url: `/connections/${connectionId}/graphs/${graphName}/data/vertices`,
      data
    })
  },

  /**
   * 查询节点列表
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {object} params - 查询参数 {label, pageNum, pageSize}
   */
  queryVertices(connectionId, graphName, params = {}) {
    return request({
      method: 'get',
      url: `/connections/${connectionId}/graphs/${graphName}/data/vertices`,
      params
    })
  },

  /**
   * 获取单个节点
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {string} uid - 节点UID
   */
  getVertex(connectionId, graphName, uid) {
    return request({
      method: 'get',
      url: `/connections/${connectionId}/graphs/${graphName}/data/vertices/${uid}`
    })
  },

  /**
   * 更新节点
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {string} uid - 节点UID
   * @param {object} data - 更新数据 {properties}
   */
  updateVertex(connectionId, graphName, uid, data) {
    return request({
      method: 'put',
      url: `/connections/${connectionId}/graphs/${graphName}/data/vertices/${uid}`,
      data
    })
  },

  /**
   * 删除节点
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {string} uid - 节点UID
   */
  deleteVertex(connectionId, graphName, uid) {
    return request({
      method: 'delete',
      url: `/connections/${connectionId}/graphs/${graphName}/data/vertices/${uid}`
    })
  },

  // ========== 边操作 ==========

  /**
   * 创建边
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {object} data - 边数据 {label, sourceUid, targetUid, properties}
   */
  createEdge(connectionId, graphName, data) {
    return request({
      method: 'post',
      url: `/connections/${connectionId}/graphs/${graphName}/data/edges`,
      data
    })
  },

  /**
   * 查询边列表
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {object} params - 查询参数 {label, pageNum, pageSize}
   */
  queryEdges(connectionId, graphName, params = {}) {
    return request({
      method: 'get',
      url: `/connections/${connectionId}/graphs/${graphName}/data/edges`,
      params
    })
  },

  /**
   * 获取单个边
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {string} uid - 边UID
   */
  getEdge(connectionId, graphName, uid) {
    return request({
      method: 'get',
      url: `/connections/${connectionId}/graphs/${graphName}/data/edges/${uid}`
    })
  },

  /**
   * 更新边
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {string} uid - 边UID
   * @param {object} data - 更新数据 {properties}
   */
  updateEdge(connectionId, graphName, uid, data) {
    return request({
      method: 'put',
      url: `/connections/${connectionId}/graphs/${graphName}/data/edges/${uid}`,
      data
    })
  },

  /**
   * 删除边
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {string} uid - 边UID
   */
  deleteEdge(connectionId, graphName, uid) {
    return request({
      method: 'delete',
      url: `/connections/${connectionId}/graphs/${graphName}/data/edges/${uid}`
    })
  },

  // ========== 原生查询 ==========

  /**
   * 执行原生查询
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {object} data - 查询数据 {queryLanguage, queryStatement}
   */
  executeNativeQuery(connectionId, graphName, data) {
    return request({
      method: 'post',
      url: `/connections/${connectionId}/graphs/${graphName}/data/native-query`,
      data
    })
  },

  // ========== CSV导入 ==========

  /**
   * 导入CSV文件
   * @param {number} connectionId - 连接ID
   * @param {string} graphName - 图名称
   * @param {object} config - 导入配置
   * @param {File} file - CSV文件
   */
  importCsv(connectionId, graphName, config, file) {
    const formData = new FormData()
    formData.append('config', JSON.stringify(config))
    formData.append('file', file)
    return request({
      method: 'post',
      url: `/connections/${connectionId}/graphs/${graphName}/data/import-csv`,
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}