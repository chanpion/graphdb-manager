package com.graphdb.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 连接配置DTO
 */
@Data
public class ConnectionConfigDTO {
    
    /**
     * 连接ID
     */
    private Long id;
    
    /**
     * 连接名称
     */
    @NotBlank(message = "连接名称不能为空")
    private String name;
    
    /**
     * 数据库类型：NEO4J/NEBULA/JANUS
     */
    @NotNull(message = "数据库类型不能为空")
    private String databaseType;
    
    /**
     * 主机地址
     */
    @NotBlank(message = "主机地址不能为空")
    private String host;
    
    /**
     * 端口号
     */
    @NotNull(message = "端口号不能为空")
    private Integer port;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码（仅请求时使用，响应时不返回）
     */
    @NotBlank(message = "密码不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
     * 存储类型（仅JANUS）：HBASE/CASSANDRA - 用于Entity映射
     */
    private String storageType;

    /**
     * 存储配置参数（仅JANUS）- 用于Entity映射
     */
    private String storageConfig;

    /**
     * 额外连接参数（JSON格式）- 用于Entity映射
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
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
