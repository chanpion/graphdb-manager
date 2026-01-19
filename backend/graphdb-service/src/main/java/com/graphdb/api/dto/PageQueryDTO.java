package com.graphdb.api.dto;

import lombok.Data;

/**
 * 分页查询DTO
 */
@Data
public class PageQueryDTO {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
    
    /**
     * 排序字段
     */
    private String sortBy;
    
    /**
     * 排序方向
     */
    private String sortOrder = "DESC";
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 获取偏移量
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
    
    /**
     * 获取分页限制
     */
    public int getLimit() {
        return pageSize;
    }
}