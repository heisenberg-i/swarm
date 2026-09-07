package com.biopower.util;

import com.biopower.model.entity.SensorNode;
import com.biopower.model.enums.NodeStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Online/offline from last reading, not from a sticky ACTIVE flag.
 * ESP hubs post about every 10s; default timeout is 10 minutes.
 */
@Component
public class SensorHeartbeat {

    @Value("${biopower.alert.sensor-timeout-minutes:10}")
    private int sensorTimeoutMinutes;

    public int timeoutMinutes() {
        return sensorTimeoutMinutes;
    }

    public boolean isFresh(SensorNode node) {
        if (node == null) {
            return false;
        }
        LocalDateTime last = node.getLastReadingAt();
        LocalDateTime now = LocalDateTime.now();
        if (last != null) {
            return last.isAfter(now.minusMinutes(sensorTimeoutMinutes));
        }
        LocalDateTime created = node.getCreatedAt();
        if (created == null) {
            return false;
        }
        return created.isAfter(now.minusMinutes(sensorTimeoutMinutes * 3L));
    }

    public NodeStatus effectiveStatus(SensorNode node) {
        if (node == null) {
            return NodeStatus.OFFLINE;
        }
        NodeStatus stored = node.getStatus();
        if (stored == NodeStatus.INACTIVE || stored == NodeStatus.FAULTY) {
            return stored;
        }
        return isFresh(node) ? NodeStatus.ACTIVE : NodeStatus.OFFLINE;
    }
}
