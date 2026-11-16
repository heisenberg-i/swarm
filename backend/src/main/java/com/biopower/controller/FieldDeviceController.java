// PURPOSE: HTTP API controller for SWARM fielddevice endpoints.
package com.biopower.controller;

import com.biopower.dto.request.DeviceChannelRequest;
import com.biopower.dto.response.ApiResponse;
import com.biopower.dto.response.FieldDeviceChannelResponse;
import com.biopower.dto.response.FieldDeviceResponse;
import com.biopower.model.entity.DeviceChannel;
import com.biopower.security.UserPrincipal;
import com.biopower.service.DeviceRegistryService;
import com.biopower.service.FieldDeviceQueryService;
import com.biopower.service.PlantAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/field-devices")
@RequiredArgsConstructor
public class FieldDeviceController {

    private final FieldDeviceQueryService fieldDeviceQueryService;
    private final DeviceRegistryService deviceRegistryService;
    private final PlantAccessService plantAccessService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FieldDeviceResponse>>> list(
            @RequestParam(required = false) Long plantId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(fieldDeviceQueryService.listDevices(plantId, principal)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FieldDeviceResponse>> get(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(fieldDeviceQueryService.getDevice(id, principal)));
    }

    @PutMapping("/{id}/channels")
    public ResponseEntity<ApiResponse<FieldDeviceChannelResponse>> upsertChannel(
            @PathVariable Long id,
            @Valid @RequestBody DeviceChannelRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        FieldDeviceResponse device = fieldDeviceQueryService.getDevice(id, principal);
        if (!plantAccessService.isSuperAdmin(principal) && !plantAccessService.canManageUsers(principal)) {
            plantAccessService.assertCanAccessPlant(principal, device.getPlantId());
        }
        DeviceChannel channel = deviceRegistryService.upsertChannel(id, new DeviceRegistryService.DeviceChannelUpdate(
                request.getChannelId(),
                request.getMeasurementType(),
                request.getEngineeringUnit(),
                request.getInstrumentTag(),
                request.getSensorNodeId(),
                request.getEquipmentId(),
                request.getProcessAreaId(),
                request.getSlope(),
                request.getIntercept(),
                request.getValidityMin(),
                request.getValidityMax(),
                request.getHardMin(),
                request.getHardMax(),
                request.getSamplingIntervalSec(),
                request.getActive()
        ));
        return ResponseEntity.ok(ApiResponse.success("Channel saved", FieldDeviceChannelResponse.builder()
                .id(channel.getId())
                .channelId(channel.getChannelId())
                .sensorNodeId(channel.getSensorNodeId())
                .measurementType(channel.getMeasurementType())
                .engineeringUnit(channel.getEngineeringUnit())
                .instrumentTag(channel.getInstrumentTag())
                .equipmentId(channel.getEquipmentId())
                .processAreaId(channel.getProcessAreaId())
                .slope(channel.getSlope())
                .intercept(channel.getIntercept())
                .validityMin(channel.getValidityMin())
                .validityMax(channel.getValidityMax())
                .hardMin(channel.getHardMin())
                .hardMax(channel.getHardMax())
                .samplingIntervalSec(channel.getSamplingIntervalSec())
                .active(channel.isActive())
                .dataStatus("Unknown")
                .build()));
    }

    @PostMapping("/{id}/rotate-secret")
    public ResponseEntity<ApiResponse<Map<String, String>>> rotateSecret(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        FieldDeviceResponse device = fieldDeviceQueryService.getDevice(id, principal);
        if (!plantAccessService.isSuperAdmin(principal)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only Super Admin can rotate device secrets");
        }
        String secret = deviceRegistryService.rotateSecret(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Device secret rotated. Store it now; it will not be shown again.",
                Map.of("deviceUid", device.getDeviceUid(), "deviceSecret", secret)));
    }
}
