// PURPOSE: Request/response DTO for SWARM API payloads (ForgotPasswordRequest).
package com.biopower.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank @Email
    private String email;
}
