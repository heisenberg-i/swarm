// PURPOSE: HTTP API controller for SWARM catalog endpoints.
package com.biopower.controller;

import com.biopower.dto.response.ApiResponse;
import com.biopower.dto.response.SensorTypeInfo;
import com.biopower.util.SensorCatalog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    @GetMapping("/sensor-types")
    public ResponseEntity<ApiResponse<List<SensorTypeInfo>>> sensorTypes() {
        return ResponseEntity.ok(ApiResponse.success(SensorCatalog.all()));
    }
}
