// PURPOSE: Request/response DTO for SWARM API payloads (DeviceChannelRequest).
package com.biopower.dto.request;

import com.biopower.model.enums.SensorType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceChannelRequest {
    @NotBlank
    private String channelId;
    private SensorType measurementType;
    private String engineeringUnit;
    private String instrumentTag;
    private Long sensorNodeId;
    private Long equipmentId;
    private Long processAreaId;
    private Double slope;
    private Double intercept;
    private Double validityMin;
    private Double validityMax;
    private Double hardMin;
    private Double hardMax;
    private Integer samplingIntervalSec;
    private Boolean active;
}
