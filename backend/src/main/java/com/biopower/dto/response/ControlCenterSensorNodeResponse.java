package com.biopower.dto.response;

import com.biopower.model.enums.NodeStatus;
import com.biopower.model.enums.SensorType;
import com.biopower.model.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ControlCenterSensorNodeResponse {
    private Long nodeId;
    private String nodeName;
    private SensorType sensorType;
    private NodeStatus status;
    private Long plantId;
    private String plantName;
    private boolean paired;
    private boolean hasReading;
    private LocalDateTime lastReadingAt;
    private List<Grant> access;

    @Data
    @Builder
    public static class Grant {
        private Long userId;
        private String name;
        private String email;
        private UserRole role;
        /** NODE = explicit node grant; PLANT = all sensors on the assigned plant */
        private String scope;
    }
}
