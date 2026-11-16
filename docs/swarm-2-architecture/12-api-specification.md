# PURPOSE: Operator/developer documentation for SWARM.
# 12 — API specification

This is the **target** contract grounded in current paths. It is not an OpenAPI dump of today’s holes.

## Conventions

| Item | Plant SPA | Partner v1 |
|---|---|---|
| Base | `https://api.swarm.co.in/api` | `.../api/partner/v1` |
| Auth | `Authorization: Bearer <access>` | `Authorization: Bearer swk_…` |
| Envelope | `{ "success": true, "message": "", "data": {} }` | Flat JSON (keep — emPOWER already coded) |
| Time | ISO-8601 UTC | ISO-8601 UTC |
| Errors | `{ success:false, code, message }` | `{ success:false, message }` until v2 |

Codes: `UNAUTHENTICATED`, `FORBIDDEN`, `NOT_FOUND`, `VALIDATION`, `RATE_LIMITED`, `CONFLICT`, `SIMULATION_ONLY`, `NOT_COMMISSIONED`.

## Identity

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/auth/login` | public RL | Local mode only |
| POST | `/auth/signup` | public RL | PENDING |
| GET | `/auth/sso/config` | public | `saasEnabled` |
| GET | `/auth/sso/login-url` | public | |
| POST | `/auth/sso/callback` | public RL | |
| POST | `/auth/refresh` | refresh cookie/body | **new** |
| POST | `/auth/logout` | access | revokes refresh |
| GET | `/auth/me` | access | |
| POST | `/auth/change-password` | access | |
| POST | `/auth/mfa/enrol` | access | **new** |

## Control Center (unchanged paths, add audit)

Keep `/control-center/companies`, `/plants`, `/users/{id}/approve`, `/plants/{id}/hmi`.  
Add `GET /control-center/audit`.

## Plants and assets

Existing `/plants` CRUD retained.

New (additive):

```
GET  /plants/{id}/areas
PUT  /plants/{id}/areas/{code}   { enabled }
GET  /plants/{id}/equipment
POST /plants/{id}/equipment
GET  /plants/{id}/instruments
```

`GET /sensor-nodes` remains as the Field Devices list (compat DTO + `source`, `chipId`, `deviceIp`).

## Devices

| Method | Path | Notes |
|---|---|---|
| POST | `/devices/pair` | Issues device secret **once** in 2.0 |
| GET | `/devices/swarm-url` | Keep |
| POST | `/devices/sync-readings` | Keep |
| POST | `/devices/{id}/rotate-credential` | new |
| GET | `/devices/esp/*` | Keep LAN IP allowlist; deprecate password query |

## Ingest

```
POST /iot/batch
Authorization: Bearer swd_<device>
Idempotency-Key: <uuid>

{
  "plantId": 1,
  "chipId": "abc123",
  "ip": "192.168.1.45",
  "rssi": -62,
  "readings": [
    { "nodeId": 10, "sensorType": "TEMPERATURE", "value": 36.2, "recordedAt": "2026-11-07T09:12:00Z" }
  ]
}
```

Reject if plant/node/chip mismatch or secret wrong. Compat unauthenticated mode: env-flagged, not default.

`POST /iot/data` remains for single-point gateways with the same device auth.

## Dashboard / analytics / alerts

Keep `/dashboard/{plantId}`, `/analytics/{plantId}`, `/alerts`, ack/resolve.  
Add `GET /alerts/policies?plantId=`, `PUT` with audit.

## HMI

Keep diagram/state/commands/master.  
Every command response includes `{ simulationMode, commandId, state }`.  
New: `GET /hmi/{plantId}/commands?limit=50` (server audit).

## Reports

```
POST /reports/generate   { plantId, reportType, format }
GET  /reports/{id}       metadata + status
GET  /reports/{id}/file  octet-stream, ACL
```

## NOVA

Keep `/nova-space-op/chat` body `{ message, threadId, plantId }`.  
Response includes `evidence[]`: `{ instrument, value, recordedAt, quality, sourceKind }`.

## Partner (compatibility — do not break)

Documented in `docs/PARTNER_API.md`. 2.0 **must** keep:

- `GET /health`, `/plants`, `/devices`, `/telemetry/latest`, `/telemetry/history`, `/telemetry/alerts`, `/telemetry/health/{plantId}`, `/aggregates/daily`
- `device_id` = `node_{id}`
- Dual camelCase / snake_case fields already returned

Additive optional fields: `quality`, `sourceKind`, `chipId`. Never remove keys.

Admin: `/admin/partner/organizations`, `/api-keys` — Super Admin only.

## Ops (new)

```
POST /ops/logs          shift notes
GET  /ops/logs?plantId&from&to
POST /ops/feedstock
POST /ops/downtime
POST /ops/work-orders
```

## Examples (target pair response)

```json
{
  "success": true,
  "message": "Device paired successfully",
  "data": {
    "chipId": "1a2b3c",
    "plantId": 1,
    "nodeIds": { "TEMPERATURE": 10, "HUMIDITY": 11, "METHANE": 12 },
    "deviceSecret": "swd_…shown-once",
    "swarmUrl": "https://api.swarm.co.in/api"
  }
}
```

Do not log `deviceSecret`.

## Deprecations

| Path | Action |
|---|---|
| `/connect-device` UI route | Already redirects to Field Devices |
| Unauthenticated `/iot/batch` | Compat flag, then remove |
| `GET /reports` without plantId | Reject |
| Password query on `/devices/esp/status` | Header `X-Device-Password` then firmware-only LAN |
