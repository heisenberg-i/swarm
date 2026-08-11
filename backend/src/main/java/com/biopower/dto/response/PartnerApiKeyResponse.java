package com.biopower.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PartnerApiKeyResponse {
    private Long id;
    private Long organizationId;
    private String name;
    private String keyPrefix;
    private boolean active;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
}
