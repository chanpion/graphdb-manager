package com.graphdb.api.controller;

import com.graphdb.api.dto.Result;
import com.graphdb.service.ImportExportService;
import com.graphdb.core.model.CsvImportConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 导入导出管理控制器
 */
@Tag(name = "导入导出", description = "图数据导入导出API")
@RestController
@RequestMapping("/api/v1/connections/{connectionId}/graphs/{graphName}")
@CrossOrigin
public class ImportExportController {
    
    @Autowired
    private ImportExportService importExportService;
    
    /**
     * CSV导入节点
     */
    @Operation(summary = "CSV导入节点", description = "通过CSV格式导入节点数据")
    @PostMapping("/import/vertices")
    public Result<Map<String, Object>> importVertices(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, Object> request) {
        try {
            String fileData = (String) request.get("fileData");
            Map<String, Integer> fieldMapping = (Map<String, Integer>) request.get("fieldMapping");
            Integer batchSize = (Integer) request.getOrDefault("batchSize", 100);
            String onError = (String) request.getOrDefault("onError", "CONTINUE");
            Boolean checkDuplicate = (Boolean) request.getOrDefault("checkDuplicate", true);
            
            Map<String, Object> result = importExportService.importVerticesFromCsv(
                    connectionId, graphName, fileData, fieldMapping, batchSize, onError, checkDuplicate);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导入节点失败: " + e.getMessage());
        }
    }
    
    /**
     * CSV导入边
     */
    @Operation(summary = "CSV导入边", description = "通过CSV格式导入边数据")
    @PostMapping("/import/edges")
    public Result<Map<String, Object>> importEdges(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @RequestBody Map<String, Object> request) {
        try {
            String fileData = (String) request.get("fileData");
            Map<String, Integer> fieldMapping = (Map<String, Integer>) request.get("fieldMapping");
            Integer batchSize = (Integer) request.getOrDefault("batchSize", 100);
            
            Map<String, Object> result = importExportService.importEdgesFromCsv(
                    connectionId, graphName, fileData, fieldMapping, batchSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导入边失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出节点CSV
     */
    @Operation(summary = "导出节点CSV", description = "导出节点数据为CSV格式")
    @GetMapping("/export/vertices")
    public void exportVertices(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "节点类型筛选") @RequestParam(required = false) String nodeType,
            @Parameter(description = "导出字段") @RequestParam(required = false) String fields,
            HttpServletResponse response) throws IOException {
        try {
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=vertices_export.csv");
            
            importExportService.exportVerticesToCsv(
                    connectionId, graphName, nodeType, fields, response.getWriter());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出边CSV
     */
    @Operation(summary = "导出边CSV", description = "导出边数据为CSV格式")
    @GetMapping("/export/edges")
    public void exportEdges(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "边类型筛选") @RequestParam(required = false) String edgeType,
            @Parameter(description = "导出字段") @RequestParam(required = false) String fields,
            HttpServletResponse response) throws IOException {
        try {
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", 
                    "attachment; filename=edges_export.csv");
            
            importExportService.exportEdgesToCsv(
                    connectionId, graphName, edgeType, fields, response.getWriter());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出图数据JSON
     */
    @Operation(summary = "导出图数据JSON", description = "导出图数据为JSON格式")
    @GetMapping("/export/json")
    public Result<Map<String, Object>> exportJson(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName) {
        try {
            Map<String, Object> result = importExportService.exportGraphToJson(connectionId, graphName);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导出JSON失败: " + e.getMessage());
        }
    }
    
    /**
     * 文件上传导入
     */
    @Operation(summary = "文件上传导入", description = "通过文件上传方式导入数据")
    @PostMapping("/import/upload")
    public Result<Map<String, Object>> importFile(
            @Parameter(description = "连接ID", required = true) @PathVariable Long connectionId,
            @Parameter(description = "图名称", required = true) @PathVariable String graphName,
            @Parameter(description = "上传文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "导入类型", required = true) @RequestParam("importType") String importType,
            @Parameter(description = "字段映射") @RequestParam(required = false) String fieldMapping) {
        try {
            if (file.isEmpty()) {
                return Result.error("上传的文件为空");
            }
            
            Map<String, Object> result = importExportService.importFromFile(
                    connectionId, graphName, file, importType, fieldMapping);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("文件导入失败: " + e.getMessage());
        }
    }
}