package com.graphdb.core.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 图查询结果实体
 * 用于封装原生查询返回的节点和边数据
 */
@Data
public class GraphQueryResult {
    
    /**
     * 查询到的节点列表
     */
    private List<Vertex> vertices;
    
    /**
     * 查询到的边列表
     */
    private List<Edge> edges;
    
    /**
     * 查询执行统计信息
     */
    private QueryStatistics statistics;
    
    /**
     * 原始查询结果（保留原始格式）
     */
    private Object rawResult;
    
    /**
     * 默认构造函数
     */
    public GraphQueryResult() {
        this.vertices = new java.util.ArrayList<>();
        this.edges = new java.util.ArrayList<>();
        this.statistics = new QueryStatistics();
    }
    
    /**
     * 带节点和边的构造函数
     */
    public GraphQueryResult(List<Vertex> vertices, List<Edge> edges) {
        this();
        if (vertices != null) {
            this.vertices.addAll(vertices);
        }
        if (edges != null) {
            this.edges.addAll(edges);
        }
    }
    
    /**
     * 添加节点
     */
    public void addVertex(Vertex vertex) {
        if (vertex != null) {
            this.vertices.add(vertex);
        }
    }
    
    /**
     * 添加边
     */
    public void addEdge(Edge edge) {
        if (edge != null) {
            this.edges.add(edge);
        }
    }
    
    /**
     * 获取节点数量
     */
    public int getVertexCount() {
        return vertices.size();
    }
    
    /**
     * 获取边数量
     */
    public int getEdgeCount() {
        return edges.size();
    }
    
    /**
     * 获取总结果数量
     */
    public int getTotalCount() {
        return getVertexCount() + getEdgeCount();
    }
    
    /**
     * 查询统计信息类
     */
    @Data
    public static class QueryStatistics {
        
        /**
         * 查询执行时间（毫秒）
         */
        private Long executionTimeMs;
        
        /**
         * 数据库命中次数
         */
        private Long dbHits;
        
        /**
         * 结果行数
         */
        private Integer resultRows;
        
        /**
         * 是否包含解释计划
         */
        private Boolean containsPlan;
        
        /**
         * 查询执行状态
         */
        private String status;
        
        /**
         * 错误信息（如果有）
         */
        private String errorMessage;
        
        public QueryStatistics() {
            this.status = "SUCCESS";
        }
    }
}