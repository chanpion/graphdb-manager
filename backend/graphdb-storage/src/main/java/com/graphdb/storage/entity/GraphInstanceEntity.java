package com.graphdb.storage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 图实例实体类
 */
@Data
@TableName("graph_instance")
public class GraphInstanceEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联连接ID
     */
    private Long connectionId;
    
    /**
     * 图名称
     */
    private String graphName;
    
    /**
     * 数据库类型
     */
    private String databaseType;
    
    /**
     * 节点数量
     */
    private Long vertexCount;
    
    /**
     * 边数量
     */
    private Long edgeCount;
    
    /**
     * 图状态：NORMAL/ARCHIVED
     */
    private String status;
    
    /**
     * 图描述
     */
    private String description;
    
    /**
     * Schema版本号
     */
    private String schemaVersion;
    
    /**
     * Schema缓存时间
     */
    private LocalDateTime cacheTime;
    
    /**
     * 是否默认图
     */
    private Integer isDefault;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 最后访问时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastAccessedAt;
    
    /**
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;

    /**
     * 图来源：PLATFORM(平台创建)/EXISTING(图数据库已有)
     */
    private String sourceType;
}
