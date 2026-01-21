<template>
  <div class="d3-graph">
    <!-- 顶部工具栏 -->
    <div class="graph-toolbar">
      <!-- 布局切换 -->
      <div class="toolbar-section">
        <el-tooltip content="布局切换" placement="bottom">
          <el-select v-model="currentLayout" size="small" @change="changeLayout" style="width: 80px;">
            <el-option label="力导向" value="force" />
            <el-option label="圆形" value="circular" />
            <el-option label="分层" value="hierarchical" />
            <el-option label="网格" value="grid" />
          </el-select>
        </el-tooltip>
      </div>
      
      <!-- 展开控制 -->
      <div class="toolbar-section">
        <el-button-group size="small">
          <el-tooltip content="展开邻居" placement="bottom">
            <el-button @click="expandSelectedNode" :disabled="!selectedNode">
              <el-icon><plus /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="收起邻居" placement="bottom">
            <el-button @click="collapseSelectedNode" :disabled="!selectedNode">
              <el-icon><minus /></el-icon>
            </el-button>
          </el-tooltip>
        </el-button-group>
      </div>
      
      <!-- 缩放控制 -->
      <div class="toolbar-section">
        <el-button-group size="small">
          <el-tooltip content="放大" placement="bottom">
            <el-button @click="zoomIn">
              <el-icon><ZoomIn /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="缩小" placement="bottom">
            <el-button @click="zoomOut">
              <el-icon><ZoomOut /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="重置视图" placement="bottom">
            <el-button @click="resetView">
              <el-icon><refresh /></el-icon>
            </el-button>
          </el-tooltip>
        </el-button-group>
      </div>
      
      <!-- 导出功能 -->
      <div class="toolbar-section">
        <el-tooltip content="导出" placement="bottom">
          <el-dropdown @command="handleExport">
            <el-button size="small">
              <el-icon><download /></el-icon>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="png">PNG图片</el-dropdown-item>
                <el-dropdown-item command="svg">SVG矢量图</el-dropdown-item>
                <el-dropdown-item command="json">JSON数据</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-tooltip>
      </div>
    </div>
    
    <!-- 左下角图例 -->
    <div class="legend-bottom-left">
      <div class="legend">
        <!-- 动态节点类型图例 -->
        <div v-for="nodeType in uniqueNodeTypes" :key="`node-${nodeType}`" class="legend-item">
          <div class="node-sample" :class="getNodeClass(nodeType)"></div>
          <span>{{ nodeType }}</span>
        </div>
        
        <!-- 动态边类型图例 -->
        <div v-for="edgeType in uniqueEdgeTypes" :key="`edge-${edgeType}`" class="legend-item">
          <div class="edge-sample"></div>
          <span>{{ edgeType }}</span>
        </div>
      </div>
    </div>
    
    <div ref="graphContainer" class="graph-container">
      <svg ref="svg" :width="width" :height="height">
        <defs>
          <!-- 箭头标记 - 改进方向显示 -->
          <marker
            id="arrow"
            viewBox="0 0 10 10"
            refX="10"
            refY="3"
            markerWidth="6"
            markerHeight="6"
            orient="auto"
            markerUnits="strokeWidth"
          >
            <path d="M 0 0 L 10 3 L 0 6 z" fill="#666" />
          </marker>
          
          <!-- 双向箭头标记 -->
          <marker
            id="arrow-bidirectional"
            viewBox="0 0 10 10"
            refX="5"
            refY="3"
            markerWidth="6"
            markerHeight="6"
            orient="auto"
            markerUnits="strokeWidth"
          >
            <path d="M 0 3 L 5 0 L 10 3 L 5 6 z" fill="#666" />
          </marker>
          
          <!-- 节点渐变 -->
          <radialGradient id="node-gradient" cx="30%" cy="30%" r="70%">
            <stop offset="0%" stop-color="#fff" />
            <stop offset="100%" stop-color="#409EFF" />
          </radialGradient>
        </defs>
        
        <!-- 边图层 -->
        <g ref="edgesLayer">
          <!-- 边将通过D3动态添加 -->
        </g>
        
        <!-- 节点图层 -->
        <g ref="nodesLayer">
          <!-- 节点将通过D3动态添加 -->
        </g>
      </svg>
    </div>
    
    <!-- 状态栏 -->
    <div class="status-bar">
      <div class="status-item">
        <span class="status-label">节点:</span>
        <span class="status-value">{{ props.data.nodes.length }}</span>
      </div>
      <div class="status-item">
        <span class="status-label">边:</span>
        <span class="status-value">{{ props.data.edges.length }}</span>
      </div>
    </div>
    
    <!-- 右键菜单 -->
    <div 
      v-if="contextMenu.visible" 
      class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
    >
      <div class="context-menu-item" @click="expandSelectedNode">
        <el-icon><plus /></el-icon> 展开邻居
      </div>
      <div class="context-menu-item" @click="collapseSelectedNode">
        <el-icon><minus /></el-icon> 收起邻居
      </div>
      <div class="context-menu-item" @click="highlightPaths">
        <el-icon><share /></el-icon> 高亮路径
      </div>
      <div class="context-menu-item" @click="showNodeDetails">
        <el-icon><info-filled /></el-icon> 节点详情
      </div>
      <div class="context-menu-divider"></div>
      <div class="context-menu-item" @click="contextMenu.visible = false">
        <el-icon><close /></el-icon> 关闭
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import * as d3 from 'd3'
import { ElMessage } from 'element-plus'
import { ZoomIn, ZoomOut, Refresh, Close, Plus, Minus, Search, Download, ArrowDown, Share, InfoFilled } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  data: {
    type: Object,
    required: true,
    default: () => ({
      nodes: [],
      edges: []
    })
  },
  width: {
    type: Number,
    default: 800
  },
  height: {
    type: Number,
    default: 600
  }
})

// Emits
const emit = defineEmits(['node-click', 'edge-click', 'layout-change', 'export'])

// Refs
const svg = ref(null)
const graphContainer = ref(null)
const edgesLayer = ref(null)
const nodesLayer = ref(null)

// 状态
const selectedNode = ref(null)
const simulation = ref(null)
const zoom = ref(null)
const svgGroup = ref(null)
const currentLayout = ref('force')
const searchKeyword = ref('')
const contextMenu = ref({ visible: false, x: 0, y: 0, node: null })

// D3选择器
let d3Svg = null
let d3NodesLayer = null
let d3EdgesLayer = null
let d3Nodes = null
let d3Edges = null

// 计算唯一节点和边类型
const uniqueNodeTypes = computed(() => {
  if (!props.data || !props.data.nodes) return []
  const types = new Set()
  props.data.nodes.forEach(node => {
    if (node.label) {
      types.add(node.label)
    }
  })
  return Array.from(types)
})

const uniqueEdgeTypes = computed(() => {
  if (!props.data || !props.data.edges) return []
  const types = new Set()
  props.data.edges.forEach(edge => {
    if (edge.label) {
      types.add(edge.label)
    }
  })
  return Array.from(types)
})

// 初始化图表
function initChart() {
  if (!svg.value) return
  
  // 清空现有内容
  d3.select(svg.value).selectAll('*').remove()
  
  // 创建主SVG组
  d3Svg = d3.select(svg.value)
  svgGroup.value = d3Svg.append('g')
  
  // 创建图层
  d3EdgesLayer = svgGroup.value.append('g').attr('class', 'edges-layer')
  d3NodesLayer = svgGroup.value.append('g').attr('class', 'nodes-layer')
  
  // 设置缩放
  zoom.value = d3.zoom()
    .scaleExtent([0.1, 4])
    .on('zoom', (event) => {
      svgGroup.value.attr('transform', event.transform)
    })
  
  d3Svg.call(zoom.value)
  
  // 初始化力导向模拟
  initSimulation()
  
  // 渲染数据
  renderData()
  
  // 添加右键菜单监听
  d3Svg.on('contextmenu', (event) => {
    event.preventDefault()
  })
}

// 初始化力导向模拟
function initSimulation() {
  simulation.value = d3.forceSimulation()
    .force('link', d3.forceLink().id(d => d.id).distance(100))
    .force('charge', d3.forceManyBody().strength(-300))
    .force('center', d3.forceCenter(props.width / 2, props.height / 2))
    .force('collision', d3.forceCollide().radius(40))
    .on('tick', ticked)
    .alphaDecay(0.0228); // 控制模拟收敛速度，使动画更平滑
}

// 切换布局算法
function changeLayout() {
  if (!simulation.value) return
  
  // 清除现有力
  simulation.value.stop()
  
  // 根据布局类型初始化不同的力
  switch (currentLayout.value) {
    case 'force':
      initForceLayout()
      break
    case 'circular':
      initCircularLayout()
      break
    case 'hierarchical':
      initHierarchicalLayout()
      break
    case 'grid':
      initGridLayout()
      break
  }
  
  emit('layout-change', currentLayout.value)
  ElMessage.success('布局切换成功')
}

// 力导向布局
function initForceLayout() {
  if (!props.data.nodes || !props.data.edges) return
  
  simulation.value = d3.forceSimulation(props.data.nodes)
    .force('link', d3.forceLink(props.data.edges).id(d => d.id).distance(100))
    .force('charge', d3.forceManyBody().strength(-300))
    .force('center', d3.forceCenter(props.width / 2, props.height / 2))
    .force('collision', d3.forceCollide().radius(40))
    .on('tick', ticked)
    .alphaDecay(0.0228); // 控制模拟收敛速度，使动画更平滑
  
  // 启动模拟
  simulation.value.alpha(1).restart()
}

// 圆形布局
function initCircularLayout() {
  const radius = Math.min(props.width, props.height) / 2 - 50
  const centerX = props.width / 2
  const centerY = props.height / 2
  
  props.data.nodes.forEach((node, i) => {
    const angle = (i / props.data.nodes.length) * 2 * Math.PI
    node.x = centerX + radius * Math.cos(angle)
    node.y = centerY + radius * Math.sin(angle)
  })
  
  ticked()
}

// 分层布局
function initHierarchicalLayout() {
  // 按度数分层
  const degrees = new Map()
  props.data.nodes.forEach(node => {
    const degree = props.data.edges.filter(e => 
      e.source.id === node.id || e.target.id === node.id
    ).length
    degrees.set(node.id, degree)
  })
  
  const levels = [...new Set(degrees.values())].sort((a, b) => b - a)
  const levelHeight = props.height / (levels.length + 1)
  
  props.data.nodes.forEach(node => {
    const levelIndex = levels.indexOf(degrees.get(node.id))
    const nodesInLevel = props.data.nodes.filter(n => degrees.get(n.id) === levels[levelIndex])
    const nodeIndex = nodesInLevel.indexOf(node)
    const levelWidth = props.width / (nodesInLevel.length + 1)
    
    node.x = (nodeIndex + 1) * levelWidth
    node.y = (levelIndex + 1) * levelHeight
  })
  
  ticked()
}

// 网格布局
function initGridLayout() {
  const cols = Math.ceil(Math.sqrt(props.data.nodes.length))
  const rows = Math.ceil(props.data.nodes.length / cols)
  const cellWidth = props.width / cols
  const cellHeight = props.height / rows
  
  props.data.nodes.forEach((node, i) => {
    const col = i % cols
    const row = Math.floor(i / cols)
    node.x = col * cellWidth + cellWidth / 2
    node.y = row * cellHeight + cellHeight / 2
  })
  
  ticked()
}

  // 渲染数据
function renderData() {
  if (!props.data.nodes || !props.data.edges) return
  
  // 更新边 - 使用完整的数据绑定模式（enter, update, exit）
  d3Edges = d3EdgesLayer
    .selectAll('g.edge-group')
    .data(props.data.edges, d => d.id)
  
  // 移除多余的边
  d3Edges.exit().remove()
  
  // 创建新的边
  const edgeGroups = d3Edges.enter()
    .append('g')
    .attr('class', 'edge-group')
    .on('click', (event, d) => {
      event.stopPropagation()
      emit('edge-click', d)
    })
  
  // 添加边线条
  edgeGroups
    .append('line')
    .attr('class', 'edge-line')
    .attr('stroke', '#666')
    .attr('stroke-width', 2)
    .attr('marker-end', 'url(#arrow)')
    .attr('marker-start', d => {
      // 检查是否为双向边
      const reverseEdge = props.data.edges.some(e => 
        e.source.id === d.target.id && e.target.id === d.source.id
      )
      return reverseEdge ? 'url(#arrow-bidirectional)' : null
    })
    // 确保边线长度足够显示箭头
    .attr('x1', d => d.source.x)
    .attr('y1', d => d.source.y)
    .attr('x2', d => d.target.x)
    .attr('y2', d => d.target.y)

  // 添加边标签
  edgeGroups
    .append('text')
    .attr('class', 'edge-label')
    .attr('text-anchor', 'middle')
    .attr('dy', -5)
    .attr('fill', '#333')
    .attr('font-size', '8px')  /* 调小边标签字体 */
    .attr('font-weight', 'bold')
    .text(d => d.label || '')
  
  // 将新添加的边与现有边合并
  d3Edges = d3Edges.merge(edgeGroups)
  
  // 更新边的箭头标记（处理更新的边）
  d3Edges.select('.edge-line')
    .attr('marker-end', 'url(#arrow)')
    .attr('marker-start', d => {
      // 检查是否为双向边
      const reverseEdge = props.data.edges.some(e => 
        e.source.id === d.target.id && e.target.id === d.source.id
      )
      return reverseEdge ? 'url(#arrow-bidirectional)' : null
    })
    // 确保边线长度足够显示箭头
    .attr('x1', d => d.source.x)
    .attr('y1', d => d.source.y)
    .attr('x2', d => d.target.x)
    .attr('y2', d => d.target.y)
  
  // 更新节点 - 使用完整的数据绑定模式（enter, update, exit）
  d3Nodes = d3NodesLayer
    .selectAll('g.node')
    .data(props.data.nodes, d => d.id)
  
  // 移除多余的节点
  d3Nodes.exit().remove()
  
  // 创建新节点
  const nodeGroups = d3Nodes.enter()
    .append('g')
    .attr('class', 'node')
    .call(d3.drag()
      .on('start', dragStarted)
      .on('drag', dragged)
      .on('end', dragEnded)
    )
    .on('click', (event, d) => {
      event.stopPropagation()
      selectNode(d)
      emit('node-click', d)
    })
    .on('contextmenu', (event, d) => {
      event.preventDefault()
      event.stopPropagation()
      showContextMenu(event, d)
    })
  
  // 添加节点圆圈
  nodeGroups.append('circle')
    .attr('r', 16)  // 减小节点半径，确保箭头可见
    .attr('fill', d => getNodeColor(d.label))
    .attr('stroke', '#fff')
    .attr('stroke-width', 2)
  
  // 添加节点标签
  nodeGroups.append('text')
    .attr('text-anchor', 'middle')
    .attr('dy', '.35em')
    .attr('fill', '#333')
    .attr('font-size', '8px')  /* 调小节点标签字体 */
    .attr('font-weight', 'bold')
    .text(d => d.label || d.id.substring(0, 8))
  
  // 将新添加的节点与现有节点合并
  d3Nodes = d3Nodes.merge(nodeGroups)
  
  // 更新模拟数据
  updateSimulation()
}

// 更新模拟数据
function updateSimulation() {
  if (!props.data.nodes || !props.data.edges) return
  
  // 如果模拟不存在，创建新的模拟
  if (!simulation.value) {
    simulation.value = d3.forceSimulation(props.data.nodes)
      .force('link', d3.forceLink(props.data.edges).id(d => d.id).distance(100))
      .force('charge', d3.forceManyBody().strength(-300))
      .force('center', d3.forceCenter(props.width / 2, props.height / 2))
      .force('collision', d3.forceCollide().radius(40))
      .on('tick', ticked)
  } else {
    // 更新现有模拟的节点和边
    simulation.value.nodes(props.data.nodes)
    simulation.value.force('link', d3.forceLink(props.data.edges).id(d => d.id).distance(100))
    simulation.value.force('charge', d3.forceManyBody().strength(-300))
    simulation.value.force('center', d3.forceCenter(props.width / 2, props.height / 2))
    simulation.value.force('collision', d3.forceCollide().radius(40))
    
    // 重新启动模拟并设置初始alpha值，确保节点分散开来
    simulation.value.alpha(1).restart()
  }
  
  // 重新应用箭头标记到所有边
  if (d3Edges) {
    d3Edges.select('.edge-line')
      .attr('marker-end', 'url(#arrow)')
      .attr('marker-start', d => {
        // 检查是否为双向边
        const reverseEdge = props.data.edges.some(e => 
          e.source.id === d.target.id && e.target.id === d.source.id
        )
        return reverseEdge ? 'url(#arrow-bidirectional)' : null
      })
  }
}

// 模拟 tick 事件
function ticked() {
  if (!d3Edges || !d3Nodes) return
  
  // 更新边位置
  d3Edges.select('.edge-line')
    .attr('x1', d => d.source.x)
    .attr('y1', d => d.source.y)
    .attr('x2', d => d.target.x)
    .attr('y2', d => d.target.y)
  
  // 更新边标签位置
  d3Edges.select('.edge-label')
    .attr('x', d => (d.source.x + d.target.x) / 2)
    .attr('y', d => (d.source.y + d.target.y) / 2)
  
  // 更新节点位置
  d3Nodes
    .attr('transform', d => `translate(${d.x},${d.y})`)
}

// 拖拽事件
function dragStarted(event, d) {
  if (!event.active) simulation.value.alphaTarget(0.3).restart()
  d.fx = d.x
  d.fy = d.y
}

function dragged(event, d) {
  d.fx = event.x
  d.fy = event.y
}

function dragEnded(event, d) {
  if (!event.active) simulation.value.alphaTarget(0)
  d.fx = null
  d.fy = null
}

// 选择节点
function selectNode(node) {
  selectedNode.value = node
  
  // 高亮相关边和节点
  highlightRelated(node.id)
}

// 高亮相关元素
function highlightRelated(nodeId) {
  // 高亮与选定节点相连的边
  d3Edges.select('.edge-line')
    .attr('stroke', d => 
      d.source.id === nodeId || d.target.id === nodeId ? '#409EFF' : '#666'
    )
    .attr('stroke-width', d => 
      d.source.id === nodeId || d.target.id === nodeId ? 3 : 2
    )
  
  // 高亮与选定节点相连的节点
  d3Nodes.select('circle')
    .attr('stroke', d => {
      if (d.id === nodeId) return '#FF6B6B'
      
      // 检查是否与选定节点相连
      const isConnected = props.data.edges.some(edge => 
        (edge.source.id === nodeId && edge.target.id === d.id) ||
        (edge.source.id === d.id && edge.target.id === nodeId)
      )
      return isConnected ? '#FFA726' : '#fff'
    })
    .attr('stroke-width', d => {
      if (d.id === nodeId) return 4
      
      const isConnected = props.data.edges.some(edge => 
        (edge.source.id === nodeId && edge.target.id === d.id) ||
        (edge.source.id === d.id && edge.target.id === nodeId)
      )
      return isConnected ? 3 : 3
    })
}

// 重置高亮
function resetHighlight() {
  d3Edges.select('.edge-line')
    .attr('stroke', '#666')
    .attr('stroke-width', 2)
    .attr('opacity', 1)
  
  d3Nodes.select('circle')
    .attr('stroke', '#fff')
    .attr('stroke-width', 3)
    .attr('opacity', 1)
}

// 搜索节点
function handleSearch() {
  if (!searchKeyword.value.trim()) {
    resetHighlight()
    return
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  const matchedNodes = props.data.nodes.filter(node => 
    node.label?.toLowerCase().includes(keyword) ||
    node.id?.toLowerCase().includes(keyword) ||
    Object.values(node.properties || {}).some(prop => 
      String(prop).toLowerCase().includes(keyword)
    )
  )
  
  if (matchedNodes.length === 0) {
    ElMessage.warning('未找到匹配的节点')
    resetHighlight()
    return
  }
  
  // 高亮匹配的节点
  d3Nodes.select('circle')
    .attr('stroke', d => {
      const isMatched = matchedNodes.some(n => n.id === d.id)
      return isMatched ? '#F56C6C' : '#fff'
    })
    .attr('stroke-width', d => {
      const isMatched = matchedNodes.some(n => n.id === d.id)
      return isMatched ? 4 : 3
    })
    .attr('opacity', d => {
      const isMatched = matchedNodes.some(n => n.id === d.id)
      return isMatched ? 1 : 0.3
    })
  
  // 高亮匹配节点的边
  d3Edges
    .attr('opacity', d => {
      const isConnected = matchedNodes.some(n => 
        n.id === d.source.id || n.id === d.target.id
      )
      return isConnected ? 1 : 0.1
    })
    .attr('stroke', d => {
      const isConnected = matchedNodes.some(n => 
        n.id === d.source.id || n.id === d.target.id
      )
      return isConnected ? '#409EFF' : '#999'
    })
  
  ElMessage.success(`找到 ${matchedNodes.length} 个匹配节点`)
}

// 展开节点邻居
function expandSelectedNode() {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个节点')
    return
  }
  
  // 找到所有邻居节点
  const neighborIds = new Set()
  props.data.edges.forEach(edge => {
    if (edge.source.id === selectedNode.value.id) {
      neighborIds.add(edge.target.id)
    } else if (edge.target.id === selectedNode.value.id) {
      neighborIds.add(edge.source.id)
    }
  })
  
  const neighbors = props.data.nodes.filter(node => neighborIds.has(node.id))
  
  if (neighbors.length === 0) {
    ElMessage.info('该节点没有邻居')
    return
  }
  
  // 高亮邻居节点
  d3Nodes.select('circle')
    .attr('stroke', d => {
      if (d.id === selectedNode.value.id) return '#FF6B6B'
      if (neighborIds.has(d.id)) return '#FFA726'
      return '#fff'
    })
    .attr('stroke-width', d => {
      if (d.id === selectedNode.value.id) return 4
      if (neighborIds.has(d.id)) return 3
      return 3
    })
  
  // 高亮连接的边
  d3Edges
    .attr('stroke', d => {
      if (d.source.id === selectedNode.value.id || d.target.id === selectedNode.value.id) {
        return '#409EFF'
      }
      return '#999'
    })
    .attr('stroke-width', d => {
      if (d.source.id === selectedNode.value.id || d.target.id === selectedNode.value.id) {
        return 3
      }
      return 2
    })
  
  ElMessage.success(`展开了 ${neighbors.length} 个邻居节点`)
  hideContextMenu()
}

// 收起节点邻居
function collapseSelectedNode() {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个节点')
    return
  }
  
  // 仅高亮选中的节点，淡化其他
  d3Nodes.select('circle')
    .attr('stroke', d => {
      if (d.id === selectedNode.value.id) return '#FF6B6B'
      return '#fff'
    })
    .attr('stroke-width', 3)
  
  d3Edges.attr('stroke', '#999').attr('stroke-width', 2)
  
  ElMessage.success('已收起邻居节点')
  hideContextMenu()
}

// 高亮路径
function highlightPaths() {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个节点')
    return
  }
  
  // 使用BFS查找2度路径
  const paths = []
  const visited = new Set([selectedNode.value.id])
  
  const queue = [{ node: selectedNode.value, path: [selectedNode.value.id] }]
  
  while (queue.length > 0 && paths.length < 10) {
    const { node, path } = queue.shift()
    
    if (path.length > 2) continue
    
    const neighbors = new Set()
    props.data.edges.forEach(edge => {
      if (edge.source.id === node.id && !visited.has(edge.target.id)) {
        neighbors.add(edge.target.id)
      } else if (edge.target.id === node.id && !visited.has(edge.source.id)) {
        neighbors.add(edge.source.id)
      }
    })
    
    neighbors.forEach(neighborId => {
      const neighbor = props.data.nodes.find(n => n.id === neighborId)
      if (neighbor) {
        const newPath = [...path, neighborId]
        paths.push(newPath)
        visited.add(neighborId)
        queue.push({ node: neighbor, path: newPath })
      }
    })
  }
  
  // 高亮路径上的节点和边
  const pathNodeIds = new Set(paths.flat())
  
  d3Nodes.select('circle')
    .attr('stroke', d => {
      if (d.id === selectedNode.value.id) return '#FF6B6B'
      if (pathNodeIds.has(d.id)) return '#67C23A'
      return '#fff'
    })
    .attr('opacity', d => pathNodeIds.has(d.id) ? 1 : 0.2)
  
  d3Edges
    .attr('stroke', d => {
      if (pathNodeIds.has(d.source.id) && pathNodeIds.has(d.target.id)) {
        return '#67C23A'
      }
      return '#999'
    })
    .attr('stroke-width', d => {
      if (pathNodeIds.has(d.source.id) && pathNodeIds.has(d.target.id)) {
        return 3
      }
      return 2
    })
    .attr('opacity', d => {
      if (pathNodeIds.has(d.source.id) && pathNodeIds.has(d.target.id)) {
        return 1
      }
      return 0.1
    })
  
  ElMessage.success(`高亮了 ${paths.length} 条路径`)
  hideContextMenu()
}

// 显示节点详情
function showNodeDetails() {
  if (!selectedNode.value) {
    ElMessage.warning('请先选择一个节点')
    return
  }
  
  const details = `
    UID: ${selectedNode.value.id}
    标签: ${selectedNode.value.label}
    属性: ${JSON.stringify(selectedNode.value.properties, null, 2)}
  `
  
  ElMessage({
    message: details,
    type: 'info',
    duration: 5000,
    dangerouslyUseHTMLString: true
  })
  hideContextMenu()
}

// 显示右键菜单
function showContextMenu(event, node) {
  selectedNode.value = node
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    node: node
  }
}

// 隐藏右键菜单
function hideContextMenu() {
  contextMenu.value.visible = false
}

// 获取节点颜色
function getNodeColor(label) {
  const colors = {
    'Person': '#409EFF',
    'Company': '#67C23A',
    'Product': '#E6A23C',
    'default': '#909399'
  }
  
  return colors[label] || colors.default
}

// 获取节点CSS类
function getNodeClass(label) {
  const classes = {
    'Person': 'node-person',
    'Company': 'node-company',
    'Product': 'node-product',
    'default': 'node-default'
  }
  
  return classes[label] || classes.default
}

// 缩放控制
function zoomIn() {
  if (!d3Svg || !zoom.value) return
  d3Svg.transition().duration(300).call(zoom.value.scaleBy, 1.2)
}

function zoomOut() {
  if (!d3Svg || !zoom.value) return
  d3Svg.transition().duration(300).call(zoom.value.scaleBy, 0.8)
}

function resetView() {
  if (!d3Svg || !zoom.value) return
  d3Svg.transition().duration(300).call(
    zoom.value.transform,
    d3.zoomIdentity.translate(0, 0).scale(1)
  )
}

// 监听数据变化
watch(() => props.data, (newData) => {
  if (newData.nodes && newData.edges) {
    // 清理现有元素
    if (d3EdgesLayer) {
      d3.select(edgesLayer.value).selectAll('*').remove();
    }
    if (d3NodesLayer) {
      d3.select(nodesLayer.value).selectAll('*').remove();
    }
    
    // 重新渲染数据
    renderData();
    
    // 更新模拟
    updateSimulation();
  }
}, { deep: true })

// 监听尺寸变化
watch(() => [props.width, props.height], () => {
  if (simulation.value) {
    simulation.value.force('center', d3.forceCenter(props.width / 2, props.height / 2))
    simulation.value.alpha(0.3).restart()
  }
})

// 生命周期
onMounted(() => {
  nextTick(() => {
    initChart()
  })
  
  // 添加点击空白处取消选择
  document.addEventListener('click', handleDocumentClick)
})

onUnmounted(() => {
  if (simulation.value) {
    simulation.value.stop()
  }
  
  document.removeEventListener('click', handleDocumentClick)
})

// 处理文档点击
function handleDocumentClick() {
  selectedNode.value = null
  resetHighlight()
  hideContextMenu()
}

// 处理导出
function handleExport(format) {
  emit('export', { format, data: props.data })
  
  switch (format) {
    case 'png':
      exportAsPNG()
      break
    case 'svg':
      exportAsSVG()
      break
    case 'json':
      exportAsJSON()
      break
  }
  
  hideContextMenu()
}

// 导出为PNG
function exportAsPNG() {
  if (!svg.value) return
  
  const svgData = new XMLSerializer().serializeToString(svg.value)
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  const img = new Image()
  
  img.onload = () => {
    canvas.width = props.width
    canvas.height = props.height
    ctx.drawImage(img, 0, 0)
    
    const pngData = canvas.toDataURL('image/png')
    const downloadLink = document.createElement('a')
    downloadLink.download = 'graph-export.png'
    downloadLink.href = pngData
    downloadLink.click()
    
    ElMessage.success('PNG导出成功')
  }
  
  img.src = 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent(svgData)))
}

// 导出为SVG
function exportAsSVG() {
  if (!svg.value) return
  
  const svgData = new XMLSerializer().serializeToString(svg.value)
  const svgBlob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' })
  const svgUrl = URL.createObjectURL(svgBlob)
  
  const downloadLink = document.createElement('a')
  downloadLink.download = 'graph-export.svg'
  downloadLink.href = svgUrl
  downloadLink.click()
  
  URL.revokeObjectURL(svgUrl)
  ElMessage.success('SVG导出成功')
}

// 导出为JSON
function exportAsJSON() {
  const jsonData = JSON.stringify(props.data, null, 2)
  const jsonBlob = new Blob([jsonData], { type: 'application/json' })
  const jsonUrl = URL.createObjectURL(jsonBlob)
  
  const downloadLink = document.createElement('a')
  downloadLink.download = 'graph-data.json'
  downloadLink.href = jsonUrl
  downloadLink.click()
  
  URL.revokeObjectURL(jsonUrl)
  ElMessage.success('JSON导出成功')
}
</script>

<style scoped>
.d3-graph {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #fff;
  border-radius: 0px;
  overflow: hidden;
}

.graph-toolbar {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
  display: flex;
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  flex-wrap: wrap;
  max-width: calc(100% - 32px);
}

.toolbar-section {
  display: flex;
  align-items: center;
}

.graph-toolbar .el-button {
  display: flex;
  align-items: center;
  justify-content: center;
}

.graph-toolbar .el-icon {
  width: unset;
  height: unset;
}

.graph-toolbar .el-button .el-icon {
  font-size: 16px;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.graph-container {
  width: 100%;
  height: 100%;
}

.legend-bottom-left {
  position: absolute;
  bottom: 16px;
  left: 16px;
  z-index: 10;
}

.legend {
  background: rgba(255, 255, 255, 0.9);
  padding: 12px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #606266;
}

.legend-item:last-child {
  margin-bottom: 0;
}

.node-sample {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin-right: 8px;
}

.node-person {
  background-color: #409EFF;
}

.node-company {
  background-color: #67C23A;
}

.node-product {
  background-color: #E6A23C;
}

.node-default {
  background-color: #909399;
}

.edge-sample {
  width: 30px;
  height: 2px;
  background-color: #999;
  margin-right: 8px;
  position: relative;
}

.edge-sample::after {
  content: '';
  position: absolute;
  right: -6px;
  top: -4px;
  width: 0;
  height: 0;
  border-left: 6px solid #999;
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
}

.context-menu {
  position: fixed;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  min-width: 150px;
  padding: 8px 0;
}

.context-menu-item {
  padding: 10px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #303133;
  transition: all 0.2s;
}

.context-menu-item:hover {
  background-color: #f5f7fa;
  color: #409EFF;
}

.context-menu-divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 8px 0;
}

/* 状态栏样式 */
.status-bar {
  position: absolute;
  bottom: 8px;
  right: 16px;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 8px 12px;
  gap: 16px;
  z-index: 10;
  box-sizing: border-box;
  font-size: 12px;
  color: #909399;
  backdrop-filter: blur(4px);
}

.status-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
}

.status-label {
  font-weight: 500;
}

.status-value {
  font-weight: 600;
  color: #409EFF;
}

.node-panel {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 300px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 20;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.panel-header h4 {
  margin: 0;
  font-size: 16px;
}

.panel-content {
  padding: 16px;
}

.info-item {
  margin-bottom: 12px;
}

.info-item label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
  font-weight: bold;
}

.info-item span {
  display: block;
  font-size: 14px;
  color: #303133;
  word-break: break-all;
}

.properties {
  background: #f9f9f9;
  border-radius: 4px;
  padding: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.property {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px solid #ebeef5;
}

.property:last-child {
  border-bottom: none;
}

.property .key {
  font-size: 12px;
  color: #606266;
}

.property .value {
  font-size: 12px;
  color: #303133;
  font-weight: bold;
}
</style>