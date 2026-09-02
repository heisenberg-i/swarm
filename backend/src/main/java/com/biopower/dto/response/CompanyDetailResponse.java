package com.biopower.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompanyDetailResponse {
    private CompanyResponse company;
    private UserResponse superAdmin;
    private List<PlantResponse> plants;
    private List<UserResponse> users;
    private List<ControlCenterSensorNodeResponse> sensors;
}
