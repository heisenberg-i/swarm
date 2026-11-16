# PURPOSE: Operator/developer documentation for SWARM.
# 26 — Architecture decision records

## ADR-001 — Modular monolith, not microservices

**Status:** Accepted  
**Context:** One Spring Boot app serves UI, ingest, Partner, Nova, HMI. Scale is tens–hundreds of plants. Runtime is a Raspberry Pi plus Railway standby.  
**Decision:** Keep one deployable. Split **packages**, not processes. Extract ingest/report workers only after measurement.  
**Consequences:** Simpler Pi ops. Discipline required (ArchUnit).  
**Exists:** single module. **Retain** deployable. **Improve** boundaries.

## ADR-002 — Flyway over Hibernate ddl-auto

**Status:** Accepted  
**Context:** Schema drift between `schema.sql` and JPA; ad-hoc migrator components.  
**Decision:** Baseline Flyway, `validate` in prod.  
**Consequences:** Migrations are code-reviewed. First slice is operationally careful.  
**Rebuild:** schema process. **Retain:** MySQL.

## ADR-003 — Product name SWARM, package rename later

**Status:** Accepted  
**Context:** `com.biopower`, Docker `biopower-*`, UI SWARM.  
**Decision:** Do not rename packages in security slices. Optional later.  
**Consequences:** Docs say SWARM; code may still say biopower.

## ADR-004 — Device-authenticated ingest

**Status:** Accepted  
**Context:** `POST /iot/batch` permitAll.  
**Decision:** Device secrets; legacy compat flag for existing hubs.  
**Consequences:** Firmware update required for full closure.

## ADR-005 — HMI remains simulation until commissioning

**Status:** Accepted  
**Context:** `HmiService` hardcodes simulation; unused React overlays.  
**Decision:** Simulation is the product default. Live writes need a commissioning record and never bypass PLC.  
**Consequences:** Honest sales; no fake EtherCAT.

## ADR-006 — Partner v1 compatibility

**Status:** Accepted  
**Context:** emPOWER already coded against `/partner/v1`.  
**Decision:** Additive changes only. Isolation bugs fixed without renaming resources.  
**Improve:** empty allow-list, scopes.

## ADR-007 — Identity: local + optional emPOWER SaaS

**Status:** Accepted  
**Context:** `IDENTITY_MODE` local vs saas.  
**Decision:** Keep both. SSO does not replace CC break-glass. New SSO users PENDING.  
**Retain:** BCrypt 12, `identity_user_id`.

## ADR-008 — NOVA read-only over SWARM data

**Status:** Accepted  
**Context:** novaspace skills + optional LLM. ERP DNA is a reference.  
**Decision:** No HMI commands from chat. Evidence required. Implement stub tools.  
**Retain:** AnswerGuard, primary-only enable.

## ADR-009 — Reports use existing iText/POI

**Status:** Accepted  
**Context:** Libraries present, unused; fake file_path.  
**Decision:** Rebuild generate/download, not a new reporting product.  
**New:** job status.

## ADR-010 — MySQL stays the historian

**Status:** Accepted  
**Context:** DNA docs mention Timescale.  
**Decision:** MySQL + daily_aggregates table. Revisit with query evidence.  
**Retain:** `sensor_readings` indexes, retention job.

## ADR-011 — Android is Capacitor SPA

**Status:** Accepted  
**Context:** Working APK.  
**Decision:** No native rewrite. Harden cleartext and tokens.  
**Out of scope:** `nova-chat-android` from DNA.

## ADR-012 — Global thresholds become templates

**Status:** Accepted  
**Context:** YAML 25–45 °C alarms hub DHT as digester.  
**Decision:** Per-instrument policies; templates need qualified accept.  
**Improve:** alerts module.

## ADR-013 — Cursor autonomy stops at the plant fence

**Status:** Accepted  
**Context:** User asked for implementation without routine chat, with hard stops.  
**Decision:** See document 27. No production deploy, no live actuation, no irreversible prod migration, no live credential rotation without a human.
