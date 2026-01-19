package com.graphdb.core.model;

import lombok.Data;

/**
 * 索引信息
 */
@Data
public class IndexInfo {
    
    /**
     * 索引名称
     */
    private String name;
    
    /**
     * 索引类型：COMPOSITE, MIXED, etc.
     */
    private String type;
    
    /**
     * 索引字段列表
     */
    private String[] fields;
    
    /**
     * 是否唯一索引
     */
    private Boolean unique;
    
    /**
     * 索引状态：ENABLED, DISABLED, BUILDING
     */
    private String status;
}