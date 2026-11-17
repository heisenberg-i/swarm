// PURPOSE: Request/response DTO for SWARM API payloads (PlantUpdateRequest).
package com.biopower.dto.request;

import com.biopower.model.enums.PlantStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class PlantUpdateRequest {
    private String plantName;
    private String location;
    private PlantStatus status;
    /** Assign an unassigned plant, or confirm the existing tenant. Cannot steal from another tenant. */
    private UUID companyId;
}
