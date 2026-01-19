package com.graphdb.core.model;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

/**
 * 边实体
 * 表示图数据库中的一条边（关系）
 */
@Data
public class Edge {
    
    /**
     * 边唯一标识符（数据库生成的ID）
     */
    private String uid;
    
    /**
     * 边标签（关联Schema中的边类型定义）
     */
    private String label;
    
    /**
     * 源节点UID
     */
    private String sourceUid;
    
    /**
     * 目标节点UID
     */
    private String targetUid;
    
    /**
     * 边属性集合
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
    public Edge() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }
    
    /**
     * 带标签和节点的构造函数
     * @param label 边标签
     * @param sourceUid 源节点UID
     * @param targetUid 目标节点UID
     */
    public Edge(String label, String sourceUid, String targetUid) {
        this();
        this.label = label;
        this.sourceUid = sourceUid;
        this.targetUid = targetUid;
    }
    
    /**
     * 带标签、节点和属性的构造函数
     * @param label 边标签
     * @param sourceUid 源节点UID
     * @param targetUid 目标节点UID
     * @param properties 边属性
     */
    public Edge(String label, String sourceUid, String targetUid, Map<String, Object> properties) {
        this();
        this.label = label;
        this.sourceUid = sourceUid;
        this.targetUid = targetUid;
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
    
    /**
     * 获取边的方向描述
     * @return 方向字符串，格式：sourceUid → targetUid
     */
    public String getDirection() {
        return sourceUid + " → " + targetUid;
    }
}