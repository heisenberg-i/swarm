# PURPOSE: Operator/developer documentation for SWARM.
# SWARM rebuild inventory (verified against code)

**Date:** 2026-11-07  
**Basis:** Source audit after Slice 0. Docs are targets; code is current truth.

## Preserve (working)

| Module | Why |
|--------|-----|
| Control Center provisioning | Company → Super Admin → plants works |
| Auth JWT + emPOWER SSO consumer | Production login path |
| Partner `/partner/v1` pull API | Contract stability |
| ESP hub pairing + LAN proxy | Field Devices Pair Sensor Hub |
| HTTP `/iot/batch` payload shape | Live firmware depends on it |
| Alerts on ingest + stale OFFLINE job | Operational |
| Simulated Plant HMI (iframe PFD) | Explicitly simulation |
| NOVA Space OP | **Protected — do not touch** |
| Capacitor Android SPA wrapper | Ship path |
| Slice 0 security guardrails | Auth ACL, Partner deny-empty, Flyway prod |

## Simulated / incomplete (keep, label clearly)

| Area | Reality |
|------|---------|
| Health score | Heuristic; null when no data (Slice 0) |
| MQ5 as METHANE | Raw ADC unless calibrated |
| Reports download | Stub file path |
| HMI commands | Demo state only |
| MQTT | Code exists, `mqtt.enabled=false` by default |

## Rebuild / harden (this assignment)

| Area | Weakness |
|------|----------|
| Device model | Hub = 3 `sensor_nodes`; no `devices` / channels |
| Ingest auth | `/iot/**` permitAll; chipId not verified |
| Telemetry metadata | No quality, messageId, receivedAt, calibration |
| Idempotency | Replays duplicate rows + re-alert |
| Rate limits | None |
| MQTT | Legacy single-point JSON; no topic ACL validation |
| Field Devices UI | Invents firmware/battery defaults |
| Channel IDs | Implicit nodeId, not engineer channel map |

## Database / migration risks

- Prod Flyway baseline-on-migrate; additive migrations only.
- Dev H2 uses Hibernate `update` (Flyway off) — entities must stay additive-safe.
- Do not DROP historical `sensor_readings`.
- Backfill `devices` from distinct `(plant_id, device_chip_id)`.

## MQTT requirements

- Replaceable broker (Mosquitto / EMQX via URL).
- Topic: `swarm/v1/{tenantId}/{plantId}/{deviceUid}/telemetry` (+ status/heartbeat).
- Shared ingest service with HTTP.
- Simulated device tests without live equipment.

## Modules requiring rebuild

Field device identity, channel mapping, shared ingest, MQTT adapter, Field Devices UI honesty, observability health for MQTT/ingest.

## Modules preserved as-is (aside from NOVA)

Partner contract field names, Control Center UX shell, firmware HTTP batch JSON (additive headers only).
