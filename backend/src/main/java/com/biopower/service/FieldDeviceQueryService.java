// PURPOSE: Application service implementing SWARM fielddevicequery business rules.
package com.biopower.service;

import com.biopower.dto.response.FieldDeviceChannelResponse;
import com.biopower.dto.response.FieldDeviceResponse;
import com.biopower.exception.ResourceNotFoundException;
import com.biopower.model.entity.Device;
import com.biopower.model.entity.DeviceChannel;
import com.biopower.model.entity.Plant;
import com.biopower.model.entity.SensorNode;
import com.biopower.repository.DeviceChannelRepository;
import com.biopower.repository.DeviceRepository;
import com.biopower.repository.PlantRepository;
import com.biopower.repository.SensorNodeRepository;
import com.biopower.repository.SensorReadingRepository;
import com.biopower.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldDeviceQueryService {

    private final DeviceRepository deviceRepository;
    private final DeviceChannelRepository deviceChannelRepository;
    private final PlantRepository plantRepository;
    private final SensorNodeRepository sensorNodeRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final PlantAccessService plantAccessService;

    @Value("${biopower.alert.sensor-timeout-minutes:10}")
    private int sensorTimeoutMinutes;

    @Transactional(readOnly = true)
    public List<FieldDeviceResponse> listDevices(Long plantId, UserPrincipal principal) {
        if (plantId != null) {
            plantAccessService.assertCanAccessPlant(principal, plantId);
            return deviceRepository.findByPlantId(plantId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return deviceRepository.findAll().stream()
                .filter(d -> {
                    try {
                        plantAccessService.assertCanAccessPlant(principal, d.getPlantId());
                        return true;
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FieldDeviceResponse getDevice(Long id, UserPrincipal principal) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + id));
        plantAccessService.assertCanAccessPlant(principal, device.getPlantId());
        return toResponse(device);
    }

    private FieldDeviceResponse toResponse(Device device) {
        Plant plant = plantRepository.findById(device.getPlantId()).orElse(null);
        List<DeviceChannel> channels = deviceChannelRepository.findByDeviceIdOrderByChannelIdAsc(device.getId());
        String connection = connectionStatus(device);
        return FieldDeviceResponse.builder()
                .id(device.getId())
                .deviceUid(device.getDeviceUid())
                .plantId(device.getPlantId())
                .plantName(plant != null ? plant.getPlantName() : null)
                .chipId(device.getChipId())
                .deviceName(device.getDeviceName())
                .deviceKind(device.getDeviceKind())
                .lifecycle(device.getLifecycle())
                .legacyCompat(device.isLegacyCompat())
                .hasCredentials(device.getSecretHash() != null && !device.getSecretHash().isBlank())
                .lastSeenAt(device.getLastSeenAt() == null ? null
                        : LocalDateTime.ofInstant(device.getLastSeenAt(), ZoneId.systemDefault()))
                .lastIp(device.getLastIp())
                .lastRssi(device.getLastRssi())
                .firmwareVersion(device.getFirmwareVersion())
                .configVersion(device.getConfigVersion())
                .connectionStatus(connection)
                .channels(channels.stream().map(this::toChannel).toList())
                .build();
    }

    private FieldDeviceChannelResponse toChannel(DeviceChannel channel) {
        Double lastValue = null;
        String lastQuality = null;
        String dataStatus = "No Data";
        if (channel.getSensorNodeId() != null) {
            SensorNode node = sensorNodeRepository.findById(channel.getSensorNodeId()).orElse(null);
            if (node != null) {
                var latest = sensorReadingRepository.findFirstByNodeIdOrderByRecordedAtDesc(node.getNodeId());
                lastValue = latest.map(r -> r.getValue()).orElse(null);
                lastQuality = latest.map(r -> r.getQuality() != null ? r.getQuality().name() : null).orElse(null);
                if (node.getLastReadingAt() == null) {
                    dataStatus = "No Data";
                } else if (node.getLastReadingAt().isBefore(LocalDateTime.now().minusMinutes(sensorTimeoutMinutes))) {
                    dataStatus = "Stale";
                } else {
                    dataStatus = "Live";
                }
            }
        }
        return FieldDeviceChannelResponse.builder()
                .id(channel.getId())
                .channelId(channel.getChannelId())
                .sensorNodeId(channel.getSensorNodeId())
                .measurementType(channel.getMeasurementType())
                .engineeringUnit(channel.getEngineeringUnit())
                .instrumentTag(channel.getInstrumentTag())
                .equipmentId(channel.getEquipmentId())
                .processAreaId(channel.getProcessAreaId())
                .slope(channel.getSlope())
                .intercept(channel.getIntercept())
                .validityMin(channel.getValidityMin())
                .validityMax(channel.getValidityMax())
                .hardMin(channel.getHardMin())
                .hardMax(channel.getHardMax())
                .samplingIntervalSec(channel.getSamplingIntervalSec())
                .active(channel.isActive())
                .lastValue(lastValue)
                .lastQuality(lastQuality)
                .dataStatus(dataStatus)
                .build();
    }

    private String connectionStatus(Device device) {
        if (device.getLastSeenAt() == null) {
            return "Not Connected";
        }
        if (device.getLastSeenAt().isBefore(Instant.now().minus(sensorTimeoutMinutes, ChronoUnit.MINUTES))) {
            return "Offline";
        }
        return "Connected";
    }
}
