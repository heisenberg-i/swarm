// PURPOSE: Historical telemetry APIs must enforce plant access before returning rows.
package com.biopower.service;

import com.biopower.dto.response.SensorReadingResponse;
import com.biopower.model.entity.SensorReading;
import com.biopower.model.enums.SensorType;
import com.biopower.repository.SensorReadingRepository;
import com.biopower.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceIsolationTest {

    @Mock
    private SensorReadingRepository sensorReadingRepository;
    @Mock
    private PlantAccessService plantAccessService;
    @Mock
    private UserPrincipal principal;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void deniesOtherPlant() {
        doThrow(new AccessDeniedException("You do not have access to this plant"))
                .when(plantAccessService).assertCanAccessPlant(principal, 99L);

        assertThrows(AccessDeniedException.class, () -> analyticsService.getHistoricalData(
                99L, SensorType.METHANE,
                LocalDateTime.of(2026, 11, 16, 0, 0),
                LocalDateTime.of(2026, 11, 16, 1, 0),
                null, null, principal));
        verifyNoInteractions(sensorReadingRepository);
    }

    @Test
    void filtersByNodeAndDevice() {
        SensorReading reading = new SensorReading();
        reading.setId(8L);
        reading.setNodeId(3L);
        reading.setDeviceUid("SWARM-HUB-ABCD");
        reading.setSensorType(SensorType.METHANE);
        reading.setValue(12.5);
        when(sensorReadingRepository.findByPlantAndTypeAndDateRange(
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.eq(SensorType.METHANE),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(reading));

        List<SensorReadingResponse> rows = analyticsService.getHistoricalData(
                7L, SensorType.METHANE,
                LocalDateTime.of(2026, 11, 16, 0, 0),
                LocalDateTime.of(2026, 11, 16, 1, 0),
                3L, "SWARM-HUB-ABCD", principal);

        assertEquals(1, rows.size());
        assertEquals(12.5, rows.get(0).getValue());
        assertEquals("SWARM-HUB-ABCD", rows.get(0).getDeviceUid());
    }
}
