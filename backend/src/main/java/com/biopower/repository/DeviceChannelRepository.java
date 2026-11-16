// PURPOSE: Spring Data access for SWARM DeviceChannel persistence.
package com.biopower.repository;

import com.biopower.model.entity.DeviceChannel;
import com.biopower.model.enums.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceChannelRepository extends JpaRepository<DeviceChannel, Long> {
    List<DeviceChannel> findByDeviceIdOrderByChannelIdAsc(Long deviceId);

    Optional<DeviceChannel> findByDeviceIdAndChannelId(Long deviceId, String channelId);

    Optional<DeviceChannel> findByDeviceIdAndSensorNodeId(Long deviceId, Long sensorNodeId);

    Optional<DeviceChannel> findByDeviceIdAndMeasurementType(Long deviceId, SensorType measurementType);

    List<DeviceChannel> findBySensorNodeId(Long sensorNodeId);

    boolean existsByDeviceIdAndChannelId(Long deviceId, String channelId);

    boolean existsByDeviceIdAndInstrumentTag(Long deviceId, String instrumentTag);
}
