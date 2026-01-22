package com.graphdb.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Map;

/**
 * 创建节点请求DTO
 */
@Data
@Schema(description = "创建节点请求")
public class VertexDTO {

    @NotBlank(message = "节点类型不能为空")
    @Size(max = 100, message = "节点类型长度不能超过100个字符")
    @Schema(description = "节点类型标签", example = "Person")
    private String label;

    @NotNull(message = "节点属性不能为空")
    @Schema(description = "节点属性", example = "{\"name\": \"张三\", \"age\": 30}")
    private Map<String, Object> properties;
}
