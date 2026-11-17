// PURPOSE: Application service implementing SWARM analytics business rules.
package com.biopower.service;

import com.biopower.dto.response.PredictiveMaintenanceResponse;
import com.biopower.dto.response.SensorReadingResponse;
import com.biopower.model.entity.PredictiveMaintenance;
import com.biopower.model.enums.EquipmentType;
import com.biopower.model.enums.SensorType;
import com.biopower.repository.PredictiveMaintenanceRepository;
import com.biopower.repository.SensorReadingRepository;
import com.biopower.security.UserPrincipal;
import com.biopower.service.PlantAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SensorReadingRepository sensorReadingRepository;
    private final PlantAccessService plantAccessService;

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getHistoricalData(Long plantId, SensorType sensorType,
                                                          LocalDateTime start, LocalDateTime end,
                                                          UserPrincipal principal) {
        plantAccessService.assertCanAccessPlant(principal, plantId);
        return getHistoricalData(plantId, sensorType, start, end);
    }

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getAllSensorData(Long plantId, LocalDateTime start, LocalDateTime end,
                                                         UserPrincipal principal) {
        plantAccessService.assertCanAccessPlant(principal, plantId);
        return getAllSensorData(plantId, start, end);
    }

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getHistoricalData(Long plantId, SensorType sensorType,
                                                          LocalDateTime start, LocalDateTime end) {
        return sensorReadingRepository.findByPlantAndTypeAndDateRange(plantId, sensorType, start, end)
                .stream()
                .map(AnalyticsService::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getAllSensorData(Long plantId, LocalDateTime start, LocalDateTime end) {
        return sensorReadingRepository.findByPlantAndDateRange(plantId, start, end)
                .stream()
                .map(AnalyticsService::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getHistoricalData(Long plantId, SensorType sensorType,
                                                          LocalDateTime start, LocalDateTime end,
                                                          Long nodeId, String deviceUid,
                                                          UserPrincipal principal) {
        plantAccessService.assertCanAccessPlant(principal, plantId);
        return getHistoricalData(plantId, sensorType, start, end).stream()
                .filter(r -> nodeId == null || nodeId.equals(r.getNodeId()))
                .filter(r -> deviceUid == null || deviceUid.isBlank()
                        || deviceUid.equalsIgnoreCase(r.getDeviceUid()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> getAllSensorData(Long plantId, LocalDateTime start, LocalDateTime end,
                                                         Long nodeId, String deviceUid,
                                                         UserPrincipal principal) {
        plantAccessService.assertCanAccessPlant(principal, plantId);
        return getAllSensorData(plantId, start, end).stream()
                .filter(r -> nodeId == null || nodeId.equals(r.getNodeId()))
                .filter(r -> deviceUid == null || deviceUid.isBlank()
                        || deviceUid.equalsIgnoreCase(r.getDeviceUid()))
                .collect(Collectors.toList());
    }

    static SensorReadingResponse toResponse(com.biopower.model.entity.SensorReading r) {
        return SensorReadingResponse.builder()
                .id(r.getId())
                .plantId(r.getPlantId())
                .nodeId(r.getNodeId())
                .deviceUid(r.getDeviceUid())
                .channelId(r.getChannelId())
                .sensorType(r.getSensorType())
                .value(r.getValue())
                .unit(r.getUnit())
                .quality(r.getQuality())
                .recordedAt(r.getRecordedAt())
                .build();
    }
}
