// PURPOSE: Application service implementing SWARM predictivemaintenance business rules.
package com.biopower.service;

import com.biopower.dto.response.PredictiveMaintenanceResponse;
import com.biopower.model.entity.PredictiveMaintenance;
import com.biopower.model.entity.SensorNode;
import com.biopower.model.entity.SensorReading;
import com.biopower.model.enums.EquipmentType;
import com.biopower.model.enums.NodeStatus;
import com.biopower.model.enums.SensorType;
import com.biopower.repository.PredictiveMaintenanceRepository;
import com.biopower.repository.SensorNodeRepository;
import com.biopower.repository.SensorReadingRepository;
import com.biopower.security.UserPrincipal;
import com.biopower.util.PartnerMetricCatalog;
import com.biopower.util.SensorHeartbeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictiveMaintenanceService {

    private static final int EXPECTED_LIFE_DAYS = 365;

    private final PredictiveMaintenanceRepository repository;
    private final SensorNodeRepository sensorNodeRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final PlantAccessService plantAccessService;
    private final SensorHeartbeat sensorHeartbeat;

    @Transactional(readOnly = true)
    public List<PredictiveMaintenanceResponse> getByPlant(Long plantId, UserPrincipal principal) {
        plantAccessService.assertCanAccessPlant(principal, plantId);
        return getByPlant(plantId);
    }

    @Transactional(readOnly = true)
    public List<PredictiveMaintenanceResponse> getByPlant(Long plantId) {
        return repository.findByPlantIdOrderByRemainingUsefulLifeDaysAsc(plantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void generatePredictionsForPlant(Long plantId) {
        repository.deleteByPlantId(plantId);
        repository.flush();
        List<SensorNode> nodes = sensorNodeRepository.findByPlantPlantId(plantId);
        for (SensorNode node : nodes) {
            repository.save(fromSensorNode(plantId, node));
        }
    }

    private PredictiveMaintenance fromSensorNode(Long plantId, SensorNode node) {
        SensorType type = node.getSensorType();
        SensorReading latest = sensorReadingRepository.findFirstByNodeIdOrderByRecordedAtDesc(node.getNodeId())
                .orElse(null);
        LocalDateTime lastAt = latest != null ? latest.getRecordedAt() : node.getLastReadingAt();
        Double lastValue = latest != null ? latest.getValue() : null;
        NodeStatus status = sensorHeartbeat.effectiveStatus(node);
        long readings24h = sensorReadingRepository.countByNodeIdAndRecordedAtAfter(
                node.getNodeId(), LocalDateTime.now().minusHours(24));

        List<String> notes = new ArrayList<>();
        double health = 100;

        if (status == NodeStatus.OFFLINE || status == NodeStatus.FAULTY) {
            health = 8;
            notes.add(status == NodeStatus.OFFLINE
                    ? "Hub/sensor is offline — no fresh reading."
                    : "Sensor marked faulty.");
        } else if (lastAt == null) {
            health = 20;
            notes.add("Paired but has never sent a reading.");
        } else {
            long minutesAgo = Math.max(0, Duration.between(lastAt, LocalDateTime.now()).toMinutes());
            if (minutesAgo > sensorHeartbeat.timeoutMinutes()) {
                health = Math.max(10, 40 - minutesAgo / 60.0);
                notes.add("Last reading " + minutesAgo + " min ago.");
            } else {
                notes.add("Online. Last reading " + minutesAgo + " min ago.");
            }
        }

        if (lastValue != null) {
            String rangeNote = rangeNote(type, lastValue);
            if (rangeNote != null) {
                health = Math.min(health, 45);
                notes.add(rangeNote);
            } else {
                notes.add("Latest " + formatValue(type, lastValue) + " is in the expected band for this plant.");
            }
        }

        if (node.getBatteryLevel() != null && node.getBatteryLevel() < 25) {
            health -= 15;
            notes.add("Battery " + node.getBatteryLevel() + "%.");
        }
        if (node.getSignalStrength() != null && node.getSignalStrength() < 30) {
            health -= 10;
            notes.add("Weak radio signal (" + node.getSignalStrength() + ").");
        }
        if (readings24h == 0 && status == NodeStatus.ACTIVE) {
            notes.add("No samples stored in the last 24 hours.");
            health = Math.min(health, 50);
        } else if (readings24h > 0) {
            notes.add(readings24h + " samples in the last 24 hours.");
        }
        if (node.getDeviceChipId() != null && !node.getDeviceChipId().isBlank()) {
            notes.add("Chip " + node.getDeviceChipId()
                    + (node.getDeviceIp() != null ? " @ " + node.getDeviceIp() : "") + ".");
        }

        health = Math.max(0, Math.min(100, health));
        int rul = (int) Math.round(EXPECTED_LIFE_DAYS * (health / 100.0));

        return PredictiveMaintenance.builder()
                .plantId(plantId)
                .equipmentType(EquipmentType.SENSOR)
                .equipmentName(node.getNodeName())
                .sensorType(type.name())
                .remainingUsefulLifeDays(rul)
                .estimatedFailureDate(LocalDate.now().plusDays(Math.max(rul, 1)))
                .healthPercentage(health)
                .notes(String.join(" ", notes))
                .build();
    }

    private String rangeNote(SensorType type, double value) {
        return switch (type) {
            case TEMPERATURE, TEMPERATURE_TRANSMITTER ->
                    (value < -10 || value > 80) ? "Temperature " + round(value) + " °C is outside -10–80 °C." : null;
            case HUMIDITY ->
                    (value < 0 || value > 100) ? "Humidity " + round(value) + " % is invalid." : null;
            case METHANE ->
                    value > 800 ? "Gas raw " + round(value) + " is above the critical MQ5 band (800)."
                            : value > 500 ? "Gas raw " + round(value) + " is in the warning band (>500)."
                            : null;
            case PH ->
                    (value < 5 || value > 9) ? "pH " + round(value) + " is outside 5–9." : null;
            case PRESSURE, PRESSURE_TRANSMITTER ->
                    (value < 0 || value > 10) ? "Pressure " + round(value) + " bar looks out of band." : null;
            case LIQUID_LEVEL ->
                    (value < 0 || value > 100) ? "Level " + round(value) + " % is invalid." : null;
            default -> null;
        };
    }

    private String formatValue(SensorType type, double value) {
        String unit = PartnerMetricCatalog.unitFor(type);
        if (type == SensorType.METHANE) {
            return "gas raw " + round(value);
        }
        return round(value) + (unit == null || unit.isBlank() ? "" : " " + unit);
    }

    private String round(double value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private PredictiveMaintenanceResponse toResponse(PredictiveMaintenance pm) {
        return PredictiveMaintenanceResponse.builder()
                .id(pm.getId())
                .plantId(pm.getPlantId())
                .equipmentType(pm.getEquipmentType())
                .equipmentName(pm.getEquipmentName())
                .sensorType(pm.getSensorType())
                .remainingUsefulLifeDays(pm.getRemainingUsefulLifeDays())
                .estimatedFailureDate(pm.getEstimatedFailureDate())
                .healthPercentage(pm.getHealthPercentage())
                .notes(pm.getNotes())
                .updatedAt(pm.getUpdatedAt())
                .build();
    }
}
