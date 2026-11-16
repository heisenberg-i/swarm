# PURPOSE: Operator/developer documentation for SWARM.
# SWARM 2.0 engineering report

**Date:** 2026-11-07  
**Checkout:** local Cursor workspace only — **no Git operations, no deploy, no live equipment**.

## Architecture changes

SWARM remains a Spring Boot modular monolith (`com.biopower`) + React SPA. This rebuild introduced a vendor-neutral field-device model and a single telemetry pipeline:

| Layer | Change |
|-------|--------|
| Identity (devices) | `devices` with UID, hashed secret, lifecycle, legacy flag |
| Channels | `device_channels` — logical IDs, calibration, units, node link |
| Ingest | `TelemetryIngestService` shared by HTTP + MQTT |
| Storage | Additive columns on `sensor_readings` + ingest audit tables |
| MQTT | Replaceable broker client; v1 topics; connection health |
| UI | Field Devices hubs/channels; honest No Data / Unknown |

## Modules rebuilt

- Device registry & pairing side-effects
- Channel mapping API
- Telemetry validation (auth, range, idempotency, quality)
- MQTT handler + config
- Field Devices frontend honesty + hubs tab
- Health endpoint MQTT/ingest flags

## Modules preserved

- Control Center tenant onboarding
- Plant app routes and workflows
- Partner `/partner/v1` field contracts
- ESP LAN pair/proxy
- Simulated HMI (banner retained)
- **NOVA (absolute — untouched)**

## Database migrations added

1. `V1__baseline.sql` (Slice 0)
2. `V2__sensor_node_device_and_user_nodes.sql` (Slice 0)
3. `V3__devices_channels_telemetry.sql` (this phase)

## Security improvements

- Device credential headers for ingest
- Legacy-compat explicit and per-device
- Cross-plant / forged identity rejection
- Rate limits (login + ingest)
- Slice 0 ACL guardrails retained
- No production secret rotation performed

## MQTT implementation status

| Item | Status |
|------|--------|
| Spring Integration MQTT client | Implemented (`mqtt.enabled`) |
| Topic `swarm/v1/.../telemetry` | Implemented |
| Payload schema 1.0 | Implemented + tested |
| Simulated device tests (no broker) | Pass |
| Live broker in this session | Not required / not connected |
| ESP MQTT firmware | Not shipped (HTTP compat kept) |

## Tests executed and results

```
cd backend && mvn test
Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Known limitations

- Compat legacy ingest still on by default
- Alert thresholds still largely global
- MFA / refresh tokens not yet built
- Report file generation still stubbed
- Equipment/process-area UI not fully built (schema ready)
- Single-node in-memory rate limiter

## Remaining blockers

See `progress.md`. Production deploy, JWT rotation, live PLC, and destructive migrations remain **hard stops**.

## Exact steps before production deployment

1. Review `PROTECTED_MODULES.md` and this report with operators.
2. Backup MySQL; enable Flyway; apply V2/V3 on a staging clone first.
3. Confirm `ddl-auto=validate` against staging.
4. Provision Partner allow-lists.
5. Roll firmware that sends device secrets; disable legacy per device/estate.
6. If MQTT: stand up Mosquitto/EMQX with TLS + per-device ACLs; set `MQTT_ENABLED=true`.
7. Rotate CC/admin passwords under change control (not automated here).
8. Deploy only after staged soak — not from this session.
