// PURPOSE: Spring Data access for SWARM SensorMessage persistence.
package com.biopower.repository;

import com.biopower.model.entity.SensorMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SensorMessageRepository extends JpaRepository<SensorMessage, String> {
    boolean existsByMessageId(String messageId);

    boolean existsByTenantId(UUID tenantId);
}
