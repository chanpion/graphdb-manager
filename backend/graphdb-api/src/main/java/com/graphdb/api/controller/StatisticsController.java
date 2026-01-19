package com.graphdb.api.controller;

import com.graphdb.api.dto.Result;
import com.graphdb.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 统计分析控制器
 */
@Tag(name = "统计分析", description = "图数据统计分析API")
@RestController
@RequestMapping("/api/v1/connections/{connectionId}/graphs/{graphName}/statistics")
@CrossOrigin
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    /**
     * 获取图统计信息
     */
    @Operation(summary = "获取图统计信息", description = "获取图的整体统计信息，包括节点数、边数、类型分布等")
    @GetMapping
    public Result<Map<String, Object>> getGraphStatistics(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName) {
        try {
            Map<String, Object> statistics = statisticsService.getGraphStatistics(
                    connectionId, graphName);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("获取图统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取节点类型分布
     */
    @Operation(summary = "获取节点类型分布", description = "获取图中各节点类型的分布情况")
    @GetMapping("/vertex-distribution")
    public Result<Map<String, Object>> getVertexTypeDistribution(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName) {
        try {
            Map<String, Object> distribution = statisticsService.getVertexTypeDistribution(
                    connectionId, graphName);
            return Result.success(distribution);
        } catch (Exception e) {
            return Result.error("获取节点类型分布失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取边类型分布
     */
    @Operation(summary = "获取边类型分布", description = "获取图中各边类型的分布情况")
    @GetMapping("/edge-distribution")
    public Result<Map<String, Object>> getEdgeTypeDistribution(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName) {
        try {
            Map<String, Object> distribution = statisticsService.getEdgeTypeDistribution(
                    connectionId, graphName);
            return Result.success(distribution);
        } catch (Exception e) {
            return Result.error("获取边类型分布失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取节点度分布
     */
    @GetMapping("/degree-distribution")
    public Result<Map<String, Object>> getDegreeDistribution(
            @PathVariable Long connectionId,
            @PathVariable String graphName,
            @RequestParam(required = false) String nodeType) {
        try {
            Map<String, Object> distribution = statisticsService.getDegreeDistribution(
                    connectionId, graphName, nodeType);
            return Result.success(distribution);
        } catch (Exception e) {
            return Result.error("获取节点度分布失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取连通分量信息
     */
    @GetMapping("/connected-components")
    public Result<Map<String, Object>> getConnectedComponents(
            @PathVariable Long connectionId,
            @PathVariable String graphName) {
        try {
            Map<String, Object> components = statisticsService.getConnectedComponents(
                    connectionId, graphName);
            return Result.success(components);
        } catch (Exception e) {
            return Result.error("获取连通分量失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取图密度
     */
    @GetMapping("/density")
    public Result<Map<String, Object>> getGraphDensity(
            @PathVariable Long connectionId,
            @PathVariable String graphName) {
        try {
            Map<String, Object> density = statisticsService.getGraphDensity(
                    connectionId, graphName);
            return Result.success(density);
        } catch (Exception e) {
            return Result.error("获取图密度失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取最短路径统计
     */
    @GetMapping("/shortest-path")
    public Result<Map<String, Object>> getShortestPathStatistics(
            @PathVariable Long connectionId,
            @PathVariable String graphName,
            @RequestParam(required = false) String sourceUid,
            @RequestParam(required = false) String targetUid) {
        try {
            Map<String, Object> pathStats = statisticsService.getShortestPathStatistics(
                    connectionId, graphName, sourceUid, targetUid);
            return Result.success(pathStats);
        } catch (Exception e) {
            return Result.error("获取最短路径统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取中心性指标
     */
    @GetMapping("/centrality")
    public Result<Map<String, Object>> getCentralityMetrics(
            @PathVariable Long connectionId,
            @PathVariable String graphName,
            @RequestParam(required = false) String metricType) {
        try {
            Map<String, Object> centrality = statisticsService.getCentralityMetrics(
                    connectionId, graphName, metricType);
            return Result.success(centrality);
        } catch (Exception e) {
            return Result.error("获取中心性指标失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取聚类系数
     */
    @GetMapping("/clustering-coefficient")
    public Result<Map<String, Object>> getClusteringCoefficient(
            @PathVariable Long connectionId,
            @PathVariable String graphName) {
        try {
            Map<String, Object> clustering = statisticsService.getClusteringCoefficient(
                    connectionId, graphName);
            return Result.success(clustering);
        } catch (Exception e) {
            return Result.error("获取聚类系数失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取图演化趋势
     */
    @GetMapping("/evolution-trend")
    public Result<Map<String, Object>> getEvolutionTrend(
            @PathVariable Long connectionId,
            @PathVariable String graphName,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        try {
            Map<String, Object> trend = statisticsService.getEvolutionTrend(
                    connectionId, graphName, startTime, endTime);
            return Result.success(trend);
        } catch (Exception e) {
            return Result.error("获取图演化趋势失败: " + e.getMessage());
        }
    }
}