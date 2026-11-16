# PURPOSE: Operator/developer documentation for SWARM.
# 07 — Module architecture

## Exists today

One Maven module, 16 REST controllers, 22 services, JPA entities. Nova is the only package-level island (`com.biopower.novaspace`).

## Target: modular monolith in one deployable

```
backend/
  com.biopower.platform      # security filters, clock, files, errors
  com.biopower.identity
  com.biopower.tenancy       # company, control center
  com.biopower.assets        # plant, area, equipment, instrument
  com.biopower.devices       # hubs, pairing, firmware
  com.biopower.telemetry
  com.biopower.alerts
  com.biopower.hmi
  com.biopower.ops
  com.biopower.analytics     # reports, charts
  com.biopower.nova          # already isolated
  com.biopower.partner
```

Move classes incrementally. Do not rewrite business logic during the move. ArchUnit tests lock “nova must not call IoTController”.

## Frontend modules (same SPA)

Keep one build. Folders:

| Folder | Owns |
|---|---|
| `pages/control-center/` | Module A |
| `pages/` plant screens | D–K, F Field Devices |
| `components/hmi/` | E — **only live path files** |
| `services/api.js` | Split per module as `services/telemetry.js` etc. when files grow |
| `context/AuthContext.jsx` | Identity only |

Delete or quarantine unused HMI React (`PidDiagram`, Tata data) in a dedicated cleanup slice so the live iframe is the only HMI story.

## REST surface by module

| Module | Paths |
|---|---|
| Identity | `/auth/**` — **not** all public (09) |
| Tenancy | `/control-center/**` |
| Assets | `/plants`, `/plants/{id}/areas`, `/equipment` |
| Devices | `/devices/**`, `/sensor-nodes` (compat) |
| Telemetry | `/iot/**`, `/dashboard`, `/analytics` |
| Alerts | `/alerts`, `/notifications` |
| HMI | `/hmi/**` |
| Ops | `/ops/**` (new) |
| Analytics | `/reports` |
| Nova | `/nova-space-op/**` |
| Partner | `/partner/v1/**`, `/admin/partner/**` |

## Internal facades

```java
public interface TelemetryQuery {
  Optional<LatestReading> latest(Long plantId, Long instrumentId);
  List<ReadingPoint> history(...);
}
```

Nova skills and Partner services call `TelemetryQuery`, not repositories of other modules. That is the extraction seam.

## Background jobs by module

| Job | Module | Primary only |
|---|---|---|
| Sensor timeout → OFFLINE | alerts | yes |
| Data retention | telemetry | yes |
| HMI auto-sequence tick | hmi | yes, simulation |
| AI health debounce | analytics | yes |
| Report worker | analytics | yes |
| Partner is pull — no push job | partner | n/a |

## Failure isolation

If Nova LLM keys are missing, chat still answers from skills (already true).  
If HMI diagram file missing, dashboard still works.  
If Partner key revoked, plant app unchanged.  
If ingest floods, rate-limit per device; do not block Control Center.

## Compatibility

External names `sensor-nodes` and Partner `device_id=node_{id}` remain until Partner v2. Internally prefer Instrument + Device.
