// PURPOSE: Spring Data access for SWARM IngestRejected persistence.
package com.biopower.repository;

import com.biopower.model.entity.IngestRejected;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngestRejectedRepository extends JpaRepository<IngestRejected, Long> {
    List<IngestRejected> findTop20ByOrderByReceivedAtDesc();
}
