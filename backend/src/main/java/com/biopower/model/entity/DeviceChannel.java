// PURPOSE: JPA entity mapping for SWARM table/fields used by DeviceChannel.
package com.biopower.model.entity;

import com.biopower.model.enums.SensorType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Logical engineer-assigned channel on a physical device.
 * channelId is NOT a GPIO pin number and has no universal measurement meaning.
 */
@Entity
@Table(name = "device_channels", uniqueConstraints = {
        @UniqueConstraint(name = "uq_device_channel", columnNames = {"device_id", "channel_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    /** Engineer-assigned logical channel identifier for this device only. */
    @Column(name = "channel_id", nullable = false)
    private String channelId;

    /** Compatibility link to legacy SensorNode inventory row. */
    @Column(name = "sensor_node_id")
    private Long sensorNodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_type", nullable = false, length = 50)
    private SensorType measurementType;

    @Column(name = "engineering_unit", length = 32)
    private String engineeringUnit;

    @Column(name = "instrument_tag", length = 64)
    private String instrumentTag;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "process_area_id")
    private Long processAreaId;

    @Builder.Default
    @Column(nullable = false)
    private double slope = 1.0;

    @Builder.Default
    @Column(nullable = false)
    private double intercept = 0.0;

    @Column(name = "validity_min")
    private Double validityMin;

    @Column(name = "validity_max")
    private Double validityMax;

    @Column(name = "hard_min")
    private Double hardMin;

    @Column(name = "hard_max")
    private Double hardMax;

    @Column(name = "sampling_interval_sec")
    private Integer samplingIntervalSec;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
