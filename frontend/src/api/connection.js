import request from './request'

export const connectionApi = {
  list() {
    return request({
      method: 'get',
      url: '/connections'
    })
  },

  create(data) {
    return request({
      method: 'post',
      url: '/connections',
      data
    })
  },

  update(id, data) {
    return request({
      method: 'put',
      url: `/connections/${id}`,
      data
    })
  },

  delete(id) {
    return request({
      method: 'delete',
      url: `/connections/${id}`
    })
  },

  testConnection(id) {
    return request({
      method: 'post',
      url: `/connections/${id}/test`
    })
  }
}
