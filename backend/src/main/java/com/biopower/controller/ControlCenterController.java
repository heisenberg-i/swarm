package com.biopower.controller;

import com.biopower.dto.request.ApproveUserRequest;
import com.biopower.dto.request.AssignPlantsRequest;
import com.biopower.dto.request.CompanyRequest;
import com.biopower.dto.request.PlantRequest;
import com.biopower.dto.request.SuperAdminRequest;
import com.biopower.dto.response.ApiResponse;
import com.biopower.dto.response.CompanyDetailResponse;
import com.biopower.dto.response.CompanyResponse;
import com.biopower.dto.response.ControlCenterOverviewResponse;
import com.biopower.dto.response.ControlCenterUserResponse;
import com.biopower.dto.response.PlantResponse;
import com.biopower.dto.response.UserResponse;
import com.biopower.model.enums.CompanyStatus;
import com.biopower.service.ControlCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/control-center")
@RequiredArgsConstructor
public class ControlCenterController {

    private final ControlCenterService controlCenterService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<ControlCenterOverviewResponse>> overview() {
        return ResponseEntity.ok(ApiResponse.success(controlCenterService.overview()));
    }

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> listCompanies() {
        return ResponseEntity.ok(ApiResponse.success(controlCenterService.listCompanies()));
    }

    @PostMapping("/companies")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company created", controlCenterService.createCompany(request)));
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<ApiResponse<CompanyDetailResponse>> getCompany(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(controlCenterService.getCompany(id)));
    }

    @PatchMapping("/companies/{id}/disable")
    public ResponseEntity<ApiResponse<CompanyResponse>> disableCompany(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Company disabled", controlCenterService.setCompanyStatus(id, CompanyStatus.DISABLED)));
    }

    @PatchMapping("/companies/{id}/enable")
    public ResponseEntity<ApiResponse<CompanyResponse>> enableCompany(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Company enabled", controlCenterService.setCompanyStatus(id, CompanyStatus.ACTIVE)));
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        controlCenterService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Company deleted", null));
    }

    @PostMapping("/companies/{id}/super-admin")
    public ResponseEntity<ApiResponse<UserResponse>> createSuperAdmin(
            @PathVariable Long id, @Valid @RequestBody SuperAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Super Admin created", controlCenterService.createSuperAdmin(id, request)));
    }

    @PutMapping("/companies/{id}/plants")
    public ResponseEntity<ApiResponse<CompanyDetailResponse>> assignPlants(
            @PathVariable Long id, @Valid @RequestBody AssignPlantsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Plants assigned", controlCenterService.assignPlants(id, request)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<ControlCenterUserResponse>>> listUsers(
            @RequestParam(name = "queue", defaultValue = "pending") String queue) {
        boolean queueOnly = !"all".equalsIgnoreCase(queue);
        return ResponseEntity.ok(ApiResponse.success(controlCenterService.listUsers(queueOnly)));
    }

    @PostMapping("/users/{id}/approve")
    public ResponseEntity<ApiResponse<ControlCenterUserResponse>> approveUser(
            @PathVariable Long id, @Valid @RequestBody ApproveUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User approved", controlCenterService.approveUser(id, request)));
    }

    @PostMapping("/users/{id}/reject")
    public ResponseEntity<ApiResponse<ControlCenterUserResponse>> rejectUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User rejected", controlCenterService.rejectUser(id)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        controlCenterService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }

    @GetMapping("/plants")
    public ResponseEntity<ApiResponse<List<PlantResponse>>> listPlants() {
        return ResponseEntity.ok(ApiResponse.success(controlCenterService.listAllPlants()));
    }

    @GetMapping("/plants/unassigned")
    public ResponseEntity<ApiResponse<List<PlantResponse>>> listUnassignedPlants() {
        return ResponseEntity.ok(ApiResponse.success(controlCenterService.listUnassignedPlants()));
    }

    @PostMapping("/plants")
    public ResponseEntity<ApiResponse<PlantResponse>> createPlant(@Valid @RequestBody PlantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Plant created", controlCenterService.createUnassignedPlant(request)));
    }
}
