// PURPOSE: Application service implementing SWARM deviceregistry business rules.
package com.biopower.service;

import com.biopower.exception.BadRequestException;
import com.biopower.exception.ResourceNotFoundException;
import com.biopower.model.entity.Device;
import com.biopower.model.entity.DeviceChannel;
import com.biopower.model.entity.Plant;
import com.biopower.model.entity.SensorNode;
import com.biopower.model.enums.DeviceKind;
import com.biopower.model.enums.DeviceLifecycle;
import com.biopower.model.enums.SensorType;
import com.biopower.repository.DeviceChannelRepository;
import com.biopower.repository.DeviceRepository;
import com.biopower.repository.PlantRepository;
import com.biopower.repository.SensorNodeRepository;
import com.biopower.util.DeviceSecretHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * First-class device + channel registry. Links ESP hub SensorNode rows as channels.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceRegistryService {

    private final DeviceRepository deviceRepository;
    private final DeviceChannelRepository deviceChannelRepository;
    private final SensorNodeRepository sensorNodeRepository;
    private final PlantRepository plantRepository;

    @Transactional
    public DeviceProvisionResult ensureEspHubDevice(Plant plant, String chipId, String deviceName,
                                                    List<SensorNode> nodes) {
        String normalizedChip = chipId.toLowerCase(Locale.ROOT);
        Optional<Device> existing = deviceRepository.findByPlantIdAndChipId(plant.getPlantId(), normalizedChip);
        if (existing.isPresent()) {
            Device device = existing.get();
            syncChannelsFromNodes(device, nodes);
            return new DeviceProvisionResult(device, null, false);
        }

        String uid = uniqueUid(normalizedChip);
        String rawSecret = DeviceSecretHasher.generateRawSecret();
        Device device = Device.builder()
                .plantId(plant.getPlantId())
                .tenantId(plant.getCompany() != null ? plant.getCompany().getId() : null)
                .deviceUid(uid)
                .chipId(normalizedChip)
                .deviceName(deviceName)
                .deviceKind(DeviceKind.ESP_HUB)
                .lifecycle(DeviceLifecycle.ACTIVE)
                .secretHash(DeviceSecretHasher.hash(rawSecret))
                .legacyCompat(true)
                .configVersion(1)
                .build();
        device = deviceRepository.save(device);
        syncChannelsFromNodes(device, nodes);
        log.info("Provisioned device {} for plant {} chip {}", uid, plant.getPlantId(), normalizedChip);
        return new DeviceProvisionResult(device, rawSecret, true);
    }

    @Transactional
    public void syncChannelsFromNodes(Device device, List<SensorNode> nodes) {
        int nextChannel = 1;
        for (SensorNode node : nodes) {
            Optional<DeviceChannel> byNode = deviceChannelRepository
                    .findByDeviceIdAndSensorNodeId(device.getId(), node.getNodeId());
            if (byNode.isPresent()) {
                continue;
            }
            Optional<DeviceChannel> byType = deviceChannelRepository
                    .findByDeviceIdAndMeasurementType(device.getId(), node.getSensorType());
            if (byType.isPresent()) {
                DeviceChannel channel = byType.get();
                channel.setSensorNodeId(node.getNodeId());
                if (channel.getInstrumentTag() == null || channel.getInstrumentTag().isBlank()) {
                    channel.setInstrumentTag(defaultTag(node));
                }
                deviceChannelRepository.save(channel);
                continue;
            }
            while (deviceChannelRepository.existsByDeviceIdAndChannelId(device.getId(), String.valueOf(nextChannel))) {
                nextChannel++;
            }
            DeviceChannel channel = DeviceChannel.builder()
                    .deviceId(device.getId())
                    .channelId(String.valueOf(nextChannel++))
                    .sensorNodeId(node.getNodeId())
                    .measurementType(node.getSensorType())
                    .engineeringUnit(defaultUnit(node.getSensorType()))
                    .instrumentTag(defaultTag(node))
                    .slope(1.0)
                    .intercept(0.0)
                    .hardMin(hardMin(node.getSensorType()))
                    .hardMax(hardMax(node.getSensorType()))
                    .samplingIntervalSec(30)
                    .active(true)
                    .build();
            deviceChannelRepository.save(channel);
        }
    }

    public String allocateDeviceUid(String chipId) {
        return uniqueUid(chipId);
    }

    @Transactional
    public String rotateSecret(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + deviceId));
        String raw = DeviceSecretHasher.generateRawSecret();
        device.setSecretHash(DeviceSecretHasher.hash(raw));
        device.setLegacyCompat(false);
        deviceRepository.save(device);
        return raw;
    }

    @Transactional(readOnly = true)
    public Optional<Device> findByUid(String deviceUid) {
        if (deviceUid == null || deviceUid.isBlank()) {
            return Optional.empty();
        }
        return deviceRepository.findFirstByDeviceUid(deviceUid.trim());
    }

    @Transactional(readOnly = true)
    public Optional<Device> findByPlantAndChip(Long plantId, String chipId) {
        if (plantId == null || chipId == null || chipId.isBlank()) {
            return Optional.empty();
        }
        return deviceRepository.findByPlantIdAndChipId(plantId, chipId.toLowerCase(Locale.ROOT));
    }

    @Transactional(readOnly = true)
    public List<DeviceChannel> channelsFor(Long deviceId) {
        return deviceChannelRepository.findByDeviceIdOrderByChannelIdAsc(deviceId);
    }

    @Transactional
    public DeviceChannel upsertChannel(Long deviceId, DeviceChannelUpdate update) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + deviceId));
        if (update.channelId() == null || update.channelId().isBlank()) {
            throw new BadRequestException("channelId is required");
        }
        DeviceChannel channel = deviceChannelRepository
                .findByDeviceIdAndChannelId(device.getId(), update.channelId())
                .orElse(DeviceChannel.builder()
                        .deviceId(device.getId())
                        .channelId(update.channelId())
                        .slope(1.0)
                        .intercept(0.0)
                        .active(true)
                        .build());

        if (update.instrumentTag() != null && !update.instrumentTag().isBlank()) {
            deviceChannelRepository.findByDeviceIdOrderByChannelIdAsc(device.getId()).stream()
                    .filter(c -> update.instrumentTag().equalsIgnoreCase(c.getInstrumentTag()))
                    .filter(c -> !update.channelId().equals(c.getChannelId()))
                    .findFirst()
                    .ifPresent(c -> {
                        throw new BadRequestException("Instrument tag already used on channel " + c.getChannelId());
                    });
            channel.setInstrumentTag(update.instrumentTag().trim());
        }
        if (update.measurementType() != null) {
            channel.setMeasurementType(update.measurementType());
        }
        if (channel.getMeasurementType() == null) {
            throw new BadRequestException("measurementType is required");
        }
        if (update.engineeringUnit() != null) {
            channel.setEngineeringUnit(update.engineeringUnit());
        }
        if (update.sensorNodeId() != null) {
            SensorNode node = sensorNodeRepository.findById(update.sensorNodeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sensor node not found"));
            if (!node.getPlant().getPlantId().equals(device.getPlantId())) {
                throw new BadRequestException("Sensor node is not on the device plant");
            }
            channel.setSensorNodeId(node.getNodeId());
        }
        if (update.slope() != null) {
            channel.setSlope(update.slope());
        }
        if (update.intercept() != null) {
            channel.setIntercept(update.intercept());
        }
        if (update.validityMin() != null) {
            channel.setValidityMin(update.validityMin());
        }
        if (update.validityMax() != null) {
            channel.setValidityMax(update.validityMax());
        }
        if (update.hardMin() != null) {
            channel.setHardMin(update.hardMin());
        }
        if (update.hardMax() != null) {
            channel.setHardMax(update.hardMax());
        }
        if (update.samplingIntervalSec() != null) {
            channel.setSamplingIntervalSec(update.samplingIntervalSec());
        }
        if (update.active() != null) {
            channel.setActive(update.active());
        }
        if (update.equipmentId() != null) {
            channel.setEquipmentId(update.equipmentId());
        }
        if (update.processAreaId() != null) {
            channel.setProcessAreaId(update.processAreaId());
        }

        device.setConfigVersion(device.getConfigVersion() + 1);
        deviceRepository.save(device);
        return deviceChannelRepository.save(channel);
    }

    @Transactional
    public int backfillFromSensorNodes() {
        List<SensorNode> hubs = sensorNodeRepository.findAll().stream()
                .filter(n -> n.getDeviceChipId() != null && !n.getDeviceChipId().isBlank())
                .toList();
        int created = 0;
        for (SensorNode sample : hubs) {
            String chip = sample.getDeviceChipId().toLowerCase(Locale.ROOT);
            Long plantId = sample.getPlant().getPlantId();
            if (deviceRepository.findByPlantIdAndChipId(plantId, chip).isPresent()) {
                continue;
            }
            Plant plant = plantRepository.findById(plantId)
                    .orElse(null);
            if (plant == null) {
                continue;
            }
            List<SensorNode> nodes = sensorNodeRepository.findByPlantPlantIdAndDeviceChipId(plantId, chip);
            ensureEspHubDevice(plant, chip, "ESP-Hub-" + chip.substring(Math.max(0, chip.length() - 4)), nodes);
            created++;
        }
        if (created > 0) {
            log.info("Backfilled {} devices from sensor_nodes chip ids", created);
        }
        return created;
    }

    private String uniqueUid(String chipId) {
        String base = DeviceSecretHasher.generateDeviceUid(chipId);
        if (!deviceRepository.existsByDeviceUid(base)) {
            return base;
        }
        for (int i = 2; i < 100; i++) {
            String candidate = base + "-" + i;
            if (!deviceRepository.existsByDeviceUid(candidate)) {
                return candidate;
            }
        }
        return DeviceSecretHasher.generateDeviceUid(null);
    }

    private static String defaultTag(SensorNode node) {
        return switch (node.getSensorType()) {
            case TEMPERATURE -> "TIT-HUB-TEMP";
            case HUMIDITY -> "HIT-HUB-HUM";
            case METHANE -> "AIT-HUB-GAS";
            default -> node.getNodeName();
        };
    }

    private static String defaultUnit(SensorType type) {
        return switch (type) {
            case TEMPERATURE, TEMPERATURE_TRANSMITTER -> "°C";
            case HUMIDITY -> "%";
            case METHANE -> "ADC";
            case PH -> "pH";
            case PRESSURE, PRESSURE_TRANSMITTER -> "bar";
            case GAS_FLOW, FLOW_TRANSMITTER -> "m³/h";
            case LIQUID_LEVEL -> "%";
            case CARBON_DIOXIDE -> "%";
            case HYDROGEN_SULFIDE, AMMONIA -> "ppm";
            default -> null;
        };
    }

    private static Double hardMin(SensorType type) {
        return switch (type) {
            case TEMPERATURE, TEMPERATURE_TRANSMITTER -> -40.0;
            case HUMIDITY, LIQUID_LEVEL, CARBON_DIOXIDE -> 0.0;
            case PH -> 0.0;
            case METHANE -> 0.0;
            case PRESSURE, PRESSURE_TRANSMITTER, GAS_FLOW, FLOW_TRANSMITTER -> 0.0;
            default -> null;
        };
    }

    private static Double hardMax(SensorType type) {
        return switch (type) {
            case TEMPERATURE, TEMPERATURE_TRANSMITTER -> 200.0;
            case HUMIDITY, LIQUID_LEVEL, CARBON_DIOXIDE -> 100.0;
            case PH -> 14.0;
            case METHANE -> 1024.0;
            default -> null;
        };
    }

    public record DeviceProvisionResult(Device device, String plaintextSecretOnce, boolean newlyCreated) {
    }

    public record DeviceChannelUpdate(
            String channelId,
            SensorType measurementType,
            String engineeringUnit,
            String instrumentTag,
            Long sensorNodeId,
            Long equipmentId,
            Long processAreaId,
            Double slope,
            Double intercept,
            Double validityMin,
            Double validityMax,
            Double hardMin,
            Double hardMax,
            Integer samplingIntervalSec,
            Boolean active
    ) {
    }
}
