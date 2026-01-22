package com.graphdb.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(min = 2, max = 50, message = "连接名称长度必须在2-50个字符之间")
    private String name;
    
    /**
     * 类型：NEO4J/NEBULA/JANUS
     */
    @NotNull(message = "类型不能为空")
    @Pattern(regexp = "^(NEO4J|NEBULA|JANUS)$", message = "类型必须是NEO4J、NEBULA或JANUS")
    private String type;
    
    /**
     * 主机地址
     */
    @NotBlank(message = "主机地址不能为空")
    @Size(max = 255, message = "主机地址长度不能超过255个字符")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "主机地址格式不正确")
    private String host;
    
    /**
     * 端口号
     */
    @NotNull(message = "端口号不能为空")
    @Min(value = 1, message = "端口号必须大于等于1")
    @Max(value = 65535, message = "端口号不能超过65535")
    private Integer port;
    
    /**
     * 用户名
     */
    @Size(max = 100, message = "用户名长度不能超过100个字符")
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