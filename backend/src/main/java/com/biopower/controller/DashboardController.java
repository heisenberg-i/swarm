// PURPOSE: HTTP API controller for SWARM dashboard endpoints.
package com.biopower.controller;

import com.biopower.dto.response.ApiResponse;
import com.biopower.dto.response.DashboardResponse;
import com.biopower.model.enums.AlertStatus;
import com.biopower.model.enums.SensorType;
import com.biopower.dto.response.AlertResponse;
import com.biopower.dto.response.AiRecommendationResponse;
import com.biopower.dto.response.SensorReadingResponse;
import com.biopower.security.UserPrincipal;
import com.biopower.service.AiHealthService;
import com.biopower.service.AlertService;
import com.biopower.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import com.biopower.util.DateTimes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final AiHealthService aiHealthService;
    private final AlertService alertService;
    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard/{plantId}")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @PathVariable Long plantId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(aiHealthService.getDashboard(plantId, principal)));
    }

    @GetMapping("/analytics/{plantId}")
    public ResponseEntity<ApiResponse<List<SensorReadingResponse>>> getAnalytics(
            @PathVariable Long plantId,
            @RequestParam(required = false) SensorType sensorType,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) Long nodeId,
            @RequestParam(required = false) String deviceUid,
            @AuthenticationPrincipal UserPrincipal principal) {
        LocalDateTime startAt = DateTimes.parseUtc(start, "start");
        LocalDateTime endAt = DateTimes.parseUtc(end, "end");
        if (endAt.isBefore(startAt)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("end must be after start"));
        }
        List<SensorReadingResponse> data = sensorType != null
                ? analyticsService.getHistoricalData(plantId, sensorType, startAt, endAt, nodeId, deviceUid, principal)
                : analyticsService.getAllSensorData(plantId, startAt, endAt, nodeId, deviceUid, principal);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlerts(
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) AlertStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                alertService.getAlertsForUser(principal, plantId, status)));
    }

    @PatchMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AlertResponse>> acknowledgeAlert(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Alert acknowledged", alertService.acknowledgeAlert(id, principal.getId(), principal)));
    }

    @PatchMapping("/alerts/{id}/resolve")
    public ResponseEntity<ApiResponse<AlertResponse>> resolveAlert(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Alert resolved", alertService.resolveAlert(id, principal)));
    }

    @GetMapping("/ai/recommendations/{plantId}")
    public ResponseEntity<ApiResponse<List<AiRecommendationResponse>>> getRecommendations(
            @PathVariable Long plantId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(aiHealthService.getRecommendations(plantId, principal)));
    }

    @PatchMapping("/ai/recommendations/{id}/acknowledge")
    public ResponseEntity<ApiResponse<Void>> acknowledgeRecommendation(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        aiHealthService.acknowledgeRecommendation(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Recommendation acknowledged", null));
    }
}
