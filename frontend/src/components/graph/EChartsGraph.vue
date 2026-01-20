<template>
  <div class="echarts-graph" ref="chartContainer" :style="{ width: width + 'px', height: height + 'px' }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Object,
    required: true,
    default: () => ({ nodes: [], edges: [] })
  },
  width: {
    type: Number,
    default: 800
  },
  height: {
    type: Number,
    default: 600
  },
  layoutType: {
    type: String,
    default: 'force'
  },
  theme: {
    type: String,
    default: 'dark'
  }
})

const emit = defineEmits(['node-click', 'edge-click', 'layout-change'])

const chartContainer = ref(null)
let chart = null

// 节点颜色映射
const nodeColors = [
  '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
  '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#6366F1'
]

const getNodeColor = (label) => {
  if (!label) return nodeColors[0]
  const index = label.charCodeAt(0) % nodeColors.length
  return nodeColors[index]
}

const transformNodes = (nodes) => {
  return nodes.map(node => ({
    id: node.id,
    name: node.label || 'Unknown',
    symbolSize: 30,
    itemStyle: {
      color: getNodeColor(node.label),
      borderColor: '#fff',
      borderWidth: 2
    },
    label: {
      show: true,
      position: 'bottom',
      fontSize: 12,
      color: '#fff',
      formatter: params => params.data.name.substring(0, 8)
    },
    data: node.properties,
    category: node.label
  }))
}

const transformEdges = (edges) => {
  return edges.map(edge => ({
    source: edge.source,
    target: edge.target,
    name: edge.label || '',
    symbol: ['none', 'arrow'],
    symbolSize: 8,
    lineStyle: {
      color: '#667eea',
      curveness: 0.2,
      width: 2,
      opacity: 0.6
    },
    label: {
      show: true,
      fontSize: 10,
      color: '#94a3b8',
      formatter: params => params.data.name
    },
    data: edge.properties
  }))
}

const getCategories = (nodes) => {
  const categories = {}
  nodes.forEach(node => {
    if (node.label && !categories[node.label]) {
      categories[node.label] = Object.keys(categories).length
    }
  })
  return Object.keys(categories).map(name => ({ name }))
}

const initChart = () => {
  if (!chartContainer.value) return

  chart = echarts.init(chartContainer.value, 'dark', {
    renderer: 'canvas',
    useDirtyRect: false
  })

  chart.on('click', (params) => {
    if (params.dataType === 'node') {
      emit('node-click', {
        id: params.data.id,
        label: params.data.name,
        properties: params.data.data
      })
    } else if (params.dataType === 'edge') {
      emit('edge-click', {
        id: `${params.data.source}-${params.data.target}`,
        source: params.data.source,
        target: params.data.target,
        label: params.data.name,
        properties: params.data.data
      })
    }
  })

  updateChart()
}

const updateChart = () => {
  if (!chart) return

  const nodes = transformNodes(props.data.nodes)
  const edges = transformEdges(props.data.edges)
  const categories = getCategories(props.data.nodes)

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      formatter: (params) => {
        if (params.dataType === 'node') {
          const props = params.data.data || {}
          const propsStr = Object.entries(props)
            .map(([k, v]) => `<div style="margin: 2px 0;">${k}: ${v}</div>`)
            .join('')
          return `
            <div style="padding: 8px;">
              <div style="font-weight: bold; margin-bottom: 6px;">${params.data.name}</div>
              <div style="font-size: 12px; color: #94a3b8;">ID: ${params.data.id}</div>
              ${propsStr}
            </div>
          `
        } else if (params.dataType === 'edge') {
          const props = params.data.data || {}
          const propsStr = Object.entries(props)
            .map(([k, v]) => `<div style="margin: 2px 0;">${k}: ${v}</div>`)
            .join('')
          return `
            <div style="padding: 8px;">
              <div style="font-weight: bold; margin-bottom: 6px;">${params.data.name}</div>
              <div style="font-size: 12px; color: #94a3b8;">${params.data.source} → ${params.data.target}</div>
              ${propsStr}
            </div>
          `
        }
        return ''
      },
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: '#409EFF',
      textStyle: {
        color: '#fff',
        fontSize: 12
      }
    },
    legend: {
      show: true,
      type: 'scroll',
      orient: 'vertical',
      left: 'left',
      bottom: 'bottom',
      padding: [5, 5, 5, 5],
      itemGap: 10,
      itemWidth: 20,
      itemHeight: 14,
      textStyle: {
        color: '#94a3b8',
        fontSize: 12
      },
      pageTextStyle: {
        color: '#94a3b8'
      },
      pageButtonItemGap: 5,
      pageButtonPosition: 'end',
      data: categories.map(cat => cat.name)
    },
    series: [{
      type: 'graph',
      layout: props.layoutType,
      data: nodes,
      links: edges,
      categories: categories,
      roam: true,
      scaleLimit: {
        min: 0.5,
        max: 3
      },
      lineStyle: {
        color: 'source',
        curveness: 0.3
      },
      emphasis: {
        focus: 'adjacency',
        lineStyle: {
          width: 3,
          color: '#409EFF'
        }
      },
      force: props.layoutType === 'force' ? {
        repulsion: 1000,
        edgeLength: [50, 200],
        gravity: 0.1,
        layoutAnimation: true
      } : undefined,
      circular: props.layoutType === 'circular' ? {
        rotateLabel: true
      } : undefined
    }]
  }

  chart.setOption(option, true)
}

const resizeChart = () => {
  if (chart) {
    chart.resize()
  }
}

const zoomIn = (scale) => {
  if (chart) {
    chart.dispatchAction({
      type: 'dataZoom',
      start: Math.max(0, 100 - scale * 33.33),
      end: 100
    })
  }
}

const zoomOut = (scale) => {
  if (chart) {
    chart.dispatchAction({
      type: 'dataZoom',
      start: 0,
      end: Math.min(100, scale * 33.33)
    })
  }
}

const resetView = () => {
  if (chart) {
    chart.dispatchAction({
      type: 'restore'
    })
  }
}

const setLayoutType = (type) => {
  if (chart) {
    chart.setOption({
      series: [{
        layout: type
      }]
    })
  }
}

// 监听数据变化
watch(() => props.data, () => {
  nextTick(() => {
    updateChart()
  })
}, { deep: true })

watch(() => props.layoutType, () => {
  updateChart()
})

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
  window.removeEventListener('resize', resizeChart)
})

// 暴露方法给父组件
defineExpose({
  resize: resizeChart,
  getChart: () => chart,
  zoomIn,
  zoomOut,
  resetView,
  setLayoutType
})
</script>

<style scoped>
.echarts-graph {
  width: 100%;
  height: 100%;
}
</style>
