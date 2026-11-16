// PURPOSE: Automated test covering SWARM PartnerTelemetryAllowListTest.
package com.biopower.service;

import com.biopower.repository.AiRecommendationRepository;
import com.biopower.repository.AlertRepository;
import com.biopower.repository.PlantRepository;
import com.biopower.repository.SensorNodeRepository;
import com.biopower.repository.SensorReadingRepository;
import com.biopower.security.PartnerPrincipal;
import com.biopower.util.SensorHeartbeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PartnerTelemetryAllowListTest {

    @Mock private PlantRepository plantRepository;
    @Mock private SensorNodeRepository sensorNodeRepository;
    @Mock private SensorReadingRepository sensorReadingRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private AiRecommendationRepository aiRecommendationRepository;
    @Mock private SensorHeartbeat sensorHeartbeat;

    private PartnerTelemetryService service;

    @BeforeEach
    void setUp() {
        service = new PartnerTelemetryService(
                plantRepository,
                sensorNodeRepository,
                sensorReadingRepository,
                alertRepository,
                aiRecommendationRepository,
                sensorHeartbeat);
    }

    @Test
    void emptyAllowListDoesNotFallBackToAllPlants() {
        PartnerPrincipal principal = new PartnerPrincipal(1L, "001", "Org", 9L, Set.of());
        assertTrue(service.listPlants(principal).isEmpty());
        verify(plantRepository, never()).findAll();
        verify(plantRepository, never()).findByPlantIdIn(any());
    }
}
