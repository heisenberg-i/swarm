// PURPOSE: Spring Data access for SWARM Equipment persistence.
package com.biopower.repository;

import com.biopower.model.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByPlantIdOrderByTagAsc(Long plantId);

    Optional<Equipment> findByPlantIdAndTag(Long plantId, String tag);
}
