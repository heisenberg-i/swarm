// PURPOSE: JPA entity mapping for SWARM table/fields used by SensorReadingId.
package com.biopower.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorReadingId implements Serializable {
    private Long id;
    private Instant recordedAt;
}
