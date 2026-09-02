package com.biopower.dto.request;

import com.biopower.model.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ApproveUserRequest {
    @NotNull
    private Long companyId;
    @NotNull
    private UserRole role;
    private List<Long> plantIds;
}
