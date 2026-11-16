// PURPOSE: Liveness payload including PostgreSQL connectivity.
package com.biopower.controller;

import com.biopower.config.DeploymentRoleProperties;
import com.biopower.config.IdentityProperties;
import com.biopower.mqtt.MqttConnectionHealth;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final IdentityProperties identityProperties;
    private final DeploymentRoleProperties deploymentRoleProperties;
    private final MqttConnectionHealth mqttConnectionHealth;
    private final JdbcTemplate jdbcTemplate;

    @Value("${mqtt.enabled:false}")
    private boolean mqttEnabled;

    @Value("${biopower.iot.compat-legacy:true}")
    private boolean iotCompatLegacy;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        boolean dbUp = databaseUp();
        body.put("status", dbUp ? "UP" : "DEGRADED");
        body.put("database", dbUp ? "UP" : "DOWN");
        body.put("identityMode", identityProperties.getMode());
        body.put("deploymentRole", deploymentRoleProperties.getRole());
        body.put("iotCompatLegacy", iotCompatLegacy);

        Map<String, Object> mqtt = new LinkedHashMap<>();
        mqtt.put("enabled", mqttEnabled);
        mqtt.put("configured", mqttConnectionHealth.isConfigured());
        mqtt.put("brokerUrl", mqttEnabled ? mqttConnectionHealth.getBrokerUrl() : null);
        mqtt.put("topics", mqttEnabled ? mqttConnectionHealth.getTopics() : new String[0]);
        mqtt.put("messagesReceived", mqttConnectionHealth.getMessagesReceived());
        mqtt.put("failures", mqttConnectionHealth.getFailures());
        mqtt.put("lastMessageAt", mqttConnectionHealth.getLastMessageAt());
        mqtt.put("lastError", mqttConnectionHealth.getLastError());
        body.put("mqtt", mqtt);
        return ResponseEntity.ok(body);
    }

    private boolean databaseUp() {
        try {
            Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return one != null && one == 1;
        } catch (Exception ex) {
            return false;
        }
    }
}
