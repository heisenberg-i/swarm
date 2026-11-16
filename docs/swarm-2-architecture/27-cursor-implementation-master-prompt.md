# PURPOSE: Operator/developer documentation for SWARM.
# 27 — Cursor implementation master prompt

Use this prompt to implement **approved** SWARM 2.0 slices. It is repository-specific. It is not permission to redesign the product.

---

You are implementing SWARM 2.0 in `/home/lavender/swarm_webapp` (GitHub `swarmnfi-svg/swarm_webapp`).

Read `docs/swarm-2-architecture/README.md` and the documents it lists. The product is SWARM / BIOPOWER plant health monitoring — **the same product**, not a generic IoT platform.

## Source of truth

- Behaviour: architecture docs 00–26 + **current code**.
- If docs and code disagree on *today*, trust the code and note the gap in `docs/swarm-2-architecture/progress.md`.
- If docs specify *target*, implement the target for the **current slice only**.

## Stack you must keep

- React 18 + MUI + Vite SPA (Control Center + plant app role split in `App.jsx`)
- Spring Boot 3 / Java 17 / `com.biopower` packages (do not mass-rename)
- MySQL + JPA; add Flyway rather than new databases
- ESP8266 hub firmware in `firmware/swarm_esp8266_hub/`
- Capacitor Android wrapper
- Partner API `/api/partner/v1` field names

## Product workflows you must not delete

Control Center provisioning; company plants; Super Admin / Plant Admin / Operator; Field Devices (inventory + Pair Sensor Hub); dashboard; alerts; analytics; simulated HMI; NOVA chat; emPOWER SSO consumer; Partner keys; Android SPA.

## Slice execution protocol

1. Read the slice in `25-implementation-roadmap.md`.
2. List Allowed files and Forbidden files.
3. Write tests first for the security/ACL behaviour.
4. Implement the smallest change that passes tests.
5. Verify UI in the browser if you touched frontend (Field Devices, login, CC, HMI banner).
6. Append `progress.md`: date, slice, files, tests, risks, follow-ups.
7. Do not commit unless the user asked.

## Hard stops — abort and report

- Production deploy (Pi, Railway, Cloudflare, DNS)
- Irreversible production migration / DROP TABLE / Partner `device_id` change
- Changing live secrets, JWT_SECRET, DB passwords, SaaS client secret
- Enabling real equipment actuation or setting `simulationMode=false` without a commissioning row **and** explicit user request
- Shipping PLC writes
- Introducing Kafka, Kubernetes, new microservices, or Timescale
- Reproducing or printing secrets in docs or logs
- Weakening `/partner/v1` isolation
- Re-adding a separate “Connect Device” sidebar item (must stay inside Field Devices)

## Security rules while coding

- Never trust React routes.
- Never leave `/auth/me` or change-password public.
- Never accept unauthenticated ingest unless `SWARM_IOT_COMPAT_LEGACY` is true **and** the device is flagged.
- Empty Partner allow-list = no plants.
- Do not log tokens, device passwords, raw `swk_` keys.
- Do not re-seed documented default CC passwords.

## Industrial rules

- HMI commands: persist `hmi_commands`; default simulation.
- MQ5 is not a calibrated CH₄ analyser unless labelled.
- Health scores are estimates.
- NOVA must cite evidence or refuse the number.
- Do not zero-fill missing telemetry.

## Quality bar

- Follow existing code style in the files you touch.
- No drive-by refactors.
- Frontend: keep Field Devices naming and pairing as the primary empty-state CTA.
- Backend: keep API envelope `{ success, message, data }` for the SPA.

## Suggested first Allowed set (Slice 0)

Allowed (examples):

- `backend/src/main/resources/db/migration/` (new)
- `backend/src/main/java/com/biopower/config/SecurityConfig.java`
- `backend/src/main/java/com/biopower/security/`
- `backend/src/main/java/com/biopower/service/PartnerTelemetryService.java` (allow-list)
- `backend/src/main/java/com/biopower/controller/` only for ACL fixes
- `backend/src/test/java/**` new security tests
- `frontend/src/pages/Dashboard.jsx` empty health copy
- `frontend/src/pages/PlantHmi.jsx` simulation banner
- `docs/swarm-2-architecture/progress.md`

Forbidden:

- `NOVA_DNA_source_for_SWARM/` (reference only)
- Docker/Railway production env files with secrets
- Mass rename `com.biopower`
- New UI product shells

## Verification commands

```text
cd backend && ./mvnw -q test
cd frontend && npm test   # once Vitest exists; otherwise skip
```

Browser: login as Super Admin, open Field Devices, confirm no Connect Device nav; open Plant HMI and see SIMULATION; Partner health still JSON-compatible.

## When a slice is done

Report: what exists, what changed, tests, remaining threats, whether firmware/humans must act next.

Then stop.
---
