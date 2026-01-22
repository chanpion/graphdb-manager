package com.graphdb.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

/**
 * 更新边请求DTO
 */
@Data
@Schema(description = "更新边请求")
public class EdgeUpdateDTO {

    @NotNull(message = "边属性不能为空")
    @Schema(description = "边属性", example = "{\"since\": \"2021\", \"weight\": 2.0}")
    private Map<String, Object> properties;
}
