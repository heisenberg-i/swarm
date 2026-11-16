// PURPOSE: Request/response DTO for SWARM API payloads (ApproveUserRequest).
package com.biopower.dto.request;

import com.biopower.model.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ApproveUserRequest {
    @NotNull
    private UUID companyId;
    @NotNull
    private UserRole role;
    private List<Long> plantIds;
}
