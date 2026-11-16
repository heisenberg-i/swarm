// PURPOSE: JPA entity mapping for SWARM table/fields used by IngestRejected.
package com.biopower.model.entity;

import com.biopower.model.enums.IngestTransport;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingest_rejected")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestRejected {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_uid", length = 64)
    private String deviceUid;

    @Column(name = "plant_id")
    private Long plantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private IngestTransport transport;

    @Column(name = "reason_code", nullable = false, length = 64)
    private String reasonCode;

    @Column(name = "reason_detail", length = 512)
    private String reasonDetail;

    @Column(name = "payload_excerpt", length = 1024)
    private String payloadExcerpt;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;
}
