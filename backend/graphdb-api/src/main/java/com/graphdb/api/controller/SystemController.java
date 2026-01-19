package com.graphdb.api.controller;

import com.graphdb.api.dto.Result;
import com.graphdb.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

/**
 * 系统管理控制器
 */
@Tag(name = "系统管理", description = "系统管理和运维API")
@RestController
@RequestMapping("/api/v1")
@CrossOrigin
public class SystemController {
    
    @Autowired
    private SystemService systemService;
    
    /**
     * 健康检查
     */
    @Operation(summary = "健康检查", description = "检查系统各组件健康状态")
    @GetMapping("/health")
    public Result<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = systemService.getSystemHealth();
            return Result.success(health);
        } catch (Exception e) {
            return Result.error("健康检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取操作日志
     */
    @Operation(summary = "获取操作日志", description = "获取系统操作日志记录")
    @GetMapping("/connections/{connectionId}/logs")
    public Result<Map<String, Object>> getOperationLogs(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "页码", example = "1") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(required = false) Integer pageSize,
            @Parameter(description = "开始时间") @RequestParam(required = false) Long startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) Long endTime,
            @Parameter(description = "操作类型筛选") @RequestParam(required = false) String operationType,
            @Parameter(description = "结果状态筛选") @RequestParam(required = false) String resultStatus) {
        try {
            Map<String, Object> logs = systemService.getOperationLogs(
                    connectionId, page, pageSize, startTime, endTime, operationType, resultStatus);
            return Result.success(logs);
        } catch (Exception e) {
            return Result.error("获取操作日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getSystemConfig() {
        try {
            Map<String, Object> config = systemService.getSystemConfig();
            return Result.success(config);
        } catch (Exception e) {
            return Result.error("获取系统配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新系统配置
     */
    @PutMapping("/config")
    public Result<Map<String, Object>> updateSystemConfig(
            @RequestBody Map<String, Object> config) {
        try {
            Map<String, Object> result = systemService.updateSystemConfig(config);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("更新系统配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取性能指标
     */
    @GetMapping("/metrics")
    public Result<Map<String, Object>> getSystemMetrics(
            @RequestParam(required = false) String metricType,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        try {
            Map<String, Object> metrics = systemService.getSystemMetrics(
                    metricType, startTime, endTime);
            return Result.success(metrics);
        } catch (Exception e) {
            return Result.error("获取性能指标失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理缓存
     */
    @PostMapping("/cache/clear")
    public Result<Map<String, Object>> clearCache(
            @RequestParam(required = false) String cacheType) {
        try {
            Map<String, Object> result = systemService.clearCache(cacheType);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("清理缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 备份系统数据
     */
    @PostMapping("/backup")
    public Result<Map<String, Object>> backupData(
            @RequestParam(required = false) String backupType) {
        try {
            Map<String, Object> result = systemService.backupData(backupType);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("数据备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 恢复系统数据
     */
    @PostMapping("/restore")
    public Result<Map<String, Object>> restoreData(
            @RequestParam String backupFile,
            @RequestParam(required = false) Boolean cleanRestore) {
        try {
            Map<String, Object> result = systemService.restoreData(backupFile, cleanRestore);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("数据恢复失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取系统版本信息
     */
    @GetMapping("/version")
    public Result<Map<String, Object>> getVersionInfo() {
        try {
            Map<String, Object> version = systemService.getVersionInfo();
            return Result.success(version);
        } catch (Exception e) {
            return Result.error("获取版本信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 重启系统服务
     */
    @PostMapping("/restart")
    public Result<Map<String, Object>> restartService(
            @RequestParam(required = false) String serviceName) {
        try {
            Map<String, Object> result = systemService.restartService(serviceName);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("重启服务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取连接池状态
     */
    @GetMapping("/connection-pool")
    public Result<Map<String, Object>> getConnectionPoolStatus() {
        try {
            Map<String, Object> status = systemService.getConnectionPoolStatus();
            return Result.success(status);
        } catch (Exception e) {
            return Result.error("获取连接池状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取数据库连接信息
     */
    @GetMapping("/database-info")
    public Result<Map<String, Object>> getDatabaseInfo() {
        try {
            Map<String, Object> info = systemService.getDatabaseInfo();
            return Result.success(info);
        } catch (Exception e) {
            return Result.error("获取数据库信息失败: " + e.getMessage());
        }
    }
}