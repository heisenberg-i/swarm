// PURPOSE: Automated test covering SWARM DeviceAuthIT.
package com.biopower.security;

import com.biopower.model.entity.Device;
import com.biopower.model.enums.DeviceKind;
import com.biopower.model.enums.DeviceLifecycle;
import com.biopower.repository.CompanyRepository;
import com.biopower.repository.DeviceRepository;
import com.biopower.util.DeviceSecretHasher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class DeviceAuthIT {

    @Autowired MockMvc mockMvc;
    @Autowired DeviceRepository deviceRepository;
    @Autowired CompanyRepository companyRepository;

    @Test
    void invalidDeviceSecretReturns401() throws Exception {
        UUID tenantId = companyRepository.findAll().stream()
                .map(c -> c.getId())
                .findFirst()
                .orElseThrow();
        Device device = deviceRepository.findFirstByDeviceUid("IT-TEST-DEVICE").orElseGet(() ->
                deviceRepository.save(Device.builder()
                        .plantId(1L)
                        .tenantId(tenantId)
                        .deviceUid("IT-TEST-DEVICE")
                        .deviceName("IT")
                        .deviceKind(DeviceKind.ESP_HUB)
                        .lifecycle(DeviceLifecycle.ACTIVE)
                        .secretHash(DeviceSecretHasher.hash("correct-secret-value"))
                        .legacyCompat(false)
                        .configVersion(1)
                        .build()));

        mockMvc.perform(post("/iot/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(DeviceAuthenticationFilter.HEADER_DEVICE_ID, device.getDeviceUid())
                        .header(DeviceAuthenticationFilter.HEADER_DEVICE_SECRET, "wrong-secret")
                        .content("""
                                {"plantId":1,"chipId":"x","readings":[{"nodeId":1,"sensorType":"TEMPERATURE","value":1}]}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
