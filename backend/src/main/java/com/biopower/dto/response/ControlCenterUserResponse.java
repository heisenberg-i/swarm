package com.biopower.dto.response;

import com.biopower.model.enums.UserRole;
import com.biopower.model.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ControlCenterUserResponse {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private UserRole role;
    private UserStatus status;
    private Long companyId;
    private String companyName;
    private List<Long> plantIds;
    private String queueReason;
    private LocalDateTime createdAt;
}
