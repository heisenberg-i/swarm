package com.biopower.dto.partner;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PartnerDeviceResponse {
    @JsonProperty("deviceId")
    private String deviceId;
    private String plantId;
    private String plantName;
    private String name;
    private String metricType;
    private String unit;
    private String readingType;
    private String status;
    private String firmwareVersion;
    private Instant lastReadingAt;

    @JsonGetter("device_id")
    public String jsonDeviceIdSnake() {
        return deviceId;
    }

    @JsonGetter("device_name")
    public String jsonDeviceNameSnake() {
        return name;
    }

    @JsonGetter("plant_name")
    public String jsonPlantNameSnake() {
        return plantName;
    }
}
