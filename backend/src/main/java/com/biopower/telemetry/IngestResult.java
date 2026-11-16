// PURPOSE: Telemetry ingest DTO/helper used by SWARM HTTP and MQTT pipelines.
package com.biopower.telemetry;

import com.biopower.dto.response.SensorReadingResponse;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class IngestResult {
    private boolean duplicate;
    private Long deviceId;
    private String deviceUid;
    @Builder.Default
    private List<SensorReadingResponse> readings = new ArrayList<>();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
