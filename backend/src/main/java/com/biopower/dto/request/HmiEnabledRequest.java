// PURPOSE: Request/response DTO for SWARM API payloads (HmiEnabledRequest).
package com.biopower.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HmiEnabledRequest {
    @NotNull
    private Boolean enabled;
}
