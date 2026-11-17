// PURPOSE: Health must distinguish configured adapter from a live broker connection.
package com.biopower.mqtt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MqttConnectionHealthTest {

    @Test
    void configuredIsNotConnected() {
        MqttConnectionHealth health = new MqttConnectionHealth();
        health.markConfigured("tcp://mosquitto:1883", new String[]{"swarm/v1/+/+/+/telemetry"});
        assertTrue(health.isConfigured());
        assertFalse(health.isConnected());
        assertEquals("tcp://mosquitto:1883", health.getBrokerUrl());
    }

    @Test
    void subscribeMarksConnectedAndFailureClearsIt() {
        MqttConnectionHealth health = new MqttConnectionHealth();
        health.markConfigured("tcp://mosquitto:1883", new String[]{"biopower/sensors/#"});
        health.markConnected();
        assertTrue(health.isConnected());
        health.markDisconnected("Connection lost");
        assertFalse(health.isConnected());
        assertEquals("Connection lost", health.getLastError());
    }

    @Test
    void receivedAndFailedCountersAreIndependent() {
        MqttConnectionHealth health = new MqttConnectionHealth();
        health.markMessageReceived();
        health.markFailure("Unknown MQTT device");
        assertEquals(1, health.getMessagesReceived());
        assertEquals(1, health.getFailures());
        assertEquals("Unknown MQTT device", health.getLastError());
    }
}
