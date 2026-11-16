// PURPOSE: Spring Data access for SWARM SensorReadingCustom persistence.
package com.biopower.repository;

import com.biopower.model.entity.SensorReading;
import com.biopower.model.enums.SensorType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorReadingRepositoryCustom {
    List<SensorReading> findByPlantAndTypeAndDateRange(Long plantId, SensorType type,
                                                       LocalDateTime start, LocalDateTime end);

    List<SensorReading> findByPlantAndDateRange(Long plantId, LocalDateTime start, LocalDateTime end);

    Optional<SensorReading> findFirstByPlantIdAndSensorTypeOrderByRecordedAtDesc(Long plantId, SensorType sensorType);

    Optional<SensorReading> findFirstByNodeIdOrderByRecordedAtDesc(Long nodeId);

    long countByNodeIdAndRecordedAtAfter(Long nodeId, LocalDateTime since);

    Double averageValue(Long plantId, SensorType type, LocalDateTime start, LocalDateTime end);

    List<SensorReading> findPartnerHistory(List<Long> plantIds, Long plantId, Long nodeId,
                                           SensorType sensorType, LocalDateTime updatedSince,
                                           LocalDateTime cursorTime, Long cursorId, Pageable pageable);

    List<SensorReading> findPartnerRange(List<Long> plantIds, Long plantId, Long nodeId,
                                         SensorType sensorType, LocalDateTime start, LocalDateTime end);

    void deleteByPlantId(Long plantId);

    int deleteByRecordedAtBefore(LocalDateTime cutoff);
}
