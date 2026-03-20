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
     * 索引标签类型：NODE, RELATIONSHIP
     */
    private String labelType;

    /**
     * 索引标签
     */
    private String label;
    /**
     * 索引字段列表
     */
    private String[] fields;

    /**
     * 是否唯一索引
     */
    private Boolean unique;
}