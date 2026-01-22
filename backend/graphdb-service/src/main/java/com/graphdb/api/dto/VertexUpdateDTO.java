package com.graphdb.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Map;

/**
 * 更新节点请求DTO
 */
@Data
@Schema(description = "更新节点请求")
public class VertexUpdateDTO {

    @NotNull(message = "节点属性不能为空")
    @Schema(description = "节点属性", example = "{\"name\": \"李四\", \"age\": 35}")
    private Map<String, Object> properties;
}
