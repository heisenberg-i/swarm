// PURPOSE: Request/response DTO for SWARM API payloads (FieldDeviceResponse).
package com.biopower.dto.response;

import com.biopower.model.enums.DeviceKind;
import com.biopower.model.enums.DeviceLifecycle;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FieldDeviceResponse {
    private Long id;
    private String deviceUid;
    private Long plantId;
    private String plantName;
    private String chipId;
    private String deviceName;
    private DeviceKind deviceKind;
    private DeviceLifecycle lifecycle;
    private boolean legacyCompat;
    private boolean hasCredentials;
    private LocalDateTime lastSeenAt;
    private String lastIp;
    private Integer lastRssi;
    /** Null unless the device reported firmware. Never invented. */
    private String firmwareVersion;
    private int configVersion;
    private String connectionStatus;
    private List<FieldDeviceChannelResponse> channels;
}
