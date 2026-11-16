// PURPOSE: Telemetry ingest DTO/helper used by SWARM HTTP and MQTT pipelines.
package com.biopower.telemetry;

import com.biopower.model.enums.IngestTransport;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class IngestEnvelope {
    private IngestTransport transport;
    private String schemaVersion;
    private String messageId;
    private Long plantId;
    private String deviceUid;
    private String chipId;
    private Integer rssi;
    private String ip;
    private String firmwareVersion;
    private LocalDateTime sourceTimestamp;
    private List<IngestPoint> readings;
    private String topic;
    private String payloadExcerpt;
}
