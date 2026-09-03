package com.biopower.dto.partner;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PartnerHealthResponse {
    private String status;
    private String apiVersion;
    /** Matches partner externalOrgId / emPOWER Settings helper (e.g. 001). */
    @JsonProperty("organizationId")
    private String organizationId;
    private String organizationName;
    private Instant serverTime;

    @JsonGetter("organization_id")
    public String jsonOrganizationIdSnake() {
        return organizationId;
    }
}
