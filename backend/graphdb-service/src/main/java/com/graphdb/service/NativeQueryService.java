package com.graphdb.service;

import com.graphdb.core.model.GraphQueryResult;
import java.util.List;
import java.util.Map;

/**
 * 原生查询服务接口
 */
public interface NativeQueryService {
    
    /**
     * 执行原生查询
     */
    Map<String, Object> executeQuery(Long connectionId, String graphName, 
                                    String query, Integer timeout, Integer limit, Boolean explain);
    
    /**
     * 获取查询执行计划
     */
    Map<String, Object> explainQuery(Long connectionId, String graphName, String query);
    
    /**
     * 获取查询模板
     */
    List<Map<String, Object>> getQueryTemplates(Long connectionId, String graphName, String category);
    
    /**
     * 获取查询历史
     */
    Map<String, Object> getQueryHistory(Long connectionId, String graphName, 
                                       Integer page, Integer pageSize, Long startTime, Long endTime);
    
    /**
     * 删除查询历史
     */
    Map<String, Object> deleteQueryHistory(Long connectionId, String graphName, List<String> ids);
    
    /**
     * 批量查询
     */
    Map<String, Object> executeBatchQueries(Long connectionId, String graphName, 
                                           List<Map<String, String>> queries, Integer timeout, 
                                           Boolean continueOnError);
    
    /**
     * 保存查询脚本
     */
    Map<String, Object> saveQueryScript(Long connectionId, String graphName, 
                                       String name, String description, List<String> queries);
    
    /**
     * 获取查询脚本列表
     */
    Map<String, Object> getQueryScripts(Long connectionId, String graphName, 
                                       Integer page, Integer pageSize, String keyword);
    
    /**
     * 执行查询脚本
     */
    Map<String, Object> executeQueryScript(Long connectionId, String graphName, String scriptId);
}