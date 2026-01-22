package com.graphdb.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Map;

/**
 * 创建边请求DTO
 */
@Data
@Schema(description = "创建边请求")
public class EdgeDTO {

    @NotBlank(message = "边类型不能为空")
    @Size(max = 100, message = "边类型长度不能超过100个字符")
    @Schema(description = "边类型标签", example = "KNOWS")
    private String label;

    @NotBlank(message = "起始节点UID不能为空")
    @Size(max = 1000, message = "起始节点UID长度不能超过1000个字符")
    @Schema(description = "起始节点UID", example = "vertex_1234567890")
    private String sourceUid;

    @NotBlank(message = "目标节点UID不能为空")
    @Size(max = 1000, message = "目标节点UID长度不能超过1000个字符")
    @Schema(description = "目标节点UID", example = "vertex_1234567891")
    private String targetUid;

    @NotNull(message = "边属性不能为空")
    @Schema(description = "边属性", example = "{\"since\": \"2020\", \"weight\": 1.5}")
    private Map<String, Object> properties;
}
