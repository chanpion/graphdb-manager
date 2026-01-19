package com.graphdb.core.model;

/**
 * CSV导入模式枚举
 */
public enum ImportMode {
    /**
     * 仅导入节点
     */
    VERTICES_ONLY,
    
    /**
     * 仅导入边
     */
    EDGES_ONLY,
    
    /**
     * 同时导入节点和边
     */
    VERTICES_AND_EDGES
}