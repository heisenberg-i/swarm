# PURPOSE: Operator/developer documentation for SWARM.
# SWARM 2.0 — Architecture

**Product:** SWARM by nanoFarm — AI-IoT plant health monitoring for biogas, Bio-CNG/CBG, sanitation, organic waste, anaerobic digestion and waste-to-energy facilities.

**Phase:** Architecture + local implementation (see [progress.md](progress.md), [ENGINEERING_REPORT.md](ENGINEERING_REPORT.md)).

**Grounding:** These documents describe the repository as it exists in `swarm_webapp` (Java package `com.biopower`, product name SWARM). Claims in `README.md` and older manuals are treated as claims until the code confirms them.

## How to read this set

| Order | Document | Purpose |
|---|---|---|
| 00 | [Executive vision](00-executive-vision.md) | Same product, better engineering |
| 01 | [Repository audit](01-repository-audit.md) | What the code actually does |
| 02 | [Capability inventory](02-capability-inventory.md) | Seventeen existing capabilities: state, reuse, improvements |
| 03 | [Product requirements](03-product-requirements.md) | Target modules A–M with acceptance criteria |
| 04 | [User journeys and roles](04-user-journeys-and-roles.md) | Control Center → company → plant → operator |
| 05 | [Business domain model](05-business-domain-model.md) | Configurable process areas for biogas / Bio-CNG |
| 06 | [Target system architecture](06-target-system-architecture.md) | Modular monolith, topology, runtime |
| 07 | [Module architecture](07-module-architecture.md) | Bounded contexts and dependency rules |
| 08 | [Database architecture](08-database-architecture.md) | Current schema, target ER, retention |
| 09 | [Authentication and authorization](09-authentication-authorization.md) | Identity, MFA, RBAC, device credentials |
| 10 | [Threat model](10-threat-model.md) | STRIDE against this codebase |
| 11 | [Tenant isolation](11-tenant-isolation.md) | Company / plant / partner allow-lists |
| 12 | [API specification](12-api-specification.md) | Conventions and target contracts |
| 13 | [Sensor and device model](13-sensor-device-model.md) | Hubs, transmitters, quality, calibration |
| 14 | [Telemetry architecture](14-telemetry-architecture.md) | Ingest, validation, storage, fan-out |
| 15 | [HMI and command lifecycle](15-hmi-command-lifecycle.md) | Diagrams, commands, audit |
| 16 | [Industrial safety](16-industrial-safety.md) | Cloud vs PLC vs hardwired |
| 17 | [Alerts and notifications](17-alerts-notifications.md) | Thresholds, ack, escalation |
| 18 | [Operations and maintenance](18-operations-maintenance.md) | Shifts, feedstock, downtime, work |
| 19 | [Analytics and AI](19-analytics-and-ai.md) | Charts, reports, NOVA, evidence rules |
| 20 | [emPOWER and Partner API](20-empower-partner-api.md) | SSO vs telemetry pull |
| 21 | [Android strategy](21-android-strategy.md) | Capacitor SPA, offline, pairing |
| 22 | [Deployment and infrastructure](22-deployment-infrastructure.md) | Pi primary, Railway standby |
| 23 | [Test strategy](23-test-strategy.md) | What to test and in what order |
| 24 | [Data migration](24-data-migration.md) | Compatibility with live MySQL |
| 25 | [Implementation roadmap](25-implementation-roadmap.md) | Incremental slices Cursor can ship |
| 26 | [Architecture decision records](26-architecture-decision-records.md) | ADRs 001–012 |
| 27 | [Cursor implementation master prompt](27-cursor-implementation-master-prompt.md) | Autonomous implementation contract |
| 28 | [MQTT and telemetry](28-mqtt-and-telemetry.md) | Implemented ingest + MQTT contract |
| — | [Protected modules (NOVA)](PROTECTED_MODULES.md) | Absolute no-touch list |
| — | [Rebuild inventory](REBUILD_INVENTORY.md) | Preserve vs rebuild |
| — | [Engineering report](ENGINEERING_REPORT.md) | Acceptance summary |
| — | [progress.md](progress.md) | Slice / phase log |

## Classification used throughout

For every major proposal:

1. **Exists today** — confirmed in source.
2. **Retain** — keep behaviour or data.
3. **Improve** — same idea, stronger engineering.
4. **Rebuild** — same product surface, different internals.
5. **New** — not in this repository.

## Hard stops for implementation

Cursor may implement approved slices from this set. It must **stop** before:

- Production deployment
- Irreversible data migrations
- Credential changes that affect live services
- Any real equipment actuation (PLC, motor, valve, compressor)

## Secrets

This architecture does not reproduce passwords, JWT secrets, API keys, firmware Wi-Fi constants, or Partner sandbox keys found in the repository. Documented default administrator credentials are **compromised** and must be replaced by a bootstrap ceremony (see 09 and 22).
