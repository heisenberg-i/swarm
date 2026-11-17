// PURPOSE: Request/response DTO for SWARM API payloads (SensorReadingResponse).
package com.biopower.dto.response;

import com.biopower.model.enums.DataQuality;
import com.biopower.model.enums.SensorType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SensorReadingResponse {
    private Long id;
    private Long plantId;
    private Long nodeId;
    private String deviceUid;
    private String channelId;
    private SensorType sensorType;
    private Double value;
    private String unit;
    private DataQuality quality;
    private LocalDateTime recordedAt;
}
