package com.graphdb.api.controller;

import com.graphdb.api.dto.Result;
import com.graphdb.service.GraphService;
import com.graphdb.core.model.Vertex;
import com.graphdb.core.model.Edge;
import com.graphdb.core.model.QueryCondition;
import com.graphdb.core.model.CsvImportConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * 数据操作控制器
 * 负责节点和边的增删改查操作
 */
@Tag(name = "数据操作", description = "图数据节点和边操作API")
@RestController
@RequestMapping("/api/v1/connections/{connectionId}/graphs/{graphName}")
@CrossOrigin
public class DataOperationController {
    
    @Autowired
    private GraphService graphService;
    
    /**
     * 创建节点
     */
    @Operation(summary = "创建节点", description = "在指定图中创建新的节点")
    @PostMapping("/vertices")
    public Result<Vertex> createVertex(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, Object> request) {
        try {
            String label = (String) request.get("label");
            Map<String, Object> properties = (Map<String, Object>) request.get("properties");
            Vertex vertex = graphService.createVertex(connectionId, graphName, label, properties);
            return Result.success(vertex);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询节点
     */
    @Operation(summary = "查询节点", description = "查询指定图中的节点")
    @GetMapping("/vertices")
    public Result<List<Vertex>> queryVertices(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "节点类型筛选") @RequestParam(required = false, name = "label") String label,
            @Parameter(description = "关键词搜索") @RequestParam(required = false, name = "keyword") String keyword,
            @Parameter(description = "页码", example = "1") @RequestParam(required = false, name = "pageNum") Integer pageNum,
            @Parameter(description = "每页大小", example = "20") @RequestParam(required = false, name = "pageSize") Integer pageSize,
            @Parameter(description = "排序字段") @RequestParam(required = false, name = "sortBy") String sortBy,
            @Parameter(description = "排序方向", example = "DESC") @RequestParam(required = false, name = "sortOrder") String sortOrder) {
        try {
            QueryCondition condition = new QueryCondition();
            condition.setLabel(label);
            if (pageNum != null && pageSize != null) {
                condition.setPagination(pageNum, pageSize);
            }
            List<Vertex> vertices = graphService.queryVertices(connectionId, graphName, condition);
            return Result.success(vertices);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取单个节点
     */
    @Operation(summary = "获取单个节点", description = "根据UID获取指定节点的详细信息")
    @GetMapping("/vertices/{uid}")
    public Result<Vertex> getVertex(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "节点UID", required = true) @PathVariable String uid) {
        try {
            // 目前通过查询所有节点然后过滤，后续可以优化为直接查询单个节点
            QueryCondition condition = new QueryCondition();
            List<Vertex> vertices = graphService.queryVertices(connectionId, graphName, condition);
            Vertex target = vertices.stream()
                    .filter(v -> uid.equals(v.getUid()))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                return Result.error("节点不存在");
            }
            return Result.success(target);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新节点
     */
    @Operation(summary = "更新节点", description = "更新指定节点的属性")
    @PutMapping("/vertices/{uid}")
    public Result<Vertex> updateVertex(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "节点UID", required = true) @PathVariable String uid,
            @RequestBody Map<String, Object> properties) {
        try {
            Vertex vertex = graphService.updateVertex(connectionId, graphName, uid, properties);
            return Result.success(vertex);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除节点
     */
    @Operation(summary = "删除节点", description = "删除指定节点")
    @DeleteMapping("/vertices/{uid}")
    public Result<Void> deleteVertex(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "节点UID", required = true) @PathVariable String uid) {
        try {
            graphService.deleteVertex(connectionId, graphName, uid);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除节点
     */
    @Operation(summary = "批量删除节点", description = "批量删除多个节点")
    @DeleteMapping("/vertices/batch")
    public Result<Void> batchDeleteVertices(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> uids = (List<String>) request.get("uids");
            Boolean cascadeDelete = (Boolean) request.getOrDefault("cascadeDelete", true);
            
            // 循环删除单个节点
            for (String uid : uids) {
                graphService.deleteVertex(connectionId, graphName, uid);
            }
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建边
     */
    @Operation(summary = "创建边", description = "在指定图中创建新的边")
    @PostMapping("/edges")
    public Result<Edge> createEdge(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, Object> request) {
        try {
            String label = (String) request.get("label");
            String sourceUid = (String) request.get("sourceUid");
            String targetUid = (String) request.get("targetUid");
            Map<String, Object> properties = (Map<String, Object>) request.get("properties");
            Edge edge = graphService.createEdge(connectionId, graphName, label, sourceUid, targetUid, properties);
            return Result.success(edge);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询边
     */
    @Operation(summary = "查询边", description = "查询指定图中的边")
    @GetMapping("/edges")
    public Result<List<Edge>> queryEdges(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "边类型筛选") @RequestParam(required = false, name = "label") String label,
            @Parameter(description = "起始节点UID") @RequestParam(required = false, name = "sourceUid") String sourceUid,
            @Parameter(description = "目标节点UID") @RequestParam(required = false, name = "targetUid") String targetUid,
            @Parameter(description = "关键词搜索") @RequestParam(required = false, name = "keyword") String keyword,
            @Parameter(description = "页码", example = "1") @RequestParam(required = false, name = "pageNum") Integer pageNum,
            @Parameter(description = "每页大小", example = "20") @RequestParam(required = false, name = "pageSize") Integer pageSize,
            @Parameter(description = "排序字段") @RequestParam(required = false, name = "sortBy") String sortBy,
            @Parameter(description = "排序方向", example = "DESC") @RequestParam(required = false, name = "sortOrder") String sortOrder) {
        try {
            QueryCondition condition = new QueryCondition();
            condition.setLabel(label);
            if (pageNum != null && pageSize != null) {
                condition.setPagination(pageNum, pageSize);
            }
            List<Edge> edges = graphService.queryEdges(connectionId, graphName, condition);
            return Result.success(edges);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取单个边
     */
    @Operation(summary = "获取单个边", description = "根据UID获取指定边的详细信息")
    @GetMapping("/edges/{uid}")
    public Result<Edge> getEdge(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "边UID", required = true) @PathVariable String uid) {
        try {
            QueryCondition condition = new QueryCondition();
            List<Edge> edges = graphService.queryEdges(connectionId, graphName, condition);
            Edge target = edges.stream()
                    .filter(e -> uid.equals(e.getUid()))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                return Result.error("边不存在");
            }
            return Result.success(target);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新边
     */
    @Operation(summary = "更新边", description = "更新指定边的属性")
    @PutMapping("/edges/{uid}")
    public Result<Edge> updateEdge(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "边UID", required = true) @PathVariable String uid,
            @RequestBody Map<String, Object> properties) {
        try {
            Edge edge = graphService.updateEdge(connectionId, graphName, uid, properties);
            return Result.success(edge);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除边
     */
    @Operation(summary = "删除边", description = "删除指定边")
    @DeleteMapping("/edges/{uid}")
    public Result<Void> deleteEdge(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "边UID", required = true) @PathVariable String uid) {
        try {
            graphService.deleteEdge(connectionId, graphName, uid);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除边
     */
    @Operation(summary = "批量删除边", description = "批量删除多个边")
    @DeleteMapping("/edges/batch")
    public Result<Void> batchDeleteEdges(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, Object> request) {
        try {
            List<String> uids = (List<String>) request.get("uids");
            graphService.batchDeleteEdges(connectionId, graphName, uids);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 执行原生查询
     */
    @Operation(summary = "执行原生查询", description = "执行原生图查询语句")
    @PostMapping("/native-query")
    public Result<Object> executeNativeQuery(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, String> request) {
        try {
            String queryLanguage = request.get("queryLanguage");
            String queryStatement = request.get("queryStatement");
            Object result = graphService.executeNativeQuery(connectionId, graphName, queryLanguage, queryStatement);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 导入CSV文件数据
     */
    @Operation(summary = "导入CSV文件数据", description = "通过CSV文件导入图数据")
    @PostMapping("/import-csv")
    public Result<String> importCsv(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestPart("config") CsvImportConfig config,
            @RequestPart("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("上传的文件为空");
            }
            String result = graphService.importFromCsv(connectionId, graphName, config, file.getInputStream());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("CSV导入失败: " + e.getMessage());
        }
    }
}