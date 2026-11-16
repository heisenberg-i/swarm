// PURPOSE: Automated test covering SWARM TelemetryIngestServiceTest.
package com.biopower.telemetry;

import com.biopower.exception.BadRequestException;
import com.biopower.exception.UnauthorizedException;
import com.biopower.model.entity.Device;
import com.biopower.model.entity.DeviceChannel;
import com.biopower.model.entity.Plant;
import com.biopower.model.entity.SensorNode;
import com.biopower.model.entity.SensorReading;
import com.biopower.model.enums.DeviceKind;
import com.biopower.model.enums.DeviceLifecycle;
import com.biopower.model.enums.IngestTransport;
import com.biopower.model.enums.NodeStatus;
import com.biopower.model.enums.SensorType;
import com.biopower.repository.DeviceChannelRepository;
import com.biopower.repository.DeviceRepository;
import com.biopower.repository.SensorMessageRepository;
import com.biopower.repository.IngestRejectedRepository;
import com.biopower.repository.SensorNodeRepository;
import com.biopower.repository.SensorReadingRepository;
import com.biopower.security.DevicePrincipal;
import com.biopower.service.AiHealthService;
import com.biopower.service.AlertService;
import com.biopower.service.TelemetryIngestService;
import com.biopower.util.DeviceSecretHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryIngestServiceTest {

    @Mock DeviceRepository deviceRepository;
    @Mock DeviceChannelRepository deviceChannelRepository;
    @Mock SensorNodeRepository sensorNodeRepository;
    @Mock SensorReadingRepository sensorReadingRepository;
    @Mock SensorMessageRepository sensorMessageRepository;
    @Mock IngestRejectedRepository ingestRejectedRepository;
    @Mock AlertService alertService;
    @Mock AiHealthService aiHealthService;

    @InjectMocks
    TelemetryIngestService service;

    Device device;
    DeviceChannel channel;
    SensorNode node;
    Plant plant;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        ReflectionTestUtils.setField(service, "compatLegacy", true);
        ReflectionTestUtils.setField(service, "maxPayloadReadings", 64);

        plant = Plant.builder().plantId(7L).plantName("Demo").build();
        device = Device.builder()
                .id(11L)
                .plantId(7L)
                .tenantId(java.util.UUID.fromString("00000000-0000-4000-8000-000000000007"))
                .deviceUid("SWARM-HUB-ABCD")
                .chipId("abcd")
                .deviceName("Hub")
                .deviceKind(DeviceKind.ESP_HUB)
                .lifecycle(DeviceLifecycle.ACTIVE)
                .secretHash(DeviceSecretHasher.hash("swd_testsecret"))
                .legacyCompat(true)
                .configVersion(1)
                .build();
        node = SensorNode.builder()
                .nodeId(101L)
                .plant(plant)
                .nodeName("Temp")
                .sensorType(SensorType.TEMPERATURE)
                .deviceChipId("abcd")
                .status(NodeStatus.ACTIVE)
                .build();
        channel = DeviceChannel.builder()
                .id(1L)
                .deviceId(11L)
                .channelId("1")
                .sensorNodeId(101L)
                .measurementType(SensorType.TEMPERATURE)
                .engineeringUnit("°C")
                .slope(1.0)
                .intercept(0.0)
                .hardMin(-40.0)
                .hardMax(200.0)
                .active(true)
                .build();
    }

    @Test
    void authenticatedIngestPersistsReading() {
        SecurityContextHolder.getContext().setAuthentication(principal());
        when(deviceRepository.findById(11L)).thenReturn(Optional.of(device));
        when(sensorMessageRepository.existsByMessageId("msg-1")).thenReturn(false);
        when(deviceChannelRepository.findByDeviceIdAndChannelId(11L, "1")).thenReturn(Optional.of(channel));
        when(sensorNodeRepository.findById(101L)).thenReturn(Optional.of(node));
        when(sensorReadingRepository.save(any())).thenAnswer(inv -> {
            SensorReading r = inv.getArgument(0);
            r.setId(999L);
            return r;
        });

        IngestResult result = service.ingest(IngestEnvelope.builder()
                .transport(IngestTransport.HTTP)
                .plantId(7L)
                .deviceUid("SWARM-HUB-ABCD")
                .messageId("msg-1")
                .readings(List.of(IngestPoint.builder().channelId("1").value(38.5).build()))
                .build());

        assertFalse(result.isDuplicate());
        assertEquals(1, result.getReadings().size());
        assertEquals(38.5, result.getReadings().get(0).getValue());
        verify(alertService).evaluateReading(any());
        verify(aiHealthService).analyzePlant(7L);
        verify(sensorMessageRepository).save(any());
    }

    @Test
    void duplicateMessageIdIsIdempotent() {
        SecurityContextHolder.getContext().setAuthentication(principal());
        when(deviceRepository.findById(11L)).thenReturn(Optional.of(device));
        when(sensorMessageRepository.existsByMessageId("msg-dup")).thenReturn(true);

        IngestResult result = service.ingest(IngestEnvelope.builder()
                .transport(IngestTransport.MQTT)
                .plantId(7L)
                .messageId("msg-dup")
                .readings(List.of(IngestPoint.builder().channelId("1").value(1.0).build()))
                .build());

        assertTrue(result.isDuplicate());
        verify(sensorReadingRepository, never()).save(any());
        verify(alertService, never()).evaluateReading(any());
    }

    @Test
    void crossPlantPayloadRejected() {
        SecurityContextHolder.getContext().setAuthentication(principal());
        when(deviceRepository.findById(11L)).thenReturn(Optional.of(device));

        assertThrows(UnauthorizedException.class, () -> service.ingest(IngestEnvelope.builder()
                .transport(IngestTransport.HTTP)
                .plantId(99L)
                .readings(List.of(IngestPoint.builder().channelId("1").value(1.0).build()))
                .build()));
    }

    @Test
    void forgedDeviceUidRejectedWhenAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(principal());
        when(deviceRepository.findById(11L)).thenReturn(Optional.of(device));

        assertThrows(UnauthorizedException.class, () -> service.ingest(IngestEnvelope.builder()
                .transport(IngestTransport.HTTP)
                .plantId(7L)
                .deviceUid("SWARM-HUB-OTHER")
                .readings(List.of(IngestPoint.builder().channelId("1").value(1.0).build()))
                .build()));
    }

    @Test
    void hardRangeRejectsValue() {
        SecurityContextHolder.getContext().setAuthentication(principal());
        when(deviceRepository.findById(11L)).thenReturn(Optional.of(device));
        when(sensorMessageRepository.existsByMessageId(anyString())).thenReturn(false);
        when(deviceChannelRepository.findByDeviceIdAndChannelId(11L, "1")).thenReturn(Optional.of(channel));
        when(sensorNodeRepository.findById(101L)).thenReturn(Optional.of(node));

        assertThrows(BadRequestException.class, () -> service.ingest(IngestEnvelope.builder()
                .transport(IngestTransport.HTTP)
                .plantId(7L)
                .messageId("msg-range")
                .readings(List.of(IngestPoint.builder().channelId("1").value(9999.0).build()))
                .build()));
        verify(ingestRejectedRepository).save(any());
    }

    @Test
    void legacyCompatAllowsChipWithoutCredentials() {
        when(deviceRepository.findByPlantIdAndChipId(7L, "abcd")).thenReturn(Optional.of(device));
        when(sensorMessageRepository.existsByMessageId(anyString())).thenReturn(false);
        when(deviceChannelRepository.findByDeviceIdAndSensorNodeId(11L, 101L)).thenReturn(Optional.of(channel));
        when(sensorNodeRepository.findById(101L)).thenReturn(Optional.of(node));
        when(sensorReadingRepository.save(any())).thenAnswer(inv -> {
            SensorReading r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        IngestResult result = service.ingest(IngestEnvelope.builder()
                .transport(IngestTransport.HTTP)
                .plantId(7L)
                .chipId("abcd")
                .messageId("legacy-1")
                .readings(List.of(IngestPoint.builder()
                        .nodeId(101L)
                        .sensorType(SensorType.TEMPERATURE)
                        .value(30.0)
                        .build()))
                .build());

        assertEquals(1, result.getReadings().size());
    }

    @Test
    void legacyDisabledRequiresCredentials() {
        ReflectionTestUtils.setField(service, "compatLegacy", false);
        device.setLegacyCompat(false);

        assertThrows(UnauthorizedException.class, () -> service.ingest(IngestEnvelope.builder()
                .transport(IngestTransport.HTTP)
                .plantId(7L)
                .chipId("abcd")
                .readings(List.of(IngestPoint.builder().nodeId(101L).value(1.0).build()))
                .build()));
    }

    @Test
    void calibrationSlopeApplied() {
        channel.setSlope(2.0);
        channel.setIntercept(1.0);
        SecurityContextHolder.getContext().setAuthentication(principal());
        when(deviceRepository.findById(11L)).thenReturn(Optional.of(device));
        when(sensorMessageRepository.existsByMessageId(anyString())).thenReturn(false);
        when(deviceChannelRepository.findByDeviceIdAndChannelId(11L, "1")).thenReturn(Optional.of(channel));
        when(sensorNodeRepository.findById(101L)).thenReturn(Optional.of(node));
        ArgumentCaptor<SensorReading> captor = ArgumentCaptor.forClass(SensorReading.class);
        when(sensorReadingRepository.save(captor.capture())).thenAnswer(inv -> {
            SensorReading r = inv.getArgument(0);
            r.setId(5L);
            return r;
        });

        service.ingest(IngestEnvelope.builder()
                .transport(IngestTransport.HTTP)
                .plantId(7L)
                .messageId("cal-1")
                .readings(List.of(IngestPoint.builder().channelId("1").value(10.0).build()))
                .build());

        assertEquals(21.0, captor.getValue().getValue());
        assertEquals(10.0, captor.getValue().getRawValue());
    }

    private DevicePrincipal principal() {
        return new DevicePrincipal(11L, "SWARM-HUB-ABCD", 7L, null, "abcd",
                DeviceKind.ESP_HUB, true, 1);
    }
}
