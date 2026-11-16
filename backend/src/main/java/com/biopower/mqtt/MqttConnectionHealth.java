// PURPOSE: MQTT ingest/health adapter for SWARM device telemetry.
package com.biopower.mqtt;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MqttConnectionHealth {

    private final AtomicReference<String> brokerUrl = new AtomicReference<>("");
    private final AtomicReference<String[]> topics = new AtomicReference<>(new String[0]);
    private final AtomicReference<Instant> lastMessageAt = new AtomicReference<>();
    private final AtomicReference<String> lastError = new AtomicReference<>();
    private final AtomicLong messagesReceived = new AtomicLong();
    private final AtomicLong failures = new AtomicLong();
    private volatile boolean configured;

    public void markConfigured(String url, String[] topicList) {
        this.configured = true;
        this.brokerUrl.set(url);
        this.topics.set(topicList != null ? topicList : new String[0]);
    }

    public void markMessageReceived() {
        messagesReceived.incrementAndGet();
        lastMessageAt.set(Instant.now());
        lastError.set(null);
    }

    public void markFailure(String message) {
        failures.incrementAndGet();
        lastError.set(message);
    }

    public boolean isConfigured() {
        return configured;
    }

    public String getBrokerUrl() {
        return brokerUrl.get();
    }

    public String[] getTopics() {
        return topics.get();
    }

    public Instant getLastMessageAt() {
        return lastMessageAt.get();
    }

    public String getLastError() {
        return lastError.get();
    }

    public long getMessagesReceived() {
        return messagesReceived.get();
    }

    public long getFailures() {
        return failures.get();
    }
}
