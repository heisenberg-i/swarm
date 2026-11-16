// PURPOSE: Request/response DTO for SWARM API payloads (IoTBatchRequest).
package com.biopower.dto.request;

import com.biopower.model.enums.SensorType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IoTBatchRequest {
    @NotNull
    private Long plantId;
    private String chipId;
    /** Optional SWARM device UID when firmware has been upgraded. */
    private String deviceId;
    private Integer rssi;
    /** LAN IPv4 reported by the ESP hub (`WiFi.localIP()`). */
    private String ip;
    /** Idempotency key; duplicates are acknowledged without re-alerting. */
    private String messageId;
    private LocalDateTime timestamp;
    @NotEmpty
    @Valid
    private List<Reading> readings;

    @Data
    public static class Reading {
        @NotNull
        private Long nodeId;
        @NotNull
        private SensorType sensorType;
        @NotNull
        private Double value;
    }
}
