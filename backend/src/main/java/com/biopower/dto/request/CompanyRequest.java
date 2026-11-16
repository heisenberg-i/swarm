// PURPOSE: Request/response DTO for SWARM API payloads (CompanyRequest).
package com.biopower.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRequest {
    @NotBlank
    private String name;
}
