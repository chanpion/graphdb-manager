package com.graphdb.core.model;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

/**
 * 节点实体
 * 表示图数据库中的一个节点
 */
@Data
public class Vertex {
    
    /**
     * 节点唯一标识符（数据库生成的ID）
     */
    private String uid;
    
    /**
     * 节点标签（关联Schema中的点类型定义）
     */
    private String label;
    
    /**
     * 节点属性集合
     */
    private Map<String, Object> properties = new HashMap<>();
    
    /**
     * 创建时间（时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（时间戳）
     */
    private Long updatedAt;
    
    /**
     * 默认构造函数
     */
    public Vertex() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }
    
    /**
     * 带标签的构造函数
     * @param label 节点标签
     */
    public Vertex(String label) {
        this();
        this.label = label;
    }
    
    /**
     * 带标签和属性的构造函数
     * @param label 节点标签
     * @param properties 节点属性
     */
    public Vertex(String label, Map<String, Object> properties) {
        this();
        this.label = label;
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }
    
    /**
     * 添加属性
     * @param key 属性名
     * @param value 属性值
     */
    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 移除属性
     * @param key 属性名
     */
    public void removeProperty(String key) {
        this.properties.remove(key);
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 获取属性值
     * @param key 属性名
     * @return 属性值，不存在则返回null
     */
    public Object getProperty(String key) {
        return this.properties.get(key);
    }
    
    /**
     * 检查是否包含属性
     * @param key 属性名
     * @return 是否包含
     */
    public boolean hasProperty(String key) {
        return this.properties.containsKey(key);
    }
}