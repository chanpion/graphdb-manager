package com.graphdb.core.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 图schema信息
 */
@Data
public class GraphSchema {
    
    /**
     * 图名称
     */
    private String graphName;
    
    /**
     * 数据库类型
     */
    private String databaseType;
    
    /**
     * 点类型列表
     */
    private List<LabelType> vertexLabels;
    
    /**
     * 边类型列表
     */
    private List<LabelType> edgeLabels;
    
    /**
     * 属性定义映射
     */
    private Map<String, List<PropertyDefinition>> propertyDefinitions;
    
    /**
     * 索引信息
     */
    private List<IndexInfo> indexes;
    
    /**
     * 统计信息（节点数、边数等）
     */
    private Map<String, Object> statistics;
}