package com.graphdb.storage.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 连接配置实体类
 */
@Data
@TableName("connection_config")
public class ConnectionEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 连接名称
     */
    private String name;
    
    /**
     * 数据库类型：NEO4J/NEBULA/JANUS
     */
    private String type;
    
    /**
     * 主机地址
     */
    private String host;
    
    /**
     * 端口号
     */
    private Integer port;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 数据库名/图空间
     */
    private String databaseName;
    
    /**
     * 存储类型（仅JANUS）：HBASE/CASSANDRA
     */
    private String storageType;
    
    /**
     * 存储配置参数（仅JANUS）
     */
    private String storageConfig;
    
    /**
     * 额外连接参数（JSON格式）
     */
    private String extraParams;
    
    /**
     * 连接状态：0-未测试，1-正常，2-异常
     */
    private Integer status;
    
    /**
     * 连接描述
     */
    private String description;
    
    /**
     * 创建人
     */
    private String createdBy;
    
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
     * 逻辑删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}