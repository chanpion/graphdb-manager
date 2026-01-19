<template>
  <div class="graph-view">
    <!-- 顶部工具栏 -->
    <div class="graph-toolbar">
      <div class="toolbar-left">
        <span class="layout-label">力导图布局</span>
      </div>

      <div class="toolbar-right">
        <el-button-group>
          <el-button size="small" @click="zoomIn" title="放大">
            <el-icon><ZoomIn /></el-icon>
          </el-button>
          <el-button size="small" @click="zoomOut" title="缩小">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
          <el-button size="small" @click="resetZoom" title="重置">
            <el-icon><RefreshLeft /></el-icon>
          </el-button>
        </el-button-group>

        <el-button size="small" @click="toggleLegend" title="图例">
          <el-icon><List /></el-icon>
          图例
        </el-button>

        <el-button size="small" @click="fitToScreen" title="适应屏幕">
          <el-icon><FullScreen /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 图可视化区域 -->
    <div class="graph-container" ref="containerRef">
      <svg
        ref="svgRef"
        class="graph-svg"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp"
        @wheel="handleWheel"
      >
        <g ref="zoomGroupRef" class="zoom-group">
          <!-- 边 -->
          <g class="edges">
            <g
              v-for="edge in filteredEdges"
              :key="edge.id"
              class="edge"
              :class="{ 'highlighted': isHighlighted(edge) }"
            >
              <!-- 边的流动动画层 -->
              <line
                v-if="isHighlighted(edge)"
                :x1="edge.source.x"
                :y1="edge.source.y"
                :x2="edge.target.x"
                :y2="edge.target.y"
                stroke="url(#highlightedEdgeGradient)"
                stroke-width="3"
                stroke-opacity="0.6"
                stroke-dasharray="10, 5"
                class="edge-flow"
              />
              <!-- 点击热区 - 不可见但更容易点击 -->
              <line
                :x1="edge.source.x"
                :y1="edge.source.y"
                :x2="edge.target.x"
                :y2="edge.target.y"
                stroke="transparent"
                stroke-width="12"
                @click="selectEdge(edge)"
                class="edge-hit-area"
              />
              <!-- 主边线 -->
              <line
                :x1="edge.source.x"
                :y1="edge.source.y"
                :x2="edge.target.x"
                :y2="edge.target.y"
                :stroke="getEdgeColor(edge)"
                stroke-width="2"
                marker-end="url(#arrowhead)"
                class="edge-line"
              />
              <text
                :x="(edge.source.x + edge.target.x) / 2"
                :y="(edge.source.y + edge.target.y) / 2 - 5"
                text-anchor="middle"
                font-size="11"
                fill="#94a3b8"
                class="edge-label"
              >
                {{ edge.label }}
              </text>
            </g>
          </g>

          <!-- 节点 -->
          <g class="nodes">
            <g
              v-for="node in filteredNodes"
              :key="node.id"
              class="node"
              :class="{ 'selected': isNodeSelected(node.id), 'highlighted': isHighlighted(node), 'dragging': draggedNode?.id === node.id }"
              :transform="`translate(${node.x}, ${node.y})`"
              @mousedown="startDrag(node, $event)"
              @click="selectNode(node, $event)"
            >
              <!-- 拖拽时的光晕效果 -->
              <circle
                v-if="draggedNode?.id === node.id"
                :r="getNodeRadius(node) + 7"
                fill="rgba(59, 130, 246, 0.2)"
                class="drag-halo"
              />
              
              <!-- 选中时的光晕效果 -->
              <circle
                v-if="isNodeSelected(node.id)"
                :r="getNodeRadius(node) + 5"
                fill="rgba(59, 130, 246, 0.3)"
                class="select-halo"
              />
              
              <!-- 节点圆形 -->
              <defs>
                <linearGradient :id="`node-gradient-${node.id}`" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" :style="{ stopColor: getNodeColor(node), stopOpacity: 1 }" />
                  <stop offset="100%" :style="{ stopColor: adjustColorBrightness(getNodeColor(node), -30), stopOpacity: 1 }" />
                </linearGradient>
                <filter :id="`node-shadow-${node.id}`" x="-50%" y="-50%" width="200%" height="200%">
                  <feGaussianBlur in="SourceAlpha" stdDeviation="3" result="blur"/>
                  <feOffset in="blur" dx="2" dy="4" result="offsetBlur"/>
                  <feFlood flood-color="rgba(0,0,0,0.4)" result="offsetColor"/>
                  <feComposite in="offsetColor" in2="offsetBlur" operator="in" result="offsetBlur"/>
                  <feMerge>
                    <feMergeNode in="offsetBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                  </feMerge>
                </filter>
              </defs>
              
              <circle
                :r="getNodeRadius(node)"
                :fill="`url(#node-gradient-${node.id})`"
                stroke="#64748b"
                stroke-width="2"
                class="node-circle"
                :filter="`url(#node-shadow-${node.id})`"
              />
              
              <!-- 节点标签 -->
              <text
                text-anchor="middle"
                dy="4"
                fill="white"
                font-size="12"
                font-weight="600"
                class="node-label"
              >
                {{ node.label.substring(0, 6) }}
              </text>
              
              <!-- 节点类型标签 -->
              <text
                :dy="getNodeRadius(node) + 20"
                text-anchor="middle"
                fill="#cbd5e1"
                font-size="12"
                class="node-type"
              >
                {{ node.label }}
              </text>
              
              <!-- 属性数量标记 -->
              <circle
                v-if="node.propertyCount > 0"
                :cx="getNodeRadius(node) - 10"
                :cy="-getNodeRadius(node) + 10"
                r="12"
                fill="#10b981"
                class="property-badge"
              />
              <text
                v-if="node.propertyCount > 0"
                :x="getNodeRadius(node) - 10"
                :y="-getNodeRadius(node) + 13"
                text-anchor="middle"
                fill="white"
                font-size="10"
                font-weight="600"
              >
                {{ node.propertyCount }}
              </text>
            </g>
          </g>
        </g>

        <!-- 箭头定义 -->
        <defs>
          <!-- 动态箭头，使用渐变色 -->
          <marker
            id="arrowhead"
            markerWidth="10"
            markerHeight="7"
            refX="9"
            refY="3.5"
            orient="auto"
          >
            <polygon
              points="0 0, 10 3.5, 0 7"
              fill="url(#arrowGradient)"
            />
          </marker>
          
          <!-- 箭头渐变 -->
          <linearGradient id="arrowGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:#64748b;stop-opacity:1" />
            <stop offset="100%" style="stop-color:#94a3b8;stop-opacity:1" />
          </linearGradient>
          
          <!-- 边的流动效果渐变 -->
          <linearGradient id="edgeGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:#64748b;stop-opacity:1" />
            <stop offset="100%" style="stop-color:#94a3b8;stop-opacity:1" />
          </linearGradient>
          
          <!-- 高亮边渐变 -->
          <linearGradient id="highlightedEdgeGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:#3B82F6;stop-opacity:1" />
            <stop offset="100%" style="stop-color:#60A5FA;stop-opacity:1" />
          </linearGradient>
        </defs>
      </svg>

      <!-- 图例面板 -->
      <div v-if="showLegend" class="legend-panel">
        <div class="legend-header">
          <span>图例</span>
          <el-button link @click="toggleLegend">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <div class="legend-content">
          <div class="legend-section">
            <div class="legend-title">点类型 ({{ nodes.length }})</div>
            <div
              v-for="node in nodes"
              :key="node.id"
              class="legend-item"
              @click="selectNode(node)"
            >
              <div
                class="legend-color"
                :style="{ backgroundColor: getNodeColor(node) }"
              />
              <span class="legend-label">{{ node.label }}</span>
              <el-tag size="small" type="info">{{ node.propertyCount }} 属性</el-tag>
            </div>
          </div>
          <div class="legend-section">
            <div class="legend-title">边类型 ({{ edges.length }})</div>
            <div
              v-for="edge in edges"
              :key="edge.id"
              class="legend-item"
            >
              <div class="legend-edge" />
              <span class="legend-label">{{ edge.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 属性详情面板 -->
    <div v-if="selectedNode || selectedEdge" class="property-details-panel">
      <div class="details-header">
        <span>{{ (selectedNode || selectedEdge).label }}</span>
        <el-button link @click="selectedNode = null; selectedEdge = null">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      <div class="details-content">
        <!-- 节点属性 -->
        <template v-if="selectedNode">
          <div class="detail-item">
            <span class="detail-label">类型:</span>
            <el-tag type="success" size="small">点类型</el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">属性数量:</span>
            <span>{{ selectedNode.propertyCount + 1 }}</span>
          </div>
          <div v-if="selectedNode.description" class="detail-item">
            <span class="detail-label">描述:</span>
            <span>{{ selectedNode.description }}</span>
          </div>
          <div class="properties-section">
            <div class="section-title">属性列表</div>
            <div class="property-item" v-for="prop in mergedNodeProperties" :key="prop.name">
              <div class="property-name">
                {{ prop.name }}
                <el-tag v-if="prop.isBuiltIn" size="small" type="warning" style="margin-left: 4px;">内置</el-tag>
              </div>
              <div class="property-type">
                <el-tag size="small" type="info">{{ prop.dataType }}</el-tag>
              </div>
              <div class="property-flags">
                <el-tag v-if="prop.required" size="small" type="danger">必填</el-tag>
                <el-tag v-if="prop.indexed" size="small" type="success">索引</el-tag>
                <el-tag v-if="prop.unique" size="small" type="warning">唯一</el-tag>
              </div>
            </div>
          </div>
        </template>
        
        <!-- 边属性 -->
        <template v-if="selectedEdge">
          <div class="detail-item">
            <span class="detail-label">类型:</span>
            <el-tag type="warning" size="small">边类型</el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">起点:</span>
            <span>{{ selectedEdge.source.label }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">终点:</span>
            <span>{{ selectedEdge.target.label }}</span>
          </div>
          <div v-if="selectedEdge.description" class="detail-item">
            <span class="detail-label">描述:</span>
            <span>{{ selectedEdge.description }}</span>
          </div>
          <div class="properties-section">
            <div class="section-title">属性列表</div>
            <div class="property-item" v-for="prop in mergedEdgeProperties" :key="prop.name">
              <div class="property-name">
                {{ prop.name }}
                <el-tag v-if="prop.isBuiltIn" size="small" type="warning" style="margin-left: 4px;">内置</el-tag>
              </div>
              <div class="property-type">
                <el-tag size="small" type="info">{{ prop.dataType }}</el-tag>
              </div>
              <div class="property-flags">
                <el-tag v-if="prop.required" size="small" type="danger">必填</el-tag>
                <el-tag v-if="prop.indexed" size="small" type="success">索引</el-tag>
                <el-tag v-if="prop.unique" size="small" type="warning">唯一</el-tag>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ZoomIn, ZoomOut, RefreshLeft, List, FullScreen, Close } from '@element-plus/icons-vue'
import * as d3 from 'd3'

const props = defineProps({
  vertexTypes: {
    type: Array,
    default: () => []
  },
  edgeTypes: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['node-click', 'edge-click'])

// 组件引用
const containerRef = ref(null)
const svgRef = ref(null)
const zoomGroupRef = ref(null)

// 视图状态
const nodes = ref([])
const edges = ref([])
const selectedNode = ref(null)
const selectedEdge = ref(null)

// 布局状态
const zoom = ref(1)
const showLegend = ref(false)

// 交互状态
const isDragging = ref(false)
const isPanning = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const panStart = ref({ x: 0, y: 0 })
const draggedNode = ref(null)
const panOffset = ref({ x: 0, y: 0 })
const selectedNodes = ref(new Set())
const isMultiSelect = ref(false)

// 过滤后的节点和边（不再使用搜索过滤）
const filteredNodes = computed(() => nodes.value)
const filteredEdges = computed(() => edges.value)

// 内置属性
const builtInProperties = computed(() => [
  {
    name: 'uid',
    dataType: 'STRING',
    required: true,
    indexed: true,
    unique: true,
    description: '唯一标识符'
  }
])

// 合并节点属性，内置属性在前
const mergedNodeProperties = computed(() => {
  if (!selectedNode.value) return []
  
  const merged = []
  // 先添加内置属性
  builtInProperties.value.forEach(prop => {
    merged.push({
      ...prop,
      isBuiltIn: true
    })
  })
  // 再添加自定义属性
  if (selectedNode.value.properties && selectedNode.value.properties.length > 0) {
    selectedNode.value.properties.forEach(prop => {
      merged.push({
        ...prop,
        isBuiltIn: false
      })
    })
  }
  return merged
})

// 合并边属性，内置属性在前
const mergedEdgeProperties = computed(() => {
  if (!selectedEdge.value) return []
  
  const merged = []
  // 先添加内置属性
  builtInProperties.value.forEach(prop => {
    merged.push({
      ...prop,
      isBuiltIn: true
    })
  })
  // 再添加自定义属性
  if (selectedEdge.value.properties && selectedEdge.value.properties.length > 0) {
    selectedEdge.value.properties.forEach(prop => {
      merged.push({
        ...prop,
        isBuiltIn: false
      })
    })
  }
  return merged
})

// 颜色映射
const colorMap = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899', '#06B6D4', '#84CC16']

const getNodeColor = (node) => {
  const index = nodes.value.findIndex(n => n.id === node.id)
  return colorMap[index % colorMap.length]
}

// 调整颜色亮度
const adjustColorBrightness = (hexColor, percent) => {
  const num = parseInt(hexColor.replace('#', ''), 16)
  const amt = Math.round(2.55 * percent)
  const R = (num >> 16) + amt
  const G = (num >> 8 & 0x00FF) + amt
  const B = (num & 0x0000FF) + amt
  return '#' + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 + (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 + (B < 255 ? B < 1 ? 0 : B : 255)).toString(16).slice(1)
}

const getEdgeColor = (edge) => {
  return '#94a3b8'
}

// 根据属性数量动态计算节点大小
const getNodeRadius = (node) => {
  const baseRadius = 35
  const maxRadius = 45
  const radiusIncrement = Math.min(5, node.propertyCount * 0.5)
  return baseRadius + radiusIncrement
}

// 获取节点渐变填充
const getNodeGradient = (node) => {
  const baseColor = getNodeColor(node)
  // 创建渐变 ID
  const gradientId = `node-gradient-${node.id}`
  return `url(#${gradientId})`
}

// 高亮判断
const isHighlighted = (item) => {
  if (selectedNodes.value.size === 0 && !selectedNode.value) return false
  
  // 检查是否是选中的节点
  if (item.id && selectedNodes.value.has(item.id)) return true
  
  // 检查是否是选中的边
  if (item.source?.id && selectedNodes.value.has(item.source.id)) return true
  if (item.target?.id && selectedNodes.value.has(item.target.id)) return true
  
  // 兼容单选模式
  if (selectedNode.value) {
    if (item.id === selectedNode.value.id) return true
    if (item.source?.id === selectedNode.value.id) return true
    if (item.target?.id === selectedNode.value.id) return true
  }
  
  return false
}

// 检查节点是否被选中
const isNodeSelected = (nodeId) => {
  return selectedNodes.value.has(nodeId) || (selectedNode.value && selectedNode.value.id === nodeId)
}

// 选择节点
const selectNode = (node, event) => {
  // 检查是否是多选模式（Shift 键）
  if (event?.shiftKey) {
    if (selectedNodes.value.has(node.id)) {
      selectedNodes.value.delete(node.id)
      if (selectedNodes.value.size === 0) {
        selectedNode.value = null
      }
    } else {
      selectedNodes.value.add(node.id)
      selectedNode.value = node
    }
  } else {
    // 单选模式
    selectedNodes.value.clear()
    selectedNode.value = node
  }
  selectedEdge.value = null
  emit('node-click', node)
}

// 选择边
const selectEdge = (edge) => {
  selectedEdge.value = edge
  selectedNode.value = null
  emit('edge-click', edge)
}

// 转换数据为图结构
const transformData = () => {
  // 将点类型转换为节点
  nodes.value = props.vertexTypes.map((vt, index) => ({
    id: vt.name,
    label: vt.name,
    type: 'vertex',
    description: vt.description,
    properties: vt.properties || [],
    propertyCount: vt.properties?.length || 0,
    x: 0,
    y: 0
  }))

  // 将边类型转换为边（暂时显示所有边类型）
  // 实际应用中需要从 Schema API 获取点类型之间的关系
  edges.value = props.edgeTypes.map((et, index) => {
    // 暂时随机连接两个节点作为示例
    const nodeIndex1 = index % nodes.value.length
    const nodeIndex2 = (index + 1) % nodes.value.length
    return {
      id: et.name,
      label: et.name,
      source: nodes.value[nodeIndex1],
      target: nodes.value[nodeIndex2],
      type: 'edge',
      description: et.description,
      properties: et.properties || []
    }
  })

  // 应用布局
  applyLayout()
}

// 应用布局（仅力导向布局）
const applyLayout = () => {
  if (nodes.value.length === 0) return

  const width = containerRef.value?.clientWidth || 800
  const height = containerRef.value?.clientHeight || 600

  // 仅使用力导向布局
  applyForceLayout(width, height)
}

// 力导向布局
const applyForceLayout = (width, height) => {
  const simulation = d3.forceSimulation(nodes.value)
    .force('link', d3.forceLink(edges.value).id(d => d.id).distance(150))
    .force('charge', d3.forceManyBody().strength(-300))
    .force('center', d3.forceCenter(width / 2, height / 2))
    .force('collide', d3.forceCollide().radius(80))

  simulation.on('tick', () => {
    // 强制更新视图
  })

  // 停止模拟
  setTimeout(() => simulation.stop(), 500)
}

// 缩放控制
const zoomIn = () => {
  zoom.value = Math.min(zoom.value + 0.1, 3)
  updateZoom()
}

const zoomOut = () => {
  zoom.value = Math.max(zoom.value - 0.1, 0.3)
  updateZoom()
}

const resetZoom = () => {
  zoom.value = 1
  panOffset.value = { x: 0, y: 0 }
  updateZoom()
}

const fitToScreen = () => {
  resetZoom()
  applyLayout()
}

const updateZoom = () => {
  if (zoomGroupRef.value) {
    // 使用 CSS transition 实现平滑缩放
    zoomGroupRef.value.style.transition = 'transform 0.1s ease-out'
    zoomGroupRef.value.setAttribute(
      'transform',
      `translate(${panOffset.value.x}, ${panOffset.value.y}) scale(${zoom.value})`
    )
    
    // 清除 transition 以避免影响后续操作
    setTimeout(() => {
      if (zoomGroupRef.value) {
        zoomGroupRef.value.style.transition = ''
      }
    }, 100)
  }
}

// 切换图例
const toggleLegend = () => {
  showLegend.value = !showLegend.value
}

// 拖拽节点
const startDrag = (node, event) => {
  event.stopPropagation()
  isDragging.value = true
  draggedNode.value = node
  dragStart.value = {
    x: event.clientX,
    y: event.clientY
  }
}

// 鼠标事件
const handleMouseDown = (event) => {
  if (event.target.tagName === 'svg' || event.target.classList.contains('graph-svg')) {
    isPanning.value = true
    panStart.value = {
      x: event.clientX - panOffset.value.x,
      y: event.clientY - panOffset.value.y
    }
  }
}

const handleMouseMove = (event) => {
  if (isDragging.value && draggedNode.value) {
    const dx = event.clientX - dragStart.value.x
    const dy = event.clientY - dragStart.value.y
    
    // 单节点拖拽或多选节点拖拽
    if (selectedNodes.value.size > 1 && selectedNodes.value.has(draggedNode.value.id)) {
      // 拖拽所有选中的节点
      nodes.value.forEach(node => {
        if (selectedNodes.value.has(node.id)) {
          node.x += dx / zoom.value
          node.y += dy / zoom.value
        }
      })
    } else {
      // 单节点拖拽
      draggedNode.value.x += dx / zoom.value
      draggedNode.value.y += dy / zoom.value
    }
    
    dragStart.value = {
      x: event.clientX,
      y: event.clientY
    }
  } else if (isPanning.value) {
    panOffset.value = {
      x: event.clientX - panStart.value.x,
      y: event.clientY - panStart.value.y
    }
    updateZoom()
  }
}

const handleMouseUp = () => {
  isDragging.value = false
  draggedNode.value = null
  isPanning.value = false
}

const handleWheel = (event) => {
  event.preventDefault()
  
  // 平滑缩放：使用缩放因子
  const zoomFactor = event.deltaY > 0 ? 0.9 : 1.1
  const newZoom = Math.max(0.1, Math.min(5, zoom.value * zoomFactor))
  
  // 计算鼠标相对于SVG的位置
  const svgRect = svgRef.value.getBoundingClientRect()
  const mouseX = event.clientX - svgRect.left
  const mouseY = event.clientY - svgRect.top
  
  // 计算缩放前的世界坐标
  const worldX = (mouseX - panOffset.value.x) / zoom.value
  const worldY = (mouseY - panOffset.value.y) / zoom.value
  
  // 更新缩放级别
  zoom.value = newZoom
  
  // 调整平移偏移，使鼠标位置保持不变
  panOffset.value = {
    x: mouseX - worldX * zoom.value,
    y: mouseY - worldY * zoom.value
  }
  
  updateZoom()
}

// 监听数据变化
watch(() => [props.vertexTypes, props.edgeTypes], () => {
  transformData()
}, { deep: true })

// 初始化
onMounted(() => {
  transformData()
  nextTick(() => {
    applyLayout()
  })
  
  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeyDown)
})

// 键盘事件处理
const handleKeyDown = (event) => {
  // ESC 键取消选择
  if (event.key === 'Escape') {
    selectedNode.value = null
    selectedEdge.value = null
    selectedNodes.value.clear()
  }
}

// 组件卸载
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})

// 组件卸载
onUnmounted(() => {
  // 清理
})
</script>

<style scoped>
.graph-view {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #1a1a2e;
  position: relative;
}

/* 工具栏 */
.graph-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #1E293B;
  border-bottom: 1px solid #334155;
  gap: 16px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.layout-label {
  font-size: 14px;
  color: #CBD5E1;
  font-weight: 500;
}

/* 图容器 */
.graph-container {
  flex: 1;
  min-height: 0;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

.graph-svg {
  width: 100%;
  height: 100%;
  cursor: grab;
}

.graph-svg:active {
  cursor: grabbing;
}

/* 节点样式 */
.node {
  cursor: move;
  transition: filter 0.2s;
}

.node .node-circle {
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.3));
  transition: filter 0.2s, stroke-width 0.2s, r 0.2s;
}

.node:hover .node-circle {
  filter: drop-shadow(0 6px 12px rgba(0, 0, 0, 0.5));
  stroke-width: 3;
}

.node.selected .node-circle {
  stroke: #3B82F6;
  stroke-width: 3;
  filter: drop-shadow(0 0 12px rgba(59, 130, 246, 0.5));
}

.node.dragging .node-circle {
  stroke: #3B82F6;
  stroke-width: 3;
  filter: drop-shadow(0 8px 16px rgba(59, 130, 246, 0.6));
}

.node.highlighted {
  opacity: 1;
}

/* 光晕动画 */
.drag-halo {
  animation: dragPulse 1s ease-in-out infinite;
}

.select-halo {
  animation: selectPulse 2s ease-in-out infinite;
}

@keyframes dragPulse {
  0%, 100% {
    opacity: 0.3;
    r: attr(r);
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes selectPulse {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.5;
  }
}

.property-badge {
  pointer-events: none;
  animation: badgePop 0.3s ease-out;
}

@keyframes badgePop {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* 边样式 */
.edge {
  transition: stroke 0.2s, stroke-width 0.2s;
}

.edge .edge-line {
  cursor: pointer;
  transition: stroke 0.2s, stroke-width 0.2s;
}

.edge:hover .edge-line {
  stroke: #3B82F6;
  stroke-width: 3;
}

.edge.selected .edge-line {
  stroke: #3B82F6;
  stroke-width: 3;
}

.edge.highlighted .edge-line {
  stroke: #3B82F6;
  stroke-width: 3;
}

/* 边的流动动画 */
.edge .edge-flow {
  animation: edgeFlow 1.5s linear infinite;
}

@keyframes edgeFlow {
  0% {
    stroke-dashoffset: 0;
  }
  100% {
    stroke-dashoffset: 15;
  }
}

.edge.highlighted {
  opacity: 1;
}

.edge-label {
  pointer-events: none;
  transition: fill 0.2s;
}

.edge:hover .edge-label,
.edge.highlighted .edge-label {
  fill: #3B82F6;
}

/* 图例面板 */
.legend-panel {
  position: absolute;
  top: 60px;
  left: 12px;
  width: 280px;
  background: #1E293B;
  border: 1px solid #334155;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  z-index: 10;
  max-height: calc(100% - 72px);
  overflow-y: auto;
}

.legend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #334155;
  font-weight: 600;
  color: #F1F5F9;
}

.legend-content {
  padding: 12px;
}

.legend-section {
  margin-bottom: 16px;
}

.legend-section:last-child {
  margin-bottom: 0;
}

.legend-title {
  font-size: 13px;
  font-weight: 600;
  color: #CBD5E1;
  margin-bottom: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.legend-item:hover {
  background: #334155;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-edge {
  width: 30px;
  height: 2px;
  background: #94a3b8;
  flex-shrink: 0;
}

.legend-label {
  flex: 1;
  font-size: 13px;
  color: #E2E8F0;
}

/* 属性详情面板 */
.property-details-panel {
  position: absolute;
  top: 60px;
  right: 12px;
  width: 360px;
  background: #1E293B;
  border: 1px solid #334155;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  z-index: 10;
  max-height: calc(100% - 72px);
  overflow-y: auto;
}

.details-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #334155;
  font-weight: 600;
  color: #F1F5F9;
  position: sticky;
  top: 0;
  background: #1E293B;
  z-index: 1;
}

.details-content {
  padding: 12px 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  color: #94A3B8;
  min-width: 80px;
}

.properties-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #334155;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #F1F5F9;
  margin-bottom: 12px;
}

.property-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #1E293B;
}

.property-item:last-child {
  border-bottom: none;
}

.property-name {
  font-weight: 500;
  color: #E2E8F0;
  min-width: 120px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.property-type {
  flex: 1;
}

.property-flags {
  display: flex;
  gap: 4px;
}
</style>
