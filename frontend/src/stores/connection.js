import { defineStore } from 'pinia'

export const useConnectionStore = defineStore('connection', {
  state: () => ({
    connections: [],
    currentConnection: null
  }),
  
  actions: {
    addConnection(state, connection) {
      state.connections.push(connection)
    },
    
    setCurrentConnection(state, connection) {
      state.currentConnection = connection
    },
    
    removeConnection(state, connectionId) {
      const index = state.connections.findIndex(c => c.id === connectionId)
      if (index !== -1) {
        state.connections.splice(index, 1)
      }
    }
  }
})
