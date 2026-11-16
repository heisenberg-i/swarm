// PURPOSE: JPA entity mapping for SWARM table/fields used by Equipment.
package com.biopower.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment", uniqueConstraints = {
        @UniqueConstraint(name = "uq_equipment_plant_tag", columnNames = {"plant_id", "tag"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plant_id", nullable = false)
    private Long plantId;

    @Column(name = "process_area_id")
    private Long processAreaId;

    @Column(nullable = false, length = 64)
    private String tag;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "equipment_kind", length = 64)
    private String equipmentKind;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
