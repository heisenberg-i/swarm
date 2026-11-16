# PURPOSE: Operator/developer documentation for SWARM.
# 02 — Capability inventory

Seventeen capabilities the product already sells. None are removed “to simplify architecture”.

Legend: **Working** = used in production path. **Partial** = real API, weak engineering or stubbed edges. **Stub** = UI or docs only. **Absent** = claimed, not in repo.

---

### 1. Control Center

| | |
|---|---|
| **State** | Working for Overview, Tenants, Plant inventory, Approvals, HMI access. |
| **Value** | How nanoFarm provisions customers without mixing tenant telemetry into an internal console. |
| **Dependencies** | `CONTROL_CENTER_ADMIN`, `companies`, `users.status`, `plants.hmi_enabled`. |
| **Reuse** | Keep the role-split SPA and `/api/control-center/**`. |
| **Improve** | Audit log, user directory, pagination, no Partner-key access via `/admin/**`. |
| **New** | Bootstrap without seeded passwords; tenant license/seat later (not emPOWER billing). |

### 2. Company and multi-plant management

| | |
|---|---|
| **State** | Working. `Company` 1:N `Plant` 1:N `SensorNode`. Super Admin created from CC. |
| **Value** | Multi-site biogas companies (one Super Admin, many plants). |
| **Reuse** | `company_id` on users and plants is the isolation kernel. |
| **Improve** | Enforce company on every query (see 11). Plant profile fields for process configuration. |

### 3. Plant Admin and Operator experiences

| | |
|---|---|
| **State** | Working. Operators: view + pair hub; no Users/Settings/Partners. HMI commands Super/Plant Admin only. |
| **Value** | Field vs office. |
| **Reuse** | `PlantAccessService`, `user_plants`, `user_sensor_nodes`. |
| **Improve** | Operator sensor filter is easy to bypass if node list is empty (empty = all plant nodes). Contextual permissions on every mutating endpoint. |

### 4. Plant overview dashboards

| | |
|---|---|
| **State** | Working. `GET /dashboard/{plantId}` + paired ESP devices + health card. |
| **Value** | First screen after login. |
| **Improve** | Plant-type KPI packs (biogas vs Bio-CNG). Stale-data banners. Do not default empty AI health to 85/GOOD in the UI. |

### 5. Live sensor monitoring

| | |
|---|---|
| **State** | Working via ingest + dashboard + Field Devices last value. Not WebSocket. |
| **Value** | Core of the product. |
| **Improve** | Quality flags, device auth, optional SSE/WebSocket later. Heartbeat already exists (`SensorHeartbeat`, 5‑minute offline job). |

### 6. Plant HMI and P&ID visualization

| | |
|---|---|
| **State** | Partial. One biomethanation PFD iframe. Simulation always on. |
| **Value** | Sales and training; future commissioned view. |
| **Reuse** | `hmi_equipment`, `hmi_equipment_state`, `hmi_plant_state`, iframe PFD, Control Center enable flag. |
| **Rebuild** | Command audit on server; plant-specific diagram packs; delete or quarantine unused Tata React overlays. |
| **New** | Explicit SIMULATION watermark; commissioning record before `simulationMode=false`. |

### 7. Equipment and process management

| | |
|---|---|
| **State** | Partial. HMI equipment tags + sensor nodes. No process-area tree, no asset register. |
| **Value** | Engineers think in TIT/PIT/FIT and skids, not “sensor nodes”. |
| **Reuse** | `HmiEquipment.tagNo`, Field Devices source chip (SWARM Hub vs Instrument). |
| **New** | Process areas, equipment templates, instruments as first-class, mapping to nodes. |

### 8. ESP-based sensor hub

| | |
|---|---|
| **State** | Working. Field Devices → Pair Sensor Hub. Firmware in repo. |
| **Value** | nanoFarm’s hardware SKU. |
| **Reuse** | Pairing contract, three hub points, chip ID. |
| **Improve** | Remove hardcoded firmware secrets; device credentials; rotate; firmware version inventory; stop unauthenticated batch ingest. |

### 9. PLC, MQTT and REST integration

| | |
|---|---|
| **State** | REST ingest working. MQTT code present, **off**. PLC/Modbus **absent**. |
| **Value** | Larger CBG plants already have PLCs. |
| **Reuse** | `MqttConfig` + `IoTDataService` as the single ingest pipeline. |
| **New** | Gateway adapter interface (ESP HTTP, MQTT, Modbus later). Never let cloud writes be the safety layer. |

### 10. Alerts and notification workflows

| | |
|---|---|
| **State** | Partial. Create on ingest, ack/resolve in UI, 5‑minute stale job. No email/SMS. Global YAML thresholds. |
| **Value** | Why customers pay for monitoring. |
| **Reuse** | `alerts` table, statuses, UI. |
| **Improve** | Per-plant/per-tag thresholds with qualified approval; notification channels; escalation; audit. |

### 11. Historical charts and analytics

| | |
|---|---|
| **State** | Working `GET /analytics/{plantId}` + Recharts. |
| **Value** | Shift handover and troubleshooting. |
| **Improve** | Quality-aware series (do not chart `BAD` as zero). Daily aggregates already computed for Partner API can feed plant analytics. |

### 12. AI plant-health recommendations

| | |
|---|---|
| **State** | Working rules in `AiHealthService`. Persisted when score moves. |
| **Value** | Acidification / methane / flow language operators understand. |
| **Reuse** | Keep rules as the **deterministic** engine. Label them “rule-based”, not “ML”. |
| **Improve** | Plant-type templates; evidence payload (node, time, value); UI must not invent 85/GOOD. |

### 13. Predictive maintenance

| | |
|---|---|
| **State** | Stub-grade heuristic. Regenerates rows on GET. Sensors only. |
| **Value** | The **screen** is wanted; the model is not. |
| **Rebuild** | Condition from heartbeat + calibration + runtime hours; work orders in operations module. Do not claim remaining useful life without a model card. |

### 14. Reports and exports

| | |
|---|---|
| **State** | Stub. Fake path, no download. |
| **Value** | Daily production pack for plant head / regulator. |
| **Reuse** | `reports` table, iText and POI already in `pom.xml`, UI generate action. |
| **Rebuild** | Async generate, object storage or local disk, signed download, plant-scoped ACL. |

### 15. emPOWER shared authentication and integration

| | |
|---|---|
| **State** | SSO consumer implemented; active only if `IDENTITY_MODE=saas` **and** client secret set. Local mode is what developers run. |
| **Value** | One person, many nanoFarm/emPOWER apps. |
| **Reuse** | `/auth/sso/*`, `identity_user_id`, `EmpowerSaaSAuthClient`. |
| **Improve** | New SSO users must not skip PENDING/plant assignment; MFA for privileged SWARM roles even if SaaS MFA is off. |

### 16. Partner API

| | |
|---|---|
| **State** | Working and documented. Read-only pull. |
| **Value** | ERP costing without embedding SWARM. |
| **Reuse** | `/partner/v1/*`, key hash, `external_org_id`, plant allow-list. |
| **Improve** | Empty allow-list must mean **no plants**; key scopes; rate limits; sandbox off in every non-dev profile. |

### 17. Android application

| | |
|---|---|
| **State** | Partial. Capacitor WebView of the SPA. SSO deep link exists. No offline. Global cleartext HTTP for LAN pairing. |
| **Value** | Operators on site with a phone. |
| **Reuse** | Same UI, same APIs, `com.nanofarm.swarm` scheme. |
| **Improve** | Cleartext only for RFC1918; token storage; queued ingest later, not a second app. |

---

## Capability map (target modules)

| Existing capability | Target module |
|---|---|
| 1–2 | A Control Center, B Company workspace |
| 3–4 | C Plant management, D Live dashboard |
| 6–7 | E Industrial HMI, C equipment |
| 8–9 | F Device management, G Telemetry |
| 10 | H Alerts |
| 11, 14 | J Reports and analytics |
| 12–13, NOVA | K NOVA AI |
| 15–16 | L Integration |
| 17 | M Android |
| (gap) | I Operations (mostly new) |
