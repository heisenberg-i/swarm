// PURPOSE: JPA entity mapping for SWARM table/fields used by Device.
package com.biopower.model.entity;

import com.biopower.model.enums.DeviceKind;
import com.biopower.model.enums.DeviceLifecycle;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
@SecondaryTable(name = "device_registry", pkJoinColumns = @PrimaryKeyJoinColumn(name = "device_pk"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "device_id", nullable = false)
    private String deviceUid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(name = "removed_at")
    private Instant removedAt;

    @Column(name = "plant_id", table = "device_registry")
    private Long plantId;

    @Column(name = "chip_id", table = "device_registry", length = 32)
    private String chipId;

    @Column(name = "device_name", table = "device_registry", nullable = false, length = 128)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_kind", table = "device_registry", nullable = false, length = 32)
    private DeviceKind deviceKind;

    @Enumerated(EnumType.STRING)
    @Column(table = "device_registry", nullable = false, length = 32)
    private DeviceLifecycle lifecycle;

    @Column(name = "secret_hash", table = "device_registry", length = 128)
    private String secretHash;

    @Builder.Default
    @Column(name = "legacy_compat", table = "device_registry", nullable = false)
    private boolean legacyCompat = true;

    @Column(name = "last_seen_at", table = "device_registry")
    private Instant lastSeenAt;

    @Column(name = "last_ip", table = "device_registry", length = 45)
    private String lastIp;

    @Column(name = "last_rssi", table = "device_registry")
    private Integer lastRssi;

    @Column(name = "firmware_version", table = "device_registry", length = 64)
    private String firmwareVersion;

    @Builder.Default
    @Column(name = "config_version", table = "device_registry", nullable = false)
    private int configVersion = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", table = "device_registry")
    private Instant updatedAt;

    public UUID getCompanyId() {
        return tenantId;
    }
}
