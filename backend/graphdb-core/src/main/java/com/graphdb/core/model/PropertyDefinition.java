package com.graphdb.core.model;

import lombok.Data;

/**
 * 属性定义
 */
@Data
public class PropertyDefinition {
    
    /**
     * 属性名称
     */
    private String name;
    
    /**
     * 属性类型：STRING, INTEGER, FLOAT, BOOLEAN, DATE, etc.
     */
    private String type;
    
    /**
     * 是否必填
     */
    private Boolean required;
    
    /**
     * 默认值
     */
    private Object defaultValue;
    
    /**
     * 是否可索引
     */
    private Boolean indexed;
    
    /**
     * 属性描述
     */
    private String description;
}