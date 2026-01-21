package com.graphdb.api.controller;

import com.graphdb.api.dto.Result;
import com.graphdb.service.NativeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 原生查询控制器
 */
@Tag(name = "原生查询", description = "图数据库原生查询API")
@RestController
@RequestMapping("/api/v1/connections/{connectionId}/graphs/{graphName}/query")
@CrossOrigin
public class NativeQueryController {
    
    @Autowired
    private NativeQueryService nativeQueryService;
    
    /**
     * 执行原生查询
     */
    @Operation(summary = "执行原生查询", description = "执行原生图查询语句")
    @PostMapping
    public Result<Map<String, Object>> executeQuery(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            Integer timeout = (Integer) request.getOrDefault("timeout", 60);
            Integer limit = (Integer) request.getOrDefault("limit", 1000);
            Boolean explain = (Boolean) request.getOrDefault("explain", false);
            
            Map<String, Object> result = nativeQueryService.executeQuery(
                    connectionId, graphName, query, timeout, limit, explain);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询执行计划
     */
    @Operation(summary = "查询执行计划", description = "获取查询语句的执行计划")
    @PostMapping("/explain")
    public Result<Map<String, Object>> explainQuery(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            Map<String, Object> plan = nativeQueryService.explainQuery(
                    connectionId, graphName, query);
            return Result.success(plan);
        } catch (Exception e) {
            return Result.error("获取查询计划失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取查询模板
     */
    @Operation(summary = "获取查询模板", description = "获取预定义的查询模板")
    @GetMapping("/templates")
    public Result<List<Map<String, Object>>> getQueryTemplates(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "模板分类") @RequestParam(required = false) String category) {
        try {
            List<Map<String, Object>> templates = nativeQueryService.getQueryTemplates(
                    connectionId, graphName, category);
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("获取查询模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取查询历史
     */
    @Operation(summary = "获取查询历史", description = "获取查询执行历史记录")
    @GetMapping("/history")
    public Result<Map<String, Object>> getQueryHistory(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "页码", example = "1") @RequestParam(required = false, name = "page") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(required = false, name = "pageSize") Integer pageSize,
            @Parameter(description = "开始时间") @RequestParam(required = false, name = "startTime") Long startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false, name = "endTime") Long endTime) {
        try {
            Map<String, Object> history = nativeQueryService.getQueryHistory(
                    connectionId, graphName, page, pageSize, startTime, endTime);
            return Result.success(history);
        } catch (Exception e) {
            return Result.error("获取查询历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除查询历史
     */
    @Operation(summary = "删除查询历史", description = "删除指定的查询历史记录")
    @DeleteMapping("/history")
    public Result<Map<String, Object>> deleteQueryHistory(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, List<String>> request) {
        try {
            List<String> ids = request.get("ids");
            Map<String, Object> result = nativeQueryService.deleteQueryHistory(
                    connectionId, graphName, ids);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("删除查询历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量查询
     */
    @Operation(summary = "批量查询", description = "批量执行多个查询语句")
    @PostMapping("/batch")
    public Result<Map<String, Object>> executeBatchQueries(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, Object> request) {
        try {
            List<Map<String, String>> queries = (List<Map<String, String>>) request.get("queries");
            Integer timeout = (Integer) request.getOrDefault("timeout", 60);
            Boolean continueOnError = (Boolean) request.getOrDefault("continueOnError", true);
            
            Map<String, Object> result = nativeQueryService.executeBatchQueries(
                    connectionId, graphName, queries, timeout, continueOnError);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("批量查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存查询脚本
     */
    @Operation(summary = "保存查询脚本", description = "保存查询脚本供后续使用")
    @PostMapping("/scripts")
    public Result<Map<String, Object>> saveQueryScript(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            List<String> queries = (List<String>) request.get("queries");
            
            Map<String, Object> result = nativeQueryService.saveQueryScript(
                    connectionId, graphName, name, description, queries);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("保存查询脚本失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取查询脚本列表
     */
    @Operation(summary = "获取查询脚本列表", description = "获取保存的查询脚本列表")
    @GetMapping("/scripts")
    public Result<Map<String, Object>> getQueryScripts(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "页码", example = "1") @RequestParam(required = false, name = "page") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(required = false, name = "pageSize") Integer pageSize,
            @Parameter(description = "关键词搜索") @RequestParam(required = false, name = "keyword") String keyword) {
        try {
            Map<String, Object> scripts = nativeQueryService.getQueryScripts(
                    connectionId, graphName, page, pageSize, keyword);
            return Result.success(scripts);
        } catch (Exception e) {
            return Result.error("获取查询脚本失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行查询脚本
     */
    @Operation(summary = "执行查询脚本", description = "执行保存的查询脚本")
    @PostMapping("/scripts/{scriptId}/execute")
    public Result<Map<String, Object>> executeQueryScript(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "脚本ID", required = true) @PathVariable String scriptId) {
        try {
            Map<String, Object> result = nativeQueryService.executeQueryScript(
                    connectionId, graphName, scriptId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("执行查询脚本失败: " + e.getMessage());
        }
    }
}