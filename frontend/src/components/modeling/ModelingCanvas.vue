<template>
  <div class="modeling-canvas">
    <div class="canvas-toolbar">
      <el-button-group>
        <el-button
          :type="currentTool === 'select' ? 'primary' : 'default'"
          @click="setTool('select')"
          title="选择工具"
        >
          <el-icon><Pointer /></el-icon>
        </el-button>
        <el-button
          :type="currentTool === 'vertex' ? 'primary' : 'default'"
          @click="setTool('vertex')"
          title="添加节点"
        >
          <el-icon><Circle /></el-icon>
        </el-button>
        <el-button
          :type="currentTool === 'edge' ? 'primary' : 'default'"
          @click="setTool('edge')"
          title="添加边"
        >
          <el-icon><Top /></el-icon>
        </el-button>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button-group>
        <el-button @click="zoomIn" title="放大">
          <el-icon><ZoomIn /></el-icon>
        </el-button>
        <el-button @click="zoomOut" title="缩小">
          <el-icon><ZoomOut /></el-icon>
        </el-button>
        <el-button @click="resetZoom" title="重置">
          <el-icon><RefreshLeft /></el-icon>
        </el-button>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button @click="autoLayout" title="自动布局">
        <el-icon><Grid /></el-icon>
        自动布局
      </el-button>

      <el-button @click="clearCanvas" title="清空画布">
        <el-icon><Delete /></el-icon>
        清空
      </el-button>

      <div class="zoom-info">
        缩放: {{ Math.round(zoom * 100) }}%
      </div>
    </div>

    <div class="canvas-container" ref="containerRef">
      <svg
        ref="svgRef"
        class="canvas-svg"
        :style="{
          transform: `translate(${panX}px, ${panY}px) scale(${zoom})`,
          transformOrigin: '0 0'
        }"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp"
        @mouseleave="handleMouseUp"
        @wheel="handleWheel"
      >
        <!-- 网格背景 -->
        <defs>
          <pattern
            id="grid"
            width="20"
            height="20"
            patternUnits="userSpaceOnUse"
          >
            <path
              d="M 20 0 L 0 0 0 20"
              fill="none"
              stroke="#e8e8e8"
              stroke-width="0.5"
            />
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#grid)" />

        <!-- 边 -->
        <g class="edges">
          <line
            v-for="edge in edges"
            :key="edge.id"
            :x1="getVertexById(edge.source)?.x"
            :y1="getVertexById(edge.source)?.y"
            :x2="getVertexById(edge.target)?.x"
            :y2="getVertexById(edge.target)?.y"
            stroke="#1890ff"
            stroke-width="2"
            marker-end="url(#arrowhead)"
            :class="{ 'selected': selectedEdge === edge.id }"
            @click.stop="selectEdge(edge.id)"
          />
        </g>

        <!-- 箭头定义 -->
        <defs>
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
              fill="#1890ff"
            />
          </marker>
        </defs>

        <!-- 节点 -->
        <g class="vertices">
          <g
            v-for="vertex in vertices"
            :key="vertex.id"
            :transform="`translate(${vertex.x}, ${vertex.y})`"
            :class="{ 'selected': selectedVertex === vertex.id, 'hover': hoveredVertex === vertex.id }"
            @mousedown.stop="startDrag(vertex, $event)"
            @click.stop="selectVertex(vertex.id)"
            @mouseenter="hoveredVertex = vertex.id"
            @mouseleave="hoveredVertex = null"
          >
            <circle
              r="30"
              :fill="getVertexColor(vertex)"
              stroke="none"
            />
            <text
              text-anchor="middle"
              dy="4"
              fill="white"
              font-size="12"
              font-weight="600"
              pointer-events="none"
            >
              {{ vertex.label.substring(0, 6) }}
            </text>
            <text
              text-anchor="middle"
              dy="50"
              fill="#606266"
              font-size="12"
              pointer-events="none"
            >
              {{ vertex.label }}
            </text>
          </g>
        </g>

        <!-- 连接中线的临时显示 -->
        <line
          v-if="drawingEdge"
          :x1="drawingEdge.source.x"
          :y1="drawingEdge.source.y"
          :x2="drawingEdge.mouseX"
          :y2="drawingEdge.mouseY"
          stroke="#1890ff"
          stroke-width="2"
          stroke-dasharray="5,5"
        />
      </svg>
    </div>

    <div class="canvas-status">
      <span v-if="currentTool === 'vertex'">点击画布添加节点</span>
      <span v-else-if="currentTool === 'edge'">从节点拖动创建边</span>
      <span v-else>拖动节点或选择编辑</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import {
  Pointer,
  Circle,
  Top,
  ZoomIn,
  ZoomOut,
  RefreshLeft,
  Grid,
  Delete
} from '@element-plus/icons-vue'

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

const emit = defineEmits(['vertex-click', 'edge-click'])

const containerRef = ref(null)
const svgRef = ref(null)

const currentTool = ref('select')
const zoom = ref(1)
const panX = ref(0)
const panY = ref(0)

const vertices = ref([])
const edges = ref([])
const selectedVertex = ref(null)
const selectedEdge = ref(null)
const hoveredVertex = ref(null)

const isDragging = ref(false)
const isPanning = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const panStart = ref({ x: 0, y: 0 })
const draggedVertex = ref(null)

const drawingEdge = ref(null)

const nextId = ref(1)

// 获取节点颜色
const getVertexColor = (vertex) => {
  const colorMap = {
    'person': '#52c41a',
    'company': '#1890ff',
    'product': '#faad14',
    'default': '#667eea'
  }
  return colorMap[vertex.type] || colorMap.default
}

// 根据ID获取节点
const getVertexById = (id) => {
  return vertices.value.find(v => v.id === id)
}

// 设置工具
const setTool = (tool) => {
  currentTool.value = tool
  selectedVertex.value = null
  selectedEdge.value = null
  drawingEdge.value = null
}

// 缩放
const zoomIn = () => {
  zoom.value = Math.min(zoom.value + 0.1, 3)
}

const zoomOut = () => {
  zoom.value = Math.max(zoom.value - 0.1, 0.3)
}

const resetZoom = () => {
  zoom.value = 1
  panX.value = 0
  panY.value = 0
}

// 自动布局
const autoLayout = () => {
  const centerX = 400
  const centerY = 300
  const radius = 150

  vertices.value.forEach((vertex, index) => {
    const angle = (2 * Math.PI * index) / vertices.value.length
    vertex.x = centerX + radius * Math.cos(angle)
    vertex.y = centerY + radius * Math.sin(angle)
  })
}

// 清空画布
const clearCanvas = () => {
  vertices.value = []
  edges.value = []
  selectedVertex.value = null
  selectedEdge.value = null
}

// 选择节点
const selectVertex = (id) => {
  selectedVertex.value = id
  selectedEdge.value = null
  const vertex = getVertexById(id)
  emit('vertex-click', vertex)
}

// 选择边
const selectEdge = (id) => {
  selectedEdge.value = id
  selectedVertex.value = null
  const edge = edges.value.find(e => e.id === id)
  emit('edge-click', edge)
}

// 开始拖动节点
const startDrag = (vertex, event) => {
  if (currentTool.value === 'edge') {
    drawingEdge.value = {
      source: vertex,
      mouseX: event.offsetX,
      mouseY: event.offsetY
    }
    return
  }

  if (currentTool.value === 'select') {
    isDragging.value = true
    draggedVertex.value = vertex
    dragStart.value = {
      x: event.offsetX,
      y: event.offsetY
    }
  }
}

// 鼠标按下
const handleMouseDown = (event) => {
  if (currentTool.value === 'vertex') {
    const rect = svgRef.value.getBoundingClientRect()
    const x = (event.clientX - rect.left - panX.value) / zoom.value
    const y = (event.clientY - rect.top - panY.value) / zoom.value

    vertices.value.push({
      id: nextId.value++,
      type: 'default',
      label: `节点${vertices.value.length + 1}`,
      x,
      y
    })
  } else if (currentTool.value === 'select' && !isDragging.value) {
    isPanning.value = true
    panStart.value = {
      x: event.clientX - panX.value,
      y: event.clientY - panY.value
    }
  }
}

// 鼠标移动
const handleMouseMove = (event) => {
  if (drawingEdge.value) {
    const rect = svgRef.value.getBoundingClientRect()
    drawingEdge.value.mouseX = (event.clientX - rect.left - panX.value) / zoom.value
    drawingEdge.value.mouseY = (event.clientY - rect.top - panY.value) / zoom.value
  } else if (isDragging.value && draggedVertex.value) {
    const rect = svgRef.value.getBoundingClientRect()
    draggedVertex.value.x = (event.clientX - rect.left - panX.value) / zoom.value
    draggedVertex.value.y = (event.clientY - rect.top - panY.value) / zoom.value
  } else if (isPanning.value) {
    panX.value = event.clientX - panStart.value.x
    panY.value = event.clientY - panStart.value.y
  }
}

// 鼠标释放
const handleMouseUp = (event) => {
  if (drawingEdge.value) {
    const rect = svgRef.value.getBoundingClientRect()
    const x = (event.clientX - rect.left - panX.value) / zoom.value
    const y = (event.clientY - rect.top - panY.value) / zoom.value

    // 检查是否释放到节点上
    const targetVertex = vertices.value.find(v => {
      const dx = v.x - x
      const dy = v.y - y
      return Math.sqrt(dx * dx + dy * dy) < 30
    })

    if (targetVertex && targetVertex.id !== drawingEdge.value.source.id) {
      edges.value.push({
        id: nextId.value++,
        source: drawingEdge.value.source.id,
        target: targetVertex.id
      })
    }

    drawingEdge.value = null
  }

  isDragging.value = false
  draggedVertex.value = null
  isPanning.value = false
}

// 鼠标滚轮
const handleWheel = (event) => {
  event.preventDefault()
  const delta = event.deltaY > 0 ? -0.1 : 0.1
  zoom.value = Math.max(0.3, Math.min(3, zoom.value + delta))
}

// 添加一些初始节点
const initDemo = () => {
  vertices.value = [
    { id: 1, type: 'person', label: '张三', x: 200, y: 200 },
    { id: 2, type: 'company', label: '公司A', x: 400, y: 200 },
    { id: 3, type: 'product', label: '产品B', x: 300, y: 350 }
  ]
  edges.value = [
    { id: 4, source: 1, target: 2 },
    { id: 5, source: 2, target: 3 }
  ]
  nextId.value = 6
}

onMounted(() => {
  initDemo()
})
</script>

<style scoped>
.modeling-canvas {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  position: relative;
}

.canvas-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
}

.zoom-info {
  margin-left: auto;
  font-size: 13px;
  color: #909399;
}

.canvas-container {
  flex: 1;
  overflow: hidden;
  position: relative;
  background: #f5f5f5;
}

.canvas-svg {
  width: 100%;
  height: 100%;
  cursor: crosshair;
}

.canvas-svg:active {
  cursor: grabbing;
}

.vertices circle {
  cursor: move;
  transition: filter 0.2s, stroke 0.2s;
}

.vertices g.selected circle {
  stroke: #1890ff;
  stroke-width: 3;
  filter: drop-shadow(0 0 8px rgba(24, 144, 255, 0.5));
}

.vertices g.hover circle {
  filter: brightness(1.1);
}

.edges line {
  cursor: pointer;
  transition: stroke 0.2s;
}

.edges line:hover {
  stroke: #40a9ff;
  stroke-width: 3;
}

.edges line.selected {
  stroke: #1890ff;
  stroke-width: 3;
  filter: drop-shadow(0 0 4px rgba(24, 144, 255, 0.5));
}

.canvas-status {
  position: absolute;
  bottom: 12px;
  left: 12px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  pointer-events: none;
}
</style>
