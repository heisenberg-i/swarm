// PURPOSE: Application service implementing SWARM iotdata business rules.
package com.biopower.service;

import com.biopower.dto.request.IoTBatchRequest;
import com.biopower.dto.request.IoTDataRequest;
import com.biopower.dto.response.SensorReadingResponse;
import com.biopower.model.enums.IngestTransport;
import com.biopower.telemetry.IngestEnvelope;
import com.biopower.telemetry.IngestPoint;
import com.biopower.telemetry.IngestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP / legacy adapter into {@link TelemetryIngestService}.
 * Keeps firmware batch JSON compatible.
 */
@Service
@RequiredArgsConstructor
public class IoTDataService {

    private final TelemetryIngestService telemetryIngestService;

    @Transactional
    public SensorReadingResponse ingestData(IoTDataRequest request) {
        IngestEnvelope envelope = IngestEnvelope.builder()
                .transport(IngestTransport.HTTP)
                .schemaVersion("legacy-1")
                .plantId(request.getPlantId())
                .sourceTimestamp(request.getTimestamp())
                .readings(List.of(IngestPoint.builder()
                        .nodeId(request.getNodeId())
                        .sensorType(request.getSensorType())
                        .value(request.getValue())
                        .build()))
                .payloadExcerpt("single nodeId=" + request.getNodeId())
                .build();
        IngestResult result = telemetryIngestService.ingest(envelope);
        if (result.isDuplicate()) {
            return result.getReadings().isEmpty() ? null : result.getReadings().get(0);
        }
        return result.getReadings().isEmpty() ? null : result.getReadings().get(0);
    }

    @Transactional
    public List<SensorReadingResponse> ingestBatch(IoTBatchRequest request) {
        List<IngestPoint> points = request.getReadings().stream()
                .map(item -> IngestPoint.builder()
                        .nodeId(item.getNodeId())
                        .sensorType(item.getSensorType())
                        .value(item.getValue())
                        .build())
                .collect(Collectors.toList());

        IngestEnvelope envelope = IngestEnvelope.builder()
                .transport(IngestTransport.HTTP)
                .schemaVersion("legacy-batch-1")
                .plantId(request.getPlantId())
                .deviceUid(request.getDeviceId())
                .chipId(request.getChipId())
                .rssi(request.getRssi())
                .ip(request.getIp())
                .messageId(request.getMessageId())
                .sourceTimestamp(request.getTimestamp())
                .readings(points)
                .payloadExcerpt("batch plantId=" + request.getPlantId() + " chipId=" + request.getChipId())
                .build();

        IngestResult result = telemetryIngestService.ingest(envelope);
        return result.getReadings();
    }
}
