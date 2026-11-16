// PURPOSE: Request/response DTO for SWARM API payloads (AuthResponse).
package com.biopower.dto.response;

import com.biopower.model.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String type;
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private List<Long> plantIds;
    private List<Long> nodeIds;
    private UUID companyId;
}
