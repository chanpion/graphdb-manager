package com.graphdb.core.model;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

/**
 * 查询条件
 * 用于节点和边的查询过滤
 */
@Data
public class QueryCondition {
    
    /**
     * 标签过滤（可选）
     */
    private String label;
    
    /**
     * 属性过滤条件
     * key: 属性名
     * value: 属性值或条件表达式
     */
    private Map<String, Object> propertyFilters = new HashMap<>();
    
    /**
     * 分页参数 - 页码（从1开始）
     */
    private Integer pageNum = 1;
    
    /**
     * 分页参数 - 每页大小
     */
    private Integer pageSize = 20;
    
    /**
     * 排序字段（可选）
     */
    private String sortField;
    
    /**
     * 排序方向：ASC / DESC
     */
    private String sortDirection = "ASC";
    
    /**
     * 默认构造函数
     */
    public QueryCondition() {}
    
    /**
     * 带标签的构造函数
     * @param label 标签名称
     */
    public QueryCondition(String label) {
        this.label = label;
    }
    
    /**
     * 添加属性过滤条件
     * @param propertyName 属性名
     * @param propertyValue 属性值
     * @return 当前实例（链式调用）
     */
    public QueryCondition addFilter(String propertyName, Object propertyValue) {
        this.propertyFilters.put(propertyName, propertyValue);
        return this;
    }
    
    /**
     * 设置分页参数
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 当前实例（链式调用）
     */
    public QueryCondition setPagination(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        return this;
    }
    
    /**
     * 设置排序
     * @param sortField 排序字段
     * @param sortDirection 排序方向
     * @return 当前实例（链式调用）
     */
    public QueryCondition setSort(String sortField, String sortDirection) {
        this.sortField = sortField;
        this.sortDirection = sortDirection;
        return this;
    }
    
    /**
     * 获取偏移量（用于数据库查询）
     * @return 偏移量
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
    
    /**
     * 检查是否有属性过滤条件
     * @return 是否有属性过滤
     */
    public boolean hasPropertyFilters() {
        return propertyFilters != null && !propertyFilters.isEmpty();
    }
}