package com.graphdb.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Map;

/**
 * 创建类型请求DTO（点类型或边类型）
 */
@Data
@Schema(description = "创建类型请求")
public class LabelTypeDTO {

    @NotBlank(message = "类型名称不能为空")
    @Size(max = 100, message = "类型名称长度不能超过100个字符")
    @Schema(description = "类型名称", example = "Person")
    private String name;

    @NotNull(message = "类型属性不能为空")
    @Schema(description = "类型属性定义", example = "{\"name\": \"String\", \"age\": \"Integer\"}")
    private Map<String, Object> properties;
}
