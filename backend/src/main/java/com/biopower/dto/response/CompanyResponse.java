// PURPOSE: Request/response DTO for SWARM API payloads (CompanyResponse).
package com.biopower.dto.response;

import com.biopower.model.enums.CompanyStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CompanyResponse {
    private UUID id;
    private String name;
    private CompanyStatus status;
    private String superAdminEmail;
    private String superAdminName;
    private int plantCount;
    private int userCount;
    private String onboardingState;
    private Instant createdAt;
}
