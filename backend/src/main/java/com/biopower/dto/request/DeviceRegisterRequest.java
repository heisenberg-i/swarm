// PURPOSE: Request/response DTO for SWARM API payloads (DeviceRegisterRequest).
package com.biopower.dto.request;

import com.biopower.model.enums.DeviceKind;
import com.biopower.model.enums.DeviceLifecycle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class DeviceRegisterRequest {
    /** Optional. If sent, must match the plant's tenant. Never used as authorization. */
    private UUID tenantId;
    @NotNull
    private Long plantId;
    /** Optional unique UID. Generated when omitted. */
    private String deviceUid;
    private String chipId;
    private String deviceName;
    private DeviceKind deviceKind;
    private DeviceLifecycle lifecycle;
    @Valid
    private List<DeviceChannelRequest> channels = new ArrayList<>();
}
