package com.graphdb.core.model;

import lombok.Data;

/**
 * 连接配置模型（用于适配器层）
 */
@Data
public class ConnectionConfig {
    
    /**
     * 连接ID
     */
    private Long id;
    
    /**
     * 连接名称
     */
    private String name;
    
    /**
     * 数据库类型：NEO4J/NEBULA/JANUS
     */
    private String databaseType;
    
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
     * 密码（明文，用于连接测试）
     */
    private String password;

    /**
     * 存储后端（仅JANUS）：cql/hbase
     */
    private String storageBackend;

    /**
     * 存储主机（仅JANUS）
     */
    private String storageHost;

    /**
     * JSON格式额外参数
     */
    private String jsonParams;

    /**
     * 存储类型（仅JANUS）：HBASE/CASSANDRA - 用于兼容Entity
     */
    private String storageType;

    /**
     * 存储配置参数（仅JANUS）- 用于兼容Entity
     */
    private String storageConfig;

    /**
     * 额外连接参数（JSON格式）- 用于兼容Entity
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
     * 优先级：1-10，数字越小优先级越高
     */
    private Integer priority;
    
    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 数据库名称
     */
    private String databaseName;
}