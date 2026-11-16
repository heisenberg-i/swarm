// PURPOSE: Spring Boot configuration/startup component for SWARM.
package com.biopower.config;

import com.biopower.service.DeviceRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
@ConditionalOnProperty(name = "biopower.deployment.role", havingValue = "primary", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DeviceBackfillRunner implements CommandLineRunner {

    private final DeviceRegistryService deviceRegistryService;

    @Override
    public void run(String... args) {
        try {
            int created = deviceRegistryService.backfillFromSensorNodes();
            if (created == 0) {
                log.debug("Device backfill: no new hubs to register");
            }
        } catch (Exception ex) {
            log.warn("Device backfill skipped: {}", ex.getMessage());
        }
    }
}
