package com.graphdb.core.model;

import lombok.Data;
import java.util.List;

/**
 * 标签类型（点或边）
 */
@Data
public class LabelType {
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 标签类型：VERTEX / EDGE
     */
    private String type;
    
    /**
     * 属性列表
     */
    private List<PropertyDefinition> properties;
    
    /**
     * 是否支持多标签
     */
    private Boolean multiLabel;
    
    /**
     * 标签描述
     */
    private String description;
}