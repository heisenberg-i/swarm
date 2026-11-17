// PURPOSE: Request/response DTO for SWARM API payloads (SensorTypeInfo).
package com.biopower.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SensorTypeInfo {
    private String code;
    private String label;
    private String defaultUnit;
    private Double hardMin;
    private Double hardMax;
}
