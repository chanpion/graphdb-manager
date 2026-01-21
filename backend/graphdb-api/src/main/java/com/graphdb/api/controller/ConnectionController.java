package com.graphdb.api.controller;

import com.graphdb.api.dto.Result;
import com.graphdb.api.dto.ConnectionConfigDTO;
import com.graphdb.api.service.ConnectionService;
import com.graphdb.api.constant.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 连接管理控制器
 */
@Tag(name = "连接管理", description = "图数据库连接管理API")
@RestController
@RequestMapping("/api/v1/connections")
@CrossOrigin
public class ConnectionController {
    
    @Autowired
    private ConnectionService connectionService;
    
    /**
     * 获取连接列表
     */
    @Operation(summary = "获取连接列表", description = "查询所有图数据库连接信息")
    @GetMapping
    public Result<List<ConnectionConfigDTO>> list(
            @Parameter(description = "页码", example = "1") @RequestParam(required = false, name = "page") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(required = false, name = "pageSize") Integer pageSize,
            @Parameter(description = "数据库类型筛选") @RequestParam(required = false, name = "databaseType") String databaseType,
            @Parameter(description = "状态筛选") @RequestParam(required = false, name = "status") Integer status,
            @Parameter(description = "关键词搜索") @RequestParam(required = false, name = "keyword") String keyword) {
        try {
            return Result.success(connectionService.list());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建连接
     */
    @Operation(summary = "创建连接", description = "创建新的图数据库连接")
    @PostMapping
    public Result<ConnectionConfigDTO> create(@RequestBody ConnectionConfigDTO config) {
        try {
            return Result.success(connectionService.create(config));
        } catch (Exception e) {
            return Result.error(ResponseCode.CREATED, e.getMessage());
        }
    }
    
    /**
     * 更新连接
     */
    @Operation(summary = "更新连接", description = "更新现有的图数据库连接")
    @PutMapping("/{id}")
    public Result<ConnectionConfigDTO> update(
            @Parameter(description = "连接ID", required = true) @PathVariable Long id, 
            @RequestBody ConnectionConfigDTO config) {
        try {
            return Result.success(connectionService.update(id, config));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除连接
     */
    @Operation(summary = "删除连接", description = "删除指定的图数据库连接")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "连接ID", required = true) @PathVariable Long id) {
        try {
            connectionService.delete(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 测试连接
     */
    @Operation(summary = "测试连接", description = "测试图数据库连接是否可用")
    @PostMapping("/{id}/test")
    public Result<Boolean> test(
            @Parameter(description = "连接ID", required = true) @PathVariable Long id, 
            @RequestBody(required = false) Map<String, Object> params) {
        try {
            String databaseType = params != null ? (String) params.get("databaseType") : null;
            String graphName = params != null ? (String) params.get("graphName") : null;
            Boolean result = connectionService.testConnectionWithDatabase(id, databaseType, graphName);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 独立测试连接（无需连接ID）
     */
    @Operation(summary = "独立测试连接", description = "测试图数据库连接配置是否可用")
    @PostMapping("/test")
    public Result<Boolean> testConnection(
            @RequestBody Map<String, Object> config) {
        try {
            String databaseType = (String) config.get("databaseType");
            String host = (String) config.get("host");
            Integer port = (Integer) config.get("port");
            String username = (String) config.get("username");
            String password = (String) config.get("password");
            String databaseName = (String) config.get("databaseName");
            
            Boolean result = connectionService.testConnection(
                    databaseType, host, port, username, password, databaseName);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取连接详情
     */
    @Operation(summary = "获取连接详情", description = "获取指定连接的详细信息")
    @GetMapping("/{id}")
    public Result<ConnectionConfigDTO> getDetail(
            @Parameter(description = "连接ID", required = true) @PathVariable Long id) {
        try {
            return Result.success(connectionService.getDetail(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
