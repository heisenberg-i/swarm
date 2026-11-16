// PURPOSE: Request/response DTO for SWARM API payloads (FieldDeviceChannelResponse).
package com.biopower.dto.response;

import com.biopower.model.enums.SensorType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FieldDeviceChannelResponse {
    private Long id;
    private String channelId;
    private Long sensorNodeId;
    private SensorType measurementType;
    private String engineeringUnit;
    private String instrumentTag;
    private Long equipmentId;
    private Long processAreaId;
    private double slope;
    private double intercept;
    private Double validityMin;
    private Double validityMax;
    private Double hardMin;
    private Double hardMax;
    private Integer samplingIntervalSec;
    private boolean active;
    private Double lastValue;
    private String lastQuality;
    private String dataStatus;
}
