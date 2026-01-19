package com.graphdb.service;

import java.util.Map;
import java.io.Writer;

/**
 * 导入导出服务接口
 */
public interface ImportExportService {
    
    /**
     * 导入节点数据
     */
    Map<String, Object> importVerticesFromCsv(Long connectionId, String graphName, 
                                             String fileData, Map<String, Integer> fieldMapping,
                                             Integer batchSize, String onError, Boolean checkDuplicate);
    
    /**
     * 导入边数据
     */
    Map<String, Object> importEdgesFromCsv(Long connectionId, String graphName, 
                                          String fileData, Map<String, Integer> fieldMapping,
                                          Integer batchSize);
    
    /**
     * 导出节点数据到CSV
     */
    void exportVerticesToCsv(Long connectionId, String graphName, String nodeType, 
                            String fields, Writer writer);
    
    /**
     * 导出边数据到CSV
     */
    void exportEdgesToCsv(Long connectionId, String graphName, String edgeType, 
                         String fields, Writer writer);
    
    /**
     * 导出图数据到JSON
     */
    Map<String, Object> exportGraphToJson(Long connectionId, String graphName);
    
    /**
     * 文件导入
     */
    Map<String, Object> importFromFile(Long connectionId, String graphName, 
                                      Object file, String importType, String fieldMapping);
}