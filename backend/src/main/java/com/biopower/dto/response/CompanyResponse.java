package com.biopower.dto.response;

import com.biopower.model.enums.CompanyStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CompanyResponse {
    private Long id;
    private String name;
    private CompanyStatus status;
    private String superAdminEmail;
    private String superAdminName;
    private int plantCount;
    private int userCount;
    private String onboardingState;
    private LocalDateTime createdAt;
}
