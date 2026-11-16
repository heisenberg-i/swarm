// PURPOSE: Request/response DTO for SWARM API payloads (DevicePairResponse).
package com.biopower.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevicePairResponse {
    private String chipId;
    private String deviceName;
    private Long plantId;
    private Long temperatureNodeId;
    private Long humidityNodeId;
    private Long gasNodeId;
    private boolean newlyPaired;
    /** Stable SWARM device identity for MQTT topics and authenticated ingest. */
    private String deviceUid;
    private Long deviceDbId;
    /**
     * One-time device secret. Shown only when newly created or rotated.
     * Firmware should send X-SWARM-Device-Id / X-SWARM-Device-Secret.
     */
    private String deviceSecret;
}
