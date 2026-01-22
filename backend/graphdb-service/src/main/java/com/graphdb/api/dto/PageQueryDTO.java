package com.graphdb.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询DTO
 */
@Data
@Schema(description = "分页查询请求")
public class PageQueryDTO {

    @Min(value = 1, message = "页码必须大于等于1")
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页大小必须大于等于1")
    @Max(value = 100, message = "每页大小不能超过100")
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段")
    private String sortBy;

    @Schema(description = "排序方向", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortOrder = "DESC";

    @Schema(description = "关键词搜索")
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