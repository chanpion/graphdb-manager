package com.graphdb.service;

import java.util.Map;

/**
 * 统计分析服务接口
 */
public interface StatisticsService {
    
    /**
     * 获取图统计信息
     */
    Map<String, Object> getGraphStatistics(Long connectionId, String graphName);
    
    /**
     * 获取节点类型分布
     */
    Map<String, Object> getVertexTypeDistribution(Long connectionId, String graphName);
    
    /**
     * 获取边类型分布
     */
    Map<String, Object> getEdgeTypeDistribution(Long connectionId, String graphName);
    
    /**
     * 获取节点度分布
     */
    Map<String, Object> getDegreeDistribution(Long connectionId, String graphName, String nodeType);
    
    /**
     * 获取连通分量信息
     */
    Map<String, Object> getConnectedComponents(Long connectionId, String graphName);
    
    /**
     * 获取图密度
     */
    Map<String, Object> getGraphDensity(Long connectionId, String graphName);
    
    /**
     * 获取最短路径统计
     */
    Map<String, Object> getShortestPathStatistics(Long connectionId, String graphName, 
                                                 String sourceUid, String targetUid);
    
    /**
     * 获取中心性指标
     */
    Map<String, Object> getCentralityMetrics(Long connectionId, String graphName, String metricType);
    
    /**
     * 获取聚类系数
     */
    Map<String, Object> getClusteringCoefficient(Long connectionId, String graphName);
    
    /**
     * 获取图演化趋势
     */
    Map<String, Object> getEvolutionTrend(Long connectionId, String graphName, 
                                         Long startTime, Long endTime);
}