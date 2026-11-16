// PURPOSE: Request/response DTO for SWARM API payloads (ControlCenterOverviewResponse).
package com.biopower.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ControlCenterOverviewResponse {
    private long companiesTotal;
    private long companiesActive;
    private long companiesDisabled;
    private long tenantsReady;
    private long tenantsNeedAdmin;
    private long tenantsNeedPlants;
    private long plantsTotal;
    private long plantsUnassigned;
    private long plantsAssigned;
    private long pendingApprovals;
    private long plantsHmiEnabled;
}
