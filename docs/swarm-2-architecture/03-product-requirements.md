# PURPOSE: Operator/developer documentation for SWARM.
# 03 — Product requirements (modules A–M)

Requirements are for **this** SWARM, not a generic SCADA suite. Each module lists exists / retain / improve / rebuild / new and acceptance criteria.

## Cross-cutting rules

- Server enforces every permission.
- Timestamps stored UTC, displayed in plant timezone.
- Measured vs calculated vs operator-entered vs AI-estimate are distinct fields.
- Simulation is labelled. Live actuation requires a commissioning record.
- No requirement here is a compliance certificate.

---

## A. Enterprise Control Center

**Exists:** Overview, companies, plants, Super Admin create, approvals, HMI flag.  
**Retain:** Separation from the plant app.  
**Improve:** Audit, directory, pagination.  
**New:** Privileged MFA, bootstrap ceremony, system-health view (API, DB, ingest lag, standby replica), integration registry.

**Acceptance**

- Creating a company does not require a password in git.
- Approving a user assigns `company_id`, role, plants, status ACTIVE.
- Control Center cannot call plant ingest or HMI commands.
- Every CC mutation writes an audit row.

## B. Company workspace

**Exists:** Super Admin sees all assigned plants; Users; Partners; Settings.  
**Improve:** Portfolio KPIs (plants online, open critical alerts, gas today if instrumented).  
**New:** Plant comparison (same metric, quality-aware).

**Acceptance**

- Super Admin never sees another company’s plants.
- Comparison refuses to plot `BAD`/`STALE` as zero.

## C. Plant management

**Exists:** Plant CRUD, `plant_type`, `enabled_sensor_types`, Field Devices inventory.  
**Improve:** Process configuration (areas, equipment, instruments) instead of a comma-separated sensor list as the only model.  
**New:** Shifts, gateways as assets, calibration records.

**Acceptance**

- A Bio-CNG plant can enable upgrading + compression areas without showing them on a sanitation plant.
- Pairing a hub still creates the three ESP points under the digestate/gas area the engineer selects (default: Gas collection).

## D. Live operational dashboard

**Exists:** Health score, alerts, node counts, paired devices, sensor overview.  
**Improve:** KPI cards driven by mapped instruments (CH₄, H₂S, flow, digester T) with quality + age.  
**New:** Production today only when a flow/totalizer mapping exists; otherwise “not instrumented”.

**Acceptance**

- Empty plant does not show health 85/GOOD.
- Stale hub (> timeout) is OFFLINE, not last-good forever without a banner.

## E. Industrial HMI

**Exists:** One PFD, simulated commands, live numbers when `sensor_node_id` set.  
**Retain:** iframe PFD + overlay model; CC enable flag.  
**Rebuild:** Server command log; per-plant diagram pack; remove dead React overlays from the live path.  
**New:** Command acknowledgement, interlock **display** (not enforcement — PLC enforces).

**Acceptance**

- Banner: `SIMULATION` unless commissioning status is `LIVE_APPROVED`.
- Operator cannot POWER_OFF. Plant Admin can in simulation. Live commands need `hmi.command.live` + dual control policy (see 15–16).

## F. Device and sensor management

**Exists:** Field Devices, pair hub, add instrument, chip ID, IP.  
**Improve:** Device identity, firmware, last seen, diagnostics.  
**New:** Calibration, mapping to equipment tag, credential rotation.

**Acceptance**

- Pairing remains the primary empty-state action.
- Add instrument is only for tags not created by the hub.
- Ingest without a valid device credential is rejected (after migration — see 14, 24).

## G. Telemetry platform

**Exists:** REST ingest, optional MQTT subscriber, MySQL readings, retention cron 02:30.  
**Improve:** Validation, quality, idempotency, device auth.  
**New:** Adapter interface; outbox for Partner/SSE later.

**Acceptance**

- Duplicate batch (same device, same `recorded_at`) does not double-alert.
- Values outside validity range are stored as `BAD` or rejected per plant policy — never silently clamped to a “healthy” number.

## H. Alerts and notifications

**Exists:** Threshold create, ack, resolve, UI.  
**Improve:** Per-tag thresholds, severity policy.  
**New:** Channels (email first, using unused JavaMail), escalation, quiet hours, preferences.

**Acceptance**

- Global YAML defaults are templates, not the only source.
- Changing a threshold is audited and requires Plant Admin or above.

## I. Operations management

**Exists:** Almost nothing (maintenance table is sensor health).  
**New:** Shift log, feedstock receipt, downtime, incidents, checklists, operator notes, work orders.

**Acceptance**

- An operator can file “feeding delayed 40 min, pump P-101 noise” without a sensor node.
- Notes never overwrite telemetry.

## J. Reports and analytics

**Exists:** Charts; report metadata stub.  
**Rebuild:** Real files.  
**New:** Daily gas, yield vs feedstock (when both exist), availability.

**Acceptance**

- Generate produces a downloadable PDF/XLSX the user can open.
- Missing instruments appear as “not mapped”, not 0 m³.

## K. NOVA AI

**Exists:** Chat, threads, skills: latest, dashboard, alerts, AI recs, analyser, 7‑day trend, inventory.  
**Improve:** Implement stub tool IDs `telemetry.history`, `plant.maintenance`.  
**New:** Grounding contract in every answer (source, time, quality).

**Acceptance**

- If no reading, NOVA says so. It does not invent 37.2 °C.
- NOVA cannot send HMI commands.

## L. Integration platform

**Exists:** Partner v1 + emPOWER SSO consumer.  
**Improve:** Scopes, rate limit, webhooks later (not P2).  
**New:** Laboratory CSV import; generic outbound webhook after Partner is hardened.

**Acceptance**

- emPOWER cannot pair devices or list users.
- SSO failure does not lock local break-glass Control Center admin (see 09).

## M. Android

**Exists:** APK of SPA, native SSO redirect.  
**Improve:** Security of pairing (LAN only), credential storage.  
**New:** Read-only offline cache of last dashboard (optional, after auth hardening).

**Acceptance**

- Pairing still works on plant Wi-Fi without a public IP for the hub.
- Logging out clears token on device.
