# PURPOSE: Operator/developer documentation for SWARM.
# SWARM 2.0 implementation progress

## Slice 0 — Guardrails

**Date:** 2026-11-07  
**Status:** Implemented locally. Not deployed. No production secrets rotated.

See prior notes: Flyway prod validate, auth ACL, Partner deny-empty, no fake 85/GOOD, HMI SIMULATION banner, bootstrap env only.

## Rebuild phase — Devices, ingest, MQTT foundations

**Date:** 2026-11-07  
**Status:** Implemented locally. Not deployed. NOVA untouched. No Git operations.

### Architecture changes

- First-class `devices` + `device_channels` + process/equipment tables (Flyway `V3`).
- Shared `TelemetryIngestService` used by HTTP (`IoTDataService`) and MQTT (`MqttTelemetryHandler`).
- Device auth filter (`X-SWARM-Device-Id` / `X-SWARM-Device-Secret`) + legacy-compat gate (`SWARM_IOT_COMPAT_LEGACY`).
- Idempotency via `ingest_messages`; rejects via `ingest_rejected`.
- Reading metadata: quality, received_at, device_id, channel_id, message_id, raw_value, unit, config_version.
- MQTT v1 topic `swarm/v1/{tenant}/{plant}/{deviceUid}/telemetry` + legacy wildcard; health exposes MQTT stats.
- Rate limits on login + ingest.
- Field Devices UI: Hubs & channels tab; no invented firmware/battery/signal.

### Modules rebuilt

Device registry, channel mapping, telemetry ingest, MQTT adapter, Field Devices honesty, health observability.

### Modules preserved

Control Center, Partner API contracts, ESP pair/proxy JSON, HMI simulation, alerts/AI hooks, **entire NOVA subsystem**.

### Database migrations added

- `V3__devices_channels_telemetry.sql` (additive)

### Security improvements

- Optional device credentials on ingest; forged deviceUid / cross-plant rejected.
- Empty Partner allow-list still deny (Slice 0).
- Secrets hashed (SHA-256); one-time plaintext only at pair/rotate.
- Rate limiting.

### MQTT implementation status

- Client integration ready behind `MQTT_ENABLED`.
- Simulated handler tests without broker: **pass**.
- Broker not required for CI; Mosquitto/EMQX via URL when enabled.
- Firmware still HTTP-batch (compat). MQTT publish from ESP not shipped.

### Tests

```
cd backend && mvn test
# Tests run: 32, Failures: 0, Errors: 0
```

Includes: TelemetryIngestServiceTest, MqttTelemetryHandlerTest, DeviceAuthIT, DeviceSecretHasherTest, Slice0 + existing suite.

### NOVA protection

Verified: no modifications under `com.biopower.novaspace` sources, frontend nova components, or `NOVA_DNA_source_for_SWARM`.

### Known limitations

- Dev profile still Hibernate `update` + Flyway off (entities additive-safe).
- Compat legacy default **true** until firmware sends credentials.
- MFA / refresh sessions still Slice 2.
- Alert policies still global YAML (Slice 3).
- Reports download still stub (Slice 3).
- Broker ACL automation not implemented (document broker-side config).
- Process area / equipment UI minimal (API/tables only).

### Remaining blockers before production

1. Operator decision: rotate existing CC passwords (not done — hard stop).
2. First prod Flyway boot: baseline + apply V2/V3; confirm no Hibernate drift under `validate`.
3. Fill Partner plant allow-lists before relying on deny-empty.
4. Firmware update to send device headers; then `SWARM_IOT_COMPAT_LEGACY=false` per estate.
5. Provision Mosquitto/EMQX + topic ACLs if MQTT will be used.
6. No deploy from this session.

### Docs added

- `PROTECTED_MODULES.md`
- `REBUILD_INVENTORY.md`
- `28-mqtt-and-telemetry.md`
- `ENGINEERING_REPORT.md`
