// PURPOSE: Spring Data access for SWARM Device persistence.
package com.biopower.repository;

import com.biopower.model.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findFirstByDeviceUid(String deviceUid);

    List<Device> findByDeviceUid(String deviceUid);

    Optional<Device> findByPlantIdAndDeviceUid(Long plantId, String deviceUid);

    Optional<Device> findByTenantIdAndDeviceUid(UUID tenantId, String deviceUid);

    Optional<Device> findByPlantIdAndChipId(Long plantId, String chipId);

    List<Device> findByPlantId(Long plantId);

    List<Device> findByTenantId(UUID tenantId);

    List<Device> findByChipId(String chipId);

    boolean existsByDeviceUid(String deviceUid);

    boolean existsByTenantId(UUID tenantId);
}
