package com.biopower.dto.response;

import com.biopower.model.enums.EquipmentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PredictiveMaintenanceResponse {
    private Long id;
    private Long plantId;
    private Long nodeId;
    private EquipmentType equipmentType;
    private String equipmentName;
    private String sensorType;
    private String unit;
    private String nodeStatus;
    private Double lastValue;
    private LocalDateTime lastReadingAt;
    private String chipId;
    private Integer remainingUsefulLifeDays;
    private LocalDate estimatedFailureDate;
    private Double healthPercentage;
    private String notes;
    private LocalDateTime updatedAt;
}
