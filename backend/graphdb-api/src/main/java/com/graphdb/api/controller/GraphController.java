package com.graphdb.api.controller;

import com.graphdb.api.dto.Result;
import com.graphdb.service.GraphService;
import com.graphdb.core.model.GraphSchema;
import com.graphdb.core.model.LabelType;
import com.graphdb.storage.entity.GraphInstanceEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 图管理控制器
 */
@Tag(name = "图管理", description = "图数据库和图管理API")
@RestController
@RequestMapping("/api/v1/connections/{connectionId}/graphs")
@CrossOrigin
public class GraphController {
    
    @Autowired
    private GraphService graphService;
    
    /**
     * 获取图列表
     */
    @Operation(summary = "获取图列表", description = "获取指定连接下的所有图列表，支持按来源筛选")
    @GetMapping
    public Result<List<GraphInstanceEntity>> list(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "页码", example = "1") @RequestParam(required = false, name = "page") Integer page,
            @Parameter(name = "pageSize", description = "每页大小", example = "20") @RequestParam(required = false, name = "pageSize") Integer pageSize,
            @Parameter(description = "状态筛选") @RequestParam(required = false, name = "status") Integer status,
            @Parameter(description = "图来源筛选: PLATFORM-平台创建, EXISTING-图数据库已有") @RequestParam(required = false, name = "sourceType") String sourceType) {
        try {
            List<GraphInstanceEntity> graphs = graphService.getGraphInstances(connectionId, sourceType);
            return Result.success(graphs);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取图详情
     */
    @Operation(summary = "获取图详情", description = "获取指定图的详细信息")
    @GetMapping("/{graphName}")
    public Result<Map<String, Object>> getDetail(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName) {
        try {
            Map<String, Object> detail = graphService.getGraphDetail(connectionId, graphName);
            return Result.success(detail);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取图schema
     */
    @Operation(summary = "获取图schema", description = "获取指定图的Schema信息")
    @GetMapping("/{graphName}/schema")
    public Result<GraphSchema> getSchema(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName) {
        try {
            GraphSchema schema = graphService.getGraphSchema(connectionId, graphName);
            return Result.success(schema);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建图
     */
    @Operation(summary = "创建图", description = "在指定连接下创建新的图")
    @PostMapping
    public Result<Void> create(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @RequestBody Map<String, String> request) {
        try {
            String graphName = request.get("graphName");
            graphService.createGraph(connectionId, graphName);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除图
     */
    @Operation(summary = "删除图", description = "删除指定的图")
    @DeleteMapping("/{graphName}")
    public Result<Void> delete(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName) {
        try {
            graphService.deleteGraph(connectionId, graphName);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取点类型列表
     */
    @Operation(summary = "获取点类型列表", description = "获取指定图的所有点类型")
    @GetMapping("/{graphName}/vertex-types")
    public Result<List<LabelType>> getVertexTypes(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName) {
        try {
            List<LabelType> vertexTypes = graphService.getVertexTypes(connectionId, graphName);
            return Result.success(vertexTypes);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取边类型列表
     */
    @Operation(summary = "获取边类型列表", description = "获取指定图的所有边类型")
    @GetMapping("/{graphName}/edge-types")
    public Result<List<LabelType>> getEdgeTypes(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName) {
        try {
            List<LabelType> edgeTypes = graphService.getEdgeTypes(connectionId, graphName);
            return Result.success(edgeTypes);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建点类型
     */
    @Operation(summary = "创建点类型", description = "在指定图中创建新的点类型")
    @PostMapping("/{graphName}/vertex-types")
    public Result<Void> createVertexType(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody LabelType labelType) {
        try {
            graphService.createVertexType(connectionId, graphName, labelType);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除点类型
     */
    @Operation(summary = "删除点类型", description = "删除指定图中的点类型")
    @DeleteMapping("/{graphName}/vertex-types/{labelName}")
    public Result<Void> deleteVertexType(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "标签名称", required = true) @PathVariable String labelName) {
        try {
            graphService.deleteVertexType(connectionId, graphName, labelName);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建边类型
     */
    @Operation(summary = "创建边类型", description = "在指定图中创建新的边类型")
    @PostMapping("/{graphName}/edge-types")
    public Result<Void> createEdgeType(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody LabelType labelType) {
        try {
            graphService.createEdgeType(connectionId, graphName, labelType);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除边类型
     */
    @Operation(summary = "删除边类型", description = "删除指定图中的边类型")
    @DeleteMapping("/{graphName}/edge-types/{labelName}")
    public Result<Void> deleteEdgeType(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "标签名称", required = true) @PathVariable String labelName) {
        try {
            graphService.deleteEdgeType(connectionId, graphName, labelName);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新图
     */
    @Operation(summary = "更新图", description = "更新指定图的信息")
    @PutMapping("/{graphName}")
    public Result<Void> update(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, Object> request) {
        try {
            graphService.updateGraph(connectionId, graphName, request);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}