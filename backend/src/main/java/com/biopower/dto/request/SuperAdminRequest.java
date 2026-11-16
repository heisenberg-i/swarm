// PURPOSE: Request/response DTO for SWARM API payloads (SuperAdminRequest).
package com.biopower.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuperAdminRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    private String mobile;
    @NotBlank
    private String password;
}
