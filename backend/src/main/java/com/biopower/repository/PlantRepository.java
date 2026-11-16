// PURPOSE: Spring Data access for SWARM Plant persistence.
package com.biopower.repository;

import com.biopower.model.entity.Company;
import com.biopower.model.entity.Plant;
import com.biopower.model.enums.PlantStatus;
import com.biopower.model.enums.PlantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    List<Plant> findByStatus(PlantStatus status);
    List<Plant> findByPlantType(PlantType plantType);
    List<Plant> findByPlantIdIn(List<Long> plantIds);
    List<Plant> findByCompany(Company company);
    List<Plant> findByCompanyIsNull();
}
