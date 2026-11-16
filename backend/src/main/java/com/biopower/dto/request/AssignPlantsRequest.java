// PURPOSE: Request/response DTO for SWARM API payloads (AssignPlantsRequest).
package com.biopower.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignPlantsRequest {
    @NotNull
    private List<Long> plantIds;
}
