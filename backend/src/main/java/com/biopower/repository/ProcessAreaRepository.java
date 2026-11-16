// PURPOSE: Spring Data access for SWARM ProcessArea persistence.
package com.biopower.repository;

import com.biopower.model.entity.ProcessArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessAreaRepository extends JpaRepository<ProcessArea, Long> {
    List<ProcessArea> findByPlantIdOrderByCodeAsc(Long plantId);

    Optional<ProcessArea> findByPlantIdAndCode(Long plantId, String code);
}
