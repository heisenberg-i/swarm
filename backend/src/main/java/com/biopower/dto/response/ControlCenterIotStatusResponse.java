// PURPOSE: Request/response DTO for SWARM API payloads (ControlCenterIotStatusResponse).
package com.biopower.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ControlCenterIotStatusResponse {
    private boolean enabled;
    private boolean configured;
    private boolean connected;
    private String brokerUrl;
    private String[] topics;
    private long messagesReceived;
    private long failures;
    private Instant lastConnectedAt;
    private Instant lastMessageAt;
    private String lastError;
    private String swarmTopicPattern;
    private String legacyTopicPattern;
    private String protocolVersion;
    private int registeredDevices;
    private int devicesSeen;
    private List<IngestRejectionSummary> recentRejections;

    @Data
    @Builder
    public static class IngestRejectionSummary {
        private String deviceUid;
        private Long plantId;
        private String transport;
        private String reasonCode;
        private String reasonDetail;
        private Instant receivedAt;
    }
}
