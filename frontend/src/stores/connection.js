import { defineStore } from 'pinia'
import { connectionApi } from '../api/connection'

export const useConnectionStore = defineStore('connection', {
  state: () => ({
    connections: [],
    currentConnection: null
  }),
  
  actions: {
    async fetchConnections() {
      try {
        const response = await connectionApi.list()
        const data = response.data
        this.connections = Array.isArray(data) ? data : []
        return this.connections
      } catch (error) {
        console.error('获取连接列表失败:', error)
        throw error
      }
    },

    addConnection(connection) {
      this.connections.push(connection)
    },
    
    setCurrentConnection(connection) {
      this.currentConnection = connection
    },
    
    removeConnection(connectionId) {
      const index = this.connections.findIndex(c => c.id === connectionId)
      if (index !== -1) {
        this.connections.splice(index, 1)
      }
    }
  }
})